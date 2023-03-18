package io.github.betterclient.agent.assistins;

import io.github.betterclient.agent.transformer.AssistinManager.*;

@Assistin(clazzName = "net/minecraft/class_310") //MinecraftClient.java
public class MinecraftClientAssistin {
    @Instance
    public static MinecraftClientAssistin assistin = new MinecraftClientAssistin();

    @Inject(method = "<init>", type = InjectType.RETURN)
    public void gameStartCallBack(@ClassObject("net/minecraft/class_542") Object runArgs) {
        System.err.println("Game Start Hook!");
    }
}
