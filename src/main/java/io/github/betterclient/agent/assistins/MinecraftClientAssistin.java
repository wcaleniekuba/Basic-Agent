package io.github.betterclient.agent.assistins;

import io.github.betterclient.agent.transformer.AssistinManager.*;
import net.minecraft.client.RunArgs;

@Assistin(clazzName = "net/minecraft/class_310") //MinecraftClient.java
public class MinecraftClientAssistin {
    @Instance
    public static MinecraftClientAssistin assistin = new MinecraftClientAssistin();

    @Inject(method = "<init>", type = InjectType.RETURN)
    public void gameStartCallBack(@ClassObject("net/minecraft/class_542") Object runArgs) {
        //You cannot use parameter types on the class or the parameters but you can do it in the code
        RunArgs args = (RunArgs) runArgs;
        System.err.println("Game Start Hook! " + args.game.version);
    }
}
