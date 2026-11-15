# MORPHE — REFERENCIA PERMANENTE DEL PROYECTO

> **Propósito de este archivo:** es el "mapa mental en texto" de nuestro proyecto. Se lee SIEMPRE antes de trabajar en cualquier patch. Vive en el repositorio (`docs/MORPHE-REFERENCIA.md`) y debe mantenerse actualizado a medida que aprendamos cosas nuevas.
>
> **Fecha de última investigación:** 2026-11-14 (fuentes listadas al final)
> **Enfoque prioritario:** Community patches (parches para apps NO oficiales: Duolingo, Instagram, Telegram, etc.)
> **Alcance:** Uso personal. No vamos a publicar ni enviar bundles al índice comunitario.

---

## 1. Qué es Morphe

- **Morphe** es una herramienta de modificación de apps Android (se pronuncia "morfe", como Morfeo; el nombre correcto es "Morphe", no "morfi").
- Modifica apps **a nivel de bytecode** directamente en el dispositivo: desensambla el APK, aplica parches y reconstruye un APK modificado que se instala en el teléfono.
- **No requiere root.** Todo el parcheo ocurre localmente en el teléfono; los datos no salen del dispositivo.
- Apps oficiales soportadas por Morphe: **YouTube, YouTube Music y Reddit** (bloqueo de anuncios, SponsorBlock, personalización de UI, etc.).
- Las demás apps se soportan mediante **community patches** (sección 5, nuestro foco).
- Proyecto **independiente basado en el trabajo previo de ReVanced**, creado por ex-contribuidores de ReVanced. Código bajo **GPL-3.0**, con regla de branding: derivados no pueden usar el nombre/logo "Morphe".
- Gratuito y open source. Organización GitHub verificada (dominio morphe.software).

## 2. Mapa del ecosistema (repos oficiales en github.com/MorpheApp)

| Repo | Rol | Lenguaje | Notas |
|---|---|---|---|
| `morphe-manager` | La app Android que parchea desde el teléfono | Kotlin (~10k estrellas) | Modo simple y modo experto; soporta APKs divididos, parcheo por lotes |
| `morphe-patches` | Parches OFICIALES (YouTube, YT Music, Reddit) | Java | ~197 parches (v2.2.0): YouTube 114, YTM 51, Reddit 32 |
| `morphe-patcher` | El motor de parcheo (librería) | Kotlin | Parchea bytecode Dalvik, recursos APK y archivos dentro del APK |
| `morphe-patches-library` | Librería compartida para desarrolladores de patches | Java + Kotlin | Utilidades comunes para bundles; v2.1.1 |
| `morphe-patches-template` | **PLANTILLA oficial para crear tu propio bundle** | Kotlin + Java | Punto de partida de nuestro repo |
| `morphe-desktop` | Herramienta de parcheo para PC | Kotlin | Requiere Java 21+ para ejecutarse; útil para probar bundles |
| `MicroG-RE` | Fork de GmsCore (microG) para apps parcheadas | Java | Necesario para YouTube/YTM sin Play Services reales |
| `ARSCLib`, `smali` | Forks de librerías (recursos binarios, ensamblador smali) | Java | Dependencias del motor |
| `morphe-documentation` | Documentación oficial | Markdown | Guías de desarrollo, FAQ, Manager y Desktop |

## 3. Cómo funciona el parcheo (flujo de usuario)

1. Instalar Morphe Manager (o Morphe Desktop en PC).
2. Elegir la app objetivo y el bundle de parches (el oficial, o un **bundle comunitario agregado por URL**).
3. Modo simple (un toque) o modo experto (seleccionar parches y opciones uno por uno).
4. Aporte del APK original (descargado desde la tienda o el sitio del desarrollador; el Manager ayuda con "Morphe Helper").
5. Morphe parchea localmente y produce el APK parcheado; se instala como app nueva o según el método elegido.
6. Opcional: personalizar nombre, ícono y logo de la app parcheada.

Requisitos del dispositivo: Android con permiso para instalar apps de fuentes desconocidas. Opcional: **Shizuku** (o Shizuku+/Sui) para funciones avanzadas. Para YouTube/YTM parcheados: **MicroG-RE** (el Manager lo maneja).

## 4. Parches oficiales vs community patches

