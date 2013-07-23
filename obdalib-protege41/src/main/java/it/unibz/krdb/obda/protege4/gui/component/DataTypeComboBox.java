package it.unibz.krdb.obda.protege4.gui.component;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.protege4.gui.IconLoader;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class DataTypeComboBox extends JComboBox {

	private static final long serialVersionUID = 1L;

	private static final Predicate[] SUPPORTED_DATATYPES = getQuestDataTypePredicates();
	
	public DataTypeComboBox() {
		super(SUPPORTED_DATATYPES);
		setRenderer(new DataTypeRenderer());
		setPreferredSize(new Dimension(130, 23));
		setSelectedIndex(-1);
	}
	
	private static Predicate[] getQuestDataTypePredicates() {
		int length = OBDAVocabulary.QUEST_DATATYPE_PREDICATES.length + 1;
		Predicate[] dataTypes = new Predicate[length];
		dataTypes[0] = null;
		System.arraycopy(OBDAVocabulary.QUEST_DATATYPE_PREDICATES, 0, dataTypes, 1, OBDAVocabulary.QUEST_DATATYPE_PREDICATES.length);
		return dataTypes;
	}

	class DataTypeRenderer extends BasicComboBoxRenderer {
		
		private static final long serialVersionUID = 1L;

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value == null) {
				setText("<Undefined data type>");
				setIcon(null);
			} else {
				if (value instanceof Predicate) {
					Predicate item = (Predicate) value;
					String name = item.toString();
					if (name.contains(OBDAVocabulary.NS_XSD)) {
						name = name.replace(OBDAVocabulary.NS_XSD, "xsd:");
					} else if (name.contains(OBDAVocabulary.NS_RDFS)) {
						name = name.replace(OBDAVocabulary.NS_RDFS, "rdfs:");
					}
					setText(name);
					setIcon(IconLoader.getImageIcon("images/datarange.png"));
				}
			}
			return this;
		}
	}
}
