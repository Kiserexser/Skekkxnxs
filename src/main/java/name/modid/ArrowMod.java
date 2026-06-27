package name.modid;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrowMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("arrowmod");
    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean enabled = false;
    private static boolean lastKeyState = false;
    private static final ResourceLocation ARROW_TEXTURE = new ResourceLocation("dusaruysclient", "textures/arrows.png");
    private static final float RADIUS = 60f;

    // === ВСТРОЕННЫЙ ЭКРАН (ПРОЗРАЧНЫЙ, НЕ БЛОКИРУЕТ УПРАВЛЕНИЕ) ===
    private static final Screen overlayScreen = new Screen(Component.literal("ArrowOverlay")) {
        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
            renderArrows(context);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_Z || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                toggle();
                return true;
            }
            return false; // все остальные клавиши идут в игру
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false; // клики идут в игру
        }

        @Override
        public boolean shouldPauseGame() {
            return false;
        }

        @Override
        public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
            // прозрачный фон – не затемняем игру
        }
    };

    // === ПЕРЕКЛЮЧЕНИЕ СТРЕЛОК ===
    public static void toggle() {
        enabled = !enabled;
        if (enabled) {
            mc.setScreen(overlayScreen);
        } else {
            if (mc.screen == overlayScreen) {
                mc.setScreen(null);
            }
        }
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(
                    enabled ? "§aArrows ON" : "§cArrows OFF"
            ), true);
        }
        LOGGER.info("Arrows: " + (enabled ? "ON" : "OFF"));
    }

    // === ОТРИСОВКА СТРЕЛОК ===
    private static void renderArrows(GuiGraphics context) {
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
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));

            // Рисуем PNG-текстуру
            RenderSystem.setShaderTexture(0, ARROW_TEXTURE);
            context.blit(ARROW_TEXTURE, -half, -half, 0, 0, size, size, size, size);

            poseStack.popPose();

            // Дистанция под стрелкой
            if (dist > 0) {
                String distText = String.format("%.1f", dist);
                int textX = (int)(arrowX - mc.font.width(distText) / 2f);
                int textY = (int)(arrowY + half + 4);
                context.drawString(mc.font, distText, textX, textY, 0xCCFFFFFF, false);
            }
        }
    }

    // === ОБРАБОТКА КЛАВИШИ ЧЕРЕЗ GLFW (БЕЗ FABRIC API) ===
    private static void initKeyListener() {
        new Thread(() -> {
            while (true) {
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                mc.execute(() -> {
                    if (mc.getWindow() == null) return;
                    long window = mc.getWindow().getWindow();
                    boolean currentState = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_Z) == GLFW.GLFW_PRESS;
                    if (currentState && !lastKeyState) {
                        toggle();
                    }
                    lastKeyState = currentState;
                });
            }
        }).start();
    }

    // === ENTRYPOINT FABRIC ===
    @Override
    public void onInitialize() {
        LOGGER.info("ArrowMod loaded. Press Z to toggle arrows.");
        initKeyListener();
    }
}