- **Oficiales** (`morphe-patches`): mantenidos por el equipo Morphe, solo para YouTube / YouTube Music / Reddit. Se cargan por defecto en el Manager.
- **Community patches**: bundles mantenidos por cualquier persona de la comunidad, para CUALQUIER otra app (Instagram, TikTok, Telegram, Duolingo, Spotify, juegos, etc.).
  - Se agregan al Manager mediante **URL de GitHub/GitLab** (release del bundle) o enlace profundo (deep link).
  - Cada fuente tiene toggle para aceptar **pre-releases** (acceso temprano).
  - El Manager los actualiza automáticamente en segundo plano.
  - Morphe no revisa ni se responsabiliza de estos parches: cada bundle es responsabilidad de su autor.
- **Dato clave para nosotros:** publicar un repo público en GitHub NO equivale a "publicar al mundo". El índice comunitario (morphe-patches.software) es opt-in: si no envías tu repo al índice, nadie lo descubre salvo que tenga la URL. Esto permite usar GitHub público sin "publicar" oficialmente. La alternativa totalmente privada es compilar el `.mpp` localmente y pasarlo al teléfono, pero entonces el Manager no puede auto-actualizar el bundle.

## 5. COMMUNITY PATCHES — el ecosistema (nuestro foco)

### 5.1 Índices donde se listan bundles

| Índice | URL | Qué es |
|---|---|---|
| Morphe Community Patches (índice oficial) | https://morphe-patches.software | Directorio mantenido por la comunidad; enviar bundle = formulario con "Repository URL" (repo público GitHub/GitLab que publique releases de parches Morphe) |
| Awesome Morphe | https://github.com/nvbangg/awesome-morphe | Meta-lista de guías, índices y recursos (no afiliada a Morphe) |
| Fresh Morphe | https://drnx64.github.io/fresh-morphe/ | Índice alternativo |
| Morphe Archive | https://rushiforai.github.io/morphe-archive/ | Índice alternativo |
| Patch Bundle Registry | https://patch-bundles.jmancentral.com/ | Registro de bundles |

### 5.2 Apps con bundles comunitarios existentes (aprox. al 2026-11-14, según el índice oficial)

- **Instagram** — 16 bundles, 289 parches
- **TikTok** — 6 bundles, 208 parches (TikTok Lite: 25 parches)
- **Twitter/X** — 11 bundles, 157 parches
- **Facebook** — 6 bundles, 169 parches
- **Gboard** — 18 bundles, 133 parches
- **Telegram** — varios bundles, ~100 parches cada uno
- **Messenger** — 4 bundles, 109 parches
- **Waze** — 12 bundles, 87 parches
- **Discord** — 4 bundles, 85 parches
- También: Spotify, Google Maps, Tinder (2 bundles, 25 parches), Ozon (13), Smart Life (20), Block Blast! (23), DealSeek (14), **Duolingo**, y más.

### 5.3 Ejemplo real de repo comunitario: `hoo-dles/morphe-patches`

- Repo público (GPL-3.0, 1.3k estrellas) con parches hechos a mano para Morphe. Generado desde la plantilla oficial.
- Apps con parches: AdGuard (Enable Premium), Amazon Prime Video (control de velocidad, saltar ads), **Duolingo** (Disable dynamic app icon, Enable Super/MAX, etc.), FotMob (FotMob+), GitHub (tema AMOLED), Google News (custom tabs), Lightroom (unlock premium), MyFitnessPal (Premium+), Proton VPN (quitar delay de cambio de servidor, custom DNS), y más.
- Lecciones que ya aprendieron (aplican a nuestro proyecto):
  - Algunas apps exigen **versiones estrictas** del APK (el parche rompe si cambia la app).
  - Algunos parches solo funcionan en CPUs **arm64-v8a**.
  - Algunos requieren instalación por **Root Mount**.
  - Para funciones que dependen de Google Play Services hace falta el parche universal de **MicroG integration** (no aplica a la mayoría de apps que nos interesan, pero hay que saberlo).

### 5.4 Nota sobre contenido de la comunidad (importante)

En la comunidad conviven parches "legítimos" (bloquear anuncios, limpiar UI, quitar telemetría) y parches que **desbloquean funciones de pago** (p. ej. "Enable Super/MAX" en Duolingo, "Unlock premium" en Lightroom). Estos últimos son riesgosos legalmente, suelen romper cuando la app se actualiza o contacta con los servidores del desarrollador, y pueden provocar suspensiones de cuenta. Para nuestro uso personal conviene centrarse en parches de personalización/bloqueo de anuncios y evitar desbloqueos de suscripciones pagas.

