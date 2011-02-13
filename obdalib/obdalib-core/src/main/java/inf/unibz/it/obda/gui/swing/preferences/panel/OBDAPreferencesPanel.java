package inf.unibz.it.obda.gui.swing.preferences.panel;

import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferences;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/*
 * OBDAPreferencesPanel.java
 *
 * Created on Jul 31, 2009, 3:06:37 PM
 */

/**
 *
 * @author Manfred Gerstgrasser
 */
public class OBDAPreferencesPanel extends javax.swing.JPanel {
	
	private static final String add = "add.Mapping";
	private static final String delete = "delete.Mapping";
	private static final String editHead = "edit.Mapping.Head";
	private static final String editBody = "edit.Mapping.Body";
	private static final String editId = "edit.Mapping.id";
	
	private MappingManagerPreferences pref = null;
	private HashMap<String, KeyStroke> shortCuts = new HashMap<String, KeyStroke>();
	
	/**
	 * The constructor 
	 */
    public OBDAPreferencesPanel(OBDAPreferences preference) {
    	pref = preference.getMappingsPreference();
        initComponents();
        addListener();
        applyPreferences();
    }

    private boolean isKeyStrokeAlreadyAssigned(KeyStroke stroke){
    	
    	return shortCuts.containsValue(stroke);
    }
    
    private void addListener(){
//    	jButtonBodyPropertyColour1.addActionListener(new ActionListener(){
//
//			public void actionPerformed(ActionEvent arg0) {
//				ColorChooser cc = new ColorChooser(jButtonBodyPropertyColour1, MappingManagerPreferences.MAPPING_BODY_COLOR);
//				cc.setVisible(true);
//			}
//    		
//    	});
    	
    	cmdClassColor.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(cmdClassColor, MappingManagerPreferences.CLASS_COLOR);
				cc.setVisible(true);
			}
    		
    	});
    	
    	cmdDataPropertyColor.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(cmdDataPropertyColor, MappingManagerPreferences.DATAPROPERTY_COLOR);
				cc.setVisible(true);
			}
    		
    	});
    	
    	cmdFunctorColor.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(cmdFunctorColor, MappingManagerPreferences.FUCNTOR_COLOR);
				cc.setVisible(true);
			}
    		
    	});
    	
