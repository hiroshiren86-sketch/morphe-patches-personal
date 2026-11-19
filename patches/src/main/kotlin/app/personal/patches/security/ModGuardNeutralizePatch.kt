package app.personal.patches.security

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.patch
import app.personal.patches.Constants.KAWAII_ANIMES
import app.personal.patches.replaceMethodBody
import app.personal.patches.resolveMethodWithFallback

/**
 * El APK "origin" (el build oficial 2.1.3 que lleva dentro el wrapper LSPatch del
 * mod anterior) trae fusionado el código del mod viejo en classes4.dex:
 *
 *   MainActivity.onCreate (inyectado) -> Class.forName("n0.W").getMethod("show").invoke()
 *   n0/W.show -> n0/G.v(context)
 *   n0/G.v    -> getPackageInfo(pkg, GET_SIGNATURES) -> SHA-256 de signatures[0]
 *             -> lo compara con el SHA-256 del certificado ORIGINAL embebido y
 *                ofuscado (verificado: 366fa8f1...f3f2cbac == SHA-256 de
 *                META-INF/NOFACE.RSA) -> si difiere, Process.killProcess(myPid()).
 *             -> además llama a zx/y.ck2() (libnoface.so) con otro killProcess.
 *
 * Con el APK re-firmado (cualquier firma que no sea la original) el proceso muere
 * dentro de MainActivity.onCreate y Android lo relanza en bucle: splash infinito.
 * El wrapper LSPatch del mod anterior lo esquivaba porque su loader devuelve la
 * firma original (sigBypassLevel 3). Este patch neutraliza la verificación de raíz:
 * el cuerpo de n0/G.v pasa a ser un return-void, así el panel del mod sigue
 * mostrándose (W.show intacto) pero jamás mata el proceso.
 */
object ModGuardVFingerprint : Fingerprint(
    definingClass = "Ln0/G;",
    name = "v",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;")
)

@Suppress("unused")
val modGuardNeutralizePatch = patch(
    name = "Neutralizar antitamper del panel (n0/G)",
    description = "El build origin trae el panel del mod anterior fusionado (classes4) y su guardia n0/G.v mata el proceso si la firma del APK no es la original (bucle de splash infinito). Este patch reemplaza n0/G.v por return-void: la verificación deja de correr y el panel sigue funcionando.",
    default = true
) {
    compatibleWith(KAWAII_ANIMES)

    execute {
        val method = resolveMethodWithFallback(
            "Neutralizar antitamper del panel (n0/G)",
            ModGuardVFingerprint,
        )

        replaceMethodBody(method, """
            return-void
        """)
    }
}
