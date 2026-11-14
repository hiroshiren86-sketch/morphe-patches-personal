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
> **[v1.0.0-dev.2](https://github.com/hiroshiren86-sketch/morphe-patches-personal/releases/tag/v1.0.0-dev.2)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;4 patches total
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
| [Desactivar Unity Ads por completo](#desactivar-unity-ads-por-completo) | Evita que el SDK de Unity Ads se inicialice (menos red y telemetría). Con los patches de anuncios activos ya no se muestran anuncios; este patch es opcional. |  |
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