## 6. CÓMO CREAR TU PROPIO BUNDLE (guía técnica paso a paso)

### 6.1 Requisitos del entorno de desarrollo

- **JDK** (la CI de validación de la comunidad usa **Java 17**; Morphe Desktop requiere Java 21+ para ejecutarse).
- **IntelliJ IDEA** (IDE recomendado; estilo de código `intellij_idea` vía `.editorconfig`; NO se permiten wildcard imports en Kotlin).
- **Gradle** (wrapper incluido en la plantilla; no se instala aparte).
- **GitHub PAT (Personal Access Token)** con scope **`read:packages`** — necesario porque los artefactos de Morphe se descargan de GitHub Packages al compilar. URL directa para crearlo: https://github.com/settings/tokens/new?scopes=read:packages&description=Morphe

### 6.2 Configuración inicial (según guía oficial `morphe-development/1_setup.md`)

1. Crear el repo propio desde la plantilla: usar `MorpheApp/morphe-patches-template` → "Use this template" → **activar 'Include all branches'** (trae la rama `dev`).
2. En Settings > Actions > General > Workflow permissions: habilitar **"Allow GitHub Actions to create and approve pull requests"**.
3. Clonar (mismo directorio padre, p. ej. `~/morphe/`):
   ```bash
   git clone -b dev https://github.com/MorpheApp/morphe-desktop
   git clone -b dev https://github.com/TU-USUARIO/tu-repo-patches
   ```
4. Configurar el token en `~/.gradle/gradle.properties` (FUERA del repo, no se commitea):
   ```properties
   gpr.user = TU-USUARIO
   gpr.key = TU_TOKEN_CON_READ_PACKAGES
   ```
5. Primera construcción:
   ```bash
   cd morphe-desktop && ./gradlew build
   cd ../tu-repo-patches && ./gradlew buildAndroid
   ```

### 6.3 Estructura del proyecto (viene de la plantilla)

```
mi-repo-patches/
├── .github/                  # Workflows (build, release semántico, PR automático)
├── extensions/
│   └── extension/            # Código que se INYECTA dentro de la app parcheada
├── gradle/                   # Wrapper y configuración
├── patches/
│   └── src/main/...          # LOS PATCHES (paquetes por app objetivo)
├── build.gradle.kts          # Configuración Gradle raíz
├── patches-bundle.json       # Metadatos del bundle
├── patches-list.json         # Lista de parches incluidos
├── patches-bundle.png        # (opcional) ícono del bundle en Morphe Manager
└── .gitignore                # Debe excluir claves y artefactos
```

### 6.4 Anatomía de un patch (lo esencial)

- Cada patch es una **clase Kotlin/Java** dentro de `patches/`, orientada a UNA app objetivo (paquete, ej. `com.duolingo`) y sus versiones compatibles.
- El patch declara **metadata** (nombre, descripción, app compatible, opciones si las tiene) y su lógica se apoya en:
  - **Fingerprints**: "huellas" que localizan de forma confiable métodos/campos/clases dentro del bytecode de la app objetivo aunque cambien los nombres entre versiones. Es el corazón de un patch estable.
  - **Bytecode patching**: modificar instrucciones Dalvik/smali (devolver otro valor, insertar llamadas, neutralizar métodos).
  - **Resource patching**: modificar recursos del APK (layouts, strings, íconos) vía ARSCLib.
  - **Extensions**: código companion que se inyecta en la app y al que el patch llama (para lógica más compleja que no cabe en bytecode).
- Documentación oficial de fingerprints y creación de patches: https://github.com/MorpheApp/morphe-patcher/blob/main/docs/README.md
- Guía para migrar patches de ReVanced a Morphe: https://github.com/MorpheApp/morphe-patcher/issues/22
- La API real ya está capturada en la sección 10 (extraída directamente del código de la plantilla oficial clonada). Doc profunda del patcher por leer: https://github.com/MorpheApp/morphe-patcher/blob/main/docs/2_2_1_fingerprinting.md

### 6.5 Compilar y probar

