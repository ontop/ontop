package it.unibz.inf.ontop.protege.gui.component;

/*
 * #%L
 * ontop-protege
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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.vocabulary.OntopInternal;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.protege.gui.IconLoader;
import org.apache.commons.rdf.api.IRI;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;


public class DataTypeComboBox extends JComboBox {

	private static final long serialVersionUID = 1L;
	
	public DataTypeComboBox() {
		super(getQuestDataTypes());
		setRenderer(new DataTypeRenderer());
		setPreferredSize(new Dimension(130, 23));
		setSelectedIndex(-1);
	}
	
	private static IRI[] getQuestDataTypes() {

		// TODO: complete?
		ImmutableList<IRI> datatypeList = ImmutableList.of(
				XSD.BASE64BINARY,
				XSD.BOOLEAN,
				XSD.BYTE,
				XSD.DATE,
				XSD.DATETIME,
				XSD.DATETIMESTAMP,
				XSD.DECIMAL,
				XSD.DOUBLE,
				XSD.FLOAT,
				XSD.GYEAR,
				XSD.INT,
				XSD.INTEGER,
				XSD.LONG,
				XSD.NEGATIVE_INTEGER,
				XSD.NON_NEGATIVE_INTEGER,
				XSD.NON_POSITIVE_INTEGER,
				XSD.POSITIVE_INTEGER,
				XSD.SHORT,
				XSD.STRING,
				XSD.TIME,
				XSD.UNSIGNED_BYTE,
				XSD.UNSIGNED_INT,
				XSD.UNSIGNED_LONG,
				XSD.UNSIGNED_SHORT);
		
		int length = datatypeList.size() + 1;
		IRI[] dataTypes = new IRI[length];
		dataTypes[0] = null;
		System.arraycopy(datatypeList.toArray(), 0, dataTypes, 1, datatypeList.size());
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
				if (value instanceof IRI) {
					IRI item = (IRI) value;
					String name = item.getIRIString();
					if (name.contains(XSD.PREFIX)) {
						name = name.replace(XSD.PREFIX, OntopInternal.PREFIX_XSD);
					} else if (name.contains(RDFS.PREFIX)) {
						name = name.replace(RDFS.PREFIX, OntopInternal.PREFIX_RDFS);
					}
					setText(name);
					setIcon(IconLoader.getImageIcon("images/datarange.png"));
				}
			}
			return this;
		}
	}
}
