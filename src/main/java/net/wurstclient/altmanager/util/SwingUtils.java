package net.wurstclient.altmanager.util;

import javax.swing.*;

public enum SwingUtils
{
    ;

    public static void setLookAndFeel()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e)
        {
            throw new RuntimeException(e);
        }
    }
}
