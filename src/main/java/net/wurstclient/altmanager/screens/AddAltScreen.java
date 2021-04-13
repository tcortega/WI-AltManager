package net.wurstclient.altmanager.screens;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.wurstclient.altmanager.libs.AltManager;

public final class AddAltScreen extends AltEditorScreen
{
    private final AltManager altManager;

    public AddAltScreen(Screen prevScreen, AltManager altManager)
    {
        super(prevScreen, new LiteralText("New Alt"));
        this.altManager = altManager;
    }

    @Override
    protected String getDoneButtonText()
    {
        return "Add";
    }

    @Override
    protected void pressDoneButton()
    {
        altManager.add(getEmail(), getPassword(), false);
        client.openScreen(prevScreen);
    }
}