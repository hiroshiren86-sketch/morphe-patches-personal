# AGENTS.md — Reglas del agente para este repositorio

## Contexto

Proyecto PERSONAL de community patches para Morphe (parcheador Android).
Generado desde `MorpheApp/morphe-patches-template`. Propietario: `hiroshiren86-sketch`.
Uso interno: NO enviar al índice comunitario (morphe-patches.software) ni promocionar.
Idioma de trabajo: español.

## Orden de lectura obligatorio al iniciar cada sesión

1. `docs/MORPHE-REFERENCIA.md` — fuente de conocimiento (ecosistema + API real de los patches).
2. `docs/PLANO-MORPHE.md` — modelo mental completo del pipeline: CLI/bundle/CI, los DOS bases (matriz de patches × bases §7), guardas conocidas y tabla de experimentos.
3. `BITACORA.md` (última entrada) — dónde quedamos la sesión anterior.
4. `docs/PROYECTO.md` — objetivo, filosofía de patches y método de prueba.

## Reglas técnicas

- Branch de trabajo: `dev`. `main` solo recibe merge desde `dev` (merge normal, SIN squash).
- Commits semánticos (definen el changelog y los releases):
  - `feat:` parche o función nueva → dispara release/pre-release automático.
  - `fix:` corrección → dispara release/pre-release automático.
  - `chore:` cambios internos → no genera release.
- Releases SOLO por el workflow (semantic-release). NUNCA crear releases ni tags manuales.
- Filosofía del propietario: VARIOS patches pequeños (uno por función), NO un mega-patch.
- Preferir fingerprints estables; declarar en `AppTarget` la versión exacta del APK probada.
- `replaceMethodBody` SOLO es seguro en métodos SIN bloques try-catch: vaciar el cuerpo
  deja el rango `.catch` corrupto (VerifyError al cargar la clase = app se cierra al
  arrancar). En métodos con try-catch usar `addInstructions(0, "...")` con retorno
  temprano (el original queda inalcanzado pero intacto). Ver BITACORA 2026-11-14.
- No vaciar métodos que inicializan estado de SDKs (ej. `AdManager.initialize`):
  dejan campos a medias y NPE en arranque. Si el mod de referencia no lo hace, replicarlo
  tal cual y no "mejorarlo".
- Verificar SIEMPRE si el APK tiene protección nativa (`lib/*.so` + strings `JNI_OnLoad`,
  `RegisterNatives`, nombre del bridge) antes de asumir que un crash es de los patches.
  Los métodos `native` NO se pueden parchear (no tienen cuerpo); se parchea su llamador.
- NO escanear `method.implementation.instructions` a mano en los patches: dentro del run
  real del parcheador ese scan puede divergir del que hace el motor (falló con el gate
  de q25 aunque el método tenía el sput-boolean). Usar SIEMPRE los `instructionMatches`
  del propio fingerprint: declarar la estructura completa con filtros encadenados
  (`methodCall` / `opcode` con `MatchAfterImmediately`) y leer índices, registros y
  literales de `Match.InstructionMatch`. El motor que matcheó es la fuente de verdad.
- Para reemplazos quirúrgicos, mantener el MISMO número de instrucciones y tamaños
  (code units): `invoke` 35c (2u) ↔ `const-wide` 51l (2u), `move-result-wide` 11x (1u) ↔
  `nop` 10x (1u). Los rangos try-catch quedan con los mismos offsets → dex válido.
- PIPELINE AUTOMÁTICO (workflow `patch-apk.yml`): cada release publicado parchea el APK
  base con el CLI oficial `morphe-desktop` (jar `-all` de MorpheApp/morphe-desktop) y
  publica el APK en el release fijo `app-mod`. Firma con keystore PKCS12 ESTABLE en los
  secrets `MORPHE_KEYSTORE_P12` + `MORPHE_KEYSTORE_PASSWORD` (alias `Morphe`, contraseña
  de prueba `kawaii-mod-2026` — NO es un secreto real, es clave personal para instalar).
  NO usar `--striplibs` (libprotection.so es necesaria). Reproducir fallos del pipeline
  en local: `java -jar morphe-desktop.jar patch --patches patches-X.Y.Z.mpp -e "Patch"
  -o out.apk base.apk` (motor idéntico al Manager, ~1s por run).
- Al hacer push a `dev`, esperar a que termine el workflow Release antes de pushear otro
  commit (si el branch queda "behind", semantic-release salta la publicación y el
  release lo termina generando el run siguiente — funciona pero confunde).
- `README.md` es regenerado automáticamente por semantic-release en cada release:
  la documentación propia vive en `docs/`, no en README.md.
- No renombrar los paquetes base de la plantilla (`app.template`, `util`) sin necesidad
  justificada y verificación de build.
- Los APK de prueba nunca se commitean (`.gitignore` ya excluye `*.apk`).
- DOS bases válidas (ver docs/PLANO-MORPHE.md §6-§7): `Proyecto.BASE.apk` (limpio oficial,
  con protección nativa V29 de fábrica → necesita gate V29 + SigSpoof) y `Proyecto.ORIGIN.apk`
  (variante con BD pre-sembrada y el panel del mod anterior fusionado → necesita SigSpoof +
  `Neutralizar antitamper del panel (n0/G)`, y NO admite el gate V29). El CI genera AMBAS
  variantes por release (`-base` y `-origin`); la matriz §7 dice qué patch admite cada base
  (un patch cuya clase no existe en el base ABORTA el run completo).
- El flujo del propietario es el de Morphe: base limpio + bundle → APK parchado entregado;
  el propietario NO puede re-modificar su APK instalado a mano. La variante origin no es
  una desviación: es la única que ha demostrado funcionar en su dispositivo y el CI la
  mantiene en paralelo mientras el base limpio no supere su validación nativa (§8a del plano).
- Antes de asumir que un hang/crash es de los patches, AUDITAR los dexes del base completo
  (los 4 de origin incluyen el mod anterior fusionado): buscar `getPackageInfo`+`GET_SIGNATURES`
  (flag 64), comparaciones SHA-256 de `signatures[0]`, `Process.killProcess`, llamadas
  reflectivas (`Class.forName`) y `System.loadLibrary` extraños. Método probado: tabla
  method_ids + `.BBBB` de las instrucciones invoke (androguard), no escaneos de operands.

## Seguridad

- NUNCA commitear tokens, claves ni secretos (`secrets/`, `*.env`, `*.key`, credenciales en `gradle.properties`).
- El token para compilar vive en `~/.gradle/gradle.properties` (`gpr.user` / `gpr.key`) o en Secrets de GitHub.
- Si un token se expone: revocarlo inmediatamente en GitHub (Settings > Developer settings > Tokens) y avisar al propietario.
- Firma del bundle (opcional): `morphe-signing.pub` SÍ se commitea; `morphe-signing.key` NUNCA
  (va como secreto `MORPHE_SIGNING_KEY` en GitHub).

## Bitácora

- Al terminar cada sesión: agregar una fila al inicio de la tabla en `BITACORA.md`
  (Fecha | Tarea | Archivos | Resultado | Próximo paso). Entradas más nuevas primero.
