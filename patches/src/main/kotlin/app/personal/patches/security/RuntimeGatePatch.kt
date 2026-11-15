package app.personal.patches.security

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.patch
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.personal.patches.Constants.KAWAII_ANIMES
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction

private const val PATCH_NAME = "Compatibilidad con integridad nativa (V29)"

/**
 * El <clinit> de la clase ofuscada del gate (q25 en 2.1.3 v29) es el unico lugar
 * que llama a confirmRuntimeV29. La estructura completa del chequeo queda
 * validada por la cadena de filtros (invoke -> move-result -> const -> cmp -> if):
 * si otro build cambia el layout, el fingerprint falla con error claro en vez
 * de corromper el metodo.
 *
 * En 2.1.3 v29 la secuencia es:
 *   const-string "protection"; invoke-static loadLibrary        <- dentro del try
 *   sget-object INSTANCE
 *   const-wide v1, 0x4a72c19e63dL (constante del chequeo)
 *   invoke-virtual confirmRuntimeV29(J)J
 *   move-result-wide v0
 *   const-wide v2, 0x2467794c18fL (valor esperado)
 *   cmp-long v0, v0, v2
 *   if-nez v0, :cond_0
 */
object RuntimeGateClinitFingerprint : Fingerprint(
    definingClass = "Lq25;",
    name = "<clinit>",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/darkdeveloper/kawaiianimes/utils/NativeProtectionBridgeV29;",
            name = "confirmRuntimeV29"
        ),
        opcode(Opcode.MOVE_RESULT_WIDE, location = InstructionLocation.MatchAfterImmediately()),
        opcode(Opcode.CONST_WIDE, location = InstructionLocation.MatchAfterImmediately()),
        opcode(Opcode.CMP_LONG, location = InstructionLocation.MatchAfterImmediately()),
        opcode(Opcode.IF_NEZ, location = InstructionLocation.MatchAfterImmediately()),
    )
)

/**
 * Variante relajada: sin definingClass (ofuscado, varia entre builds). La
 * cadena de filtros identifica de forma unica al <clinit> del gate porque
 * confirmRuntimeV29 solo se invoca ahi (verificado en 2.1.3 v29).
 */
object RuntimeGateClinitRelaxedFingerprint : Fingerprint(
    filters = listOf(
        methodCall(
            definingClass = "Lcom/darkdeveloper/kawaiianimes/utils/NativeProtectionBridgeV29;",
            name = "confirmRuntimeV29"
        ),
        opcode(Opcode.MOVE_RESULT_WIDE, location = InstructionLocation.MatchAfterImmediately()),
        opcode(Opcode.CONST_WIDE, location = InstructionLocation.MatchAfterImmediately()),
        opcode(Opcode.CMP_LONG, location = InstructionLocation.MatchAfterImmediately()),
        opcode(Opcode.IF_NEZ, location = InstructionLocation.MatchAfterImmediately()),
    )
)

/**
 * La app verifica su propia firma en codigo NATIVO (libprotection.so):
 * NativeProtectionBridgeV29.confirmRuntimeV29(J)J es un metodo JNI registrado
 * por la lib, y el <clinit> de la clase ofuscada del gate compara su resultado
 * con una constante. Con el APK re-firmado por Morphe el chequeo devuelve otro
 * valor, el flag queda en false y la app usa rutas degradadas (peticiones al
 * catalogo sin proof nativo) que el servidor rechaza: el catalogo se queda
 * cargando.
 *
 * Este patch NEUTRALIZA el chequeo sin alterar la estructura del metodo:
 * reemplaza el resultado de confirmRuntimeV29 por el valor esperado (leido del
 * propio <clinit>), de modo que la comparacion siempre da valida y el flag
 * queda en true. La carga de la lib nativa y el try-catch originales quedan
 * intactos (mismo numero de instrucciones y mismos tamanos -> los rangos del
 * try no se mueven), asi que si la lib no esta disponible el catchall original
 * deja el flag en false y la app se degrada sin cerrarse.
 *
 * Opcional (default = false): activarlo si el catalogo no carga con los demas
 * patches. Requiere que libprotection.so siga en el APK (no eliminar libs).
 */
@Suppress("unused")
val runtimeGatePatch = patch(
    name = PATCH_NAME,
    description = "La app firma sus peticiones al catalogo con libprotection.so, que verifica la firma del APK. Al re-firmar con Morphe ese chequeo falla y el gate queda en false: la app degrada sus rutas y el catalogo puede quedarse cargando. Este patch neutraliza el chequeo (la comparacion siempre da valida) conservando la carga de la lib nativa y el manejo de errores original. Requiere no eliminar las librerias nativas del APK.",
    default = false
) {
    compatibleWith(KAWAII_ANIMES)

    execute {
        val fingerprint = listOf(RuntimeGateClinitFingerprint, RuntimeGateClinitRelaxedFingerprint)
            .firstOrNull { it.matchOrNull() != null }
            ?: throw PatchException(
                "$PATCH_NAME: el <clinit> del gate no matcheo en este APK (se busca la llamada a " +
                    "confirmRuntimeV29 seguida de move-result-wide, const-wide, cmp-long e if-nez). " +
                    "Re-analizar con el workflow Analyze APK."
            )

        val matches = fingerprint.instructionMatches
        val invokeIndex = matches[0].index
        val resultRegister = (matches[1].instruction as OneRegisterInstruction).registerA
        val expectedValue = (matches[2].instruction as WideLiteralInstruction).wideLiteral
        val literal = "0x" + java.lang.Long.toHexString(expectedValue) + "L"

        val method = fingerprint.methodOrNull
            ?: throw PatchException("$PATCH_NAME: match sin metodo")

        // El invoke se sustituye por la constante esperada escrita directamente en
        // el registro del resultado; el move-result sobrante se vuelve nop. Mismo
        // numero de instrucciones y mismos tamanos: el try-catch original queda
        // valido y loadLibrary sigue corriendo dentro de el.
        method.replaceInstruction(invokeIndex, "const-wide v$resultRegister, $literal")
        method.replaceInstruction(matches[1].index, "nop")
    }
}