- Compilar el bundle: `./gradlew buildAndroid` → produce **`patches/build/libs/patches-*.mpp`** (formato de bundle de Morphe).
- Probar en PC con **Morphe Desktop** (aplica el `.mpp` como cualquier bundle) o pasar el `.mpp` al teléfono.
- Probar en el teléfono con **Morphe Manager**: agregar el bundle por URL del release de GitHub (fuentes del Manager) o instalando el `.mpp` manualmente.

### 6.6 Firmar releases (opcional, recomendado si se usa Manager por URL)

1. `./gradlew generateSigningKey` → crea `morphe-signing.pub` y `morphe-signing.key`.
2. **Commitear SOLO el `.pub`.** El `.key` NUNCA se commitea.
3. Guardar el contenido de `morphe-signing.key` como secreto `MORPHE_SIGNING_KEY` en Settings > Secrets and variables > Actions.

### 6.7 Publicar versiones (flujo de la plantilla)

1. Todo el desarrollo va a la rama **`dev`**.
2. Commits semánticos (definen el changelog automático):
   - `feat:` nueva función/parche → dispara pre-release automático.
   - `fix:` corrección → dispara pre-release automático.
   - `chore:` cambios internos → no aparece en el changelog de usuarios.
3. Cuando `dev` está estable: **merge a `main` SIN squash** (merge normal).
4. Los releases los genera SIEMPRE el workflow (`release.yml`, semantic-release). NUNCA crear releases manuales en GitHub.
5. Cada PR a `dev` ejecuta validación: `./gradlew build --no-daemon` (Ubuntu, Java 17) y crea un PR borrador automático hacia `main`.

## 7. PLAN DE NUESTRO PROYECTO (uso personal)

### 7.1 Decisiones ya tomadas

- Objetivo: crear patches comunitarios para apps no oficiales (Duolingo, Instagram, Telegram, las que definamos), **uso personal**.
- No enviaremos el repo al índice comunitario (morphe-patches.software). Opciones de visibilidad: repo público sin anunciar (el Manager puede auto-actualizarlo) o repo privado + pasar el `.mpp` al teléfono a mano. Decidir juntos.
- Repo construido sobre la plantilla oficial `morphe-patches-template` (hereda workflows, estructura y release semántico).
- Bitácora y reglas de agente en el repo (AGENTS.md, BITACORA.md).

### 7.2 Estructura propuesta del repo (sobre la plantilla)

```
morphe-patches-personal/            # nombre por definir
├── AGENTS.md                       # REGLAS para el agente (lo más importante)
├── BITACORA.md                     # Bitácora de trabajo (fecha, tarea, resultado)
├── README.md                       # Descripción del proyecto (personal)
├── docs/
│   └── MORPHE-REFERENCIA.md        # ESTE archivo (fuente de conocimiento)
├── .github/                        # (de la plantilla) workflows
├── extensions/extension/           # (de la plantilla)
├── patches/                        # Nuestros patches, organizados por app
├── build.gradle.kts
├── patches-bundle.json
├── patches-list.json
└── .gitignore                      # Incluir: *.key, gradle.properties locales, .mpp, secrets
```

### 7.3 Plantilla de AGENTS.md (para crear cuando armemos el repo)

```markdown
# AGENTS.md — Reglas del agente para este repositorio

## Contexto
Proyecto PERSONAL de community patches para Morphe (parcheador Android).
Basado en MorpheApp/morphe-patches-template. Uso interno; no enviar al índice
comunitario ni promocionar. Idioma de trabajo: español.

## Antes de trabajar
- Leer docs/MORPHE-REFERENCIA.md (fuente de conocimiento del proyecto).
- Leer la última entrada de BITACORA.md para saber dónde quedamos.

## Reglas técnicas
- Branch de desarrollo: `dev`. main solo recibe merge desde dev (sin squash).
- Commits semánticos: feat: / fix: / chore: (definen el changelog).
- Releases SOLO por el workflow (semantic-release); nunca manuales.
- Un patch = una app objetivo + versiones compatibles declaradas.
- Preferir fingerprints estables; probar contra la versión exacta del APK.
- No modificar el código de la plantilla salvo necesidad justificada (anotar en bitácora).

## Seguridad
- NUNCA commitear tokens, claves o el archivo de secretos.
- El token vive en ~/.gradle/gradle.properties (gpr.user / gpr.key) o en Secrets de GitHub.
- Si un token se expone: revocar inmediatamente en GitHub y avisar al usuario.

## Bitácora
- Al terminar cada sesión: agregar entrada en BITACORA.md
  (Fecha | Tarea | Archivos tocados | Resultado | Próximo paso).
```

