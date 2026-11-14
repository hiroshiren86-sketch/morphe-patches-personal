package app.personal.patches.security

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.patch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.personal.patches.Constants.KAWAII_ANIMES
import app.personal.patches.resolveMethodWithFallback
import app.morphe.patcher.methodCall
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

/**
 * El <clinit> de la clase ofuscada (q25 en el build 2.1.3 v29) es el unico
 * lugar que llama a confirmRuntimeV29. Es la unica pista estable: el nombre
 * de la clase cambia entre builds porque esta ofuscada.
 */
object RuntimeGateClinitFingerprint : Fingerprint(
    definingClass = "Lq25;",
    name = "<clinit>",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/darkdeveloper/kawaiianimes/utils/NativeProtectionBridgeV29;",
            name = "confirmRuntimeV29"
        )
    )
)

/**
 * Variante relajada: sin definirClass (ofuscado, varia entre builds). El
 * filtro methodCall identifica de forma unica al <clinit> del gate porque
 * confirmRuntimeV29 solo se invoca ahi (verificado en 2.1.3 v29).
 */
object RuntimeGateClinitRelaxedFingerprint : Fingerprint(
    filters = listOf(
        methodCall(
            definingClass = "Lcom/darkdeveloper/kawaiianimes/utils/NativeProtectionBridgeV29;",
            name = "confirmRuntimeV29"
        )
    )
)

/**
 * Descubre el nombre de la lib nativa que carga el <clinit>: es el const-string
 * previo al invoke de System.loadLibrary (en 2.1.3 v29: "protection").
 */
private fun nativeLibraryName(method: MutableMethod): String {
    val instructions = method.implementation?.instructions?.toList()
        ?: throw PatchException("El gate (${method.definingClass}) no tiene implementacion")
    instructions.forEachIndexed { index, insn ->
        if (insn is ReferenceInstruction) {
            val ref = insn.reference as? MethodReference ?: return@forEachIndexed
            if (ref.definingClass == "Ljava/lang/System;" && ref.name == "loadLibrary") {
                val prev = instructions.getOrNull(index - 1) as? ReferenceInstruction
                val libName = (prev?.reference as? StringReference)?.string
                    ?: throw PatchException("No se pudo deducir el nombre de la lib nativa del gate")
                return libName
            }
        }
    }
    throw PatchException("El gate no llama a System.loadLibrary; el layout de este build cambio. Re-analizar con el workflow Analyze APK.")
}

/**
 * Descubre el campo estatico booleano que el <clinit> escribe con el resultado
 * del chequeo (en 2.1.3 v29: Lq25;->a:Z).
 */
private fun gateFlagField(method: MutableMethod): FieldReference {
    val instructions = method.implementation?.instructions?.toList()
        ?: throw PatchException("El gate (${method.definingClass}) no tiene implementacion")
    val fields = instructions.asSequence()
        .filterIsInstance<ReferenceInstruction>()
        .filter { it.opcode.name.startsWith("SPUT") }
        .mapNotNull { it.reference as? FieldReference }
        .filter { it.type == "Z" && it.definingClass == method.definingClass }
        .toList()
    return fields.lastOrNull()
        ?: throw PatchException("No se encontro el campo estatico booleano del gate en ${method.definingClass}")
}

/**
 * La app verifica su propia firma en codigo NATIVO (libprotection.so):
 * NativeProtectionBridgeV29.confirmRuntimeV29(J)J es un metodo JNI registrado
 * por la lib, y el <clinit> de la clase ofuscada del gate guarda su resultado
 * en un campo estatico booleano. Con el APK re-firmado por Morphe el chequeo
 * devuelve otro valor, el flag queda en false y la app usa rutas degradadas
 * (peticiones al catalogo sin proof nativo) que el servidor puede rechazar.
 *
 * Este patch NO reemplaza el cuerpo (el <clinit> tiene un bloque catchall y
 * borrar instrucciones dejaria el rango try corrupto: VerifyError al arrancar).
 * En su lugar inserta un retorno temprano que carga la lib nativa (necesaria
 * para approveSourceV29, catalogSyncProofV29, etc.) y fuerza el flag a true,
 * dejando el chequeo original sin ejecutar.
 *
 * Opcional (default = false): activarlo solo si la app abre pero el catalogo
 * no carga, o si sigue cerrandose con los demas patches.
 */
@Suppress("unused")
val runtimeGatePatch = patch(
    name = "Compatibilidad con integridad nativa (V29)",
    description = "La app firma sus peticiones al catalogo con libprotection.so, que verifica la firma del APK. Al re-firmar con Morphe ese gate queda en false. Este patch carga la lib nativa y fuerza el gate a true sin ejecutar el chequeo, para que el app use las rutas de proteccion normales. Pruebalo solo si la app se cierra al arrancar o el catalogo no carga con los demas patches.",
    default = false
) {
    compatibleWith(KAWAII_ANIMES)

    execute {
        val method = resolveMethodWithFallback(
            "Compatibilidad con integridad nativa (V29)",
            RuntimeGateClinitFingerprint,
            RuntimeGateClinitRelaxedFingerprint,
        )

        val libName = nativeLibraryName(method)
        val flag = gateFlagField(method)

        // Retorno temprano: el resto del <clinit> original (chequeo nativo +
        // comparacion) queda inalcanzado pero intacto, con su try-catch valido.
        method.addInstructions(0, """
            const-string v0, "$libName"
            invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
            const/4 v0, 0x1
            sput-boolean v0, ${flag.definingClass}->${flag.name}:Z
            return-void
        """)
    }
}
