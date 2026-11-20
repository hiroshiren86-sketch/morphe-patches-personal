# PLANO-MORPHE — modelo mental completo del pipeline

> Documento de referencia. Todo lo que está aquí está VERIFICADO contra el código
> y los experimentos (BITACORA.md). Si algo de este documento se contradice con
> la realidad, se corrige aquí primero y luego se actúa.

## 1. Qué es Morphe (el ecosistema)

| Pieza | Qué es | Dónde vive |
| --- | --- | --- |
| **morphe-desktop (CLI)** | Jar único "all-in-one" que parchea APKs. Comandos: `patch`, `list-patches`. | `MorpheApp/morphe-desktop` releases (usamos `v2.2.0-dev.18`) |
| **Bundle `.mpp`** | Jar con las clases Kotlin de los patches compiladas (plugin `app.morphe.patches`). Lo genera nuestro repo. | `hiroshiren86-sketch/morphe-patches-personal` releases (tag `v1.0.0-dev.X`) |
| **Extensión `.mpe`** | Classes precompiladas que un patch puede fusionar dentro del APK (`extendWith`). Solo la necesita el SigSpoof (`SigSpoof.install`). Módulo Android `extensions/extension` → solo compila en CI (necesita SDK). | Se compila dentro del bundle por el `:patches:buildAndroid` |
| **Morphe Manager** | App Android que aplica un `.mpp` sobre un APK base elegido por el usuario, en el teléfono. Flujo alternativo al CLI. | Play/GitHub de MorpheApp |

El flujo mental SIEMPRE es el mismo: **APK base + bundle de patches → APK parchado y firmado**. El usuario no puede re-modificar a mano su APK instalado; por eso el pipeline CI hace de "Morphe Manager": descarga base, aplica bundle, firma con clave estable y publica en el release `app-mod`.

## 2. El CLI: comandos y flags que usamos

```
java -jar morphe-desktop.jar list-patches --patches patches.mpp
java -jar morphe-desktop.jar patch \
  --patches patches.mpp --exclusive \
  -e "<NOMBRE EXACTO del patch>" \
  --keystore morphe-personal.p12 --keystore-password "$PW" \
  -o salida.apk base.apk
```

- `--exclusive`: aplica SOLO los patches listados con `-e`. Sin él, aplica todos los `default = true`.
- `-e`: el nombre debe ser EXACTO (lista con `list-patches`). Si el fingerprint de un patch seleccionado **no matchea, el run entero ABORTA** → nunca seleccionar un patch cuya clase no exista en el base elegido (p. ej. `n0/G` no existe en el base limpio).
- `--disable-purge`: conserva el scratch (`morphe-data/tmp/patching-*/patcher/patched/originalDex`) para depurar.
- Scratch: `morphe-data/tmp/patching-<ts>/` (bin, workspace, patched/originalDex|newDex|storedDex).
- La firma SIEMPRE cambia (keystore propio, v1+v2 + zipalign). Cualquier guarda de firma del app o de un mod reacciona a esto.

## 3. Anatomía de un patch (API que usamos)

```kotlin
object MiFingerprint : Fingerprint(
    definingClass = "Lcom/darkdeveloper/.../Clase;",
    name = "metodo", returnType = "V",
    parameters = listOf("Landroid/content/Context;")
)

val miPatch = patch(
    name = "Nombre visible en Morphe Manager / list-patches",
    description = "...",
    default = true
) {
    compatibleWith(Constants.KAWAII_ANIMES)
    extendWith("extensions/extension.mpe")   // solo si necesita clases extra
    execute {
        val m = resolveMethodWithFallback("Nombre del patch",
            MiFingerprint, VarianteRelajada, SoloPorNombre)
        replaceMethodBody(m, """ const/4 v0, 0
                                 return-void """)     // sustituye el cuerpo
        m.addInstructions(0, """invoke-static {p0}, Lapp/template/...->install(...)V""")
        // inyectar métodos nuevos:
        mutableClassDefBy(CLASE).methods.add(MutableMethod(ImmutableMethod(...)))
    }
}
```

- `resolveMethodWithFallback` permite fingerprints relajados en cascada (exacto → prefijo de clase → solo nombre).
- Los patches se registran **automáticamente** (`loadPatchesFromJar` + `PatchListGenerator`): un `.kt` nuevo en `patches/src/main/kotlin/app/personal/patches/**` basta. No hay lista manual.
- `patches-list.json` y `patches-bundle.json` los reescribe semantic-release; NO editarlos a mano.
- Compilar el bundle: `./gradlew :patches:buildAndroid --no-daemon` con `GITHUB_ACTOR`/`GITHUB_TOKEN` (maven privado `MorpheApp/registry`). En local falla el módulo `extensions:extension` (requiere Android SDK) → compilar bundles SOLO en CI.

