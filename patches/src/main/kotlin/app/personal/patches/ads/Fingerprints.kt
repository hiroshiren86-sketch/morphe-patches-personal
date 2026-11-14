package app.personal.patches.ads

import app.morphe.patcher.Fingerprint

/**
 * Cascada para shouldShowAdNext: primero la firma exacta del build de referencia,
 * luego la clase por prefijo. "shouldShowAdNext" es un nombre único en la app.
 */
object ShouldShowAdNextFingerprint : Fingerprint(
    definingClass = "Lcom/darkdeveloper/kawaiianimes/data/ads/EpisodeAdGateManager;",
    name = "shouldShowAdNext",
    returnType = "Z",
    parameters = listOf("Z")
)

object ShouldShowAdNextRelaxedFingerprint : Fingerprint(
    definingClass = "Lcom/darkdeveloper/kawaiianimes/data/ads/EpisodeAdGateManager",
    name = "shouldShowAdNext",
    returnType = "Z",
    parameters = listOf("Z")
)

/**
 * Cascada para isAdRewarded. "isAdRewarded" es un nombre único en la app.
 */
object IsAdRewardedFingerprint : Fingerprint(
    definingClass = "Lcom/darkdeveloper/kawaiianimes/data/ads/EpisodeAdGateManager;",
    name = "isAdRewarded",
    returnType = "Z",
    parameters = listOf("I")
)

object IsAdRewardedRelaxedFingerprint : Fingerprint(
    definingClass = "Lcom/darkdeveloper/kawaiianimes/data/ads/EpisodeAdGateManager",
    name = "isAdRewarded",
    returnType = "Z",
    parameters = listOf("I")
)
