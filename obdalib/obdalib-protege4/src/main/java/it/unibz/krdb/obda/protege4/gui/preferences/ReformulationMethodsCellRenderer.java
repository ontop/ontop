package it.unibz.krdb.obda.protege4.gui.preferences;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ReformulationMethodsCellRenderer extends JLabel implements
		ListCellRenderer {
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -543240735952113261L;

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		String optValue = (String) value;
		String optText = "";
		
		if (optValue.equals("dlr")) {
			optText = "Perfect Reformulation";
		}
		else if (optValue.equals("improved")) {
			optText = "TreeRed Reformulation";
		}
		
		setText(optText);
		
		return this;
	}

}