## 4. Pipeline de Morphe por dentro (verificado con scratch)

1. Descomprime el APK y carga los dexes (dexlib2, sin texto smali).
2. Aplica cada patch: matchea fingerprints, reemplaza cuerpos/inyecta.
3. Reensambla y **reparte dexes** (puede crear `classes5.dex`, `classes6.dex`…).
4. Fusiona extensiones `.mpe` si algún patch las declara.
5. Re-firma (v1+v2) con el keystore dado y alinea el zip.

## 5. Nuestro CI (repo `morphe-patches-personal`, branch `dev`)

```
commit "feat:/fix:/chore:" en dev
  └─ Release (semantic-release) → tag v1.0.0-dev.X + patches-X.mpp adjunto
       └─ Patch APK (workflow patch-apk.yml, disparado por el release)
            ├─ descarga bundle (tag) + bases (release apks-trabajo)
            ├─ morphe patch --exclusive -e ... (ver sección 7)
            ├─ firma con MORPHE_KEYSTORE_P12 (clave estable → instalar encima OK)
            └─ publica en release "app-mod" (asset con el tag en el nombre, --clobber)
```

- `workflow_dispatch` permite re-lanzar con un tag de bundle concreto.
- El release `apks-trabajo` guarda los bases: `Proyecto.BASE.apk` (limpio oficial), `Proyecto.ORIGIN.apk` (variante del wrapper del mod anterior), `app.ya.parcheada.por.mi.apk` (referencia LSPatch del usuario).
- La PR de `open_pull_request.yml` es automática; no depende de nosotros.

## 6. Los DOS bases (2.1.3 build 29) — la lección más importante

| | `Proyecto.BASE.apk` (el "limpiecito" del usuario) | `Proyecto.ORIGIN.apk` |
| --- | --- | --- |
| Origen | `animelhd/kawaii-animes` repo oficial (`app-release.apk`), MD5 `b5b66a85` | Extraído del wrapper LSPatch del mod anterior, MD5 `2119ef58` |
| Dexes | 3 (16+9+1.9 MB) | 4 (el 4º = 55 KB = panel del mod viejo) |
| Protección nativa | `libprotection.so b7793dbd` **con puente V29** (`q25`/`confirmRuntimeV29`, `preparePlaybackV29`) | `libprotection.so bf6ed44f` **sin puente V29** |
| BD del catálogo | Se sincroniza por red (`/api/sync`) | **Pre-sembrada** (`assets/databases/kawaii_animes.db`) |
| Extras | Ninguno | Panel del mod viejo fusionado (`n0/W`, `n0/G`, `zx/y` + `libnoface.so`) y `MainActivity.onCreate` con llamada reflectiva inyectada |
| Re-firmado | La guarda nativa V29 + validaciones adicionales rechazan la firma nueva | Solo estorbaba el guardia `n0/G.v` del panel (resuelto en dev.9) |

**Ninguno de los dos es "el base equivocado"**: el limpio es el que el usuario quiere parchear (flujo Morphe Manager) y origin es la variante que demostró funcionar en su dispositivo. El CI ahora genera **ambas variantes** para decidir con datos, no con teoría.

## 7. Matriz de patches × bases (qué se puede aplicar a qué)

| Patch (`-e` exacto) | Base limpio | Origin |
| --- | --- | --- |
| `Bypass del detector de AdBlock` | ✅ (`isDomainBlocked`→false) | ✅ |
| `Saltar anuncios de episodios` | ✅ (`shouldShowAdNext`→false) | ✅ |
| `Recompensa instantánea de episodios` | ✅ (`isAdRewarded`→true) | ✅ |
| `Compatibilidad con integridad nativa (V29)` | ✅ necesario (gate `confirmRuntimeV29`→true) | ❌ NO existe `q25` → abortaría el run |
| `Firma original en PackageManager (V29)` | ✅ necesario (SigSpoof) | ✅ necesario (cubre lecturas PM del panel/mod) |
| `Neutralizar antitamper del panel (n0/G)` | ❌ NO existe `n0/G` → abortaría el run | ✅ necesario (evita el killProcess → splash infinito) |

## 8. Lo que sabemos de cada guarda (verificado)

