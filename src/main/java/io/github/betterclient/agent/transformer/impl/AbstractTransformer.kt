package io.github.betterclient.agent.transformer.impl

import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain

abstract class AbstractTransformer : ClassFileTransformer {
    override fun transform(
        loader: ClassLoader,
        className: String,
        classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain,
        classfileBuffer: ByteArray
    ): ByteArray {
        this.loader = loader
        return try {
            this.transform(className, classfileBuffer)
        } catch (e: Exception) {
            e.printStackTrace()
            classfileBuffer
        }
    }

    @Throws(Exception::class)
    abstract fun transform(name: String?, unTransformedClass: ByteArray?): ByteArray
    var loader: ClassLoader? = null
}