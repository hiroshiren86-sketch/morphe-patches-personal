package app.personal.patches.ads

import app.morphe.patcher.Fingerprint

object ShouldShowAdNextFingerprint : Fingerprint(
    definingClass = "Lcom/darkdeveloper/kawaiianimes/data/ads/EpisodeAdGateManager;",
    name = "shouldShowAdNext",
    returnType = "Z",
    parameters = listOf("Z")
)

object IsAdRewardedFingerprint : Fingerprint(
    definingClass = "Lcom/darkdeveloper/kawaiianimes/data/ads/EpisodeAdGateManager;",
    name = "isAdRewarded",
    returnType = "Z",
    parameters = listOf("I")
)

object AdManagerInitializeFingerprint : Fingerprint(
    definingClass = "Lcom/darkdeveloper/kawaiianimes/data/ads/AdManager;",
    name = "initialize",
    returnType = "V",
    parameters = listOf("Z")
)
