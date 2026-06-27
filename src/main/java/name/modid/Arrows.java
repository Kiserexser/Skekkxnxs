package name.modid;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class Arrows {
    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean enabled = false;
    private static final ResourceLocation ARROW_TEXTURE = new ResourceLocation("dusaruysclient", "textures/arrows.png");
    private static final float RADIUS = 60f;

    public static void toggle() {
        enabled = !enabled;
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(
                    enabled ? "§aArrows ON" : "§cArrows OFF"
            ), true);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void render(GuiGraphics context) {
        if (!enabled || mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        for (AbstractClientPlayer player : mc.level.players()) {
            if (player == mc.player || player.isDeadOrDying() || !player.isAlive()) continue;

            double dx = player.getX() - mc.player.getX();
            double dz = player.getZ() - mc.player.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 1.5 || dist > 50) continue;

            float yaw = mc.player.getYRot();
            double cos = Mth.cos((float) (yaw * (Math.PI * 2 / 360)));
            double sin = Mth.sin((float) (yaw * (Math.PI * 2 / 360)));
            double rotY = -(dz * cos - dx * sin);
            double rotX = -(dx * cos + dz * sin);

            float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI);

            float arrowX = RADIUS * Mth.cos((float) Math.toRadians(angle)) + screenWidth / 2f;
            float arrowY = RADIUS * Mth.sin((float) Math.toRadians(angle)) + screenHeight / 2f;

            int size = 24;
            int half = size / 2;

            var poseStack = context.pose();
            poseStack.pushPose();
            poseStack.translate(arrowX, arrowY, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(angle));

            context.blit(ARROW_TEXTURE, -half, -half, 0, 0, size, size, size, size);

            poseStack.popPose();

            if (dist > 0) {
                String distText = String.format("%.1f", dist);
                int textX = (int)(arrowX - mc.font.width(distText) / 2f);
                int textY = (int)(arrowY + half + 4);
                context.drawString(mc.font, distText, textX, textY, 0xCCFFFFFF, false);
            }
        }
    }
}