### 7.4 Formato de BITACORA.md

```markdown
# Bitácora

| Fecha | Tarea | Archivos | Resultado | Próximo paso |
|---|---|---|---|---|
| 2026-11-14 | Investigación y referencia Morphe | docs/MORPHE-REFERENCIA.md | Doc creada | Definir apps objetivo y crear repo |
```

### 7.5 El archivito de claves — reglas de seguridad

- Necesitamos UN token de GitHub (PAT) con scope **`read:packages`** para compilar, y permisos de repo/push para subir cambios.
- Dónde vive el token:
  1. **Local:** `~/.gradle/gradle.properties` (`gpr.user`, `gpr.key`) — fuera del repo. Recomendado.
  2. Si se prefiere un archivo dentro del proyecto: nombrarlo p. ej. `secrets/local.env`, agregarlo a `.gitignore` ANTES de crearlo, y jamás hacer `git add -f`.
  3. **CI:** usar GitHub Secrets (no archivos) — `MORPHE_SIGNING_KEY` y variables para packages.
- Buenas prácticas: token con fecha de expiración corta (90 días), permisos mínimos, revocar y rotar si se sospecha fuga; nunca pegarlo en commits, issues, screenshots o el chat si se puede evitar.
- Recordatorio para mí: si el usuario comparte el token en un archivo, NO copiarlo jamás a ningún archivo trackeado ni imprimirlo completo.

## 8. Glosario rápido

- **APK / split APK**: paquete instalable de Android / conjunto de APKs parciales (por arquitectura, idioma, densidad). Morphe soporta splits.
- **Bytecode Dalvik / smali**: código ejecutable de Android; smali es su representación legible.
- **Patch**: modificación unitaria aplicada a una app (quitar ads, cambiar UI...).
- **Bundle**: paquete distribuido de patches (archivo `.mpp`) que el Manager/Desktop carga.
- **Fingerprint**: patrón que identifica un método/campo/clase de la app objetivo de forma estable entre versiones.
- **Extension**: código inyectado en la app parcheada para lógica compleja.
- **PAT**: Personal Access Token de GitHub.
- **Shizuku**: servicio que da privilegios elevados a apps sin root (opcional para Morphe).
- **microG / MicroG-RE**: reimplementación libre de Google Play Services; necesaria para YouTube/YTM parcheados.
- **Sideloading**: instalar apps fuera de la tienda (permiso de "fuentes desconocidas").
- **Root Mount**: método de instalación que monta la app parcheada sobre la original (requiere root).

## 9. Tareas siguientes (roadmap del proyecto)

1. Definir las primeras apps objetivo y qué debe hacer cada patch (el usuario lo dirá).
2. Crear el repo desde `morphe-patches-template` ("Include all branches" activado) y preparar workflows.
3. Crear el PAT (`read:packages`), configurar `~/.gradle/gradle.properties`.
4. Agregar al repo: `AGENTS.md`, `BITACORA.md`, `README.md`, `docs/MORPHE-REFERENCIA.md`, `.gitignore` reforzado.
5. Preparar el entorno local (JDK 17, IntelliJ IDEA, clonar morphe-desktop + nuestro repo).
6. Leer juntos la doc del patcher (fingerprints y ejemplos) antes del primer patch.
7. Primer patch piloto en una app sencilla; validar el ciclo completo: patch → buildAndroid → .mpp → Manager/Desktop → probar → release en dev.

## 10. API REAL de los patches (extraída del código de la plantilla oficial, v2.1.0)

> Verificada en vivo el 2026-11-14 clonando `MorpheApp/morphe-patches-template`. Ejemplos en `patches/src/main/kotlin/app/template/patches/`.

