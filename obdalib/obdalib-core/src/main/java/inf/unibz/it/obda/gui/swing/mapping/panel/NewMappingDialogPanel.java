/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package inf.unibz.it.obda.gui.swing.mapping.panel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.api.controller.exception.DuplicateMappingException;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.datasource.DatasourceSelectorListener;
import inf.unibz.it.obda.gui.swing.datasource.panels.DatasourceSelector;
import inf.unibz.it.obda.gui.swing.datasource.panels.SQLQueryPanel;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferences;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.text.StyleContext;

import org.antlr.runtime.RecognitionException;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.DatalogProgram;
import org.obda.query.tools.parser.DatalogProgramParser;
import org.obda.query.tools.parser.DatalogQueryHelper;
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
public class NewMappingDialogPanel extends javax.swing.JPanel implements DatasourceSelectorListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4351696247473906680L;
	private APIController controller = null;
	private DatalogProgramParser datalogParser = new DatalogProgramParser();
	private MappingManagerPreferences preferences = null;
	private DataSource selectedSource = null;
	private JDialog parent = null;
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
    /** Creates new form NewMappingDialogPanel */
    public NewMappingDialogPanel(APIController apic, OBDAPreferences pref, JDialog parent) {
        controller = apic;
        preferences = pref.getMappingsPreference();
        this.parent = parent;
    	initComponents();
    	init();
    }

    private void init(){
    	
    	MappingStyledDocument mapdoc = new MappingStyledDocument(new StyleContext(), controller, preferences);
    	jTextPaneHead.setDocument(mapdoc);
    	
    	jButtonInsert.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				insertMapping();
				
			}
		});
    	
    	jPanel1.setLayout(new GridBagLayout());
    	
    	 JLabel label = new JLabel("Select datasource: ");
    	 label.setBackground(new java.awt.Color(153, 153, 153));
    	 label.setFont(new java.awt.Font("Arial", 1, 11));
    	 label.setForeground(new java.awt.Color(153, 153, 153));
    	 label.setPreferredSize(new Dimension(119,14));
    	    
    	 GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
    	 gridBagConstraints.gridx = 0;
    	 gridBagConstraints.gridy = 0;
    	 gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    	 gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    	 jPanel1.add(label, gridBagConstraints);
    	 
    	 Vector<DataSource> vecDatasource = 
    	        new Vector<DataSource>(controller.getDatasourcesController().getAllSources().values());
    	 DatasourceSelector datasourceSelector = new DatasourceSelector(vecDatasource);
    	 datasourceSelector.addDatasourceListListener(this);
    	 
    	 gridBagConstraints = new java.awt.GridBagConstraints();
    	 gridBagConstraints.gridx = 1;
    	 gridBagConstraints.gridy = 0;
    	 gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    	 gridBagConstraints.weightx = 1.0;
    	 gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    	 jPanel1.add(datasourceSelector, gridBagConstraints);
    	 
    	 ActionListener actionListenerCancel = new ActionListener() {
       	  public void actionPerformed(ActionEvent actionEvent) {
       	     parent.setVisible(false);
       	     parent.dispose();
       	  }
       	};
       	KeyStroke ks_ecape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0);
       	this.registerKeyboardAction(actionListenerCancel, ks_ecape, JComponent.WHEN_IN_FOCUSED_WINDOW);
       
       
       	ActionListener actionListenerAccept = new ActionListener() {
     	  public void actionPerformed(ActionEvent actionEvent) {
     	     insertMapping();
     	  }
     	};
     	KeyStroke ks_enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,KeyEvent.CTRL_DOWN_MASK);
     	this.registerKeyboardAction(actionListenerAccept, ks_enter, JComponent.WHEN_IN_FOCUSED_WINDOW);
     	
     	jButtonCancel.setMnemonic('c');
     	jButtonTest.setMnemonic('t');
     	jButtonInsert.setMnemonic('i');
    }
    
    private void insertMapping(){
    	
    	String headstring = jTextPaneHead.getText();
    	String bodystring = jTextPaneBody.getText();
    	if(selectedSource == null){
    		JOptionPane.showMessageDialog(null, "Please select a data source first.");
    		return;
    	}
    	CQIE head = parse(headstring);
    	if(head != null){
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
				JOptionPane.showMessageDialog(null, "Error while inserting mapping.\n "+e.getMessage()+ "\nPlease refer to the log file for more information.");
				log.error("Error while counting tuples.",e);
			}
    	}		
    }
    
 
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabelMappingHead = new javax.swing.JLabel();
        jLabelMappingBody = new javax.swing.JLabel();
        jButtonTest = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jButtonInsert = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPaneHead = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPaneBody = new javax.swing.JTextPane();

        setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 392, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jPanel1, gridBagConstraints);

        jLabelMappingHead.setText("Mapping Head");
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
        jButtonTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTestActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jButtonTest, gridBagConstraints);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jButtonCancel, gridBagConstraints);

        jButtonInsert.setText("Insert");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jButtonInsert, gridBagConstraints);

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
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTestActionPerformed
    	 final JDialog resultquery = new JDialog();
    	 SQLQueryPanel query_panel = new SQLQueryPanel(selectedSource, jTextPaneBody.getText());
    	 resultquery.setLocationRelativeTo(null);
    	 resultquery.add(query_panel);
    	 resultquery.pack();
    	 resultquery.setVisible(true);
    	 resultquery.setTitle("Query Results");
    }//GEN-LAST:event_jButtonTestActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
       parent.setVisible(false);
       parent.dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed


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
			if(dp.getRules().size() > 0){
				cq = dp.getRules().get(0);
			}
		}
		catch (RecognitionException e) {
			log.warn(e.getMessage());
		}
		return cq;
	}
	
	private String prepareQuery(String input) {
		String query = "";
		DatalogQueryHelper queryHelper =
			new DatalogQueryHelper(controller.getIOManager().getPrefixManager());

		String[] atoms = input.split(DatalogQueryHelper.DATALOG_IMPLY_SYMBOL, 2); 
		if (atoms.length == 1)  // if no head
			input = queryHelper.getDefaultHead()+ " " + DatalogQueryHelper.DATALOG_IMPLY_SYMBOL + " " +
			 	input;

		// Append the prefixes
		query = queryHelper.getPrefixes() + input;
		
		return query;
	}

	@Override
	public void datasourceChanged(DataSource oldSource, DataSource newSource) {
		selectedSource = newSource;		
	}
}