### 8a. Antitamper nativo V29 (base limpio)
- `q25.<clinit>` carga `libprotection.so`; `confirmRuntimeV29` = gate de arranque (fingerprint claro, reemplazo a `true` funciona).
- `preparePlaybackV29` descifra los tokens `kce1` de los streams; hay "proofs" para `/api/sync` del catálogo.
- La lib lee la firma vía PackageManager (JNI desde `ActivityThread.currentApplication()`); `SigSpoof` (proxy de PM instalado en `attachBaseContext` + `onCreate`) le devuelve el cert ORIGINAL (SHA-256 `366fa8f1…f3f2cbac` = `META-INF/NOFACE.RSA`).
- **Mecanismo verificado por disassembly** (2026-11-15; scripts en `scripts/`: `disasm_libprotection.py`, `annotated_dump.py`, `find_callers.py`; capstone + pyelftools, xrefs vía `.rela.dyn`; OJO: capstone imprime la PAGINA ABSOLUTA en `adrp`):
  - Path del APK: `GetMethodID(android/content/Context, "getPackageCodePath")` -> `CallObjectMethod` (JNI vía punteros, la lib no importa símbolos de libart). El Context lo obtiene desde `android/app/ActivityThread` (currentApplication -> KawaiiApp).
  - Lectura: `new java.util.zip.ZipFile(path)` (JNI) -> `entries()` -> `ZipEntry.getName()` -> comparación contra sufijo `.apk` (string `hook.apk` también presente) -> SHA-256 vía `MessageDigest.getInstance("SHA-256")` (JNI). Los `fopen`/`fread`/`socket` importados complementan el flujo.
  - Firma Java: `PackageManager.getPackageInfo` -> `signatures`.
  - Por qué LSPatch funciona: su SigBypass nivel 2/3 extrae `assets/lspatch/origin.apk` a cache y redirige `openat`/`svc` al archivo ORIGINAL (`org.matrix.vector.nativebridge.SigBypass.enableOpenatHook`/`enableSvcRedirect`); nivel 1 hookea PackageParser, el Parcelable.CREATOR de PackageInfo, ApplicationPackageManager, getPackageArchiveInfo y hasSigningCertificate. La lib vieja de origin y la V29 son idénticas salvo 24 bytes -> el check es el mismo y LSPatch lo pasa leyendo el original.
  - Historia en dispositivo: dev.5/6 (gate forzado) -> catálogo vacío y streams "sin conexión". dev.7 (SigSpoof PM) -> 26+ lecturas interceptadas pero igual falla -> coherente con la validación a nivel ARCHIVO (el spoof de PM no cubre getPackageCodePath/ZipFile).
  - Fix (dev.11, SigSpoof v3): el CI inyecta el APK ORIGINAL como `assets/orig/app.apk` antes del parcheo; SigSpoof lo extrae a `cacheDir/orig/app.apk` y KawaiiApp overridea `getPackageCodePath`/`getPackageResourcePath` (dispatch virtual) para que la lib lea los bytes originales; además hasSigningCertificate -> true si el cert es el original y `applicationInfo.sourceDir` se redirige en el PackageInfo spoofeado. Equivalencia LSPatch nivel 3 sin código nativo.

### 8b. Guardia del panel del mod viejo (origin) — RESUELTO
- `MainActivity.onCreate` (inyectado por el mod anterior) → `Class.forName("n0.W").getMethod("show").invoke(null, this)`.
- `n0/W.show` → `n0/G.v(Context)`: `getPackageInfo(pkg, GET_SIGNATURES)` → SHA-256 de `signatures[0]` → comparado contra hash ofuscado (XOR 90) `366fa8f1…f3f2cbac` (el cert original) → **`Process.killProcess(myPid())` si difiere**; además llama `zx/y.ck2()` (`libnoface.so`).
- Firma distinta = kill en bucle = **splash infinito** (causa raíz del fallo de dev.8, confirmada matemáticamente).
- El wrapper LSPatch del usuario lo esquivaba con su `sigBypassLevel 3` (equivale funcional a nuestro SigSpoof).
- dev.9: `n0/G.v` → `return-void` + SigSpoof → el panel sigue mostrándose pero nunca mata el proceso.
- `zx/y.ck2()` solo se llama desde `G.v` (neutralizado). `Lo92.run` (kill con `importance>=400`) es Unity OMS/EmojiCompat y solo actúa en background: inofensivo.

## 9. Equivalencias LSPatch ↔ nuestro pipeline

| LSPatch (mod viejo) | Nuestro pipeline Morphe |
| --- | --- |
| Wrapper re-firmado | Re-firma con keystore estable |
| `sigBypassLevel 3` (PM devuelve firma original) | Patch `Firma original en PackageManager (V29)` (SigSpoof) |
| `origin.apk` como código de la app | `Proyecto.ORIGIN.apk` como base |
| Panel del mod (`n0/W`) con su guardia | Guardia neutralizado (`n0/G.v`→return-void) |

## 10. Tabla de experimentos (fuente: BITACORA)

