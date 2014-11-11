package it.unibz.krdb.obda.protege4.gui.component;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.DatatypeFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
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
		int length = DatatypeFactoryImpl.QUEST_DATATYPE_PREDICATES.length + 1;
		Predicate[] dataTypes = new Predicate[length];
		dataTypes[0] = null;
		System.arraycopy(DatatypeFactoryImpl.QUEST_DATATYPE_PREDICATES, 0, dataTypes, 1, DatatypeFactoryImpl.QUEST_DATATYPE_PREDICATES.length);
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
