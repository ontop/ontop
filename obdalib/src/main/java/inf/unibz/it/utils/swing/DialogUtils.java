package inf.unibz.it.utils.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class DialogUtils {

	public static void centerDialogWRTParent(Component parent, Component dialog) {
		int x;
		int y;
	
		// Find out our parent
		Container myParent = (Container) parent;
		Point topLeft = myParent.getLocationOnScreen();
		Dimension parentSize = myParent.getSize();
		Dimension mySize = dialog.getSize();
	
		if (parentSize.width > mySize.width)
			x = ((parentSize.width - mySize.width) / 2) + topLeft.x;
		else
			x = topLeft.x;
	
		if (parentSize.height > mySize.height)
			y = ((parentSize.height - mySize.height) / 2) + topLeft.y;
		else
			y = topLeft.y;
	
		dialog.setLocation(x, y);
	
	}

	public static JDialog createDialogForPanel(JDialog parent, JPanel panel) {
		JDialog newdialog = new JDialog(parent);
		newdialog.getContentPane().add(panel, java.awt.BorderLayout.CENTER);
		newdialog.pack();
		return newdialog;
	}

	public static JDialog createDialogForPanel(JFrame parent, JPanel panel) {
		JDialog newdialog = new JDialog(parent);
		newdialog.getContentPane().add(panel, java.awt.BorderLayout.CENTER);
		newdialog.pack();
		return newdialog;
	}

	public static void setAntializaing(Component component, boolean value) {
	// if (component instanceof JComponent) {
	// ((JComponent)
	// component).putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,
	// value);
	// Component[] children = ((JComponent)component).getComponents();
	// for (int i = 0; i < children.length; i++) {
	// if (children[i] instanceof JComponent) {
	// setAntializaing((JComponent)children[i], value);
	// }
	// if (children[i] instanceof JTree) {
	// TreeCellRenderer rend = ((JTree)children[i]).getCellRenderer();
	// if (rend instanceof JComponent) {
	// ((JComponent)rend).putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,
	// true);
	// }
	// }
	// if (children[i] instanceof JTable) {
	// JTable table = (JTable)children[i];
	// TableCellRenderer crenderer = table.getDefaultRenderer(Object.class);
	// if (crenderer instanceof JComponent) {
	// ((JComponent)crenderer).putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,
	// true);
	// }
	// crenderer = table.getDefaultRenderer(Number.class);
	// if (crenderer instanceof JComponent) {
	// ((JComponent)crenderer).putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,
	// true);
	// }
	// crenderer = table.getDefaultRenderer(Boolean.class);
	// if (crenderer instanceof JComponent) {
	// ((JComponent)crenderer).putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,
	// true);
	// }
	//					
	//					
	// crenderer = table.getTableHeader().getDefaultRenderer();
	// if (crenderer instanceof JComponent) {
	// ((JComponent)crenderer).putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,
	// true);
	// }
	// }
	//				
	// }
	//		}
			
	
		}

}
