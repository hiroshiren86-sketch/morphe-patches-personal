# PROYECTO — Patches personales para Morphe

## Objetivo

Replicar, como **varios patches de Morphe separados (uno por función)**, el comportamiento
del APK ya parcheado del propietario. Uso 100% personal; no publicar en índices comunitarios.

## Método de prueba (definido por el propietario)

1. Push a `dev` con commits `feat:`/`fix:` → la CI genera pre-release automático con el `.mpp`.
2. En Morphe Manager: agregar la fuente de patches por URL del repositorio
   (`https://github.com/hiroshiren86-sketch/morphe-patches-personal`).
3. Parchear el APK base y verificar que cada patch se aplica y que Morphe lo acepta sin errores.

## Filosofía de patches

- Varios patches pequeños (uno por función detectada en el APK de referencia), NO un mega-patch.
- Cada patch declara su compatibilidad (paquete, versión exacta probada, tipo de APK).
- Fingerprints estables antes que nombres hardcodeados.
- Si una función es compleja, mover la lógica a una extension (`extensions/extension/`).

## Estado actual

- [x] Repo creado desde `morphe-patches-template` (2026-11-14)
- [x] Reglas (`AGENTS.md`), bitácora, referencia técnica (`docs/`)
- [x] Metadatos del bundle y `.gitignore` reforzado
- [x] Recibir APK base + APK parcheado (vía release `apks-trabajo`)
- [x] Diff base vs parcheado → funciones a replicar identificadas
- [x] Escribir los patches reales (4 patches, reemplazan el example de la plantilla)
- [x] Pre-release v1.0.0-dev.1 publicado en `dev` (bundle patches-1.0.0-dev.1.mpp)
- [x] Primera prueba en Morphe Manager: 3 de 4 patches OK, falló el Bypass de AdBlock (v1.0.0-dev.1)
- [x] Depuración: fingerprints en cascada + verificación con el motor real 2.3.0 contra los 3 APK (base, origin embebido, wrapper LSPatch)
- [ ] Prueba 2 en Morphe Manager con el release nuevo (usar el APK base original, NO el wrapper LSPatch)

### Lección aprendida en la depuración (2026-11-14)

- El fingerprint `isDomainBlocked` original matchea OK con el motor real 2.3.0 tanto en base.apk
  como en el `origin.apk` embebido de LSPatch: los builds difieren solo en la Continuation ofuscada
  (`lf1` vs `kf1`), que el parámetro relajado `"L"` (STARTS_WITH) ya cubre.
- Contra el **wrapper LSPatch** (parcheada.apk) todo fingerprint falla: el código real está
  embebido en `assets/lspatch/origin.apk`, no en los dex del APK. Morphe no parchea wrappers LSPatch:
  hay que usar el APK original de la app.
- Por robustez, cada patch ahora resuelve su método con una **cascada de fingerprints**
  (`resolveMethodWithFallback`): exacto → clase por prefijo → solo nombre+firma suspend, con error
  claro si nada matchea.

## Patches actuales (Kawaii Animes `com.darkdeveloper.kawaiianimes` 2.1.3)

| Patch | default | Qué replica del APK de referencia |
|---|---|---|
| Saltar anuncios de episodios | activado | `EpisodeAdGateManager.shouldShowAdNext` → `false` fijo |
| Recompensa instantánea de episodios | activado | `EpisodeAdGateManager.isAdRewarded` → `true` fijo |
| Bypass del detector de AdBlock | activado | `AdBlockDetector_androidKt.isDomainBlocked` → `false`, sin DNS |
| Desactivar Unity Ads por completo | desactivado (opcional) | `AdManager.initialize` → no-op (ahorra red/telemetría) |

No replicables como patches: el guardián de firma del modder (`n0/G` + `libnoface.so`),
el overlay de branding del modder (`n0/W`, logo + Telegram) y las funciones de una build
más nueva de la app que el APK de referencia traía (GoogleCast, UpdateInstaller, ETag...).

## Enlaces rápidos

- Plantilla origen: https://github.com/MorpheApp/morphe-patches-template
- Referencia completa del ecosistema y API: `docs/MORPHE-REFERENCIA.md`
- Índice comunitario (solo referencia, NO enviaremos nuestro repo): https://morphe-patches.software
