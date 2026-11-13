package app.personal.patches

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod

/**
 * Reemplaza el cuerpo completo de un método por las instrucciones smali dadas.
 */
internal fun replaceMethodBody(method: MutableMethod, smaliInstructions: String) {
    val count = method.implementation?.instructions?.size
        ?: throw PatchException("El método no tiene implementación mutable: ${method.name}")
    method.removeInstructions(count)
    method.addInstructions(0, smaliInstructions)
}
