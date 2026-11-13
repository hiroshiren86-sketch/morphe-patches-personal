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
- [ ] Recibir APK base + APK parcheado (no llegaron a la sesión 1)
- [ ] Diff base vs parcheado → lista de funciones a replicar
- [ ] Escribir los patches reales (reemplazar el example de la plantilla)
- [ ] Primer release en `dev` y prueba en Morphe Manager

## Enlaces rápidos

- Plantilla origen: https://github.com/MorpheApp/morphe-patches-template
- Referencia completa del ecosistema y API: `docs/MORPHE-REFERENCIA.md`
- Índice comunitario (solo referencia, NO enviaremos nuestro repo): https://morphe-patches.software
