package net.wurstclient.altmanager.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.util.Session;
import net.minecraft.util.snooper.SnooperListener;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.wurstclient.altmanager.WiAltManager;
import net.wurstclient.altmanager.mixinterface.IMinecraftClient;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin
        extends ReentrantThreadExecutor<Runnable> implements SnooperListener,
        WindowEventHandler, AutoCloseable, IMinecraftClient
{
    @Shadow
    private Session session;

    private Session altManagerSession;

    private MinecraftClientMixin(WiAltManager wiAltManager, String string_1)
    {
        super(string_1);
    }

    @Inject(at = {@At("HEAD")},
            method = {"getSession()Lnet/minecraft/client/util/Session;"},
            cancellable = true)
    private void onGetSession(CallbackInfoReturnable<Session> cir)
    {
        if (altManagerSession == null)
            return;

        cir.setReturnValue(altManagerSession);
    }

    @Redirect(at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/MinecraftClient;session:Lnet/minecraft/client/util/Session;",
            opcode = Opcodes.GETFIELD,
            ordinal = 0),
            method = {
                    "getSessionProperties()Lcom/mojang/authlib/properties/PropertyMap;"})
    private Session getSessionForSessionProperties(MinecraftClient mc)
    {
        if (altManagerSession != null)
            return altManagerSession;
        else
            return session;
    }

    @Override
    public void setSession(Session session)
    {
        altManagerSession = session;
    }
}
