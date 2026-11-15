## [1.0.0-dev.9](https://github.com/hiroshiren86-sketch/morphe-patches-personal/compare/v1.0.0-dev.8...v1.0.0-dev.9) (2026-11-15)

### ✨ New Features

* neutralizar antitamper del panel del mod anterior (n0/G) ([6b19984](https://github.com/hiroshiren86-sketch/morphe-patches-personal/commit/6b19984decf5eee0e5591ca9c31c77218cc8c3f1))

## [1.0.0-dev.8](https://github.com/hiroshiren86-sketch/morphe-patches-personal/compare/v1.0.0-dev.7...v1.0.0-dev.8) (2026-11-15)

### 🐛 Bug Fixes

* base correcto para el pipeline (Proyecto.ORIGIN.apk) ([01313bc](https://github.com/hiroshiren86-sketch/morphe-patches-personal/commit/01313bc4e681b6d5e3dd9e1d46cfef0fbbef0595))

## [1.0.0-dev.7](https://github.com/hiroshiren86-sketch/morphe-patches-personal/compare/v1.0.0-dev.6...v1.0.0-dev.7) (2026-11-15)

### 🐛 Bug Fixes

* doble punto y coma en el template smali del invoke-static (lexer error) ([54c1d8a](https://github.com/hiroshiren86-sketch/morphe-patches-personal/commit/54c1d8a4c4bcabb171757037696fc91fc7643cb8))

## [1.0.0-dev.6](https://github.com/hiroshiren86-sketch/morphe-patches-personal/compare/v1.0.0-dev.5...v1.0.0-dev.6) (2026-11-14)

### 🐛 Bug Fixes

* global mutableClassDefBy + hiddenApiRestrictions en ImmutableMethod (smali 3.x) ([aede160](https://github.com/hiroshiren86-sketch/morphe-patches-personal/commit/aede1607617bb145dabafba368b6cd445b242475))
* imports de dexlib2 en paquete immutable ([ef99e6b](https://github.com/hiroshiren86-sketch/morphe-patches-personal/commit/ef99e6b1410c52778c086d3fb488096fa90fafc7))
* orden de parametros de ImmutableMethod (annotations antes de hiddenApiRestrictions) ([dbafbc7](https://github.com/hiroshiren86-sketch/morphe-patches-personal/commit/dbafbc7a5ec64e841f3a3df48cc6ff646ebf2e08))
* spoof de firma en attachBaseContext + diagnostico sin ADB ([39d9770](https://github.com/hiroshiren86-sketch/morphe-patches-personal/commit/39d97709a0083e2d20aa00c332d0c2eb87ae6485))

## [1.0.0-dev.5](https://github.com/hiroshiren86-sketch/morphe-patches-personal/compare/v1.0.0-dev.4...v1.0.0-dev.5) (2026-11-14)

### 🐛 Bug Fixes

* spoof de firma original via proxy de PackageManager para proofs validos ([6c0f89a](https://github.com/hiroshiren86-sketch/morphe-patches-personal/commit/6c0f89a8deda0ef92be967df7bc05a189712f64c))

## [1.0.0-dev.4](https://github.com/hiroshiren86-sketch/morphe-patches-personal/compare/v1.0.0-dev.3...v1.0.0-dev.4) (2026-11-14)

### 🐛 Bug Fixes

* patch V29 engine-driven + pipeline de APK automatico en CI ([bf79785](https://github.com/hiroshiren86-sketch/morphe-patches-personal/commit/bf797855ada27bdf8e6edfed6bf1c4747a5ada07))

## [1.0.0-dev.3](https://github.com/hiroshiren86-sketch/morphe-patches-personal/compare/v1.0.0-dev.2...v1.0.0-dev.3) (2026-11-14)

### 🐛 Bug Fixes

* crash al arrancar por replaceMethodBody en initialize con try-catch ([804f570](https://github.com/hiroshiren86-sketch/morphe-patches-personal/commit/804f5705a634058129ea2abef07bcb54ac59c882))

## [1.0.0-dev.2](https://github.com/hiroshiren86-sketch/morphe-patches-personal/compare/v1.0.0-dev.1...v1.0.0-dev.2) (2026-11-14)

### 🐛 Bug Fixes

* cascada de fingerprints para robustez entre builds de la app ([369ec40](https://github.com/hiroshiren86-sketch/morphe-patches-personal/commit/369ec4041a3b4e42b2cf6ffd924c87899ae27c48))

## 1.0.0-dev.1 (2026-11-13)

### ✨ New Features

* Agregar 4 patches para Kawaii Animes 2.1.3 (saltar anuncios, recompensa instantanea, bypass adblock, desactivar Unity Ads) ([2b225cc](https://github.com/hiroshiren86-sketch/morphe-patches-personal/commit/2b225cc952bdb5e62d7ae4cb66f44faf8826be13))

### 🐛 Bug Fixes

* appIconColor en formato 0xRRGGBB sin canal alfa ([fa22a2c](https://github.com/hiroshiren86-sketch/morphe-patches-personal/commit/fa22a2cf149522eea7b4345c2684c6d3423d95b2))
