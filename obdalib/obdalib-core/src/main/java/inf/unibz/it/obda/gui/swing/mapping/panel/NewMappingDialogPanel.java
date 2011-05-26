/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package inf.unibz.it.obda.gui.swing.mapping.panel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.exception.DuplicateMappingException;
import inf.unibz.it.obda.gui.swing.datasource.DatasourceSelectorListener;
import inf.unibz.it.obda.gui.swing.datasource.panels.SQLQueryPanel;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferences;
import inf.unibz.it.obda.model.CQIE;
import inf.unibz.it.obda.model.DatalogProgram;
import inf.unibz.it.obda.model.rdbms.impl.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.model.rdbms.impl.RDBMSSQLQuery;
import inf.unibz.it.obda.parser.DatalogProgramParser;
import inf.unibz.it.obda.parser.DatalogQueryHelper;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.StyleContext;

import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * NewMappingDialogPanel.java
 * 
 * Created on 11-apr-2011, 17.39.34
 */

/**
 * 
 * @author obda
 */
public class NewMappingDialogPanel extends javax.swing.JPanel implements DatasourceSelectorListener {

	/**
	 * 
	 */
	private static final long			serialVersionUID	= 4351696247473906680L;
	private APIController				controller			= null;
	private DatalogProgramParser		datalogParser		= new DatalogProgramParser();
	private MappingManagerPreferences	preferences			= null;
	private DataSource					selectedSource		= null;
	private JDialog						parent				= null;

	private final Logger				log					= LoggerFactory.getLogger(this.getClass());

	/** Creates new form NewMappingDialogPanel */
	public NewMappingDialogPanel(APIController apic, OBDAPreferences pref, JDialog parent, DataSource dataSource) {
		controller = apic;
		preferences = pref.getMappingsPreference();
		this.parent = parent;
		selectedSource = dataSource;
		initComponents();
		init();
	}

	private void init() {

		final MappingStyledDocument mapdoc = new MappingStyledDocument(new StyleContext(), controller, preferences);
		jTextPaneHead.setDocument(mapdoc);

		jButtonInsert.setEnabled(false);
		jButtonInsert.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				insertMapping();
			}
		});

		ActionListener actionListenerCancel = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				parent.setVisible(false);
				parent.dispose();
			}
		};
		KeyStroke ks_ecape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		this.registerKeyboardAction(actionListenerCancel, ks_ecape, JComponent.WHEN_IN_FOCUSED_WINDOW);

		ActionListener actionListenerAccept = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				insertMapping();
			}
		};
		KeyStroke ks_enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK);
		this.registerKeyboardAction(actionListenerAccept, ks_enter, JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		mapdoc.addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				Runnable action = new Runnable() {
					
					@Override
					public void run() {
						try {
							Thread.currentThread().sleep(10);
						} catch (InterruptedException e) {
						}
						
						jButtonInsert.setEnabled(mapdoc.isValidQuery());
					}
				};
				SwingUtilities.invokeLater(action);
				
			}
		});

		jButtonCancel.setMnemonic('c');
		jButtonTest.setMnemonic('t');
		jButtonInsert.setMnemonic('i');
	}

	private void insertMapping() {

		String headstring = jTextPaneHead.getText();
		String bodystring = jTextPaneBody.getText();
		CQIE head = parse(headstring);
		if (head != null) {
			parent.setVisible(false);
			parent.dispose();
			MappingController mapcon = controller.getMappingController();
			URI sourceID = selectedSource.getSourceID();
			String id = mapcon.getNextAvailableMappingID(sourceID);
			RDBMSSQLQuery body = new RDBMSSQLQuery(bodystring);
			RDBMSOBDAMappingAxiom mapping = new RDBMSOBDAMappingAxiom(id, body, head);
			try {
				mapcon.insertMapping(sourceID, mapping);
			} catch (DuplicateMappingException e) {
				JOptionPane.showMessageDialog(null, "Error while inserting mapping.\n " + e.getMessage()
						+ "\nPlease refer to the log file for more information.");
				log.error("Error while counting tuples.", e);
			}
		}
	}
	
	

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jButtonInsert = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jLabelMappingHead = new javax.swing.JLabel();
        jLabelMappingBody = new javax.swing.JLabel();
        jButtonTest = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPaneHead = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPaneBody = new javax.swing.JTextPane();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createTitledBorder("Create Mapping"));
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButtonInsert.setText("Insert");
        jPanel1.add(jButtonInsert);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jPanel1.add(jButtonCancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jPanel1, gridBagConstraints);

        jLabelMappingHead.setText("Mapping Head:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jLabelMappingHead, gridBagConstraints);

        jLabelMappingBody.setText("Mapping Body");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(7, 4, 4, 4);
        add(jLabelMappingBody, gridBagConstraints);

        jButtonTest.setText("Test");
        jButtonTest.setActionCommand("Test SQL query");
        jButtonTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTestActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jButtonTest, gridBagConstraints);

        jTextPaneHead.setToolTipText("Write the query that will be the head of the mapping. \\nThis is a conjunctive query, possibly with function simbols to create object uris from the data of the databse. \\n For example: obdap:q($id) :- Person(individual-uri($id))");
        jScrollPane1.setViewportView(jTextPaneHead);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jScrollPane2.setViewportView(jTextPaneBody);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane2, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

	private void jButtonTestActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonTestActionPerformed
		final JDialog resultquery = new JDialog();
		SQLQueryPanel query_panel = new SQLQueryPanel(selectedSource, jTextPaneBody.getText());

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		panel.add(query_panel, gridBagConstraints);

		resultquery.setContentPane(panel);
		resultquery.pack();
		resultquery.setLocationRelativeTo(null);
		resultquery.setVisible(true);
		resultquery.setTitle("Query Results");
	}// GEN-LAST:event_jButtonTestActionPerformed

	private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonCancelActionPerformed
		parent.setVisible(false);
		parent.dispose();
	}// GEN-LAST:event_jButtonCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonInsert;
    private javax.swing.JButton jButtonTest;
    private javax.swing.JLabel jLabelMappingBody;
    private javax.swing.JLabel jLabelMappingHead;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPaneBody;
    private javax.swing.JTextPane jTextPaneHead;
    // End of variables declaration//GEN-END:variables

	private CQIE parse(String query) {
		CQIE cq = null;
		try {
			String input = prepareQuery(query);
			DatalogProgram dp = datalogParser.parse(input);
			if (dp.getRules().size() > 0) {
				cq = dp.getRules().get(0);
			}
		} catch (RecognitionException e) {
			log.warn(e.getMessage());
		}
		return cq;
	}

	private String prepareQuery(String input) {
		String query = "";
		DatalogQueryHelper queryHelper = new DatalogQueryHelper(controller.getPrefixManager());

		String[] atoms = input.split(DatalogQueryHelper.DATALOG_IMPLY_SYMBOL, 2);
		if (atoms.length == 1) // if no head
			input = queryHelper.getDefaultHead() + " " + DatalogQueryHelper.DATALOG_IMPLY_SYMBOL + " " + input;

		query += queryHelper.getPrefixes() + input;

		return query;
	}

	@Override
	public void datasourceChanged(DataSource oldSource, DataSource newSource) {
		selectedSource = newSource;
	}
}
