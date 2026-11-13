# Bitácora del proyecto

> Entradas más nuevas primero. Formato: Fecha | Tarea | Archivos | Resultado | Próximo paso.

| Fecha | Tarea | Archivos | Resultado | Próximo paso |
|---|---|---|---|---|
| 2026-11-14 | Creación del repo desde la plantilla oficial `morphe-patches-template` (ramas main+dev). Personalización: reglas de agente, bitácora, referencia técnica del ecosistema + API real de patches, doc del proyecto, metadatos del bundle, `.gitignore` reforzado (secrets, env, keystores). Habilitado en GitHub: "Allow GitHub Actions to create and approve pull requests" + permisos de workflow en modo escritura | `AGENTS.md`, `BITACORA.md`, `docs/MORPHE-REFERENCIA.md`, `docs/PROYECTO.md`, `patches/build.gradle.kts`, `.gitignore` | Repo creado y entregado: `hiroshiren86-sketch/morphe-patches-personal`. CI debe validar la compilación de la plantilla en el push. Pendiente: los 2 APK subidos por el propietario (base y parcheado) NO llegaron a la sesión (eliminados/no disponibles), no se pudo hacer el diff | Re-subir los 2 APK; hacer diff base vs parcheado; listar las funciones a replicar como patches separados; escribir los patches reales; release en `dev` y prueba en Morphe Manager con la URL de la fuente |
