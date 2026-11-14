package app.personal.patches.security

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.patch
import app.personal.patches.Constants.KAWAII_ANIMES
import app.personal.patches.resolveMethodWithFallback
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions

/**
 * KawaiiApp es la Application declarada en el manifest (no ofuscada). La lib
 * nativa obtiene el PackageManager via ActivityThread.currentApplication(),
 * que devuelve ESTA instancia: instalar el proxy aqui cubre tambien las
 * llamadas JNI (dispatch virtual).
 */
object KawaiiAppOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/darkdeveloper/kawaiianimes/KawaiiApp;",
    superclass = "Landroid/app/Application;",
    name = "onCreate",
    returnType = "V",
)

/**
 * La proteccion nativa (libprotection.so) lee la firma del APK directamente
 * desde PackageManager (via JNI: ActivityThread.currentApplication() ->
 * getPackageManager() -> getPackageInfo -> signatures). Con el APK re-firmado
 * por Morphe, la libreria ve la firma nueva, el gate queda en false y los
 * proofs del catalogo salen invalidos: el servidor rechaza /api/sync y el
 * catalogo se queda cargando.
 *
 * Este patch instala un proxy del IPackageManager (campo mPM de
 * ApplicationPackageManager) que devuelve la firma ORIGINAL del APK para el
 * paquete propio. Asi la libreria nativa calcula proofs validos sin tocar el
 * codigo de la libreria. La firma original va embebida en la extension
 * (SigSpoof, tomada del mod de referencia LSPatch).
 *
 * Requiere la extension extension.mpe (extendWith) y va de la mano con el
 * patch de integridad nativa.
 */
@Suppress("unused")
val signatureSpoofPatch = patch(
    name = "Firma original en PackageManager (V29)",
    description = "La proteccion nativa lee la firma del APK via PackageManager y el servidor valida proofs calculados con la firma ORIGINAL. Este patch instala un proxy del PackageManager que devuelve la firma original, para que la libreria nativa genere proofs validos y el catalogo cargue con el APK re-firmado. Debe usarse sin eliminar las librerias nativas.",
    default = true
) {
    compatibleWith(KAWAII_ANIMES)
    extendWith("extensions/extension.mpe")

    execute {
        val method = resolveMethodWithFallback(
            "Firma original en PackageManager (V29)",
            KawaiiAppOnCreateFingerprint,
        )
        method.addInstructions(
            0,
            """
            invoke-static {p0}, Lapp/template/extension/signature/SigSpoof;->install(Landroid/content/Context;)V
            """.trimIndent()
        )
    }
}
