package io.github.betterclient.agent.transformer

import io.github.betterclient.agent.transformer.impl.AbstractTransformer
import io.github.betterclient.agent.transformer.impl.AssistinTransformer
import java.lang.instrument.Instrumentation
import java.util.function.Consumer
import kotlin.reflect.KClass

class AssistinManager {
    var assistins: MutableList<Class<*>> = object : ArrayList<Class<*>>() {
        override fun add(element: Class<*>): Boolean {
            if (!element.isAnnotationPresent(Assistin::class.java)) return false
            transformers.add(
                AssistinTransformer(
                    element
                )
            )
            return super.add(element)
        }
    }
    var transformers: MutableList<AbstractTransformer> = ArrayList()

    init {
        instance = this
    }

    fun setupInjections(inst: Instrumentation) {
        transformers.forEach(Consumer { classFileTransformer: AbstractTransformer? ->
            inst.addTransformer(
                classFileTransformer
            )
        })
    }

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Assistin(vararg val value: KClass<*> = [], val clazzName: Array<String> = [])

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
    annotation class Inject(val method: String, val type: InjectType, val line: Int = 0)

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    annotation class Instance

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    annotation class ClassObject(val value: String)
    enum class InjectType {
        HEAD, RETURN, OVERRIDE, ATLINE
    }

    companion object {
        private var instance: AssistinManager? = null
        fun getInstance(): AssistinManager? {
            if (instance == null) AssistinManager()
            return instance
        }
    }
}