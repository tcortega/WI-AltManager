package net.wurstclient.altmanager.screens;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.LiteralText;
import net.wurstclient.altmanager.libs.LoginManager;

public final class DirectLoginScreen extends AltEditorScreen
{
    public DirectLoginScreen(Screen prevScreen)
    {
        super(prevScreen, new LiteralText("Direct Login"));
    }

    @Override
    protected String getDoneButtonText()
    {
        return "Login";
    }

    @Override
    protected void pressDoneButton()
    {
        if (getPassword().isEmpty())
        {
            message = "";
            LoginManager.changeCrackedName(getEmail());

        } else
            message = LoginManager.login(getEmail(), getPassword());

        if (message.isEmpty())
            client.openScreen(new TitleScreen());
        else
            doErrorEffect();
    }
}

