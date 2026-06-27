package name.modid.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;
import name.modid.Arrows;

public class DusaruysClientClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyMapping keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.arrows.toggle",
                GLFW.GLFW_KEY_Z,
                "category.arrows"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBinding.consumeClick()) {
                Arrows.toggle();
            }
        });

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (Arrows.isEnabled()) {
                Arrows.render((GuiGraphics) context);
            }
        });
    }
}
