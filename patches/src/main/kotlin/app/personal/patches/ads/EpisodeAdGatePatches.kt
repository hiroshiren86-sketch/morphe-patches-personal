package app.personal.patches.ads

import app.morphe.patcher.patch.patch
import app.personal.patches.Constants.KAWAII_ANIMES
import app.personal.patches.replaceMethodBody
import app.personal.patches.resolveMethodWithFallback

/**
 * Réplica del mod de referencia (origin):
 * EpisodeAdGateManager.shouldShowAdNext siempre devuelve false.
 */
@Suppress("unused")
val skipEpisodeAdGatePatch = patch(
    name = "Saltar anuncios de episodios",
    description = "La app nunca decide mostrar anuncios entre episodios: shouldShowAdNext devuelve false fijo, igual que el APK parcheado de referencia.",
    default = true
) {
    compatibleWith(KAWAII_ANIMES)

    execute {
        val method = resolveMethodWithFallback(
            "Saltar anuncios de episodios",
            ShouldShowAdNextFingerprint,
            ShouldShowAdNextRelaxedFingerprint,
        )

        replaceMethodBody(method, """
            const/4 p0, 0x0
            return p0
        """)
    }
}

/**
 * Réplica del mod de referencia (origin):
 * EpisodeAdGateManager.isAdRewarded siempre devuelve true.
 */
@Suppress("unused")
val instantAdRewardPatch = patch(
    name = "Recompensa instantánea de episodios",
    description = "Todos los episodios se consideran con la recompensa del anuncio ya pagada, desbloqueando el contenido que exige ver anuncios sin verlos.",
    default = true
) {
    compatibleWith(KAWAII_ANIMES)

    execute {
        val method = resolveMethodWithFallback(
            "Recompensa instantánea de episodios",
            IsAdRewardedFingerprint,
            IsAdRewardedRelaxedFingerprint,
        )

        replaceMethodBody(method, """
            const/4 p0, 0x1
            return p0
        """)
    }
}
