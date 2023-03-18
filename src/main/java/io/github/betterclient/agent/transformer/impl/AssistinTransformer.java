package io.github.betterclient.agent.transformer.impl;

import java.io.ByteArrayInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.lang.reflect.Modifier;
import java.util.*;

import io.github.betterclient.agent.transformer.AssistinHelper;
import io.github.betterclient.agent.transformer.AssistinManager.*;
import javassist.*;
import org.jetbrains.annotations.NotNull;

/**
 * This class is used to load assistin files
 */
public class AssistinTransformer extends AbstractTransformer {

	public final Class<?> assistinClass;

	public String transformName;

	public List<Method> injectInto = new ArrayList<>();
	public String instanceFieldName;

	public AssistinTransformer(Class<?> assistinClass) {
		this.assistinClass = assistinClass;
		Assistin assist = this.assistinClass.getAnnotation(Assistin.class);
		List<String> clazzNames = Arrays.asList(assist.clazzName());
		List<Class<?>> classes = Arrays.asList(assist.value());

		if (clazzNames.isEmpty()) {
			transformName = classes.get(0).getName();
		} else {
			transformName = clazzNames.get(0);
		}
		
		Arrays.stream(this.assistinClass.getMethods()).filter(m -> m.isAnnotationPresent(Inject.class)).forEach(injectInto::add);

		boolean anyMatch =
				Arrays.stream(this.assistinClass.getFields())
						.filter(f -> Modifier.isStatic(f.getModifiers()))
						.filter(f -> f.getType().equals(assistinClass))
						.filter(f -> f.isAnnotationPresent(Instance.class))
						.noneMatch(f -> {
							instanceFieldName = f.getName();
							return true;
						});

		if(anyMatch) {
			throw new RuntimeException(new NullPointerException("No instance field on assistin class"));
		}
	}

	int count = 0;

	@Override
	public byte @NotNull [] transform(String name, byte[] unTransformedClass) throws Exception {
		assert name != null;
		if (!name.equals(transformName))
			return unTransformedClass;

		count++;

		ClassPool pool = ClassPool.getDefault();
		CtClass cc = pool.makeClass(new ByteArrayInputStream(unTransformedClass));

		for (Method m : injectInto) {
			Inject annotation = m.getAnnotation(Inject.class);

			String injectingMethodName = annotation.method();
			String injectingMethodDesc = getSignature(m);
			InjectType type = annotation.type();
			CtBehavior injectingMethod;
			if(injectingMethodName.equals("<init>")) {
				injectingMethod = cc.getConstructor(injectingMethodDesc);
			} else {
				injectingMethod = cc.getMethod(injectingMethodName, injectingMethodDesc);
			}

			String isStatic = Modifier.isStatic(injectingMethod.getModifiers()) ? "static" : "";
			String methodName = "handler$zz" + (System.currentTimeMillis()) + "$" + m.getName();
			StringBuilder methodArgs = new StringBuilder();
			int index = 0;

			for(CtClass param : injectingMethod.getParameterTypes()) {
				if (index > 0) {
					methodArgs.append(", ");
				}
				methodArgs.append(param.getName()).append(" arg").append(index);
				index++;
			}

			String inject = String.format("%s($$);", methodName);
			String returnType = injectingMethod instanceof CtMethod ? ((CtMethod) injectingMethod).getReturnType().getName() : "void";

			String inject2 = new AssistinHelper().getString2(
					isStatic,
					returnType,
					assistinClass,
					instanceFieldName,
					m,
					methodArgs,
					methodName
			);

			CtMethod methodInjector = CtMethod.make(inject2, cc);
			cc.addMethod(methodInjector);

			if (type == InjectType.OVERRIDE) {
				if(injectingMethod instanceof CtMethod && !((CtMethod) injectingMethod).getReturnType().equals(CtClass.voidType)) {
					inject = "return (" + returnType + ") " + inject;
				}

				injectingMethod.setBody(inject);
			}

			if (type == InjectType.HEAD) {
				injectingMethod.insertBefore(inject);
			}

			if (type == InjectType.RETURN) {
				injectingMethod.insertAfter(inject);
			}

			if (type == InjectType.ATLINE) {
				injectingMethod.insertAt(annotation.line(), inject);
			}
		}

		return cc.toBytecode();
	}

	String getSignature(Method method){
		StringBuilder result = new StringBuilder();
		result.append('(');
		Class<?>[] parameterTypes = method.getParameterTypes();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		int counter = 0;
		for (Class<?> parameterType : parameterTypes) {
			if(parameterAnnotations[counter].length == 0)
				result.append(getPSignature(parameterType));
			else {
				result.append("L").append(((ClassObject) parameterAnnotations[counter][0]).value()).append(";");
			}

			counter++;
		}
		result.append(')');
		result.append(getPSignature(method.getReturnType()));
		return result.toString();
	}

	public String getPSignature(Class<?> clazz) {
		Map<Class<?>, String> PRIMITIVE_TO_SIGNATURE;
		PRIMITIVE_TO_SIGNATURE = new HashMap<>(9);
		PRIMITIVE_TO_SIGNATURE.put(byte.class, "B");
		PRIMITIVE_TO_SIGNATURE.put(char.class, "C");
		PRIMITIVE_TO_SIGNATURE.put(short.class, "S");
		PRIMITIVE_TO_SIGNATURE.put(int.class, "I");
		PRIMITIVE_TO_SIGNATURE.put(long.class, "J");
		PRIMITIVE_TO_SIGNATURE.put(float.class, "F");
		PRIMITIVE_TO_SIGNATURE.put(double.class, "D");
		PRIMITIVE_TO_SIGNATURE.put(void.class, "V");
		PRIMITIVE_TO_SIGNATURE.put(boolean.class, "Z");

		String primitiveSignature = PRIMITIVE_TO_SIGNATURE.get(clazz);
		if (primitiveSignature != null) {
			return primitiveSignature;
		} else if (clazz.isArray()) {
			return "[" + getSignature(clazz.getComponentType().getEnclosingMethod());
		} else {
			return "L" + clazz.getName().replace('.', '/') + ";";
		}
	}
}