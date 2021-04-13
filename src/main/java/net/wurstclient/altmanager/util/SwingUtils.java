package net.wurstclient.altmanager.util;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public enum SwingUtils
{
    ;

    public static void setLookAndFeel()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        }catch(ReflectiveOperationException | UnsupportedLookAndFeelException e)
        {
            throw new RuntimeException(e);
        }
    }
}
