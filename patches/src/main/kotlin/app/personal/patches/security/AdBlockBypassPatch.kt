package app.personal.patches.security

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.patch
import app.personal.patches.Constants.KAWAII_ANIMES
import app.personal.patches.replaceMethodBody
import app.personal.patches.resolveMethodWithFallback

/**
 * Build de referencia (base.apk 2.1.3 v29): el método existe con esta firma exacta.
 * La clase es una función suspend de Kotlin, por eso retorna Object y el único
 * parámetro es la Continuation ofuscada (varía entre builds: lf1, kf1...).
 */
object IsDomainBlockedFingerprint : Fingerprint(
    definingClass = "Lcom/darkdeveloper/kawaiianimes/data/security/AdBlockDetector_androidKt;",
    name = "isDomainBlocked",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("L")
)

/**
 * Variante relajada: la clase por prefijo (cubre renombres del paquete o sufijo
 * como AdBlockDetector_androidKt -> AdBlockDetectorKt), el método igual.
 */
object IsDomainBlockedRelaxedFingerprint : Fingerprint(
    definingClass = "Lcom/darkdeveloper/kawaiianimes/data/security/AdBlockDetector",
    name = "isDomainBlocked",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("L")
)

/**
 * Último recurso: solo el nombre del método y su firma suspend.
 * "isDomainBlocked" es un nombre único en toda la app (verificado en 2.1.3),
 * así que este match no es ambiguo.
 */
object IsDomainBlockedNameOnlyFingerprint : Fingerprint(
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
        val method = resolveMethodWithFallback(
            "Bypass del detector de AdBlock",
            IsDomainBlockedFingerprint,
            IsDomainBlockedRelaxedFingerprint,
            IsDomainBlockedNameOnlyFingerprint,
        )

        replaceMethodBody(method, """
            sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
            return-object v0
        """)
    }
}
