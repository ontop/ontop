/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 *
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.gui.swing.mapping.panel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.domain.Query;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingBodyNode;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingHeadNode;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingNode;
import inf.unibz.it.obda.model.CQIE;
import inf.unibz.it.obda.model.impl.RDBMSSQLQuery;
import inf.unibz.it.obda.parser.DatalogProgramParser;
import inf.unibz.it.obda.parser.DatalogQueryHelper;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;

import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeCellEditorDialog extends JDialog {

	/**
	 *
	 */
	private static final long			serialVersionUID	= -5948486809738359179L;
	private DefaultMutableTreeNode		editedNode			= null;
	private String						input;
	private JDialog						myself				= null;
	private JTextPane					area				= null;
	private JTree						tree				= null;
	private boolean						update				= true;

//	private static TreeCellEditorDialog	instance			= null;

	private DatasourcesController		dsc					= null;

	private MappingController			mapc				= null;
	private final APIController	apic;

	DatalogProgramParser datalogParser = new DatalogProgramParser();

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public TreeCellEditorDialog(APIController apic, DatasourcesController dsc, MappingController mapc) {
		this.apic = apic;
		this.dsc = dsc;
		this.mapc = mapc;
		myself = this;
//		instance = this;
		createDialog();
	}

//	public static TreeCellEditorDialog getCurrentInstance(DatasourcesController dsc, MappingController mapc) {
//
//		if (instance == null) {
//			instance = new TreeCellEditorDialog(dsc, mapc);
//		}
//		return instance;
//	}

	public void showDialog(JTree t, DefaultMutableTreeNode node, MouseEvent e) {

		editedNode = node;
		tree = t;
		update = true;

		int width = 800;  // TODO Put to a global object.
		int hight = 75;
		this.setMinimumSize(new Dimension(450, 40));
		int maxwidth = (int) t.getParent().getSize().getWidth() - 20;
		this.setMaximumSize(new Dimension(maxwidth, 80));
		this.setPreferredSize(new Dimension(maxwidth, 80));
		// TODO get them from Preferences

		MappingRenderer ren = (MappingRenderer) t.getCellRenderer();
		JPanel jp = (JPanel) ren.getTreeCellRendererComponent(t, node, true, true, true, 5, true);

		// Rectangle nodepositionInTree = tree.getPathBounds(new
		// TreePath(node.getPath()));

		JTextPane text = (JTextPane) jp.getComponent(1);

		this.setSize(width, hight);
		this.setLocation(getLocation(tree, e));

		if (node instanceof MappingHeadNode) {
			MappingStyledDocument doc = (MappingStyledDocument) text.getDocument();
			area.setDocument(doc);
			this.setSize(width, hight);
			area.setBounds(0, 0, width, hight);

		} else {

			String txt = (String) node.getUserObject();
			DefaultStyledDocument doc = (DefaultStyledDocument) text.getDocument();
			area.setDocument(doc);
			this.setSize(width, hight);
			area.setBounds(0, 0, width, hight);

		}

		this.setModal(true);
		// this.setUndecorated(true);
		this.pack();
		this.setVisible(true);
	}

	private void createDialog() {
		Container content = this.getContentPane();
		this.setUndecorated(true);
		area = new JTextPane();
		area.scrollRectToVisible(new Rectangle(0, 0, 250, 100));
		area.setToolTipText("Press CRTL+ENTER to submit your changes, press ESC to cancel editing");
		area.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		JScrollPane pane = new JScrollPane(area);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.add(pane);
		area.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK && e.getKeyCode() == KeyEvent.VK_ENTER) {

					//Starting main edit do not let go unless the query is valid
					input = area.getText();

					String nodeContent = (String) editedNode.getUserObject();
					if (editedNode instanceof MappingHeadNode) {


//						try {
//							TargetQuery h = new ConjunctiveQuery();
//						} catch (QueryParseException e1) {
//							// Invalid input do not update, do not close
//							return;
//						}
					}

					try {
						update(input);
					} catch (Exception e1) {
						return;
					}
					myself.setVisible(false);
				}

				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {

					update = false;
					myself.setVisible(false);
				}
			}

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent e) {
			}
		});
		this.addWindowFocusListener(new WindowFocusListener() {

			public void windowGainedFocus(WindowEvent arg0) {
			}

			public void windowLostFocus(WindowEvent arg0) {

				if (update) {
					input = area.getText();
					try {
						update(input);
					} catch (Exception e) {
						return;
					}
					finally{
						myself.setVisible(false);
					}
				}
			}

		});
	}

	private Point getLocation(JTree t, MouseEvent e) {

		Point p = tree.getLocationOnScreen();
		int x = p.x + 20;
		int y = p.y + e.getY() - 10;

		return new Point(x, y);
	}

	private void update(String str) throws Exception {

		MappingController con = mapc;
		
		URI sourceName = null;
		
		String nodeContent = (String) editedNode.getUserObject();
		if (editedNode instanceof MappingNode) {

			con.updateMapping(sourceName, nodeContent, str);

		} else if (editedNode instanceof MappingBodyNode) {

			MappingBodyNode node = (MappingBodyNode) editedNode;
			MappingNode parent = (MappingNode) node.getParent();

			Query b = new RDBMSSQLQuery(str);
			con.updateSourceQueryMapping(sourceName, parent.getMappingID(), b);

		} else if (editedNode instanceof MappingHeadNode) {

			MappingHeadNode node = (MappingHeadNode) editedNode;
			MappingNode parent = (MappingNode) node.getParent();

			Query h = parse(str);
//			checkValidityOfConjunctiveQuery((CQIE) h);
			con.updateTargetQueryMapping(sourceName, parent.getMappingID(), h);
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
		//TODO Revisar este methodo urgentemente. Prefix related
		
		String query = "";
		DatalogQueryHelper queryHelper =
			new DatalogQueryHelper(apic.getPrefixManager());

		String[] atoms = input.split(DatalogQueryHelper.DATALOG_IMPLY_SYMBOL, 2);
		if (atoms.length == 1)  // if no head
			query = queryHelper.getDefaultHead() + " " +
			 	DatalogQueryHelper.DATALOG_IMPLY_SYMBOL + " " +
			 	input;

		// Append the prefixes
		query = queryHelper.getPrefixes() + query;

		return query;
	}
}
