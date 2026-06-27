package name.modid;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class Arrows {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static boolean enabled = false;
    private static final Identifier ARROW_TEXTURE = Identifier.of("dusaruysclient", "textures/arrows.png");
    private static final float RADIUS = 60f; // расстояние от центра экрана

    public static void toggle() {
        enabled = !enabled;
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(
                    enabled ? "§aArrows ON" : "§cArrows OFF"
            ), true);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void render(DrawContext context) {
        if (!enabled || mc.player == null || mc.world == null) return;

        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || player.isDead() || !player.isAlive()) continue;

            double dx = player.getX() - mc.player.getX();
            double dz = player.getZ() - mc.player.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 1.5 || dist > 50) continue;

            float yaw = mc.player.getYaw();
            double cos = MathHelper.cos((float) (yaw * (Math.PI * 2 / 360)));
            double sin = MathHelper.sin((float) (yaw * (Math.PI * 2 / 360)));
            double rotY = -(dz * cos - dx * sin);
            double rotX = -(dx * cos + dz * sin);

            float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI);

            float arrowX = RADIUS * MathHelper.cos((float) Math.toRadians(angle)) + screenWidth / 2f;
            float arrowY = RADIUS * MathHelper.sin((float) Math.toRadians(angle)) + screenHeight / 2f;

            int size = 24;
            int half = size / 2;

            var matrices = context.getMatrices();
            matrices.push();
            matrices.translate(arrowX, arrowY, 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));

            // Рисуем PNG-текстуру
            context.drawTexture(ARROW_TEXTURE, -half, -half, 0, 0, size, size, size, size);

            matrices.pop();

            // Дистанция под стрелкой
            if (dist > 0) {
                String distText = String.format("%.1f", dist);
                int textX = (int)(arrowX - mc.textRenderer.getWidth(distText) / 2f);
                int textY = (int)(arrowY + half + 4);
                context.drawText(mc.textRenderer, distText, textX, textY, 0xCCFFFFFF, false);
            }
        }
    }
}
