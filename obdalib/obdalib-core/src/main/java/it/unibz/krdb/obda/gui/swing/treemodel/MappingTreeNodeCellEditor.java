/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 *
 * a) The OBDA-API developed by the author and licensed under the LGPL; and,
 * b) third-party components licensed under terms that may be different from
 *   those of the LGPL.  Information about such licenses can be found in the
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.gui.swing.IconLoader;
import it.unibz.krdb.obda.gui.swing.panel.MappingManagerPanel;
import it.unibz.krdb.obda.gui.swing.utils.MappingStyledDocument;
import it.unibz.krdb.obda.gui.swing.utils.MappingTreeCellRenderer;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.parser.DatalogProgramParser;
import it.unibz.krdb.obda.parser.DatalogQueryHelper;
import it.unibz.krdb.obda.utils.TargetQueryValidator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.tree.TreeCellEditor;

import org.antlr.runtime.RecognitionException;
import org.semanticweb.owl.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingTreeNodeCellEditor implements TreeCellEditor {

	Icon			mappingIcon				= null;
	Icon			mappingheadIcon			= null;
	Icon			mappingbodyIcon			= null;

	final String	PATH_MAPPING_ICON		= "images/mapping.png";
	final String	PATH_MAPPINGHEAD_ICON	= "images/head.png";
	final String	PATH_MAPPINGBODY_ICON	= "images/body.png";

	private OBDAModel controller = null;
	private MappingEditorPanel parent = null;
	private Vector<CellEditorListener> listener = null;
	private MappingManagerPanel mappingmanagerpanel = null;
	private TargetQueryValidator validator = null;

	private boolean bEditingCanceled = false;

	DatalogProgramParser datalogParser = new DatalogProgramParser();

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public MappingTreeNodeCellEditor(JTree tree, MappingManagerPanel panel, OBDAModel apic) {
		mappingmanagerpanel = panel;
		controller = apic;
		listener = new Vector<CellEditorListener>();
		mappingIcon = IconLoader.getImageIcon(PATH_MAPPING_ICON);
		mappingheadIcon = IconLoader.getImageIcon(PATH_MAPPINGHEAD_ICON);
		mappingbodyIcon = IconLoader.getImageIcon(PATH_MAPPINGBODY_ICON);
		parent = new MappingEditorPanel(tree);

	    OWLOntology ontology = mappingmanagerpanel.getOntology();
	    validator = new TargetQueryValidator(ontology);
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		parent.setText(value);
		return parent;
	}

	public void validate() throws Exception {
	    final String text = parent.getText().trim();
	    final Object node = parent.getEditingObject();
	    
	    if (text.isEmpty()) {
	    	String msg = String.format("ERROR: The %s cannot be empty!", node.toString().toLowerCase());
	    	throw new Exception(msg);
	    }
	
		if (node instanceof MappingHeadNode) {
			final CQIE query = parse(text);
	  		if (!validator.validate(query)) {
	  			Vector<String> invalidPredicates = validator.getInvalidPredicates();
	  			String invalidList = "";
		        for (String predicate : invalidPredicates) {
		        	invalidList += "- " + predicate + "\n";
		        }
		        String msg = String.format("ERROR: The below list of predicates is unknown by the ontology: \n %s", invalidList);
		        throw new Exception(msg);
	  		}
		}
	}

	private CQIE parse(String query) {
		CQIE cq = null;
		query = prepareQuery(query);
		try {
			datalogParser.parse(query);
			cq = datalogParser.getRule(0);
		}
		catch (RecognitionException e) {
			log.warn(e.getMessage());
		}
		return cq;
	}

	private String prepareQuery(String input) {
		String query = "";
		DatalogQueryHelper queryHelper = new DatalogQueryHelper(controller.getPrefixManager());

		String[] atoms = input.split(DatalogQueryHelper.DATALOG_IMPLY_SYMBOL, 2);
		if (atoms.length == 1){ // if no head
			query = String.format("%s %s %s", queryHelper.getDefaultHead(), DatalogQueryHelper.DATALOG_IMPLY_SYMBOL, input);
		}
		query = queryHelper.getPrefixes() + query; // Append the prefixes

		return query;
	}

	@Override
	public void addCellEditorListener(CellEditorListener arg0) {
		listener.add(arg0);
	}

	@Override
	public void cancelCellEditing() {
		if (!bEditingCanceled){
			final String text = getCellEditorValue().toString().trim();
			mappingmanagerpanel.applyChangedToNode(text);
		}
	}

	@Override
	public Object getCellEditorValue() {
		return parent.getText();
	}

	public boolean isCellEditable(EventObject eo) {
		if ((eo == null) || ((eo instanceof MouseEvent) && (((MouseEvent) eo).isMetaDown()))) {
			return true;
		}
		return false;
	}

	public void removeCellEditorListener(CellEditorListener arg0) {
		listener.remove(arg0);
	}

	public boolean shouldSelectCell(EventObject arg0) {
		return true;
	}

	@Override
	public boolean stopCellEditing() {
		try {
			validate();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(parent.getPane(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private class HeadInputVerifier extends InputVerifier {
		@Override
		public boolean verify(JComponent field) {
			JTextPane pane = (JTextPane) field;
			String txt = pane.getText();
			CQIE cq = parse(txt);

			if (cq != null) {
				return true;
			}
			else {
				return false;
			}
		}
	}

		private class MappingEditorPanel extends JPanel{


			/**
			 *
			 */
			private static final long serialVersionUID = 8573053733072805037L;
			private final String value="";
		    private final javax.swing.JScrollPane jScrollPane1;
		    private final javax.swing.JTextPane pane;
		    private final JTree tree;
		    private Object obj = null;

			private MappingEditorPanel(JTree tree){

				this.tree = tree;
				this.setMinimumSize(new Dimension(800,75));
				this.setPreferredSize(new Dimension(800,75));

		        java.awt.GridBagConstraints gridBagConstraints;
		        jScrollPane1 = new javax.swing.JScrollPane();
		        pane = new javax.swing.JTextPane();
		        pane.setText(value.toString());

		        setLayout(new java.awt.GridBagLayout());

		        jScrollPane1.setViewportView(pane);

		        gridBagConstraints = new java.awt.GridBagConstraints();
		        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
		        gridBagConstraints.weightx = 1.0;
		        gridBagConstraints.weighty = 1.0;
		        add(jScrollPane1, gridBagConstraints);

		        pane.addKeyListener(new KeyListener(){
		        	@Override
					public void keyPressed(KeyEvent e) {
						if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK && e.getKeyCode() == KeyEvent.VK_ENTER) {
							mappingmanagerpanel.stopTreeEditing();
						}
						else if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
							bEditingCanceled = true;
						}
					}		        	
		        	@Override
					public void keyReleased(KeyEvent e) {
						// Does nothing
					}		        	
		        	@Override
					public void keyTyped(KeyEvent e) {
						// Does nothing
					}
		        });
			}

			public void setText(Object value){
				obj = value;
				MappingTreeCellRenderer ren = (MappingTreeCellRenderer) tree.getCellRenderer();
				JPanel jp = (JPanel) ren.getTreeCellRendererComponent(tree, value, true, true, true, 5, true);
				JTextPane text = (JTextPane) jp.getComponent(1);
				if (value instanceof MappingHeadNode) {
//					JTextField field = (JTextField) editingComponent;
					MappingStyledDocument doc = (MappingStyledDocument) text.getDocument();
					pane.setStyledDocument(doc);
					setInputVerifier(new HeadInputVerifier());
				}else{
					DefaultStyledDocument doc = (DefaultStyledDocument) text.getDocument();
					pane.setStyledDocument(doc);
				}
			}

			public Object getEditingObject(){
				return obj;
			}

			public String getText(){
				return pane.getText();
			}

			public JTextPane getPane(){
				return pane;
			}
		}

}
