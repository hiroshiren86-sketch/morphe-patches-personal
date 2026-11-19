package app.personal.patches.security

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.mutableClassDefBy
import app.morphe.patcher.patch.patch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.personal.patches.Constants.KAWAII_ANIMES
import app.personal.patches.resolveMethodWithFallback
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.HiddenApiRestriction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction10x
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction11x
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction21t
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction35c
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
 * La proteccion nativa (libprotection.so) lee la firma del APK por DOS rutas
 * (verificado con disassembly en docs/PLANO-MORPHE.md §8a):
 *
 * 1. PackageManager (via JNI: ActivityThread.currentApplication() ->
 *    getPackageManager() -> getPackageInfo -> signatures/signingInfo).
 * 2. EL ARCHIVO: Context.getPackageCodePath() -> java.util.zip.ZipFile(path)
 *    -> escaneo de entradas (incluidas *.apk) + SHA-256 con MessageDigest.
 *
 * Con el APK re-firmado por Morphe ambas rutas ven bytes nuevos y los proofs
 * salen invalidos: el servidor rechaza /api/sync y el catalogo queda vacio
 * (verificado en dispositivo dev.5/6/7). LSPatch lo resuelve en su nivel 3
 * redirigiendo las lecturas al APK original embebido (assets/lspatch/
 * origin.apk extraido a cache + hook openat).
 *
 * Este patch replica ese comportamiento sin codigo nativo:
 * - Proxy del IPackageManager (campo mPM) que devuelve firma ORIGINAL para el
 *   paquete propio, incluye hasSigningCertificate (true si el cert pedido es
 *   el original) y redirige applicationInfo.sourceDir (SigSpoof).
 * - El CI inyecta el APK ORIGINAL como asset orig/app.apk ANTES del parcheo;
 *   SigSpoof lo extrae a cacheDir/orig/app.apk en el arranque.
 * - Overrides de getPackageCodePath/getPackageResourcePath en KawaiiApp que
 *   devuelven la ruta del APK original extraido (dispatch virtual: la lib
 *   nativa obtiene el Context via ActivityThread.currentApplication(), que es
 *   ESTA instancia, y los metodos framework de ContextWrapper quedan
 *   redirigidos para TODO lector de ese path, nativo o Java).
 *
 * La instalacion ocurre en Application.attachBaseContext (antes de
 * ContentProviders y de onCreate). La firma original va embebida en la
 * extension (SigSpoof). SigSpoof deja un log en Android/data/<paquete>/
 * files/sigspoof.log para diagnosticar sin ADB.
 *
 * Requiere la extension extension.mpe (extendWith) y el asset orig/app.apk.
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
                emptySet<HiddenApiRestriction>(),
                ImmutableMethodImplementation(2, instructions, null, null),
            )
            kawaii.methods.add(MutableMethod(attach))
        }

        // 1b) Overrides de las rutas del APK: la proteccion nativa llama
        // Context.getPackageCodePath() (y el escaneo ZipFile abre ese archivo);
        // con dispatch virtual desde la instancia KawaiiApp el path apunta al
        // APK ORIGINAL extraido por SigSpoof (asset orig/app.apk del CI).
        val pathMethodNames = listOf("getPackageCodePath", "getPackageResourcePath")
        for (pathName in pathMethodNames) {
            val alreadyDefined = kawaii.methods.any { it.name == pathName }
            if (alreadyDefined) continue

            val redirectRef: MethodReference = ImmutableMethodReference(
                SIGSPOOF_CLASS,
                "redirectedCodePath",
                listOf("Landroid/content/Context;"),
                "Ljava/lang/String;",
            )
            val superRef: MethodReference = ImmutableMethodReference(
                "Landroid/content/ContextWrapper;",
                pathName,
                emptyList(),
                "Ljava/lang/String;",
            )
            // 0: invoke-static {p0}, SigSpoof;->redirectedCodePath(Context)   (3u)
            // 1: move-result-object v0                                        (1u)
            // 2: if-eqz v0, +2   -> salta a 4 si no hay redireccion           (1u)
            // 3: return-object v0                                             (1u)
            // 4: invoke-super {p0}, ContextWrapper;-><pathName>()             (3u)
            // 5: move-result-object v0                                        (1u)
            // 6: return-object v0                                             (1u)
            val instructions = listOf<ImmutableInstruction>(
                ImmutableInstruction35c(Opcode.INVOKE_STATIC, 1, 0, 0, 0, 0, 0, redirectRef),
                ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0),
                ImmutableInstruction21t(Opcode.IF_EQZ, 0, 2),
                ImmutableInstruction11x(Opcode.RETURN_OBJECT, 0),
                ImmutableInstruction35c(Opcode.INVOKE_SUPER, 1, 0, 0, 0, 0, 0, superRef),
                ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0),
                ImmutableInstruction11x(Opcode.RETURN_OBJECT, 0),
            )
            val override = ImmutableMethod(
                KAWAII_APP_CLASS,
                pathName,
                emptyList(),
                "Ljava/lang/String;",
                AccessFlags.PUBLIC.value,
                null,
                emptySet<HiddenApiRestriction>(),
                ImmutableMethodImplementation(2, instructions, null, null),
            )
            kawaii.methods.add(MutableMethod(override))
        }

        // 2) Redundancia: tambien al inicio de onCreate.
        val method = resolveMethodWithFallback(PATCH_NAME, KawaiiAppOnCreateFingerprint)
        method.addInstructions(
            0,
            """
            invoke-static {p0}, $SIGSPOOF_CLASS->install(Landroid/content/Context;)V
            """.trimIndent()
        )
    }
}
