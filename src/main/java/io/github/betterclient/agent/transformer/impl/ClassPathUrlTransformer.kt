package io.github.betterclient.agent.transformer.impl

import javassist.ClassPool
import java.io.ByteArrayInputStream

class ClassPathUrlTransformer : AbstractTransformer() {
    @Throws(Exception::class)
    override fun transform(name: String?, unTransformedClass: ByteArray?): ByteArray {
        if (name != "net/minecraft/client/main/Main" || injected) return unTransformedClass!!

        val pool = ClassPool.getDefault()
        val cc = pool.makeClass(ByteArrayInputStream(unTransformedClass))

        val main = cc.getDeclaredMethod("main")
        main.insertBefore(injectorSrc)
        injected = true

        return cc.toBytecode()
    }

    companion object {
        var injected = false
        var injectorSrc = String.format(
            """
                   try {
                       java.lang.Thread thread = java.lang.Thread.currentThread();
                       java.net.URLClassLoader loader = (java.net.URLClassLoader) thread.getContextClassLoader().getParent(); //getparent because of fabric.
                       Class loaderClz = loader.getClass();
                       
                       java.lang.reflect.Method[] methods = loaderClz.getMethods();
					   for(int i = 0; i < methods.length; i++) {
					        if(methods[i].getName().equals("addURL")) {
					            Object[] array = {new java.io.File("%s").toURI().toURL()};
					            methods[i].setAccessible(true);
								methods[i].invoke((Object) loader, array);
								break;
							}
					   }
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
            
            """.trimIndent(), ClassPathUrlTransformer::class.java.protectionDomain.codeSource.location.path
        )
    }
}