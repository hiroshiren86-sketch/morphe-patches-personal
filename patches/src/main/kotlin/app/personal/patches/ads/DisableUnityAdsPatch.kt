package app.personal.patches.ads

import app.morphe.patcher.patch.patch
import app.personal.patches.Constants.KAWAII_ANIMES
import app.personal.patches.replaceMethodBody
import app.personal.patches.resolveMethodWithFallback

/**
 * Patch opcional (no replicado del mod de referencia, que mantiene la
 * inicialización del SDK). Ahorra red y telemetría al no inicializar Unity Ads.
 */
@Suppress("unused")
val disableUnityAdsPatch = patch(
    name = "Desactivar Unity Ads por completo",
    description = "Evita que el SDK de Unity Ads se inicialice (menos red y telemetría). Con los patches de anuncios activos ya no se muestran anuncios; este patch es opcional.",
    default = false
) {
    compatibleWith(KAWAII_ANIMES)

    execute {
        val method = resolveMethodWithFallback(
            "Desactivar Unity Ads por completo",
            AdManagerInitializeFingerprint,
            AdManagerInitializeRelaxedFingerprint,
        )

        replaceMethodBody(method, "return-void")
    }
}
