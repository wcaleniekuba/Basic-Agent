package io.github.betterclient.agent.transformer

import java.lang.StringBuilder
import java.lang.reflect.Method

class AssistinHelper {
    fun getString2(isStatic: String, returnType: String, assistinClass: Class<*>, instanceFieldName: String, m: Method, methodArgs: StringBuilder, methodName: String): String {
        return String.format(
            """
			public %s %s %s (%s) { 						
                try {
			  	    java.lang.Class clazz = java.lang.Class.forName("%s");
			  		java.lang.reflect.Field instance = clazz.getField("%s");
			  		java.lang.reflect.Method method = null;
			 
			 		java.lang.reflect.Method[] methods = clazz.getMethods();
			 		for(int i = 0; i < methods.length; i++) {
			 			if(methods[i].getName().equals("%s")) {
			  				method = methods[i];
			  				break;
			            } 							
                    }
			 
			  		Object clazzInstance = instance.get(null);
			 
			  		%s (%s method.invoke(clazzInstance, ${'$'}args));
			  	} catch (Exception e) {
			  		e.printStackTrace();
			 	}

                %s;
			}
			""".trimIndent(),
            isStatic,
            returnType,
            methodName,
            methodArgs,
            assistinClass.name,
            instanceFieldName,
            m.name,
            if (returnType == "void") "" else "return",
            if (returnType == "void") "" else "($returnType)",
            if (returnType == "void") "return" else "return null"
        )
    }
}