package net.wurstclient.altmanager.libs;

import net.wurstclient.altmanager.util.SwingUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public final class ImportAltsFileChooser extends JFileChooser
{
	public static void main(String[] args)
	{
		SwingUtils.setLookAndFeel();
		JFileChooser fileChooser = new ImportAltsFileChooser(new File(args[0]));
		
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(
			new FileNameExtensionFilter("TXT file (username:password)", "txt"));
		fileChooser.addChoosableFileFilter(
			new FileNameExtensionFilter("JSON file", "json"));
		
		if(fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
			return;
		
		String path = fileChooser.getSelectedFile().getAbsolutePath();
		System.out.println(path);
	}
	
	public ImportAltsFileChooser(File currentDirectory)
	{
		super(currentDirectory);
	}
	
	@Override
	protected JDialog createDialog(Component parent) throws HeadlessException
	{
		JDialog dialog = super.createDialog(parent);
		dialog.setAlwaysOnTop(true);
		return dialog;
	}
	
}
