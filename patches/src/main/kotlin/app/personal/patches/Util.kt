package app.personal.patches

import app.morphe.patcher.Fingerprint
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

/**
 * Resuelve el método del primer fingerprint de la cascada que matchee en el APK.
 *
 * Los builds distintos de una misma versión de la app pueden variar la ofuscación
 * (por ejemplo la clase Continuation de las funciones suspend) o mover/renombrar
 * clases auxiliares, así que cada patch declara varios fingerprints en orden de
 * preferencia: primero el exacto, luego variantes relajadas.
 *
 * @param patchName nombre del patch, para el mensaje de error.
 * @param fingerprints cascada en orden de preferencia.
 * @return el método mutable del primer fingerprint que matchee.
 * @throws PatchException si ninguno matchea.
 */
internal fun resolveMethodWithFallback(patchName: String, vararg fingerprints: Fingerprint): MutableMethod {
    fingerprints.forEach { fingerprint ->
        fingerprint.methodOrNull?.let { return it }
    }
    throw PatchException(
        "$patchName: ningún fingerprint matcheó en este APK. Intentados: " +
            fingerprints.joinToString("; ") { it.javaClass.simpleName } +
            ". Si el APK es un instalador LSPatch o un wrapper (contiene assets/lspatch/), " +
            "parchea el APK original de la app, no el wrapper."
    )
}