| Build | Base | Patches | Resultado en dispositivo |
| --- | --- | --- | --- |
| dev.5/6 | BASE limpio | 3 funcionales + gate V29 | Arranca; catálogo vacío; streams "sin conexión" |
| dev.7 | BASE limpio | + SigSpoof | Spoof activo (26+ lecturas); catálogo/streams fallan igual |
| dev.8 | ORIGIN | 3 funcionales (sin SigSpoof, sin n0/G) | **Splash infinito** (kill en bucle del guardia del panel) |
| dev.9 | ORIGIN | 3 funcionales + SigSpoof + n0/G→return-void | Listo para probar (estáticamente equivalente al APK que sí funcionaba) |
| dev.10 | BASE limpio **y** ORIGIN (dos assets) | Cada base con su matriz (§7) | Prueba A/B en dispositivo |

## 11. Reglas de operación (checklist antes de tocar el pipeline)

1. El asset publicado SIEMPRE lleva el tag del bundle en el nombre (`--clobber` en `app-mod`).
2. Nunca quitar el SigSpoof del build origin; nunca añadir `n0/G` al base limpio (aborta) ni el gate V29 a origin (aborta).
3. Tras cada build, verificar el APK final LEYENDO EL BYTECODE (androguard/baksmali), no confiando en el log del CLI: hooks (`const/4`+`return`), `n0/G.v`=`return-void`, `SigSpoof.install` en `attachBaseContext`/`onCreate`, BD pre-sembrada en origin.
4. Cada hallazgo nuevo → BITACORA.md con fecha, evidencia y archivos tocados.
5. Si el base limpio sigue fallando en catálogo/streams tras la prueba A/B, el siguiente paso de investigación es la validación a nivel archivo/servidor (§8a), NO volver a las andanzas ya descartadas.

## 10. Panel web "Kawaii Factory" (2026-11-20)

**Qué es**: panel AMOLED (#000 puro, acentos rosa/violeta, logo 改, pétalos sakura) que convierte el repo en una fábrica operable desde el celular. Un solo archivo `web/index.html` AUTOCONTENIDO (React 18 UMD + htm INLINE, sin build, sin CDN, ~176 KB), publicado en GitHub Pages (`.github/workflows/deploy-pages.yml`).

**Arquitectura (sin backend)**:
- Lecturas ANÓNIMAS (repo público): `GET /releases`, `GET /releases/tags/app-mod`, `GET /actions/runs?workflow=patch-apk.yml`, `GET /actions/runs/{id}/jobs` (pasos REALES del job → stepper de 5 etapas: Preparando → Descargando bundle → Embebiendo original → Parcheando/firmando → Publicando).
- Escritura con PAT (localStorage, nunca sale hacia otro sitio): `POST /actions/workflows/patch-apk.yml/dispatches` {ref:'dev', inputs:{variant, gate_patch, patches_tag}}. Permiso necesario: **Actions:write** (fine-grained) o scope **workflow** (classic).
- Caché localStorage (`kfactory.cache.v1`) para render instantáneo + sobrevivir al rate limit anónimo (60/h por IP); con token refetch autenticado (5000/h).
- Éxito → cajón expendedor animado (puerta CSS + cajas con glow) con descarga directa de los assets `-origin`/`-base` más recientes de app-mod.

**Lecciones del desarrollo** (bugs reales encontrados):
1. `htm.bind(createElement)` SOLO sirve para templates tagged; llamarlo directo como `h(App, null)` rompe (el parser recibe una función como statics → `TypeError: reading 'length'`). Fix: `const h = React.createElement` aparte.
2. `gh('/user')` se concatenaba con la URL del repo → 404 silencioso. Fix: gh() acepta URLs absolutas.
3. El dispatch valida inputs contra el workflow EN EL REF: si el archivo remoto no tiene el input nuevo → `Unexpected inputs provided: ["variant"]`. Siempre pushear el workflow antes de usar el input desde el panel.
4. Activar Pages por API requiere token con Administration:write; si el PAT no la tiene, es un clic manual (Settings → Pages → Source: GitHub Actions) y luego re-run de "Deploy Web (Pages)".

**Inputs de patch-apk.yml (v2)**: `variant` (ambas|origin|base, normalizado en el step tag; en evento release queda 'ambas'), `gate_patch` (AHORA se respeta de verdad en la variante base vía bash array), `patches_tag`. Publica solo los artefactos construidos.

**Correlación de versiones (duda cerrada)**: el APK embebido (assets/orig/app.apk) es byte-idéntico al pristine que se usa como entrada del parcheo — ambos v2.1.3 (code 29); los tags dev.N son versiones del BUNDLE de patches, no de la app. Verificado con androguard + SHA-256 (scripts/verify_versions.py).
