# 👋🧩 Morphe Patches template

Template repository for Morphe Patches.

## ❓ About

Patches for apps I like.

<!-- TODO: Update this about section with a brief introduction/summary about this repo and what it offers. -->

### How to use these patches

Click the button below, or manually add this repo url to Morphe Manager: https://github.com/hiroshiren86-sketch/morphe-patches-personal

<a href="https://morphe.software/add-source?github=hiroshiren86-sketch/morphe-patches-personal">
  <img src=".github/assets/add-to-morphe.svg" alt="Add to Morphe" height="48">
</a>

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0-dev.3](https://github.com/hiroshiren86-sketch/morphe-patches-personal/releases/tag/v1.0.0-dev.3)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;4 patches total
<details open>
<summary>📦 Kawaii Animes&nbsp;&nbsp;•&nbsp;&nbsp;4 patches</summary>
<br>

**Package name:** `com.darkdeveloper.kawaiianimes`

**🎯 Supported versions:**

| 2.1.3 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Bypass del detector de AdBlock](#bypass-del-detector-de-adblock) | Desactiva la detección de bloqueadores de anuncios por DNS: isDomainBlocked siempre responde false y no se consultan dominios de Unity Ads. |  |
| [Compatibilidad con integridad nativa (V29)](#compatibilidad-con-integridad-nativa-v29) | La app firma sus peticiones al catalogo con libprotection.so, que verifica la firma del APK. Al re-firmar con Morphe ese gate queda en false. Este patch carga la lib nativa y fuerza el gate a true sin ejecutar el chequeo, para que el app use las rutas de proteccion normales. Pruebalo solo si la app se cierra al arrancar o el catalogo no carga con los demas patches. |  |
| [Recompensa instantánea de episodios](#recompensa-instant-nea-de-episodios) | Todos los episodios se consideran con la recompensa del anuncio ya pagada, desbloqueando el contenido que exige ver anuncios sin verlos. |  |
| [Saltar anuncios de episodios](#saltar-anuncios-de-episodios) | La app nunca decide mostrar anuncios entre episodios: shouldShowAdNext devuelve false fijo, igual que el APK parcheado de referencia. |  |

</details>

<!-- PATCHES_END -->

### 🛠️ Building locally

- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## 📜 License

UserXYZ Patches are licensed under the [GNU General Public License v3.0](LICENSE)
