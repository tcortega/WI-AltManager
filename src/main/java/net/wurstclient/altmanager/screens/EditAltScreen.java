package net.wurstclient.altmanager.screens;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.wurstclient.altmanager.libs.Alt;
import net.wurstclient.altmanager.libs.AltManager;

public final class EditAltScreen extends AltEditorScreen {
    private final AltManager altManager;
    private Alt editedAlt;

    public EditAltScreen(Screen prevScreen, AltManager altManager,
                         Alt editedAlt) {
        super(prevScreen, new LiteralText("Edit Alt"));
        this.altManager = altManager;
        this.editedAlt = editedAlt;
    }

    @Override
    protected String getDefaultEmail() {
        return editedAlt.getEmail();
    }

    @Override
    protected String getDefaultPassword() {
        return editedAlt.getPassword();
    }

    @Override
    protected String getDoneButtonText() {
        return "Save";
    }

    @Override
    protected void pressDoneButton() {
        altManager.edit(editedAlt, getEmail(), getPassword());
        client.openScreen(prevScreen);
    }
}