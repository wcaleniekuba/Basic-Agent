package io.github.betterclient.agent

import io.github.betterclient.agent.assistins.MinecraftClientAssistin
import io.github.betterclient.agent.transformer.AssistinManager
import io.github.betterclient.agent.transformer.impl.ClassPathUrlTransformer
import java.io.File
import java.lang.instrument.Instrumentation
import java.util.jar.JarFile

object AgentMain {
    @JvmStatic
    fun premain(args: String?, inst: Instrumentation) {
        println("Launching with $inst")

        inst.appendToSystemClassLoaderSearch(JarFile(File(
            AgentMain.javaClass.protectionDomain.codeSource.location.path
        )))

        AssistinManager.getInstance()?.assistins?.add(MinecraftClientAssistin::class.java)

        AssistinManager.getInstance()?.transformers?.add(ClassPathUrlTransformer())

        AssistinManager.getInstance()?.setupInjections(inst)
    }
}