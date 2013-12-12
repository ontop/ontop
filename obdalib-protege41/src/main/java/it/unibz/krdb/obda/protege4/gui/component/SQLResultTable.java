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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class SQLResultTable extends JTable {

	private static final long serialVersionUID = -7327949276473574914L;

	private int selectedColumn;
	
	public SQLResultTable() {
		super();
		setAutoscrolls(false);
		setColumnSelectionAllowed(true);
		setCellSelectionEnabled(true);
		setDefaultRenderer(String.class, new ColumnHighlightRenderer());
		
		setPreferredScrollableViewportSize(getPreferredSize());
		setIntercellSpacing(new Dimension(1, 1));
		
		JTableHeader tableHeader = getTableHeader();
		tableHeader.setReorderingAllowed(false);
		tableHeader.setDefaultRenderer(new HeaderHighlightRenderer());
		tableHeader.addMouseListener(new ColumnHeaderAdapter(this));
	}
	
	public void setColumnOnSelect(int index) {
		selectedColumn = index;
	}
	
	public int getColumnOnSelect() {
		return selectedColumn;
	}
	
	/**
	 * An adapter class to capture the column index when user selects the table header
	 */
	class ColumnHeaderAdapter extends MouseAdapter {
		
		private SQLResultTable table;
		
		private JTextField textFieldOnFocus;
		
		public ColumnHeaderAdapter(SQLResultTable table) {
			this.table = table;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			JTableHeader header = table.getTableHeader();
			int index = header.columnAtPoint(e.getPoint());
			table.setColumnOnSelect(index);
			table.repaint();
			header.repaint();
			
			// Find any text field component that is on focus and put the table header name there
			Container parent = findParentContainer(table);
			Component compOnFocus = findFocus(parent);
			if (compOnFocus != null) {
				if (!(compOnFocus instanceof JComboBox) && compOnFocus instanceof JTextField) {
					textFieldOnFocus = (JTextField) compOnFocus;
					String existingText = getExistingText();
					if (existingText.isEmpty()) {
						String text = String.format("{%s}", table.getColumnName(index));
						textFieldOnFocus.setText(text);
					} else {
						if (hasPrefix(existingText) || writtenInFullUri(existingText)) {
							int caretPosition = textFieldOnFocus.getCaretPosition();
							String firstPortion = existingText.substring(0, caretPosition);
							String secondPortion = existingText.substring(caretPosition, existingText.length());
							String columnName = table.getColumnName(index);
							
							// Append all the texts
							String text = String.format("%s{%s}%s", firstPortion, columnName, secondPortion);
							textFieldOnFocus.setText(text);
							textFieldOnFocus.setCaretPosition(text.length());
						}
					}
				}
			}
		}

		private boolean hasPrefix(String input) {
			// If contains prefix string, e.g., &example;person#
			String prefix = input.substring(0, input.indexOf(":") + 1);
			return (prefix.isEmpty()) ? false : true;
		}

		private boolean writtenInFullUri(String input) {
			return input.startsWith("<") || input.endsWith(">");
		}

		private String getExistingText() {
			String existingText = textFieldOnFocus.getText();
			String selectedText = textFieldOnFocus.getSelectedText();
			if (selectedText != null) {
				return existingText.replace(selectedText, ""); // remove text on highlight
			} else {
				return existingText;
			}
		}
		
		private Container findParentContainer(Component c) {
			boolean loop = true;
			Component comp = c;
			while (loop) {
				comp = comp.getParent();
				if (comp != null) {
					if (comp instanceof JPanel) {
						String compName = comp.getName();
						if (compName != null && compName.equals("panel_master")) {
							return (Container) comp;
						}
					}
				} else {
					loop = false;
				}
			}
			return null;
		}
		
		private Component findFocus(Container c) {
			Component comps[] = c.getComponents();
			for (int i = 0; i < comps.length; i++) {
				if (comps[i].isFocusOwner()) {
					return comps[i];
				}
				if (comps[i] instanceof Container) {
					Component comp = findFocus((Container) comps[i]);
					if (comp != null) {
						return comp;
					}
				}
			}
			return null;
		}
	}

	/**
	 * A custom cell renderer to highlight the entire column cells if user selects the table header
	 */
	class ColumnHighlightRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		public ColumnHighlightRenderer() {
			super();
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			
			SQLResultTable resultTable = (SQLResultTable) table;
			if (resultTable.getColumnOnSelect() == col) {
				setBackground(UIManager.getDefaults().getColor("Table.selectionBackground"));
			} else {
				setBackground(UIManager.getDefaults().getColor("Table.background"));
			}
			return this;
		}
	}
	
	/**
	 * A custom cell renderer to highlight the table header when selected
	 */
	class HeaderHighlightRenderer extends DefaultTableCellRenderer {
		
		private static final long serialVersionUID = 1L;
		
		public HeaderHighlightRenderer() {
			super();
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setHorizontalAlignment(SwingConstants.CENTER);
			setFont(new Font("Tahoma", Font.BOLD, 13));

			SQLResultTable resultTable = (SQLResultTable) table;
			if (resultTable.getColumnOnSelect() == col) {
				setBackground(UIManager.getDefaults().getColor("Button.select"));
			} else {
				setBackground(UIManager.getDefaults().getColor("Button.background"));
			}
			return this;
		}
	}
}
