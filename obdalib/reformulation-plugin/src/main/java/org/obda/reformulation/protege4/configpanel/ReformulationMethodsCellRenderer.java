package org.obda.reformulation.protege4.configpanel;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ReformulationMethodsCellRenderer extends JLabel implements
		ListCellRenderer {
	
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
