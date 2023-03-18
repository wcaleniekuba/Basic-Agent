# BasicAgent

Basic java/kotlin agent that can inject into minecraft code

Best works with fabric.

Use the `build` task to build.

Don't run MinecraftClient/MinecraftServer task.

# Usage

> I have not found a way to do this in the minecraft launcher but in multimc you can add the .jar file from the releases folder as a jarmod (add to minecraft jar).

Run Minecraft fabric with the java arguments `-javaagent:basicagent.jar`

Replace basicagent.jar with the full directory of the built jar file.