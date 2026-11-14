package app.personal.patches.security

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.patch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.personal.patches.Constants.KAWAII_ANIMES
import app.personal.patches.resolveMethodWithFallback
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.ImmutableMethod
import com.android.tools.smali.dexlib2.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.builder.ImmutableInstruction
import com.android.tools.smali.dexlib2.builder.instruction.ImmutableInstruction10x
import com.android.tools.smali.dexlib2.builder.instruction.ImmutableInstruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference

private const val PATCH_NAME = "Firma original en PackageManager (V29)"
private const val KAWAII_APP_CLASS = "Lcom/darkdeveloper/kawaiianimes/KawaiiApp;"
private const val SIGSPOOF_CLASS = "Lapp/template/extension/signature/SigSpoof;"

/**
 * KawaiiApp es la Application declarada en el manifest (no ofuscada). La lib
 * nativa obtiene el PackageManager via ActivityThread.currentApplication(),
 * que devuelve ESTA instancia: instalar el proxy aqui cubre tambien las
 * llamadas JNI (dispatch virtual).
 */
object KawaiiAppOnCreateFingerprint : Fingerprint(
    definingClass = KAWAII_APP_CLASS,
    superclass = "Landroid/app/Application;",
    name = "onCreate",
    returnType = "V",
)

/**
 * La proteccion nativa (libprotection.so) lee la firma del APK directamente
 * desde PackageManager (via JNI: ActivityThread.currentApplication() ->
 * getPackageManager() -> getPackageInfo -> signatures). Con el APK re-firmado
 * por Morphe, la libreria ve la firma nueva, y aunque el gate se fuerce a true
 * los proofs del catalogo salen invalidos: el servidor rechaza /api/sync y el
 * catalogo queda vacio (y los streams fallan con "no hay conexion").
 *
 * Este patch instala un proxy del IPackageManager (campo mPM de
 * ApplicationPackageManager) que devuelve la firma ORIGINAL del APK para el
 * paquete propio. La instalacion ocurre en Application.attachBaseContext (el
 * punto mas temprano del codigo de la app, antes de ContentProviders y de
 * onCreate) para cubrir cualquier inicializacion temprana de la proteccion.
 *
 * La firma original va embebida en la extension (SigSpoof, tomada del
 * config.json del mod de referencia LSPatch). SigSpoof escribe un log en
 * Android/data/com.darkdeveloper.kawaiianimes/files/sigspoof.log para poder
 * diagnosticar sin ADB.
 *
 * Requiere la extension extension.mpe (extendWith).
 */
@Suppress("unused")
val signatureSpoofPatch = patch(
    name = PATCH_NAME,
    description = "La proteccion nativa lee la firma del APK via PackageManager y el servidor valida proofs calculados con la firma ORIGINAL. Este patch instala un proxy del PackageManager que devuelve la firma original (instalado en attachBaseContext, antes de cualquier inicializacion), para que la libreria nativa genere proofs validos y el catalogo y los streams funcionen con el APK re-firmado. Deja un log en Android/data/<paquete>/files/sigspoof.log.",
    default = true
) {
    compatibleWith(KAWAII_ANIMES)
    extendWith("extensions/extension.mpe")

    execute {
        // 1) Instalacion temprana: override attachBaseContext (no existe en KawaiiApp).
        val kawaii = mutableClassDefBy(KAWAII_APP_CLASS)
            ?: throw PatchException("$PATCH_NAME: no se encontro $KAWAII_APP_CLASS")

        val alreadyAttached = kawaii.methods.any { it.name == "attachBaseContext" }
        if (!alreadyAttached) {
            val superRef: MethodReference = ImmutableMethodReference(
                "Landroid/app/Application;",
                "attachBaseContext",
                listOf("Landroid/content/Context;"),
                "V",
            )
            val installRef: MethodReference = ImmutableMethodReference(
                SIGSPOOF_CLASS,
                "install",
                listOf("Landroid/content/Context;"),
                "V",
            )
            val instructions = listOf<ImmutableInstruction>(
                // invoke-super {v0, v1}, Application;->attachBaseContext(Context)V
                ImmutableInstruction35c(
                    Opcode.INVOKE_SUPER, 2, 0, 1, 0, 0, 0, superRef
                ),
                // invoke-static {v0}, SigSpoof;->install(Context)V
                ImmutableInstruction35c(
                    Opcode.INVOKE_STATIC, 1, 0, 0, 0, 0, 0, installRef
                ),
                ImmutableInstruction10x(Opcode.RETURN_VOID),
            )
            val attach = ImmutableMethod(
                KAWAII_APP_CLASS,
                "attachBaseContext",
                listOf(ImmutableMethodParameter("Landroid/content/Context;", null, null)),
                "V",
                AccessFlags.PROTECTED.value,
                null,
                ImmutableMethodImplementation(2, instructions, null, null),
            )
            kawaii.methods.add(MutableMethod(attach))
        }

        // 2) Redundancia: tambien al inicio de onCreate.
        val method = resolveMethodWithFallback(PATCH_NAME, KawaiiAppOnCreateFingerprint)
        method.addInstructions(
            0,
            """
            invoke-static {p0}, $SIGSPOOF_CLASS;->install(Landroid/content/Context;)V
            """.trimIndent()
        )
    }
}
