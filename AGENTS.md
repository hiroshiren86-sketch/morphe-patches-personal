# AGENTS.md — Reglas del agente para este repositorio

## Contexto

Proyecto PERSONAL de community patches para Morphe (parcheador Android).
Generado desde `MorpheApp/morphe-patches-template`. Propietario: `hiroshiren86-sketch`.
Uso interno: NO enviar al índice comunitario (morphe-patches.software) ni promocionar.
Idioma de trabajo: español.

## Orden de lectura obligatorio al iniciar cada sesión

1. `docs/MORPHE-REFERENCIA.md` — fuente de conocimiento (ecosistema + API real de los patches).
2. `BITACORA.md` (última entrada) — dónde quedamos la sesión anterior.
3. `docs/PROYECTO.md` — objetivo, filosofía de patches y método de prueba.

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
- `README.md` es regenerado automáticamente por semantic-release en cada release:
  la documentación propia vive en `docs/`, no en README.md.
- No renombrar los paquetes base de la plantilla (`app.template`, `util`) sin necesidad
  justificada y verificación de build.
- Los APK de prueba nunca se commitean (`.gitignore` ya excluye `*.apk`).

## Seguridad

- NUNCA commitear tokens, claves ni secretos (`secrets/`, `*.env`, `*.key`, credenciales en `gradle.properties`).
- El token para compilar vive en `~/.gradle/gradle.properties` (`gpr.user` / `gpr.key`) o en Secrets de GitHub.
- Si un token se expone: revocarlo inmediatamente en GitHub (Settings > Developer settings > Tokens) y avisar al propietario.
- Firma del bundle (opcional): `morphe-signing.pub` SÍ se commitea; `morphe-signing.key` NUNCA
  (va como secreto `MORPHE_SIGNING_KEY` en GitHub).

## Bitácora

- Al terminar cada sesión: agregar una fila al inicio de la tabla en `BITACORA.md`
  (Fecha | Tarea | Archivos | Resultado | Próximo paso). Entradas más nuevas primero.
