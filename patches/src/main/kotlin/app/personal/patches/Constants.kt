package app.personal.patches

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val KAWAII_ANIMES = Compatibility(
        name = "Kawaii Animes",
        packageName = "com.darkdeveloper.kawaiianimes",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2E7DF6,
        targets = listOf(
            // Versión exacta del APK base sobre la que se desarrollaron los patches
            AppTarget(version = "2.1.3")
        )
    )
}
