package net.wurstclient.altmanager.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.wurstclient.altmanager.WiAltManager;
import net.wurstclient.altmanager.screens.AltManagerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen
{
    private TitleScreenMixin(WiAltManager altManager, Text text_1)
    {
        super(text_1);
    }

    @Inject(at = {@At("RETURN")}, method = {"initWidgetsNormal(II)V"})
    private void onInitWidgetsNormal(int y, int spacingY, CallbackInfo ci)
    {
        addButton(new ButtonWidget(width / 2 + 2, y + spacingY * 2, 98, 20,
                new LiteralText("Alt Manager"),
                b -> client.openScreen(new AltManagerScreen(this,
                        WiAltManager.INSTANCE.getAltManager()))));

        for (AbstractButtonWidget button : buttons)
        {
            if (!button.getMessage().getString()
                    .equals(I18n.translate("menu.online")))
                continue;

            button.setWidth(98);
        }
    }
}
