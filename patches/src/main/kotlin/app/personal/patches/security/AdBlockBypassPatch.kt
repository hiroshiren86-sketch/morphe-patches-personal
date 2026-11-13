package app.personal.patches.security

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.patch
import app.personal.patches.Constants.KAWAII_ANIMES
import app.personal.patches.replaceMethodBody

object IsDomainBlockedFingerprint : Fingerprint(
    definingClass = "Lcom/darkdeveloper/kawaiianimes/data/security/AdBlockDetector_androidKt;",
    name = "isDomainBlocked",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("L")
)

/**
 * Réplica del mod de referencia (origin):
 * el detector AdBlock por DNS queda desactivado (isDomainBlocked -> false,
 * sin consultas DNS contra gateway.unityads.unity3d.com y similares).
 */
@Suppress("unused")
val adblockBypassPatch = patch(
    name = "Bypass del detector de AdBlock",
    description = "Desactiva la detección de bloqueadores de anuncios por DNS: isDomainBlocked siempre responde false y no se consultan dominios de Unity Ads.",
    default = true
) {
    compatibleWith(KAWAII_ANIMES)

    execute {
        replaceMethodBody(IsDomainBlockedFingerprint.method, """
            sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
            return-object v0
        """)
    }
}
