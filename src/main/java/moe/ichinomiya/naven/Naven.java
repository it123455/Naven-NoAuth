package moe.ichinomiya.naven;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.ViaMCP;
import lombok.Getter;
import moe.ichinomiya.naven.commands.CommandManager;
import moe.ichinomiya.naven.events.api.EventManager;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.files.FileManager;
import moe.ichinomiya.naven.protocols.MythProtocol;
import moe.ichinomiya.naven.protocols.germ.GermMod;
import moe.ichinomiya.naven.protocols.world.Wrapper;
import moe.ichinomiya.naven.modules.ModuleManager;
import moe.ichinomiya.naven.ui.cooldown.CooldownBarManager;
import moe.ichinomiya.naven.utils.RotationManager;
import moe.ichinomiya.naven.ui.notification.NotificationManager;
import moe.ichinomiya.naven.utils.*;
import moe.ichinomiya.naven.utils.font.FontManager;
import moe.ichinomiya.naven.values.HasValueManager;
import moe.ichinomiya.naven.values.ValueManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.main.Main;
import net.minecraft.client.shader.Framebuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class Naven {
    public static final String CLIENT_NAME = "SilenceFix";
    public static final String CLIENT_DISPLAY_NAME = "Naven";
    private static final Logger logger = LogManager.getLogger(Naven.class);
    @Getter
    private static Naven instance;

    private final TimeHelper blurTimer = new TimeHelper();
    private final TimeHelper shadowTimer = new TimeHelper();
    private Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);

    @Getter
    private ModuleManager moduleManager;
    @Getter
    private EventManager eventManager;
    @Getter
    private CommandManager commandManager;
    @Getter
    private FileManager fileManager;
    @Getter
    private ValueManager valueManager;
    @Getter
    private HasValueManager hasValueManager;
    @Getter
    private FontManager fontManager;
    @Getter
    private NotificationManager notificationManager;
    @Getter
    private CooldownBarManager cooldownBarManager;

    public Naven() {
        instance = this;
    }

    public void onClientInit() {
        Display.setTitle(CLIENT_DISPLAY_NAME + " " + Version.getVersion());

        ViaMCP.create();
        ViaMCP.INSTANCE.initAsyncSlider();
        ViaLoadingBase.getInstance().reload(ProtocolVersion.v1_12_2);

        this.fontManager = new FontManager();

        eventManager = new EventManager();
        hasValueManager = new HasValueManager();
        valueManager = new ValueManager();
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();
        fileManager = new FileManager();
        notificationManager = new NotificationManager();
        cooldownBarManager = new CooldownBarManager();

        eventManager.register(notificationManager);
        eventManager.register(cooldownBarManager);
        fileManager.load();

        eventManager.register(new Wrapper());
        eventManager.register(new GermMod());
        eventManager.register(new RotationManager());
        eventManager.register(Naven.getInstance());
        eventManager.register(Minecraft.getMinecraft().ingameGUI);
        eventManager.register(new ServerUtils());
        eventManager.register(new ChatMessageQueue());
        eventManager.register(new EntityWatcher());
        eventManager.register(new WorldMonitor());
        eventManager.register(new MythProtocol());

        if (Main.rawInput) {
            eventManager.register(new RawInput());
        }

        logger.info("Client initialized!");
        eventManager.call(new EventClientInit());
    }

    public void onClientShutdown() {
        this.fileManager.save();
    }

    private boolean disableShadow = false, disableBlur = false;

    public void glslShaderUpdate() {
        if (ShaderUtils.isSupportGLSL()) {
            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

            if (!disableShadow) {
                float shadowDelay = 1000 / 90f;
                boolean shadow = shadowTimer.delay(shadowDelay, true);
                if (shadow) {
                    bloomFramebuffer = RenderUtils.createFrameBuffer(bloomFramebuffer);
                    bloomFramebuffer.framebufferClear();
                    bloomFramebuffer.bindFramebuffer(true);
                    getEventManager().call(new EventShader(scaledResolution, EventType.SHADOW));
                    bloomFramebuffer.unbindFramebuffer();
                }
                DropShadowUtils.renderDropShadow(bloomFramebuffer.framebufferTexture, 10, 2, shadow);

                int shadowError = GL11.glGetError();
                if (shadowError != 0) {
                    disableShadow = true;
                    logger.error("OpenGL Error: {}, disabling shadow!", shadowError);
                }
            }

            if (!disableBlur) {
                float blurDelay = 1000 / 60f;
                boolean blur = blurTimer.delay(blurDelay, true);
                StencilUtils.write(false);
                getEventManager().call(new EventShader(scaledResolution, EventType.BLUR));
                StencilUtils.erase(true);
                DualBlurUtils.renderBlur(3, 5, blur);
                StencilUtils.dispose();

                int blurError = GL11.glGetError();
                if (blurError != 0) {
                    disableBlur = true;
                    logger.error("OpenGL Error: {}, disabling blur!", blurError);
                }
            }
        }
    }
}