### 10.1 Definir un patch
```kotlin
val examplePatch = patch(
    name = "Example Patch",          // visible en Morphe Manager
    description = "...",
    default = true                   // activado por defecto
) {
    compatibleWith(COMPATIBILITY_EXAMPLE)   // objeto Compatibility (ver 10.3)
    dependsOn(internalPatch)                // encadenar patches internos
    extendWith("extensions/extension.mpe")  // inyectar extension compilada
    execute {
        AlgoFingerprint.method.addInstructions(0, """
            invoke-static {}, Lapp/extension/Clase;->metodo()Z
            move-result v0
            return v0
        """)
    }
}
```
- Patch interno (no aparece en la lista del Manager): `val x = patch { execute { ... } }`.
- Variantes de edición: `addInstruction`, `addInstructions`, etc. (paquete `app.morphe.patcher.extensions.InstructionExtensions`).

### 10.2 Fingerprints (localizar código objetivo de forma estable)
```kotlin
object AdLoaderFingerprint : Fingerprint(
    definingClass = "Lcom/some/app/ads/AdsLoader;",  // parcial ok: ":com/some/app/ads/", "/AdsLoader;"
    name = "showAds",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;", "I", "L"),  // "L" = clase ofuscada
    filters = listOf(
        fieldAccess(opcode = Opcode.IGET, definingClass = "this", type = "Ljava/util/Map;"),
        string("showBannerAds"),
        methodCall(definingClass = "Ljava/lang/String;", name = "equals"),
        opcode(Opcode.MOVE_RESULT, MatchAfterImmediately()),
        literal(1337),
        opcode(Opcode.IF_EQ)
    )
)
// Uso: AlgoFingerprint.method → el método encontrado
// Fingerprint inline: Fingerprint(classFingerprint = X, name = "otro", parameters = listOf(...))
```
- Doc oficial de fingerprinting: https://github.com/MorpheApp/morphe-patcher/blob/main/docs/2_2_1_fingerprinting.md

### 10.3 Declarar compatibilidad de app (Constants.kt)
```kotlin
val COMPAT = Compatibility(
    name = "XYZ app",                    // nombre en el launcher
    packageName = "com.example.app",
    apkFileType = ApkFileType.APK,       // debe coincidir con APKMirror/Uptodown (APK/APKM/XAPK)
    appIconColor = 0xFF0045,
    targets = listOf(
        AppTarget(version = "2.0.0"),    // versión confirmada 100% funcional
        AppTarget(version = null, isExperimental = true)  // "cualquier versión" experimental
        // AppTarget(version = "1.0.5", versionCodes = mapOf(SupportedAbi.ARM64_V8A to 584009457))
    )
)
```

### 10.4 Extensiones (código Java inyectado en la app)
- Ubicación: `extensions/extension/src/main/java/...` (clases Java normales).
- El bundle la empaqueta como `extensions/extension.mpe` y se declara con `extendWith(...)`.
- Solo necesaria para lógica compleja; los overrides de valores fijos no la requieren.

### 10.5 Metadatos del bundle (`patches/build.gradle.kts`)
```kotlin
patches { about { name / description / source / author / contact / website / license } }
```

### 10.6 Generación automática (`.releaserc`)
- En release, semantic-release: regenera `patches-list.json`, `patches-bundle.json`, `README.md` (¡se sobrescribe!) y `CHANGELOG.md`, y publica el `.mpp` en releases.
- Por eso la documentación personal va en `docs/`, NO en README.md.

## 11. Fuentes consultadas (2026-11-14)

- Sitio oficial: https://morphe.software
- Org GitHub: https://github.com/MorpheApp
- Manager: https://github.com/MorpheApp/morphe-manager
- Patches oficiales: https://github.com/MorpheApp/morphe-patches
- Patcher: https://github.com/MorpheApp/morphe-patcher
- Template: https://github.com/MorpheApp/morphe-patches-template
- Patches library: https://github.com/MorpheApp/morphe-patches-library
- Desktop docs: https://github.com/MorpheApp/morphe-desktop/blob/main/docs/documentation.md
- Documentación oficial: https://github.com/MorpheApp/morphe-documentation
- Guía de desarrollo: https://github.com/MorpheApp/morphe-documentation/blob/main/docs/morphe-development/README.md
- Setup del entorno: https://github.com/MorpheApp/morphe-documentation/blob/main/docs/morphe-development/1_setup.md
- Índice community: https://morphe-patches.software
- Ejemplo comunitario: https://github.com/hoo-dles/morphe-patches
- Awesome Morphe: https://github.com/nvbangg/awesome-morphe
- Migración ReVanced → Morphe: https://github.com/MorpheApp/morphe-patcher/issues/22