//    	jButtonIDColour2.addActionListener(new ActionListener(){
//
//			public void actionPerformed(ActionEvent arg0) {
//				ColorChooser cc = new ColorChooser(jButtonIDColour2, MappingManagerPreferences.MAPPING_ID_COLOR);
//				cc.setVisible(true);
//			}
//    		
//    	});
    	
    	cmdObjectPropertyColor.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(cmdObjectPropertyColor, MappingManagerPreferences.OBJECTPROPTERTY_COLOR);
				cc.setVisible(true);
			}
    		
    	});
    	
    	cmdParameterColor.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(cmdParameterColor, MappingManagerPreferences.PARAMETER_COLOR);
				cc.setVisible(true);
			}
    		
    	});
    	
    	cmdVariableColor.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(cmdVariableColor, MappingManagerPreferences.VARIABLE_COLOR);
				cc.setVisible(true);
			}
    		
    	});
    	
    	cmdInvalidQueryColor.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(cmdInvalidQueryColor, MappingManagerPreferences.INVALIDQUERY_COLOR);
				cc.setVisible(true);
			}
    		
    	});
    	String aux = pref.getShortCut(add);
    	KeyStroke ks = KeyStroke.getKeyStroke(aux);
    	lblAddMappingKey.setText(KeyEvent.getKeyModifiersText(ks.getModifiers()) + " + "+ KeyEvent.getKeyText(ks.getKeyCode()));
    	lblAddMappingKey.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				
				lblAddMappingKey.setText("");
				lblAddMappingKey.requestFocus();
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
    		
    	});
    	lblAddMappingKey.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
				
				int mod = e.getModifiers();
				int key = e.getKeyCode();
				if(key == KeyEvent.VK_CONTROL || key ==KeyEvent.VK_CANCEL || key ==KeyEvent.VK_ALT || key ==KeyEvent.VK_ALT_GRAPH
						|| key ==KeyEvent.VK_SHIFT || key ==KeyEvent.VK_ESCAPE){
					return;
				}
				KeyStroke stroke = KeyStroke.getKeyStroke(key, mod);
				System.out.println(stroke.toString());
				if(!isKeyStrokeAlreadyAssigned(stroke)){
					shortCuts.put(add, stroke);
				    lblAddMappingKey.setText(KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + "+ KeyEvent.getKeyText(stroke.getKeyCode()));
					lblAddMappingKey.setToolTipText(stroke.toString());
					pref.setShortcut(add, stroke.toString());
				}else{
					KeyStroke oldValue = shortCuts.get(add);
					if(oldValue != null){
						lblAddMappingKey.setText(KeyEvent.getKeyModifiersText(oldValue.getModifiers()) + " + "+ KeyEvent.getKeyText(oldValue.getKeyCode()));
					}
					JOptionPane.showMessageDialog(null, "Key stroke already assigned. Please choose an other combination.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}

			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
    	});
    	
//    	String aux1 = pref.getShortCut(delete);
//    	KeyStroke ks1 = KeyStroke.getKeyStroke(aux1);
//    	jLabelDeleteKey.setText(KeyEvent.getKeyModifiersText(ks1.getModifiers()) + " + "+ KeyEvent.getKeyText(ks1.getKeyCode()));
//    	jLabelDeleteKey.addMouseListener(new MouseListener(){
//
//			public void mouseClicked(MouseEvent e) {
//				
//				jLabelDeleteKey.setText("");
//				jLabelDeleteKey.requestFocus();
//			}
//			public void mouseEntered(MouseEvent e) {}
//			public void mouseExited(MouseEvent e) {}
//			public void mousePressed(MouseEvent e) {}
//			public void mouseReleased(MouseEvent e) {}
//    		
//    	});
//    	jLabelDeleteKey.addKeyListener(new KeyListener(){
//
//			public void keyPressed(KeyEvent e) {
//				int mod = e.getModifiers();
//				int key = e.getKeyCode();
//				if(key == KeyEvent.VK_CONTROL || key ==KeyEvent.VK_CANCEL || key ==KeyEvent.VK_ALT || key ==KeyEvent.VK_ALT_GRAPH
//						|| key ==KeyEvent.VK_SHIFT || key ==KeyEvent.VK_ESCAPE){
//					return;
//				}
//				KeyStroke stroke = KeyStroke.getKeyStroke(key, mod);
//				System.out.println(stroke.toString());
//				if(!isKeyStrokeAlreadyAssigned(stroke)){
//					shortCuts.put(delete, stroke);
//					jLabelDeleteKey.setText(KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + "+ KeyEvent.getKeyText(stroke.getKeyCode()));
//					jLabelDeleteKey.setToolTipText(stroke.toString());
//					pref.setShortcut(delete, stroke.toString());
//				}else{
//					KeyStroke oldValue = shortCuts.get(delete);
//					if(oldValue != null){
//						jLabelDeleteKey.setText(KeyEvent.getKeyModifiersText(oldValue.getModifiers()) + " + "+ KeyEvent.getKeyText(oldValue.getKeyCode()));
//					}
//					JOptionPane.showMessageDialog(null, "Key stroke already assigned. Please choose an other combination.", "ERROR", JOptionPane.ERROR_MESSAGE);
//				}
//			}
//
//			public void keyReleased(KeyEvent e) {}
//			public void keyTyped(KeyEvent e) {}
//    	});
    	
    	String aux2 = pref.getShortCut(editBody);
    	KeyStroke ks2 = KeyStroke.getKeyStroke(aux2);
    	lblEditMappingBodyKey.setText(KeyEvent.getKeyModifiersText(ks2.getModifiers()) + " + "+ KeyEvent.getKeyText(ks2.getKeyCode()));
    	lblEditMappingBodyKey.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				
				lblEditMappingBodyKey.setText("");
				lblEditMappingBodyKey.requestFocus();
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
    		
    	});
    	lblEditMappingBodyKey.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
				int mod = e.getModifiers();
				int key = e.getKeyCode();
				if(key == KeyEvent.VK_CONTROL || key ==KeyEvent.VK_CANCEL || key ==KeyEvent.VK_ALT || key ==KeyEvent.VK_ALT_GRAPH
						|| key ==KeyEvent.VK_SHIFT || key ==KeyEvent.VK_ESCAPE){
					return;
				}
				KeyStroke stroke = KeyStroke.getKeyStroke(key, mod);
				System.out.println(stroke.toString());
				if(!isKeyStrokeAlreadyAssigned(stroke)){
					shortCuts.put(editBody, stroke);
					lblEditMappingBodyKey.setText(KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + "+ KeyEvent.getKeyText(stroke.getKeyCode()));
					lblEditMappingBodyKey.setToolTipText(stroke.toString());
					pref.setShortcut(editBody, stroke.toString());
				}else{
					KeyStroke oldValue = shortCuts.get(editBody);
					if(oldValue != null){
						lblEditMappingBodyKey.setText(KeyEvent.getKeyModifiersText(oldValue.getModifiers()) + " + "+ KeyEvent.getKeyText(oldValue.getKeyCode()));
					}
					JOptionPane.showMessageDialog(null, "Key stroke already assigned. Please choose an other combination.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}

			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
    	});
    	
    	String aux3 = pref.getShortCut(editHead);
    	KeyStroke ks3 = KeyStroke.getKeyStroke(aux3);
    	lblEditMappingHeadKey.setText(KeyEvent.getKeyModifiersText(ks3.getModifiers()) + " + "+ KeyEvent.getKeyText(ks3.getKeyCode()));
    	lblEditMappingHeadKey.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				
				lblEditMappingHeadKey.setText("");
				lblEditMappingHeadKey.requestFocus();
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
    		
    	});
    	lblEditMappingHeadKey.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
				int mod = e.getModifiers();
				int key = e.getKeyCode();
				if(key == KeyEvent.VK_CONTROL || key ==KeyEvent.VK_CANCEL || key ==KeyEvent.VK_ALT || key ==KeyEvent.VK_ALT_GRAPH
						|| key ==KeyEvent.VK_SHIFT || key ==KeyEvent.VK_ESCAPE){
					return;
				}
				KeyStroke stroke = KeyStroke.getKeyStroke(key, mod);
				System.out.println(stroke.toString());
				if(!isKeyStrokeAlreadyAssigned(stroke)){
					shortCuts.put(editHead, stroke);
					lblEditMappingHeadKey.setText(KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + "+ KeyEvent.getKeyText(stroke.getKeyCode()));
					lblEditMappingHeadKey.setToolTipText(stroke.toString());
					pref.setShortcut(editHead, stroke.toString());
				}else{
					KeyStroke oldValue = shortCuts.get(editHead);
					if(oldValue != null){
						lblEditMappingHeadKey.setText(KeyEvent.getKeyModifiersText(oldValue.getModifiers()) + " + "+ KeyEvent.getKeyText(oldValue.getKeyCode()));
					}
					JOptionPane.showMessageDialog(null, "Key stroke already assigned. Please choose an other combination.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}

			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
    	});
    	
    	String aux4 = pref.getShortCut(editId);
    	KeyStroke ks4 = KeyStroke.getKeyStroke(aux4);
    	lblMappingIdKey.setText(KeyEvent.getKeyModifiersText(ks4.getModifiers()) + " + "+ KeyEvent.getKeyText(ks4.getKeyCode()));
    	lblMappingIdKey.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				
				lblMappingIdKey.setText("");
				lblMappingIdKey.requestFocus();
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
    		
    	});
    	lblMappingIdKey.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
				int mod = e.getModifiers();
				int key = e.getKeyCode();
				if(key == KeyEvent.VK_CONTROL || key ==KeyEvent.VK_CANCEL || key ==KeyEvent.VK_ALT || key ==KeyEvent.VK_ALT_GRAPH
						|| key ==KeyEvent.VK_SHIFT || key ==KeyEvent.VK_ESCAPE){
					return;
				}
				KeyStroke stroke = KeyStroke.getKeyStroke(key, mod);
				System.out.println(stroke.toString());
				if(!isKeyStrokeAlreadyAssigned(stroke)){
					shortCuts.put(editHead, stroke);
					lblMappingIdKey.setText(KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + "+ KeyEvent.getKeyText(stroke.getKeyCode()));
					lblMappingIdKey.setToolTipText(stroke.toString());
					pref.setShortcut(editId, stroke.toString());
				}else{
					KeyStroke oldValue = shortCuts.get(editId);
					if(oldValue != null){
						lblMappingIdKey.setText(KeyEvent.getKeyModifiersText(oldValue.getModifiers()) + " + "+ KeyEvent.getKeyText(oldValue.getKeyCode()));
					}
					JOptionPane.showMessageDialog(null, "Key stroke already assigned. Please choose an other combination.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}

			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
    	});
    }
    
    private void applyPreferences(){
 
    	Color clazz = pref.getColor(MappingManagerPreferences.CLASS_COLOR);
    	cmdClassColor.setBackground(clazz);
    	cmdClassColor.setOpaque(true);
    	cmdClassColor.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	Color dp = pref.getColor(MappingManagerPreferences.DATAPROPERTY_COLOR);
    	cmdDataPropertyColor.setBackground(dp);
    	cmdDataPropertyColor.setOpaque(true);
    	cmdDataPropertyColor.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	Color op = pref.getColor(MappingManagerPreferences.OBJECTPROPTERTY_COLOR);
    	cmdObjectPropertyColor.setBackground(op);
    	cmdObjectPropertyColor.setOpaque(true);
    	cmdObjectPropertyColor.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	Color var = pref.getColor(MappingManagerPreferences.VARIABLE_COLOR);
    	cmdVariableColor.setBackground(var);
    	cmdVariableColor.setOpaque(true);
    	cmdVariableColor.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	Color par = pref.getColor(MappingManagerPreferences.PARAMETER_COLOR);
    	cmdParameterColor.setBackground(par);
    	cmdParameterColor.setOpaque(true);
    	cmdParameterColor.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	Color fun = pref.getColor(MappingManagerPreferences.FUCNTOR_COLOR);
    	cmdFunctorColor.setBackground(fun);
    	cmdFunctorColor.setOpaque(true);
    	cmdFunctorColor.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
//    	Color body = pref.getColor(MappingManagerPreferences.MAPPING_BODY_COLOR);
//    	jButtonBodyPropertyColour1.setBackground(body);
//    	jButtonBodyPropertyColour1.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
//    	Color id = pref.getColor(MappingManagerPreferences.MAPPING_ID_COLOR);
//    	jButtonIDColour2.setBackground(id);
//    	jButtonIDColour2.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	Color iq = pref.getColor(MappingManagerPreferences.INVALIDQUERY_COLOR);
    	cmdInvalidQueryColor.setBackground(iq);
    	cmdInvalidQueryColor.setOpaque(true);
    	cmdInvalidQueryColor.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	
    	String fontClassFam = pref.getFontFamily(MappingManagerPreferences.CLASS_FONTFAMILY);
    	int classSize = pref.getFontSize(MappingManagerPreferences.CLASS_FONTSIZE);
    	cmdClassFont.setText(fontClassFam + ", " + classSize);
    	cmdClassFont.setToolTipText(fontClassFam + ", " + classSize);
    	
    	String fontDP = pref.getFontFamily(MappingManagerPreferences.DATAPROPERTY_FONTFAMILY);
    	int sizeDP = pref.getFontSize(MappingManagerPreferences.DATAPROPERTY_FONTSIZE);
    	cmdDataPropertyFont.setText(fontDP + ", " + sizeDP);
    	cmdDataPropertyFont.setToolTipText(fontDP + ", " + sizeDP);
    	
    	String fontOP = pref.getFontFamily(MappingManagerPreferences.OBJECTPROPTERTY_FONTFAMILY);
    	int sizeOP = pref.getFontSize(MappingManagerPreferences.OBJECTPROPTERTY_FONTSIZE);
    	cmdObjectPropertyFont.setText(fontOP + ", " + sizeOP);
    	cmdObjectPropertyFont.setToolTipText(fontOP + ", " + sizeOP);
    	
    	String fontVar = pref.getFontFamily(MappingManagerPreferences.VARIABLE_FONTFAMILY);
    	int varSize = pref.getFontSize(MappingManagerPreferences.VARIABLE_FONTSIZE);
    	cmdVariableFonr.setText(fontVar + ", " + varSize);
    	cmdVariableFonr.setToolTipText(fontVar + ", " + varSize);
    	
    	String fontPara = pref.getFontFamily(MappingManagerPreferences.PARAMETER_FONTFAMILY);
    	int paraSize = pref.getFontSize(MappingManagerPreferences.PARAMETER_FONTSIZE);
    	cmdParameterFont.setText(fontPara + ", "+ paraSize);
    	cmdParameterFont.setToolTipText(fontPara + ", "+ paraSize);
    	
    	String fontFunc = pref.getFontFamily(MappingManagerPreferences.FUCNTOR_FONTFAMILY);
    	int funcSize = pref.getFontSize(MappingManagerPreferences.FUCNTOR_FONTSIZE);
    	cmdFunctorFont.setText(fontFunc + ", " +funcSize);
    	cmdFunctorFont.setToolTipText(fontFunc + ", " +funcSize);
    	
    	String fontBody = pref.getFontFamily(MappingManagerPreferences.MAPPING_BODY_FONTFAMILY);
    	int bodySize = pref.getFontSize(MappingManagerPreferences.MAPPING_BODY_FONTSIZE);
    	cmdMappingBodyFont.setText(fontBody + ", " + bodySize);
    	cmdMappingBodyFont.setToolTipText(fontBody + ", " + bodySize);
    	
    	String fontID = pref.getFontFamily(MappingManagerPreferences.MAPPING_ID_FONTFAMILY);
    	int idSize = pref.getFontSize(MappingManagerPreferences.MAPPING_ID_FONTSIZE);
    	cmdMappingIdFont.setText(fontID + ", "+ idSize);
    	cmdMappingIdFont.setToolTipText(fontID + ", "+ idSize);
    	
    	String fontIQ= pref.getFontFamily(MappingManagerPreferences.INVALIDQUERY_FONTFAMILY);
    	int iqSize = pref.getFontSize(MappingManagerPreferences.INVALIDQUERY_FONTSIZE);
    	cmdInvalidQueryFont.setText(fontIQ + ", "+ iqSize);
    	cmdInvalidQueryFont.setToolTipText(fontIQ + ", "+ iqSize);
    	
    	String fontDep = pref.getFontFamily(MappingManagerPreferences.DEPENDENCIES_FONTFAMILY);
    	int depSize = pref.getFontSize(MappingManagerPreferences.DEPENDENCIES_FONTSIZE);
    	cmdDependencyFont.setText(fontDep + ", " + depSize);
    	cmdDependencyFont.setToolTipText(fontDep + ", " + depSize);
    	
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

        tabMainPanel = new javax.swing.JTabbedPane();
        pnlDisplayPreferencesTab = new javax.swing.JPanel();
        pnlClassPreferences = new javax.swing.JPanel();
        lblClass = new javax.swing.JLabel();
        cmdClassColor = new javax.swing.JButton();
        cmdClassFont = new javax.swing.JButton();
        lblDataProperty = new javax.swing.JLabel();
        cmdDataPropertyFont = new javax.swing.JButton();
        cmdDataPropertyColor = new javax.swing.JButton();
        lblObjectProperty = new javax.swing.JLabel();
        cmdObjectPropertyFont = new javax.swing.JButton();
        cmdObjectPropertyColor = new javax.swing.JButton();
        pnlVariablePreferences = new javax.swing.JPanel();
        lblParameter = new javax.swing.JLabel();
        cmdParameterColor = new javax.swing.JButton();
        cmdParameterFont = new javax.swing.JButton();
        lblVariable = new javax.swing.JLabel();
        cmdVariableColor = new javax.swing.JButton();
        cmdVariableFonr = new javax.swing.JButton();
        lblFunctors = new javax.swing.JLabel();
        cmdFunctorColor = new javax.swing.JButton();
        cmdFunctorFont = new javax.swing.JButton();
        pnlMappingPreferences = new javax.swing.JPanel();
        lblMappingBody = new javax.swing.JLabel();
        cmdMappingBodyColor = new javax.swing.JButton();
        cmdMappingBodyFont = new javax.swing.JButton();
        lblMappingId = new javax.swing.JLabel();
        cmdMappingIdColor = new javax.swing.JButton();
        cmdMappingIdFont = new javax.swing.JButton();
        pnlDataSourceDependencies = new javax.swing.JPanel();
        cmdDependencyFont = new javax.swing.JButton();
        cmdDependencyColor = new javax.swing.JButton();
        lblDependency = new javax.swing.JLabel();
        pnlInvalidQueries = new javax.swing.JPanel();
        cmdInvalidQueryFont = new javax.swing.JButton();
        cmdInvalidQueryColor = new javax.swing.JButton();
        lblInvalidQuery = new javax.swing.JLabel();
        pnlEditingShortcutTab = new javax.swing.JPanel();
        pnlShortcutSettings = new javax.swing.JPanel();
        lblAddMapping = new javax.swing.JLabel();
        lblAddMappingKey = new javax.swing.JLabel();
        lblDeleteMapping = new javax.swing.JLabel();
        lblDeleteMappingKey = new javax.swing.JLabel();
        lblEditMappingHead = new javax.swing.JLabel();
        lblEditMappingHeadKey = new javax.swing.JLabel();
        lblEditMappingBody = new javax.swing.JLabel();
        lblEditMappingBodyKey = new javax.swing.JLabel();
        lblEditMappingId = new javax.swing.JLabel();
        lblMappingIdKey = new javax.swing.JLabel();
        lblInfo = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(520, 600));
        setPreferredSize(new java.awt.Dimension(520, 600));
        setLayout(new java.awt.BorderLayout());

        tabMainPanel.setMinimumSize(new java.awt.Dimension(200, 200));
        tabMainPanel.setOpaque(true);
        tabMainPanel.setPreferredSize(new java.awt.Dimension(500, 800));

        pnlDisplayPreferencesTab.setMinimumSize(new java.awt.Dimension(485, 560));
        pnlDisplayPreferencesTab.setPreferredSize(new java.awt.Dimension(485, 560));
        pnlDisplayPreferencesTab.setLayout(new java.awt.GridBagLayout());

        pnlClassPreferences.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.lightGray), "OWL Entities preferences"));
        pnlClassPreferences.setMinimumSize(new java.awt.Dimension(475, 110));
        pnlClassPreferences.setPreferredSize(new java.awt.Dimension(475, 110));
        pnlClassPreferences.setLayout(new java.awt.GridBagLayout());

        lblClass.setText("Class:\n\n\n");
        lblClass.setMaximumSize(new java.awt.Dimension(100, 20));
        lblClass.setMinimumSize(new java.awt.Dimension(100, 20));
        lblClass.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlClassPreferences.add(lblClass, gridBagConstraints);

        cmdClassColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        cmdClassColor.setMaximumSize(new java.awt.Dimension(90, 17));
        cmdClassColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdClassColor.setPreferredSize(new java.awt.Dimension(60, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        pnlClassPreferences.add(cmdClassColor, gridBagConstraints);

        cmdClassFont.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        cmdClassFont.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdClassFont.setMinimumSize(new java.awt.Dimension(90, 17));
        cmdClassFont.setPreferredSize(new java.awt.Dimension(120, 17));
        cmdClassFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdClassFontActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        pnlClassPreferences.add(cmdClassFont, gridBagConstraints);

        lblDataProperty.setText("Data Property:");
        lblDataProperty.setMaximumSize(new java.awt.Dimension(100, 20));
        lblDataProperty.setMinimumSize(new java.awt.Dimension(100, 20));
        lblDataProperty.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlClassPreferences.add(lblDataProperty, gridBagConstraints);

        cmdDataPropertyFont.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        cmdDataPropertyFont.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdDataPropertyFont.setMinimumSize(new java.awt.Dimension(90, 17));
        cmdDataPropertyFont.setPreferredSize(new java.awt.Dimension(120, 17));
        cmdDataPropertyFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdDataPropertyFontActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        pnlClassPreferences.add(cmdDataPropertyFont, gridBagConstraints);

        cmdDataPropertyColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdDataPropertyColor.setMaximumSize(new java.awt.Dimension(90, 17));
        cmdDataPropertyColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdDataPropertyColor.setPreferredSize(new java.awt.Dimension(60, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        pnlClassPreferences.add(cmdDataPropertyColor, gridBagConstraints);

        lblObjectProperty.setText("Object Property:");
        lblObjectProperty.setMaximumSize(new java.awt.Dimension(100, 20));
        lblObjectProperty.setMinimumSize(new java.awt.Dimension(100, 20));
        lblObjectProperty.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlClassPreferences.add(lblObjectProperty, gridBagConstraints);

        cmdObjectPropertyFont.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdObjectPropertyFont.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdObjectPropertyFont.setMinimumSize(new java.awt.Dimension(90, 17));
        cmdObjectPropertyFont.setPreferredSize(new java.awt.Dimension(120, 17));
        cmdObjectPropertyFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdObjectPropertyFontActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        pnlClassPreferences.add(cmdObjectPropertyFont, gridBagConstraints);

        cmdObjectPropertyColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdObjectPropertyColor.setMaximumSize(new java.awt.Dimension(90, 17));
        cmdObjectPropertyColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdObjectPropertyColor.setPreferredSize(new java.awt.Dimension(60, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        pnlClassPreferences.add(cmdObjectPropertyColor, gridBagConstraints);

        pnlDisplayPreferencesTab.add(pnlClassPreferences, new java.awt.GridBagConstraints());

        pnlVariablePreferences.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.lightGray), "Variable preferences"));
        pnlVariablePreferences.setMinimumSize(new java.awt.Dimension(475, 110));
        pnlVariablePreferences.setPreferredSize(new java.awt.Dimension(475, 110));
        pnlVariablePreferences.setLayout(new java.awt.GridBagLayout());

        lblParameter.setText("Parameter:");
        lblParameter.setMaximumSize(new java.awt.Dimension(100, 20));
        lblParameter.setMinimumSize(new java.awt.Dimension(100, 20));
        lblParameter.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlVariablePreferences.add(lblParameter, gridBagConstraints);

        cmdParameterColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        cmdParameterColor.setMaximumSize(new java.awt.Dimension(90, 17));
        cmdParameterColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdParameterColor.setPreferredSize(new java.awt.Dimension(60, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlVariablePreferences.add(cmdParameterColor, gridBagConstraints);

        cmdParameterFont.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdParameterFont.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdParameterFont.setMinimumSize(new java.awt.Dimension(90, 17));
        cmdParameterFont.setPreferredSize(new java.awt.Dimension(120, 17));
        cmdParameterFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdParameterFontActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        pnlVariablePreferences.add(cmdParameterFont, gridBagConstraints);

        lblVariable.setText("Variable:");
        lblVariable.setMaximumSize(new java.awt.Dimension(100, 20));
        lblVariable.setMinimumSize(new java.awt.Dimension(100, 20));
        lblVariable.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlVariablePreferences.add(lblVariable, gridBagConstraints);

        cmdVariableColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        cmdVariableColor.setMaximumSize(new java.awt.Dimension(90, 17));
        cmdVariableColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdVariableColor.setPreferredSize(new java.awt.Dimension(60, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlVariablePreferences.add(cmdVariableColor, gridBagConstraints);

        cmdVariableFonr.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        cmdVariableFonr.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdVariableFonr.setMinimumSize(new java.awt.Dimension(90, 17));
        cmdVariableFonr.setPreferredSize(new java.awt.Dimension(120, 17));
        cmdVariableFonr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdVariableFonrActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        pnlVariablePreferences.add(cmdVariableFonr, gridBagConstraints);

        lblFunctors.setText("Functor:");
        lblFunctors.setMaximumSize(new java.awt.Dimension(100, 20));
        lblFunctors.setMinimumSize(new java.awt.Dimension(100, 20));
        lblFunctors.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlVariablePreferences.add(lblFunctors, gridBagConstraints);

        cmdFunctorColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdFunctorColor.setMaximumSize(new java.awt.Dimension(90, 17));
        cmdFunctorColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdFunctorColor.setPreferredSize(new java.awt.Dimension(60, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlVariablePreferences.add(cmdFunctorColor, gridBagConstraints);

        cmdFunctorFont.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdFunctorFont.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdFunctorFont.setMinimumSize(new java.awt.Dimension(90, 17));
        cmdFunctorFont.setPreferredSize(new java.awt.Dimension(120, 17));
        cmdFunctorFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdFunctorFontActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        pnlVariablePreferences.add(cmdFunctorFont, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        pnlDisplayPreferencesTab.add(pnlVariablePreferences, gridBagConstraints);

        pnlMappingPreferences.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.lightGray), "Mapping preferences"));
        pnlMappingPreferences.setMinimumSize(new java.awt.Dimension(475, 90));
        pnlMappingPreferences.setPreferredSize(new java.awt.Dimension(475, 90));
        pnlMappingPreferences.setLayout(new java.awt.GridBagLayout());

        lblMappingBody.setText("Mappings Body:");
        lblMappingBody.setMaximumSize(new java.awt.Dimension(100, 20));
        lblMappingBody.setMinimumSize(new java.awt.Dimension(100, 20));
        lblMappingBody.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlMappingPreferences.add(lblMappingBody, gridBagConstraints);

        cmdMappingBodyColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdMappingBodyColor.setEnabled(false);
        cmdMappingBodyColor.setMaximumSize(new java.awt.Dimension(90, 17));
        cmdMappingBodyColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdMappingBodyColor.setPreferredSize(new java.awt.Dimension(60, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlMappingPreferences.add(cmdMappingBodyColor, gridBagConstraints);

        cmdMappingBodyFont.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdMappingBodyFont.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdMappingBodyFont.setMinimumSize(new java.awt.Dimension(90, 17));
        cmdMappingBodyFont.setPreferredSize(new java.awt.Dimension(120, 17));
        cmdMappingBodyFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdMappingBodyFontActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        pnlMappingPreferences.add(cmdMappingBodyFont, gridBagConstraints);

        lblMappingId.setText("Mappings ID:");
        lblMappingId.setMaximumSize(new java.awt.Dimension(100, 20));
        lblMappingId.setMinimumSize(new java.awt.Dimension(100, 20));
        lblMappingId.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlMappingPreferences.add(lblMappingId, gridBagConstraints);

        cmdMappingIdColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdMappingIdColor.setEnabled(false);
        cmdMappingIdColor.setMaximumSize(new java.awt.Dimension(90, 17));
        cmdMappingIdColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdMappingIdColor.setPreferredSize(new java.awt.Dimension(60, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlMappingPreferences.add(cmdMappingIdColor, gridBagConstraints);

        cmdMappingIdFont.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdMappingIdFont.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdMappingIdFont.setMinimumSize(new java.awt.Dimension(90, 17));
        cmdMappingIdFont.setPreferredSize(new java.awt.Dimension(120, 17));
        cmdMappingIdFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdMappingIdFontActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        pnlMappingPreferences.add(cmdMappingIdFont, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        pnlDisplayPreferencesTab.add(pnlMappingPreferences, gridBagConstraints);

        pnlDataSourceDependencies.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.lightGray), "Datasource dependencies"));
        pnlDataSourceDependencies.setMinimumSize(new java.awt.Dimension(475, 70));
        pnlDataSourceDependencies.setPreferredSize(new java.awt.Dimension(475, 70));
        pnlDataSourceDependencies.setLayout(new java.awt.GridBagLayout());

        cmdDependencyFont.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdDependencyFont.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdDependencyFont.setMinimumSize(new java.awt.Dimension(90, 17));
        cmdDependencyFont.setPreferredSize(new java.awt.Dimension(120, 17));
        cmdDependencyFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdDependencyFontActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        pnlDataSourceDependencies.add(cmdDependencyFont, gridBagConstraints);

        cmdDependencyColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        cmdDependencyColor.setEnabled(false);
        cmdDependencyColor.setMaximumSize(new java.awt.Dimension(90, 17));
        cmdDependencyColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdDependencyColor.setPreferredSize(new java.awt.Dimension(60, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlDataSourceDependencies.add(cmdDependencyColor, gridBagConstraints);

        lblDependency.setText("Dependency:");
        lblDependency.setMaximumSize(new java.awt.Dimension(100, 20));
        lblDependency.setMinimumSize(new java.awt.Dimension(100, 20));
        lblDependency.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlDataSourceDependencies.add(lblDependency, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        pnlDisplayPreferencesTab.add(pnlDataSourceDependencies, gridBagConstraints);

        pnlInvalidQueries.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.lightGray), "Invalid queries"));
        pnlInvalidQueries.setMinimumSize(new java.awt.Dimension(475, 70));
        pnlInvalidQueries.setPreferredSize(new java.awt.Dimension(475, 70));
        pnlInvalidQueries.setLayout(new java.awt.GridBagLayout());

        cmdInvalidQueryFont.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdInvalidQueryFont.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdInvalidQueryFont.setMinimumSize(new java.awt.Dimension(90, 17));
        cmdInvalidQueryFont.setPreferredSize(new java.awt.Dimension(120, 17));
        cmdInvalidQueryFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdInvalidQueryFontActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        pnlInvalidQueries.add(cmdInvalidQueryFont, gridBagConstraints);

        cmdInvalidQueryColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdInvalidQueryColor.setMaximumSize(new java.awt.Dimension(90, 17));
        cmdInvalidQueryColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdInvalidQueryColor.setPreferredSize(new java.awt.Dimension(60, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlInvalidQueries.add(cmdInvalidQueryColor, gridBagConstraints);

        lblInvalidQuery.setText("Query:");
        lblInvalidQuery.setMaximumSize(new java.awt.Dimension(100, 20));
        lblInvalidQuery.setMinimumSize(new java.awt.Dimension(100, 20));
        lblInvalidQuery.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlInvalidQueries.add(lblInvalidQuery, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        pnlDisplayPreferencesTab.add(pnlInvalidQueries, gridBagConstraints);

        tabMainPanel.addTab("Display Preference", pnlDisplayPreferencesTab);

        pnlEditingShortcutTab.setMinimumSize(new java.awt.Dimension(212, 150));
        pnlEditingShortcutTab.setPreferredSize(new java.awt.Dimension(450, 150));
        pnlEditingShortcutTab.setLayout(new java.awt.GridBagLayout());

        pnlShortcutSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.lightGray), "Shortcut Settings"));
        pnlShortcutSettings.setMinimumSize(new java.awt.Dimension(475, 165));
        pnlShortcutSettings.setPreferredSize(new java.awt.Dimension(475, 165));
        pnlShortcutSettings.setLayout(new java.awt.GridBagLayout());

        lblAddMapping.setText("Add Mapping: *");
        lblAddMapping.setMaximumSize(new java.awt.Dimension(100, 20));
        lblAddMapping.setMinimumSize(new java.awt.Dimension(100, 20));
        lblAddMapping.setPreferredSize(new java.awt.Dimension(50, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 170;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlShortcutSettings.add(lblAddMapping, gridBagConstraints);

        lblAddMappingKey.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblAddMappingKey.setMaximumSize(new java.awt.Dimension(120, 20));
        lblAddMappingKey.setMinimumSize(new java.awt.Dimension(120, 20));
        lblAddMappingKey.setPreferredSize(new java.awt.Dimension(120, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        pnlShortcutSettings.add(lblAddMappingKey, gridBagConstraints);

        lblDeleteMapping.setText("Delete Mapping: *");
        lblDeleteMapping.setMaximumSize(new java.awt.Dimension(100, 20));
        lblDeleteMapping.setMinimumSize(new java.awt.Dimension(100, 20));
        lblDeleteMapping.setPreferredSize(new java.awt.Dimension(50, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 170;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlShortcutSettings.add(lblDeleteMapping, gridBagConstraints);

        lblDeleteMappingKey.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblDeleteMappingKey.setEnabled(false);
        lblDeleteMappingKey.setMaximumSize(new java.awt.Dimension(120, 20));
        lblDeleteMappingKey.setMinimumSize(new java.awt.Dimension(120, 20));
        lblDeleteMappingKey.setPreferredSize(new java.awt.Dimension(120, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        pnlShortcutSettings.add(lblDeleteMappingKey, gridBagConstraints);

        lblEditMappingHead.setText("Edit Mapping Head: *");
        lblEditMappingHead.setMaximumSize(new java.awt.Dimension(100, 20));
        lblEditMappingHead.setMinimumSize(new java.awt.Dimension(100, 20));
        lblEditMappingHead.setPreferredSize(new java.awt.Dimension(50, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 170;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlShortcutSettings.add(lblEditMappingHead, gridBagConstraints);

        lblEditMappingHeadKey.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblEditMappingHeadKey.setMaximumSize(new java.awt.Dimension(120, 20));
        lblEditMappingHeadKey.setMinimumSize(new java.awt.Dimension(120, 20));
        lblEditMappingHeadKey.setPreferredSize(new java.awt.Dimension(120, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        pnlShortcutSettings.add(lblEditMappingHeadKey, gridBagConstraints);

        lblEditMappingBody.setText("Edit Mapping Body: *");
        lblEditMappingBody.setMaximumSize(new java.awt.Dimension(100, 20));
        lblEditMappingBody.setMinimumSize(new java.awt.Dimension(100, 20));
        lblEditMappingBody.setPreferredSize(new java.awt.Dimension(50, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 170;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlShortcutSettings.add(lblEditMappingBody, gridBagConstraints);

        lblEditMappingBodyKey.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblEditMappingBodyKey.setMaximumSize(new java.awt.Dimension(120, 20));
        lblEditMappingBodyKey.setMinimumSize(new java.awt.Dimension(120, 20));
        lblEditMappingBodyKey.setPreferredSize(new java.awt.Dimension(120, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        pnlShortcutSettings.add(lblEditMappingBodyKey, gridBagConstraints);

        lblEditMappingId.setText("Edit Mapping ID: *");
        lblEditMappingId.setMaximumSize(new java.awt.Dimension(100, 20));
        lblEditMappingId.setMinimumSize(new java.awt.Dimension(100, 20));
        lblEditMappingId.setPreferredSize(new java.awt.Dimension(50, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 170;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlShortcutSettings.add(lblEditMappingId, gridBagConstraints);

        lblMappingIdKey.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblMappingIdKey.setMaximumSize(new java.awt.Dimension(120, 20));
        lblMappingIdKey.setMinimumSize(new java.awt.Dimension(120, 20));
        lblMappingIdKey.setPreferredSize(new java.awt.Dimension(120, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        pnlShortcutSettings.add(lblMappingIdKey, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 25;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        pnlEditingShortcutTab.add(pnlShortcutSettings, gridBagConstraints);

        lblInfo.setText("* Having the effect of the new shortcut requires a program restart.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        pnlEditingShortcutTab.add(lblInfo, gridBagConstraints);

        tabMainPanel.addTab("Mapping Editing Shortcut", pnlEditingShortcutTab);

        add(tabMainPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void cmdClassFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdClassFontActionPerformed

    	FontChooser2 fe =new FontChooser2(cmdClassFont, MappingManagerPreferences.CLASS_FONTFAMILY, MappingManagerPreferences.CLASS_FONTSIZE, MappingManagerPreferences.CLASS_ISBOLD);
    }//GEN-LAST:event_cmdClassFontActionPerformed

    private void cmdDataPropertyFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdDataPropertyFontActionPerformed
    	FontChooser2 fe = new FontChooser2(cmdDataPropertyFont, MappingManagerPreferences.DATAPROPERTY_FONTFAMILY, MappingManagerPreferences.DATAPROPERTY_FONTSIZE, MappingManagerPreferences.DATAPROPERTY_ISBOLD);
    }//GEN-LAST:event_cmdDataPropertyFontActionPerformed

    private void cmdObjectPropertyFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdObjectPropertyFontActionPerformed
    	FontChooser2 fe = new FontChooser2(cmdObjectPropertyFont, MappingManagerPreferences.OBJECTPROPTERTY_FONTFAMILY, MappingManagerPreferences.OBJECTPROPTERTY_FONTSIZE, MappingManagerPreferences.OBJECTPROPTERTY_ISBOLD);
    	
    }//GEN-LAST:event_cmdObjectPropertyFontActionPerformed

    private void cmdVariableFonrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdVariableFonrActionPerformed
    	FontChooser2 fe = new FontChooser2(cmdVariableFonr, MappingManagerPreferences.VARIABLE_FONTFAMILY, MappingManagerPreferences.VARIABLE_FONTSIZE, MappingManagerPreferences.VARIABLE_ISBOLD);
    	
    }//GEN-LAST:event_cmdVariableFonrActionPerformed

    private void cmdParameterFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdParameterFontActionPerformed
    	FontChooser2 fe = new FontChooser2(cmdParameterFont, MappingManagerPreferences.PARAMETER_FONTFAMILY, MappingManagerPreferences.PARAMETER_FONTSIZE, MappingManagerPreferences.PARAMETER_ISBOLD);
    	;
    }//GEN-LAST:event_cmdParameterFontActionPerformed

    private void cmdFunctorFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdFunctorFontActionPerformed
    	FontChooser2 fe = new FontChooser2(cmdFunctorFont, MappingManagerPreferences.FUCNTOR_FONTFAMILY, MappingManagerPreferences.FUCNTOR_FONTSIZE, MappingManagerPreferences.FUCNTOR_ISBOLD);
    	
    }//GEN-LAST:event_cmdFunctorFontActionPerformed

    private void cmdMappingBodyFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMappingBodyFontActionPerformed
    	FontChooser2 fe = new FontChooser2(cmdMappingBodyFont, MappingManagerPreferences.MAPPING_BODY_FONTFAMILY, MappingManagerPreferences.MAPPING_BODY_FONTSIZE, MappingManagerPreferences.MAPPING_BODY_ISBOLD);
    	
    }//GEN-LAST:event_cmdMappingBodyFontActionPerformed

    private void cmdMappingIdFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMappingIdFontActionPerformed
    	FontChooser2 fe = new FontChooser2(cmdMappingIdFont, MappingManagerPreferences.MAPPING_ID_FONTFAMILY, MappingManagerPreferences.MAPPING_ID_FONTSIZE, MappingManagerPreferences.MAPPING_ID_ISBOLD);
    	
    }//GEN-LAST:event_cmdMappingIdFontActionPerformed

    private void cmdInvalidQueryFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdInvalidQueryFontActionPerformed
    	FontChooser2 fe = new FontChooser2(cmdInvalidQueryFont, MappingManagerPreferences.INVALIDQUERY_FONTFAMILY, MappingManagerPreferences.INVALIDQUERY_FONTSIZE, MappingManagerPreferences.INVALIDQUERY_ISBOLD);
    }//GEN-LAST:event_cmdInvalidQueryFontActionPerformed

    private void cmdDependencyFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdDependencyFontActionPerformed
    	FontChooser2 fe = new FontChooser2(cmdDependencyFont, MappingManagerPreferences.DEPENDENCIES_FONTFAMILY, MappingManagerPreferences.DEPENDENCIES_FONTSIZE, MappingManagerPreferences.DEPENDENCIES_ISBOLD);
    }//GEN-LAST:event_cmdDependencyFontActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdClassColor;
    private javax.swing.JButton cmdClassFont;
    private javax.swing.JButton cmdDataPropertyColor;
    private javax.swing.JButton cmdDataPropertyFont;
    private javax.swing.JButton cmdDependencyColor;
    private javax.swing.JButton cmdDependencyFont;
    private javax.swing.JButton cmdFunctorColor;
    private javax.swing.JButton cmdFunctorFont;
    private javax.swing.JButton cmdInvalidQueryColor;
    private javax.swing.JButton cmdInvalidQueryFont;
    private javax.swing.JButton cmdMappingBodyColor;
    private javax.swing.JButton cmdMappingBodyFont;
    private javax.swing.JButton cmdMappingIdColor;
    private javax.swing.JButton cmdMappingIdFont;
    private javax.swing.JButton cmdObjectPropertyColor;
    private javax.swing.JButton cmdObjectPropertyFont;
    private javax.swing.JButton cmdParameterColor;
    private javax.swing.JButton cmdParameterFont;
    private javax.swing.JButton cmdVariableColor;
    private javax.swing.JButton cmdVariableFonr;
    private javax.swing.JLabel lblAddMapping;
    private javax.swing.JLabel lblAddMappingKey;
    private javax.swing.JLabel lblClass;
    private javax.swing.JLabel lblDataProperty;
    private javax.swing.JLabel lblDeleteMapping;
    private javax.swing.JLabel lblDeleteMappingKey;
    private javax.swing.JLabel lblDependency;
    private javax.swing.JLabel lblEditMappingBody;
    private javax.swing.JLabel lblEditMappingBodyKey;
    private javax.swing.JLabel lblEditMappingHead;
    private javax.swing.JLabel lblEditMappingHeadKey;
    private javax.swing.JLabel lblEditMappingId;
    private javax.swing.JLabel lblFunctors;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblInvalidQuery;
    private javax.swing.JLabel lblMappingBody;
    private javax.swing.JLabel lblMappingId;
    private javax.swing.JLabel lblMappingIdKey;
    private javax.swing.JLabel lblObjectProperty;
    private javax.swing.JLabel lblParameter;
    private javax.swing.JLabel lblVariable;
    private javax.swing.JPanel pnlClassPreferences;
    private javax.swing.JPanel pnlDataSourceDependencies;
    private javax.swing.JPanel pnlDisplayPreferencesTab;
    private javax.swing.JPanel pnlEditingShortcutTab;
    private javax.swing.JPanel pnlInvalidQueries;
    private javax.swing.JPanel pnlMappingPreferences;
    private javax.swing.JPanel pnlShortcutSettings;
    private javax.swing.JPanel pnlVariablePreferences;
    private javax.swing.JTabbedPane tabMainPanel;
    // End of variables declaration//GEN-END:variables
    
    
    private class ColorChooser extends JPanel {
    	
    	/**
		 * 
		 */
		private static final long serialVersionUID = -5704647065277117955L;
		JButton button = null;
    	String key = null;
    	
    	private ColorChooser(JButton button, String key){
    		super();
//    		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    		this.button = button;
    		this.key = key;
    		this.setSize(500,500);
    		initComponents();
    		JOptionPane pane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    		JDialog d = pane.createDialog("Color Chooser");
    		d.setVisible(true);
    		Object o = pane.getValue();
            if (pane != null){
                int ret = (Integer)o;
                if (ret == JOptionPane.OK_OPTION) {
                    applyPreferences();
                }
            }
    	}  
    	
    	private void initComponents() {
            java.awt.GridBagConstraints gridBagConstraints;

            jColorChooser1 = new javax.swing.JColorChooser();
//            jButton1 = new javax.swing.JButton();
            jLabel1 = new javax.swing.JLabel();
            jLabel2 = new javax.swing.JLabel();

            setLayout(new java.awt.GridBagLayout());

            jColorChooser1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.ipady = 1;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
            add(jColorChooser1, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.weighty = 1.0;
            add(jLabel1, gridBagConstraints);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridheight = 3;
            gridBagConstraints.weightx = 1.0;
            add(jLabel2, gridBagConstraints);
        }// </editor-fold>

        private void applyPreferences() {
        	Color aux = jColorChooser1.getSelectionModel().getSelectedColor();
        	pref.setColor(key, aux);
        	button.setBackground(aux);
        	button.setOpaque(true);
        }


        // Variables declaration - do not modify
        private javax.swing.JColorChooser jColorChooser1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        // End of variables declaration
        
    }
    
    public class FontChooser2 extends JPanel{

    	  // Results:

    	  /**
		 * 
		 */
		private static final long serialVersionUID = -602845875138513553L;

		/** The font the user has chosen */
    	  protected Font resultFont;

    	  /** The resulting font name */
    	  protected String resultName;

    	  /** The resulting font size */
    	  protected int resultSize;

    	  /** The resulting boldness */
    	  protected boolean isBold;

    	  /** The resulting italicness */
    	  protected boolean isItalic;

    	  // Working fields

    	  /** Display text */
    	  protected String displayText = "Qwerty Yuiop";

    	  /** The list of Fonts */
    	  protected String fontList[];

    	  /** The font name chooser */
    	  protected JList fontNameChoice;

    	  /** The font size chooser */
    	  protected JList fontSizeChoice;

    	  /** The bold and italic choosers */
    	  JCheckBox bold;

    	  /** The list of font sizes */
    	  protected String fontSizes[] = { "8", "10", "11", "12", "14", "16", "18",
    	      "20", "24", "30", "36", "40", "48", "60", "72" };

    	  /** The index of the default size (e.g., 14 point == 4) */
    	  protected static final int DEFAULT_SIZE = 4;

    	  /**
    	   * The display area. Use a JLabel as the AWT label doesn't always honor
    	   * setFont() in a timely fashion :-)
    	   */
//    	  protected JLabel previewArea;
    	  
    	  protected String fontfamily = null;
    	  
    	  protected String fontsize = null;
    	  
    	  protected String isbold = null;
    	  
//    	  protected JButton jButtonOK = null;
//    	  
//    	  protected JButton jButtonCancel = null;
//    	  
    	  protected JButton button = null;
    	  /**
    	   * Construct a FontChooser -- Sets title and gets array of fonts on the
    	   * system. Builds a GUI to let the user choose one font at one size.
    	   */
    	  
    	  public FontChooser2(JButton b, String ff, String fs, String isb) {
    	    super();
    	    fontfamily = ff;
    	    fontsize = fs;
    	    isbold = isb;
    	    button = b;
    	    init();
    	    JOptionPane pane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    		JDialog d = pane.createDialog("Font Chooser");
    		d.setResizable(true);
    		d.setVisible(true);
    		Object o = pane.getValue();
            if (o != null){
                int ret = (Integer)o;
                if (ret == JOptionPane.OK_OPTION) {
                    applyChanges();
                }
            }
    	  }
    	  
//    	  private void cancel(){
//    		  this.setVisible(false);
//    	  }
    	  
    	  private void init(){
    		  
    		  java.awt.GridBagConstraints gridBagConstraints;
			  javax.swing.JScrollPane scrFontNameList;
			  javax.swing.JScrollPane scrFontSizeList;
				
    	        fontNameChoice = new javax.swing.JList();
    	        fontSizeChoice = new javax.swing.JList();
    	        scrFontNameList = new javax.swing.JScrollPane();
    	        scrFontSizeList = new javax.swing.JScrollPane();
    	        bold = new javax.swing.JCheckBox();
    	        pnlDisplayPreferencesTab = new javax.swing.JPanel();
//    	        jButtonCancel = new javax.swing.JButton();
//    	        jButtonOK = new javax.swing.JButton();

    	        setMinimumSize(new java.awt.Dimension(400, 300));
    	        setLayout(new java.awt.GridBagLayout());

    	        fontNameChoice.setModel(new javax.swing.AbstractListModel() {
    	            String[] strings = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    	            public int getSize() { return strings.length; }
    	            public Object getElementAt(int i) { return strings[i]; }
    	        });
    	        scrFontNameList.setViewportView(fontNameChoice);

    	        gridBagConstraints = new java.awt.GridBagConstraints();
    	        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    	        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    	        gridBagConstraints.weightx = 1.0;
    	        gridBagConstraints.weighty = 1.0;
    	        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    	        add(scrFontNameList, gridBagConstraints);

    	        fontSizeChoice.setModel(new javax.swing.AbstractListModel() {
    	            String[] strings = { "8", "10", "11", "12", "14", "16", "18", "20", "24", "30", "36", "40", "48", "60", "72" };
    	            public int getSize() { return strings.length; }
    	            public Object getElementAt(int i) { return strings[i]; }
    	        });
    	        fontSizeChoice.setMaximumSize(new java.awt.Dimension(50, 285));
    	        fontSizeChoice.setMinimumSize(new java.awt.Dimension(50, 285));
    	        fontSizeChoice.setPreferredSize(new java.awt.Dimension(50, 285));
    	        scrFontSizeList.setViewportView(fontSizeChoice);

    	        gridBagConstraints = new java.awt.GridBagConstraints();
    	        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    	        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    	        gridBagConstraints.weightx = 1.0;
    	        gridBagConstraints.weighty = 1.0;
    	        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    	        add(scrFontSizeList, gridBagConstraints);

    	        bold.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
    	        bold.setText("Bold");
    	        bold.setBorder(null);
    	        gridBagConstraints = new java.awt.GridBagConstraints();
    	        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    	        gridBagConstraints.weightx = 1.0;
    	        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    	        add(bold, gridBagConstraints);

    	        pnlDisplayPreferencesTab.setLayout(new java.awt.GridBagLayout());
    		  
    	        String currentFont = pref.getFontFamily(fontfamily);
    	        int currentSize = pref.getFontSize(fontsize);
	      	    int i = getIndexOfFont(currentFont);
	      	    int j = getIndexOfSize(String.valueOf(currentSize));
	      	    fontNameChoice.setSelectedIndex(i);
	      	    fontSizeChoice.setSelectedIndex(j);

    	  }
    	  
    	  private int getIndexOfFont(String font){
    		  
    		  AbstractListModel model = (AbstractListModel) fontNameChoice.getModel();
    		  int size = model.getSize();
    		  for(int i=0;i<size;i++){
    			  if(model.getElementAt(i).equals(font)){
    				  return i;
    			  }  
    		  }
    		  return 0;
    	  }
    	  
    	  private int getIndexOfSize(String size){
    		  
    		  AbstractListModel model = (AbstractListModel) fontSizeChoice.getModel();
    		  int length = model.getSize();
    		  for(int i=0;i<length;i++){
    			  if(model.getElementAt(i).equals(size)){
    				  return i;
    			  }  
    		  }
    		  return 0;
    	  }

    	  protected void applyChanges(){
    		  
    		resultName = (String) fontNameChoice.getSelectedValue();
      	    String resultSizeName = (String) fontSizeChoice.getSelectedValue();
      	    int resultSize = Integer.parseInt(resultSizeName);
      	    isBold = bold.isSelected();
      	    pref.setFontFamily(fontfamily, resultName);
      	    pref.setFontSize(fontsize, resultSize);
      	    pref.setIsBold(isbold, new Boolean(isBold));
      	    button.setText(resultName + ", " + resultSize);
      	    button.setToolTipText(resultName + ", " + resultSize);
    	  }
    	  
    	  /**
    	   * Called from the action handlers to get the font info, build a font, and
    	   * set it.
    	   */
    	  protected void previewFont() {
    	    resultName = (String) fontNameChoice.getSelectedValue();
    	    String resultSizeName = (String) fontSizeChoice.getSelectedValue();
    	    int resultSize = Integer.parseInt(resultSizeName);
    	    isBold = bold.isSelected();
//    	    isItalic = italic.getState();
    	    int attrs = Font.PLAIN;
    	    if (isBold)
    	      attrs = Font.BOLD;
    	    if (isItalic)
    	      attrs |= Font.ITALIC;
    	    resultFont = new Font(resultName, attrs, resultSize);
    	  }

    	  /** Retrieve the selected font name. */
    	  public String getSelectedName() {
    	    return resultName;
    	  }

    	  /** Retrieve the selected size */
    	  public int getSelectedSize() {
    	    return resultSize;
    	  }

    	  /** Retrieve the selected font, or null */
    	  public Font getSelectedFont() {
    	    return resultFont;
    	  
    	  }
    }
}


