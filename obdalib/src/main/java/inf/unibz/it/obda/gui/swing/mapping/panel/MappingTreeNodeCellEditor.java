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
package inf.unibz.it.obda.gui.swing.mapping.panel;


import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.APICoupler;
import inf.unibz.it.obda.gui.IconLoader;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingHeadNode;
import inf.unibz.it.ucq.domain.BinaryQueryAtom;
import inf.unibz.it.ucq.domain.ConceptQueryAtom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.tree.TreeCellEditor;

public class MappingTreeNodeCellEditor implements TreeCellEditor {

	Icon			mappingIcon				= null;
	Icon			mappingheadIcon			= null;
	Icon			mappingbodyIcon			= null;

	final String	PATH_MAPPING_ICON		= "images/mapping.png";
	final String	PATH_MAPPINGHEAD_ICON	= "images/head.png";
	final String	PATH_MAPPINGBODY_ICON	= "images/body.png";

	private APIController controller = null;
	private MappingEditorPanel me = null;
	private Vector<CellEditorListener> listener = null;
	private MappingManagerPanel mappingmanagerpanel = null;
	private boolean editingCanceled = false;
	
	public MappingTreeNodeCellEditor(JTree tree, MappingManagerPanel panel, APIController apic) {
//		super(tree, new MappingRenderer(apic));
//		super(tree, renderer);
////		this.renderer = new MappingRenderer(apic);
//		this.renderer = renderer;
		mappingmanagerpanel = panel;
		controller = apic;
		listener = new Vector<CellEditorListener>();
		mappingIcon = IconLoader.getImageIcon(PATH_MAPPING_ICON);
		mappingheadIcon = IconLoader.getImageIcon(PATH_MAPPINGHEAD_ICON);
		mappingbodyIcon = IconLoader.getImageIcon(PATH_MAPPINGBODY_ICON);
		me = new MappingEditorPanel(tree);
		
		

	}
//
//	public HeadNodeTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer, TreeCellEditor editor, APIController apic) {
////		super(tree, new MappingRenderer(apic), editor);
//		controller = apic;
////		this.renderer = new MappingRenderer(apic);
//		mappingIcon = IconLoader.getImageIcon(PATH_MAPPING_ICON);
//		mappingheadIcon = IconLoader.getImageIcon(PATH_MAPPINGHEAD_ICON);
//		mappingbodyIcon = IconLoader.getImageIcon(PATH_MAPPINGBODY_ICON);
//	}

	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
//		this.renderer = new MappingRenderer(controller);

//		Dimension d = this.editingContainer.getPreferredSize();
//		this.editingContainer.setPreferredSize(new Dimension(800, d.height));
//		this.editingContainer.setMaximumSize(new Dimension(800, d.height));
//		this.editingContainer.setMinimumSize(new Dimension(800, d.height));
//		this.editingContainer.setSize(new Dimension(800, d.height));
//		Component editor = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
//		System.out.println(editor.getClass());
//		editingContainer.validate();
//		this.renderer = new MappingRenderer(controller);

//		if (value instanceof MappingNode) {
//			this.editingIcon = mappingIcon;
//
//		} else if (value instanceof MappingBodyNode) {
//			this.editingIcon = mappingbodyIcon;
//
//		} else if (value instanceof MappingHeadNode) {
//			this.editingIcon = mappingheadIcon;
//
//		} else {
//			this.editingIcon = null;
//		}
		me.setText(value);
		return me;
	}

	public boolean isInputValid(){
		
		if(me.getEditingObject() instanceof MappingHeadNode){
			String txt = me.getText();
			try {
				ConjunctiveQuery query = new ConjunctiveQuery(txt,controller);
//				checkValidityOfConjunctiveQuery(query );
				return true;
			} catch (Exception e) {
				return false;
			}
		}else {
			return true;
		}
		
	}
	
	private void checkValidityOfConjunctiveQuery(ConjunctiveQuery cq ) throws Exception{
		ArrayList<QueryAtom> atoms = cq.getAtoms();
		Iterator<QueryAtom> it = atoms.iterator();
		APICoupler coup= controller.getCoupler();
		URI onto_uri =controller.getCurrentOntologyURI();
		while(it.hasNext()){
			QueryAtom atom = it.next();
			if(atom instanceof ConceptQueryAtom){
				ConceptQueryAtom cqa = (ConceptQueryAtom) atom;
				String name = controller.getEntityNameRenderer().getPredicateName(cqa);
				boolean isConcept =coup.isNamedConcept(onto_uri,new URI(name));
				if(!isConcept){
					throw new Exception("Concept "+name+" not present in ontology.");
				}
				
			}else{
				BinaryQueryAtom bqa = (BinaryQueryAtom) atom;
				String name = controller.getEntityNameRenderer().getPredicateName(bqa);
				ArrayList<QueryTerm> terms = bqa.getTerms();
				QueryTerm t2 = terms.get(1);
				boolean found = false;
				if(t2 instanceof FunctionTerm){
					found =coup.isObjectProperty(onto_uri,new URI(name));
				}else{
					found =coup.isDatatypeProperty(onto_uri,new URI(name));
				}
				if(!found){
					throw new Exception("Property "+name+" not present in ontology.");
				}
			}
		}
	}

		public void addCellEditorListener(CellEditorListener arg0) {
			listener.add(arg0);
		}

		public void cancelCellEditing() {
			
			if(!editingCanceled){
				mappingmanagerpanel.applyChangedToNode(getCellEditorValue().toString());
			}
		}

		public Object getCellEditorValue() {
			return  me.getText();

		}

		public boolean isCellEditable(EventObject eo) {
			 if ((eo == null) || ((eo instanceof MouseEvent) && (((MouseEvent) eo)
				            .isMetaDown()))) {
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

		public boolean stopCellEditing() {
			
			return isInputValid();
		}
		
		
		
		
		private class HeadInputVerifier extends InputVerifier {

			public boolean verify(JComponent field) {
				JTextPane pane = (JTextPane) field;
				String txt = pane.getText();
				try {
					ConjunctiveQuery query = new ConjunctiveQuery(txt,controller);
				} catch (QueryParseException e) {
					return false;
				}
				return true;
			}

		}
		
		private class MappingEditorPanel extends JPanel{

			
			/**
			 * 
			 */
			private static final long serialVersionUID = 8573053733072805037L;
			private String value="";
		    private javax.swing.JScrollPane jScrollPane1;
		    private javax.swing.JTextPane pane;
		    private JTree tree;
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

					public void keyPressed(KeyEvent e) {
						
						if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK && e.getKeyCode() == KeyEvent.VK_ENTER) {
							mappingmanagerpanel.stopTreeEditing();
						}else if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
							editingCanceled = true;
						}

					}
					public void keyReleased(KeyEvent e) {}
					public void keyTyped(KeyEvent e) {}
		        	
		        });
			}
			
			public void setText(Object value){
				obj = value;
				MappingRenderer ren = (MappingRenderer) tree.getCellRenderer();
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
