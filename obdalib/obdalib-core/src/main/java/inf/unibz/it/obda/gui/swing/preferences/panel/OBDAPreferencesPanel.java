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
import javax.swing.JFrame;
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

    /**
	 * 
	 */
	private static final long serialVersionUID = 715907443527650509L;
	
	private static final String add = "add.Mapping";
	private static final String delete = "delete.Mapping";
	private static final String editHead = "edit.Mapping.Head";
	private static final String editBody = "edit.Mapping.Body";
	private static final String editId = "edit.Mapping.id";
	private MappingManagerPreferences pref = null;
	private OBDAPreferences obdaPref = null;
	private JFrame protegeFrame = null;
	private HashMap<String, KeyStroke> shortCuts = null;
	/** Creates new form OBDAPreferencesPanel */
    public OBDAPreferencesPanel() {
    	pref =  OBDAPreferences.getOBDAPreferences().getMappingsPreference();
    	obdaPref = OBDAPreferences.getOBDAPreferences();
    	shortCuts = new HashMap<String, KeyStroke>();
        initComponents();
        addListener();
        showPreferences();

    }
    
    public OBDAPreferencesPanel(JFrame frame) {
    	pref =  OBDAPreferences.getOBDAPreferences().getMappingsPreference();
    	this.protegeFrame = frame;
        initComponents();
        addListener();
        showPreferences();

    }

    private boolean isKeyStrokeAlreadyAssigned(KeyStroke stroke){
    	
    	return shortCuts.containsValue(stroke);
    }
    
    public static void main(String[] args){
    	
    	JFrame frame = new JFrame();
    	frame.add(new OBDAPreferencesPanel());
    	frame.setSize(400,400);
    	frame.setVisible(true);
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
    	
    	jButtonClassColour.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(jButtonClassColour, MappingManagerPreferences.CLASS_COLOR);
				cc.setVisible(true);
			}
    		
    	});
    	
    	jButtonDataPropertyColour.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(jButtonDataPropertyColour, MappingManagerPreferences.DATAPROPERTY_COLOR);
				cc.setVisible(true);
			}
    		
    	});
    	
    	jButtonFunctorPropertyColour1.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(jButtonFunctorPropertyColour1, MappingManagerPreferences.FUCNTOR_COLOR);
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
    	
    	jButtonObjectPropertyColour.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(jButtonObjectPropertyColour, MappingManagerPreferences.OBJECTPROPTERTY_COLOR);
				cc.setVisible(true);
			}
    		
    	});
    	
    	jButtonParameterColour.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(jButtonParameterColour, MappingManagerPreferences.PARAMETER_COLOR);
				cc.setVisible(true);
			}
    		
    	});
    	
    	jButtonVariableColour1.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(jButtonVariableColour1, MappingManagerPreferences.VARIABLE_COLOR);
				cc.setVisible(true);
			}
    		
    	});
    	
    	jButtonInvalidQueryColour.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(jButtonInvalidQueryColour, MappingManagerPreferences.INVALIDQUERY_COLOR);
				cc.setVisible(true);
			}
    		
    	});
    	String aux = pref.getShortCut(add);
    	KeyStroke ks = KeyStroke.getKeyStroke(aux);
    	jLabelAddKey.setText(KeyEvent.getKeyModifiersText(ks.getModifiers()) + " + "+ KeyEvent.getKeyText(ks.getKeyCode()));
    	jLabelAddKey.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				
				jLabelAddKey.setText("");
				jLabelAddKey.requestFocus();
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
    		
    	});
    	jLabelAddKey.addKeyListener(new KeyListener(){

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
				    jLabelAddKey.setText(KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + "+ KeyEvent.getKeyText(stroke.getKeyCode()));
					jLabelAddKey.setToolTipText(stroke.toString());
					pref.setShortcut(add, stroke.toString());
				}else{
					KeyStroke oldValue = shortCuts.get(add);
					if(oldValue != null){
						jLabelAddKey.setText(KeyEvent.getKeyModifiersText(oldValue.getModifiers()) + " + "+ KeyEvent.getKeyText(oldValue.getKeyCode()));
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
    	jLabelEditBodyKey.setText(KeyEvent.getKeyModifiersText(ks2.getModifiers()) + " + "+ KeyEvent.getKeyText(ks2.getKeyCode()));
    	jLabelEditBodyKey.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				
				jLabelEditBodyKey.setText("");
				jLabelEditBodyKey.requestFocus();
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
    		
    	});
    	jLabelEditBodyKey.addKeyListener(new KeyListener(){

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
					jLabelEditBodyKey.setText(KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + "+ KeyEvent.getKeyText(stroke.getKeyCode()));
					jLabelEditBodyKey.setToolTipText(stroke.toString());
					pref.setShortcut(editBody, stroke.toString());
				}else{
					KeyStroke oldValue = shortCuts.get(editBody);
					if(oldValue != null){
						jLabelEditBodyKey.setText(KeyEvent.getKeyModifiersText(oldValue.getModifiers()) + " + "+ KeyEvent.getKeyText(oldValue.getKeyCode()));
					}
					JOptionPane.showMessageDialog(null, "Key stroke already assigned. Please choose an other combination.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}

			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
    	});
    	
    	String aux3 = pref.getShortCut(editHead);
    	KeyStroke ks3 = KeyStroke.getKeyStroke(aux3);
    	jLabelEditHeadKey.setText(KeyEvent.getKeyModifiersText(ks3.getModifiers()) + " + "+ KeyEvent.getKeyText(ks3.getKeyCode()));
    	jLabelEditHeadKey.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				
				jLabelEditHeadKey.setText("");
				jLabelEditHeadKey.requestFocus();
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
    		
    	});
    	jLabelEditHeadKey.addKeyListener(new KeyListener(){

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
					jLabelEditHeadKey.setText(KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + "+ KeyEvent.getKeyText(stroke.getKeyCode()));
					jLabelEditHeadKey.setToolTipText(stroke.toString());
					pref.setShortcut(editHead, stroke.toString());
				}else{
					KeyStroke oldValue = shortCuts.get(editHead);
					if(oldValue != null){
						jLabelEditHeadKey.setText(KeyEvent.getKeyModifiersText(oldValue.getModifiers()) + " + "+ KeyEvent.getKeyText(oldValue.getKeyCode()));
					}
					JOptionPane.showMessageDialog(null, "Key stroke already assigned. Please choose an other combination.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}

			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
    	});
    	
    	String aux4 = pref.getShortCut(editId);
    	KeyStroke ks4 = KeyStroke.getKeyStroke(aux4);
    	jLabelEditIDKey.setText(KeyEvent.getKeyModifiersText(ks4.getModifiers()) + " + "+ KeyEvent.getKeyText(ks4.getKeyCode()));
    	jLabelEditIDKey.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				
				jLabelEditIDKey.setText("");
				jLabelEditIDKey.requestFocus();
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
    		
    	});
    	jLabelEditIDKey.addKeyListener(new KeyListener(){

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
					jLabelEditIDKey.setText(KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + "+ KeyEvent.getKeyText(stroke.getKeyCode()));
					jLabelEditIDKey.setToolTipText(stroke.toString());
					pref.setShortcut(editId, stroke.toString());
				}else{
					KeyStroke oldValue = shortCuts.get(editId);
					if(oldValue != null){
						jLabelEditIDKey.setText(KeyEvent.getKeyModifiersText(oldValue.getModifiers()) + " + "+ KeyEvent.getKeyText(oldValue.getKeyCode()));
					}
					JOptionPane.showMessageDialog(null, "Key stroke already assigned. Please choose an other combination.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}

			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
    	});
    }
    
    private void showPreferences(){
 
    	Color clazz = pref.getColor(MappingManagerPreferences.CLASS_COLOR);
    	jButtonClassColour.setBackground(clazz);
    	jButtonClassColour.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	Color dp = pref.getColor(MappingManagerPreferences.DATAPROPERTY_COLOR);
    	jButtonDataPropertyColour.setBackground(dp);
    	jButtonDataPropertyColour.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	Color op = pref.getColor(MappingManagerPreferences.OBJECTPROPTERTY_COLOR);
    	jButtonObjectPropertyColour.setBackground(op);
    	jButtonObjectPropertyColour.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	Color var = pref.getColor(MappingManagerPreferences.VARIABLE_COLOR);
    	jButtonVariableColour1.setBackground(var);
    	jButtonVariableColour1.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	Color par = pref.getColor(MappingManagerPreferences.PARAMETER_COLOR);
    	jButtonParameterColour.setBackground(par);
    	jButtonParameterColour.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	Color fun = pref.getColor(MappingManagerPreferences.FUCNTOR_COLOR);
    	jButtonFunctorPropertyColour1.setBackground(fun);
    	jButtonFunctorPropertyColour1.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
//    	Color body = pref.getColor(MappingManagerPreferences.MAPPING_BODY_COLOR);
//    	jButtonBodyPropertyColour1.setBackground(body);
//    	jButtonBodyPropertyColour1.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
//    	Color id = pref.getColor(MappingManagerPreferences.MAPPING_ID_COLOR);
//    	jButtonIDColour2.setBackground(id);
//    	jButtonIDColour2.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	Color iq = pref.getColor(MappingManagerPreferences.INVALIDQUERY_COLOR);
    	jButtonInvalidQueryColour.setBackground(iq);
    	jButtonInvalidQueryColour.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	
    	String fontClassFam = pref.getFontFamily(MappingManagerPreferences.CLASS_FONTFAMILY);
    	int classSize = pref.getFontSize(MappingManagerPreferences.CLASS_FONTSIZE);
    	jButtonClassFont.setText(fontClassFam + ", " + classSize);
    	jButtonClassFont.setToolTipText(fontClassFam + ", " + classSize);
    	
    	String fontDP = pref.getFontFamily(MappingManagerPreferences.DATAPROPERTY_FONTFAMILY);
    	int sizeDP = pref.getFontSize(MappingManagerPreferences.DATAPROPERTY_FONTSIZE);
    	jButtonDataPropertyFont.setText(fontDP + ", " + sizeDP);
    	jButtonDataPropertyFont.setToolTipText(fontDP + ", " + sizeDP);
    	
    	String fontOP = pref.getFontFamily(MappingManagerPreferences.OBJECTPROPTERTY_FONTFAMILY);
    	int sizeOP = pref.getFontSize(MappingManagerPreferences.OBJECTPROPTERTY_FONTSIZE);
    	jButtonObjectPropertyFont.setText(fontOP + ", " + sizeOP);
    	jButtonObjectPropertyFont.setToolTipText(fontOP + ", " + sizeOP);
    	
    	String fontVar = pref.getFontFamily(MappingManagerPreferences.VARIABLE_FONTFAMILY);
    	int varSize = pref.getFontSize(MappingManagerPreferences.VARIABLE_FONTSIZE);
    	jButtonVariableFont1.setText(fontVar + ", " + varSize);
    	jButtonVariableFont1.setToolTipText(fontVar + ", " + varSize);
    	
    	String fontPara = pref.getFontFamily(MappingManagerPreferences.PARAMETER_FONTFAMILY);
    	int paraSize = pref.getFontSize(MappingManagerPreferences.PARAMETER_FONTSIZE);
    	jButtonParameterFont1.setText(fontPara + ", "+ paraSize);
    	jButtonParameterFont1.setToolTipText(fontPara + ", "+ paraSize);
    	
    	String fontFunc = pref.getFontFamily(MappingManagerPreferences.FUCNTOR_FONTFAMILY);
    	int funcSize = pref.getFontSize(MappingManagerPreferences.FUCNTOR_FONTSIZE);
    	jButtonFunctorPropertyFont1.setText(fontFunc + ", " +funcSize);
    	jButtonFunctorPropertyFont1.setToolTipText(fontFunc + ", " +funcSize);
    	
    	String fontBody = pref.getFontFamily(MappingManagerPreferences.MAPPING_BODY_FONTFAMILY);
    	int bodySize = pref.getFontSize(MappingManagerPreferences.MAPPING_BODY_FONTSIZE);
    	jButtonbodyPropertyFont2.setText(fontBody + ", " + bodySize);
    	jButtonbodyPropertyFont2.setToolTipText(fontBody + ", " + bodySize);
    	
    	String fontID = pref.getFontFamily(MappingManagerPreferences.MAPPING_ID_FONTFAMILY);
    	int idSize = pref.getFontSize(MappingManagerPreferences.MAPPING_ID_FONTSIZE);
    	jButtonIDFont2.setText(fontID + ", "+ idSize);
    	jButtonIDFont2.setToolTipText(fontID + ", "+ idSize);
    	
    	String fontIQ= pref.getFontFamily(MappingManagerPreferences.INVALIDQUERY_FONTFAMILY);
    	int iqSize = pref.getFontSize(MappingManagerPreferences.INVALIDQUERY_FONTSIZE);
    	jButtonInvalidQueryFont.setText(fontIQ + ", "+ iqSize);
    	jButtonInvalidQueryFont.setToolTipText(fontIQ + ", "+ iqSize);
    	
    	String fontDep = pref.getFontFamily(MappingManagerPreferences.DEPENDENCIES_FONTFAMILY);
    	int depSize = pref.getFontSize(MappingManagerPreferences.DEPENDENCIES_FONTSIZE);
    	jButtonDependenciesFont.setText(fontDep + ", " + depSize);
    	jButtonDependenciesFont.setToolTipText(fontDep + ", " + depSize);
    	
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

        jLabel5 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanelClassPreferences = new javax.swing.JPanel();
        jLabelClass = new javax.swing.JLabel();
        jButtonClassColour = new javax.swing.JButton();
        jButtonClassFont = new javax.swing.JButton();
        jButtonDataPropertyFont = new javax.swing.JButton();
        jButtonDataPropertyColour = new javax.swing.JButton();
        jLabelDataProperty = new javax.swing.JLabel();
        jButtonObjectPropertyFont = new javax.swing.JButton();
        jButtonObjectPropertyColour = new javax.swing.JButton();
        jLabelObjectProperty = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanelVariablePreferences = new javax.swing.JPanel();
        jLabelFunctors = new javax.swing.JLabel();
        jButtonVariableColour1 = new javax.swing.JButton();
        jButtonVariableFont1 = new javax.swing.JButton();
        jLabelVariables = new javax.swing.JLabel();
        jButtonParameterColour = new javax.swing.JButton();
        jButtonParameterFont1 = new javax.swing.JButton();
        jButtonFunctorPropertyFont1 = new javax.swing.JButton();
        jButtonFunctorPropertyColour1 = new javax.swing.JButton();
        jLabelParameters = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanelBodyProperty1 = new javax.swing.JPanel();
        jButtonbodyPropertyFont2 = new javax.swing.JButton();
        jButtonBodyPropertyColour1 = new javax.swing.JButton();
        jLabelID = new javax.swing.JLabel();
        jLabelBody = new javax.swing.JLabel();
        jButtonIDColour2 = new javax.swing.JButton();
        jButtonIDFont2 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabelPlaceHolder = new javax.swing.JLabel();
        jPanelInvalidQuery = new javax.swing.JPanel();
        jButtonInvalidQueryFont = new javax.swing.JButton();
        jButtonInvalidQueryColour = new javax.swing.JButton();
        jLabelInvalidQuery = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanelInvalidDependencies = new javax.swing.JPanel();
        jButtonDependenciesFont = new javax.swing.JButton();
        jButtonDependenciesColour = new javax.swing.JButton();
        jLabelDependencies = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabelAdd = new javax.swing.JLabel();
        jLabelAddKey = new javax.swing.JLabel();
        jLabelPH = new javax.swing.JLabel();
        jLabelDelete = new javax.swing.JLabel();
        jLabelDeleteKey = new javax.swing.JLabel();
        jLabelPH1 = new javax.swing.JLabel();
        jLabelEditID = new javax.swing.JLabel();
        jLabelEditIDKey = new javax.swing.JLabel();
        jLabelPH2 = new javax.swing.JLabel();
        jLabelPH3 = new javax.swing.JLabel();
        jLabelEditBodyKey = new javax.swing.JLabel();
        jLabelEditBody = new javax.swing.JLabel();
        jLabelPH4 = new javax.swing.JLabel();
        jLabelEditHeadKey = new javax.swing.JLabel();
        jLabelEditHead = new javax.swing.JLabel();
        jLabelPlaceHolder1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setBorder(null);
        setMinimumSize(new java.awt.Dimension(400, 400));
        setPreferredSize(new java.awt.Dimension(500, 800));
        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jLabel5, gridBagConstraints);

        jTabbedPane1.setBorder(null);
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(200, 200));
        jTabbedPane1.setOpaque(true);
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(500, 800));

        jScrollPane1.setBorder(null);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(400, 100));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(500, 800));

        jPanel1.setBorder(null);
        jPanel1.setMinimumSize(new java.awt.Dimension(894, 491));
        jPanel1.setPreferredSize(new java.awt.Dimension(450, 750));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanelClassPreferences.setBorder(javax.swing.BorderFactory.createTitledBorder("OWL Entities preferences"));
        jPanelClassPreferences.setLayout(new java.awt.GridBagLayout());

        jLabelClass.setText("Class:\n\n\n");
        jLabelClass.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelClass.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelClass.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelClassPreferences.add(jLabelClass, gridBagConstraints);

        jButtonClassColour.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonClassColour.setMaximumSize(new java.awt.Dimension(90, 17));
        jButtonClassColour.setMinimumSize(new java.awt.Dimension(60, 17));
        jButtonClassColour.setPreferredSize(new java.awt.Dimension(60, 17));
        jButtonClassColour.setRolloverEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        jPanelClassPreferences.add(jButtonClassColour, gridBagConstraints);

        jButtonClassFont.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonClassFont.setMaximumSize(new java.awt.Dimension(200, 17));
        jButtonClassFont.setMinimumSize(new java.awt.Dimension(90, 17));
        jButtonClassFont.setPreferredSize(new java.awt.Dimension(120, 17));
        jButtonClassFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClassFontActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        jPanelClassPreferences.add(jButtonClassFont, gridBagConstraints);

        jButtonDataPropertyFont.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonDataPropertyFont.setMaximumSize(new java.awt.Dimension(200, 17));
        jButtonDataPropertyFont.setMinimumSize(new java.awt.Dimension(90, 17));
        jButtonDataPropertyFont.setPreferredSize(new java.awt.Dimension(120, 17));
        jButtonDataPropertyFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDataPropertyFontActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        jPanelClassPreferences.add(jButtonDataPropertyFont, gridBagConstraints);

        jButtonDataPropertyColour.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonDataPropertyColour.setMaximumSize(new java.awt.Dimension(90, 17));
        jButtonDataPropertyColour.setMinimumSize(new java.awt.Dimension(60, 17));
        jButtonDataPropertyColour.setPreferredSize(new java.awt.Dimension(60, 17));
        jButtonDataPropertyColour.setRolloverEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        jPanelClassPreferences.add(jButtonDataPropertyColour, gridBagConstraints);

        jLabelDataProperty.setText("Data Properties:");
        jLabelDataProperty.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelDataProperty.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelDataProperty.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelClassPreferences.add(jLabelDataProperty, gridBagConstraints);

        jButtonObjectPropertyFont.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonObjectPropertyFont.setMaximumSize(new java.awt.Dimension(200, 17));
        jButtonObjectPropertyFont.setMinimumSize(new java.awt.Dimension(90, 17));
        jButtonObjectPropertyFont.setPreferredSize(new java.awt.Dimension(120, 17));
        jButtonObjectPropertyFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonObjectPropertyFontActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        jPanelClassPreferences.add(jButtonObjectPropertyFont, gridBagConstraints);

        jButtonObjectPropertyColour.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonObjectPropertyColour.setMaximumSize(new java.awt.Dimension(90, 17));
        jButtonObjectPropertyColour.setMinimumSize(new java.awt.Dimension(60, 17));
        jButtonObjectPropertyColour.setPreferredSize(new java.awt.Dimension(60, 17));
        jButtonObjectPropertyColour.setRolloverEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        jPanelClassPreferences.add(jButtonObjectPropertyColour, gridBagConstraints);

        jLabelObjectProperty.setText("Object Properties:");
        jLabelObjectProperty.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelObjectProperty.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelObjectProperty.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelClassPreferences.add(jLabelObjectProperty, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelClassPreferences.add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 25;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        jPanel1.add(jPanelClassPreferences, gridBagConstraints);

        jPanelVariablePreferences.setBorder(javax.swing.BorderFactory.createTitledBorder("Variable preferences"));
        jPanelVariablePreferences.setLayout(new java.awt.GridBagLayout());

        jLabelFunctors.setText("Functors:");
        jLabelFunctors.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelFunctors.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelFunctors.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelVariablePreferences.add(jLabelFunctors, gridBagConstraints);

        jButtonVariableColour1.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonVariableColour1.setMaximumSize(new java.awt.Dimension(90, 17));
        jButtonVariableColour1.setMinimumSize(new java.awt.Dimension(60, 17));
        jButtonVariableColour1.setPreferredSize(new java.awt.Dimension(60, 17));
        jButtonVariableColour1.setRolloverEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelVariablePreferences.add(jButtonVariableColour1, gridBagConstraints);

        jButtonVariableFont1.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonVariableFont1.setMaximumSize(new java.awt.Dimension(200, 17));
        jButtonVariableFont1.setMinimumSize(new java.awt.Dimension(90, 17));
        jButtonVariableFont1.setPreferredSize(new java.awt.Dimension(120, 17));
        jButtonVariableFont1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonVariableFont1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        jPanelVariablePreferences.add(jButtonVariableFont1, gridBagConstraints);

        jLabelVariables.setText("Varaiables:");
        jLabelVariables.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelVariables.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelVariables.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelVariablePreferences.add(jLabelVariables, gridBagConstraints);

        jButtonParameterColour.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonParameterColour.setMaximumSize(new java.awt.Dimension(90, 17));
        jButtonParameterColour.setMinimumSize(new java.awt.Dimension(60, 17));
        jButtonParameterColour.setPreferredSize(new java.awt.Dimension(60, 17));
        jButtonParameterColour.setRolloverEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelVariablePreferences.add(jButtonParameterColour, gridBagConstraints);

        jButtonParameterFont1.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonParameterFont1.setMaximumSize(new java.awt.Dimension(200, 17));
        jButtonParameterFont1.setMinimumSize(new java.awt.Dimension(90, 17));
        jButtonParameterFont1.setPreferredSize(new java.awt.Dimension(120, 17));
        jButtonParameterFont1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonParameterFont1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        jPanelVariablePreferences.add(jButtonParameterFont1, gridBagConstraints);

        jButtonFunctorPropertyFont1.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonFunctorPropertyFont1.setMaximumSize(new java.awt.Dimension(200, 17));
        jButtonFunctorPropertyFont1.setMinimumSize(new java.awt.Dimension(90, 17));
        jButtonFunctorPropertyFont1.setPreferredSize(new java.awt.Dimension(120, 17));
        jButtonFunctorPropertyFont1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFunctorPropertyFont1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        jPanelVariablePreferences.add(jButtonFunctorPropertyFont1, gridBagConstraints);

        jButtonFunctorPropertyColour1.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonFunctorPropertyColour1.setMaximumSize(new java.awt.Dimension(90, 17));
        jButtonFunctorPropertyColour1.setMinimumSize(new java.awt.Dimension(60, 17));
        jButtonFunctorPropertyColour1.setPreferredSize(new java.awt.Dimension(60, 17));
        jButtonFunctorPropertyColour1.setRolloverEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelVariablePreferences.add(jButtonFunctorPropertyColour1, gridBagConstraints);

        jLabelParameters.setText("Parameters:");
        jLabelParameters.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelParameters.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelParameters.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelVariablePreferences.add(jLabelParameters, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanelVariablePreferences.add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 25;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jPanelVariablePreferences, gridBagConstraints);

        jPanelBodyProperty1.setBorder(javax.swing.BorderFactory.createTitledBorder("Mapping Preferences"));
        jPanelBodyProperty1.setLayout(new java.awt.GridBagLayout());

        jButtonbodyPropertyFont2.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonbodyPropertyFont2.setMaximumSize(new java.awt.Dimension(200, 17));
        jButtonbodyPropertyFont2.setMinimumSize(new java.awt.Dimension(90, 17));
        jButtonbodyPropertyFont2.setPreferredSize(new java.awt.Dimension(120, 17));
        jButtonbodyPropertyFont2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonbodyPropertyFont2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        jPanelBodyProperty1.add(jButtonbodyPropertyFont2, gridBagConstraints);

        jButtonBodyPropertyColour1.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonBodyPropertyColour1.setEnabled(false);
        jButtonBodyPropertyColour1.setMaximumSize(new java.awt.Dimension(90, 17));
        jButtonBodyPropertyColour1.setMinimumSize(new java.awt.Dimension(60, 17));
        jButtonBodyPropertyColour1.setPreferredSize(new java.awt.Dimension(60, 17));
        jButtonBodyPropertyColour1.setRolloverEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelBodyProperty1.add(jButtonBodyPropertyColour1, gridBagConstraints);

        jLabelID.setText("Mappings ID:");
        jLabelID.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelID.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelID.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelBodyProperty1.add(jLabelID, gridBagConstraints);

        jLabelBody.setText("Mappings Body:");
        jLabelBody.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelBody.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelBody.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelBodyProperty1.add(jLabelBody, gridBagConstraints);

        jButtonIDColour2.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonIDColour2.setEnabled(false);
        jButtonIDColour2.setMaximumSize(new java.awt.Dimension(90, 17));
        jButtonIDColour2.setMinimumSize(new java.awt.Dimension(60, 17));
        jButtonIDColour2.setPreferredSize(new java.awt.Dimension(60, 17));
        jButtonIDColour2.setRolloverEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelBodyProperty1.add(jButtonIDColour2, gridBagConstraints);

        jButtonIDFont2.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonIDFont2.setMaximumSize(new java.awt.Dimension(200, 17));
        jButtonIDFont2.setMinimumSize(new java.awt.Dimension(90, 17));
        jButtonIDFont2.setPreferredSize(new java.awt.Dimension(120, 17));
        jButtonIDFont2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonIDFont2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        jPanelBodyProperty1.add(jButtonIDFont2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanelBodyProperty1.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 25;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jPanelBodyProperty1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jLabelPlaceHolder, gridBagConstraints);

        jPanelInvalidQuery.setBorder(javax.swing.BorderFactory.createTitledBorder("Invalid Queries"));
        jPanelInvalidQuery.setLayout(new java.awt.GridBagLayout());

        jButtonInvalidQueryFont.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonInvalidQueryFont.setMaximumSize(new java.awt.Dimension(200, 17));
        jButtonInvalidQueryFont.setMinimumSize(new java.awt.Dimension(90, 17));
        jButtonInvalidQueryFont.setPreferredSize(new java.awt.Dimension(120, 17));
        jButtonInvalidQueryFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInvalidQueryFontActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        jPanelInvalidQuery.add(jButtonInvalidQueryFont, gridBagConstraints);

        jButtonInvalidQueryColour.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonInvalidQueryColour.setMaximumSize(new java.awt.Dimension(90, 17));
        jButtonInvalidQueryColour.setMinimumSize(new java.awt.Dimension(60, 17));
        jButtonInvalidQueryColour.setPreferredSize(new java.awt.Dimension(60, 17));
        jButtonInvalidQueryColour.setRolloverEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelInvalidQuery.add(jButtonInvalidQueryColour, gridBagConstraints);

        jLabelInvalidQuery.setText("Query:");
        jLabelInvalidQuery.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelInvalidQuery.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelInvalidQuery.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelInvalidQuery.add(jLabelInvalidQuery, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanelInvalidQuery.add(jLabel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 25;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jPanelInvalidQuery, gridBagConstraints);

        jPanelInvalidDependencies.setBorder(javax.swing.BorderFactory.createTitledBorder("Data Source Dependencies"));
        jPanelInvalidDependencies.setLayout(new java.awt.GridBagLayout());

        jButtonDependenciesFont.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonDependenciesFont.setMaximumSize(new java.awt.Dimension(200, 17));
        jButtonDependenciesFont.setMinimumSize(new java.awt.Dimension(90, 17));
        jButtonDependenciesFont.setPreferredSize(new java.awt.Dimension(120, 17));
        jButtonDependenciesFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDependenciesFontActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 15);
        jPanelInvalidDependencies.add(jButtonDependenciesFont, gridBagConstraints);

        jButtonDependenciesColour.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jButtonDependenciesColour.setEnabled(false);
        jButtonDependenciesColour.setMaximumSize(new java.awt.Dimension(90, 17));
        jButtonDependenciesColour.setMinimumSize(new java.awt.Dimension(60, 17));
        jButtonDependenciesColour.setPreferredSize(new java.awt.Dimension(60, 17));
        jButtonDependenciesColour.setRolloverEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelInvalidDependencies.add(jButtonDependenciesColour, gridBagConstraints);

        jLabelDependencies.setText("Dependencies:");
        jLabelDependencies.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelDependencies.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelDependencies.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelInvalidDependencies.add(jLabelDependencies, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanelInvalidDependencies.add(jLabel7, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 25;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jPanelInvalidDependencies, gridBagConstraints);

        jScrollPane1.setViewportView(jPanel1);

        jTabbedPane1.addTab("Display Preferences", jScrollPane1);

        jScrollPane2.setBorder(null);
        jScrollPane2.setMinimumSize(new java.awt.Dimension(400, 493));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(600, 800));

        jPanel3.setBorder(null);
        jPanel3.setMinimumSize(new java.awt.Dimension(212, 236));
        jPanel3.setPreferredSize(new java.awt.Dimension(450, 550));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Shortcut Settings"));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabelAdd.setText("Add Mapping: *");
        jLabelAdd.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelAdd.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelAdd.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 170;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelAdd, gridBagConstraints);

        jLabelAddKey.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabelAddKey.setMaximumSize(new java.awt.Dimension(120, 20));
        jLabelAddKey.setMinimumSize(new java.awt.Dimension(120, 20));
        jLabelAddKey.setPreferredSize(new java.awt.Dimension(120, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelAddKey, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelPH, gridBagConstraints);

        jLabelDelete.setText("Delete Mapping: *");
        jLabelDelete.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelDelete.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelDelete.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 170;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelDelete, gridBagConstraints);

        jLabelDeleteKey.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabelDeleteKey.setEnabled(false);
        jLabelDeleteKey.setMaximumSize(new java.awt.Dimension(120, 20));
        jLabelDeleteKey.setMinimumSize(new java.awt.Dimension(120, 20));
        jLabelDeleteKey.setPreferredSize(new java.awt.Dimension(120, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelDeleteKey, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelPH1, gridBagConstraints);

        jLabelEditID.setText("Edit Mapping ID: *");
        jLabelEditID.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelEditID.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelEditID.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 170;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelEditID, gridBagConstraints);

        jLabelEditIDKey.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabelEditIDKey.setMaximumSize(new java.awt.Dimension(120, 20));
        jLabelEditIDKey.setMinimumSize(new java.awt.Dimension(120, 20));
        jLabelEditIDKey.setPreferredSize(new java.awt.Dimension(120, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelEditIDKey, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelPH2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelPH3, gridBagConstraints);

        jLabelEditBodyKey.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabelEditBodyKey.setMaximumSize(new java.awt.Dimension(120, 20));
        jLabelEditBodyKey.setMinimumSize(new java.awt.Dimension(120, 20));
        jLabelEditBodyKey.setPreferredSize(new java.awt.Dimension(120, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelEditBodyKey, gridBagConstraints);

        jLabelEditBody.setText("Edit Mapping Body: *");
        jLabelEditBody.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelEditBody.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelEditBody.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 170;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelEditBody, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelPH4, gridBagConstraints);

        jLabelEditHeadKey.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabelEditHeadKey.setMaximumSize(new java.awt.Dimension(120, 20));
        jLabelEditHeadKey.setMinimumSize(new java.awt.Dimension(120, 20));
        jLabelEditHeadKey.setPreferredSize(new java.awt.Dimension(120, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelEditHeadKey, gridBagConstraints);

        jLabelEditHead.setText("Edit Mapping Head: *");
        jLabelEditHead.setMaximumSize(new java.awt.Dimension(100, 20));
        jLabelEditHead.setMinimumSize(new java.awt.Dimension(100, 20));
        jLabelEditHead.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 170;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabelEditHead, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 25;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        jPanel3.add(jPanel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabelPlaceHolder1, gridBagConstraints);

        jLabel6.setText("* changing the shortcut requires a new start of the program.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel6, gridBagConstraints);

        jScrollPane2.setViewportView(jPanel3);

        jTabbedPane1.addTab("Mapping Editing Shortcuts", jScrollPane2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(jTabbedPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonClassFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClassFontActionPerformed

    	FontChooser2 fe =new FontChooser2(jButtonClassFont, MappingManagerPreferences.CLASS_FONTFAMILY, MappingManagerPreferences.CLASS_FONTSIZE, MappingManagerPreferences.CLASS_ISBOLD);
    }//GEN-LAST:event_jButtonClassFontActionPerformed

    private void jButtonDataPropertyFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDataPropertyFontActionPerformed
    	FontChooser2 fe = new FontChooser2(jButtonDataPropertyFont, MappingManagerPreferences.DATAPROPERTY_FONTFAMILY, MappingManagerPreferences.DATAPROPERTY_FONTSIZE, MappingManagerPreferences.DATAPROPERTY_ISBOLD);
    }//GEN-LAST:event_jButtonDataPropertyFontActionPerformed

    private void jButtonObjectPropertyFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonObjectPropertyFontActionPerformed
    	FontChooser2 fe = new FontChooser2(jButtonObjectPropertyFont, MappingManagerPreferences.OBJECTPROPTERTY_FONTFAMILY, MappingManagerPreferences.OBJECTPROPTERTY_FONTSIZE, MappingManagerPreferences.OBJECTPROPTERTY_ISBOLD);
    	
    }//GEN-LAST:event_jButtonObjectPropertyFontActionPerformed

    private void jButtonVariableFont1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonVariableFont1ActionPerformed
    	FontChooser2 fe = new FontChooser2(jButtonVariableFont1, MappingManagerPreferences.VARIABLE_FONTFAMILY, MappingManagerPreferences.VARIABLE_FONTSIZE, MappingManagerPreferences.VARIABLE_ISBOLD);
    	
    }//GEN-LAST:event_jButtonVariableFont1ActionPerformed

    private void jButtonParameterFont1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonParameterFont1ActionPerformed
    	FontChooser2 fe = new FontChooser2(jButtonParameterFont1, MappingManagerPreferences.PARAMETER_FONTFAMILY, MappingManagerPreferences.PARAMETER_FONTSIZE, MappingManagerPreferences.PARAMETER_ISBOLD);
    	;
    }//GEN-LAST:event_jButtonParameterFont1ActionPerformed

    private void jButtonFunctorPropertyFont1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFunctorPropertyFont1ActionPerformed
    	FontChooser2 fe = new FontChooser2(jButtonFunctorPropertyFont1, MappingManagerPreferences.FUCNTOR_FONTFAMILY, MappingManagerPreferences.FUCNTOR_FONTSIZE, MappingManagerPreferences.FUCNTOR_ISBOLD);
    	
    }//GEN-LAST:event_jButtonFunctorPropertyFont1ActionPerformed

    private void jButtonbodyPropertyFont2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonbodyPropertyFont2ActionPerformed
    	FontChooser2 fe = new FontChooser2(jButtonbodyPropertyFont2, MappingManagerPreferences.MAPPING_BODY_FONTFAMILY, MappingManagerPreferences.MAPPING_BODY_FONTSIZE, MappingManagerPreferences.MAPPING_BODY_ISBOLD);
    	
    }//GEN-LAST:event_jButtonbodyPropertyFont2ActionPerformed

    private void jButtonIDFont2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonIDFont2ActionPerformed
    	FontChooser2 fe = new FontChooser2(jButtonIDFont2, MappingManagerPreferences.MAPPING_ID_FONTFAMILY, MappingManagerPreferences.MAPPING_ID_FONTSIZE, MappingManagerPreferences.MAPPING_ID_ISBOLD);
    	
    }//GEN-LAST:event_jButtonIDFont2ActionPerformed

    private void jButtonInvalidQueryFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInvalidQueryFontActionPerformed
    	FontChooser2 fe = new FontChooser2(jButtonInvalidQueryFont, MappingManagerPreferences.INVALIDQUERY_FONTFAMILY, MappingManagerPreferences.INVALIDQUERY_FONTSIZE, MappingManagerPreferences.INVALIDQUERY_ISBOLD);
    }//GEN-LAST:event_jButtonInvalidQueryFontActionPerformed

    private void jButtonDependenciesFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDependenciesFontActionPerformed
    	FontChooser2 fe = new FontChooser2(jButtonDependenciesFont, MappingManagerPreferences.DEPENDENCIES_FONTFAMILY, MappingManagerPreferences.DEPENDENCIES_FONTSIZE, MappingManagerPreferences.DEPENDENCIES_ISBOLD);
    }//GEN-LAST:event_jButtonDependenciesFontActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBodyPropertyColour1;
    private javax.swing.JButton jButtonClassColour;
    private javax.swing.JButton jButtonClassFont;
    private javax.swing.JButton jButtonDataPropertyColour;
    private javax.swing.JButton jButtonDataPropertyFont;
    private javax.swing.JButton jButtonDependenciesColour;
    private javax.swing.JButton jButtonDependenciesFont;
    private javax.swing.JButton jButtonFunctorPropertyColour1;
    private javax.swing.JButton jButtonFunctorPropertyFont1;
    private javax.swing.JButton jButtonIDColour2;
    private javax.swing.JButton jButtonIDFont2;
    private javax.swing.JButton jButtonInvalidQueryColour;
    private javax.swing.JButton jButtonInvalidQueryFont;
    private javax.swing.JButton jButtonObjectPropertyColour;
    private javax.swing.JButton jButtonObjectPropertyFont;
    private javax.swing.JButton jButtonParameterColour;
    private javax.swing.JButton jButtonParameterFont1;
    private javax.swing.JButton jButtonVariableColour1;
    private javax.swing.JButton jButtonVariableFont1;
    private javax.swing.JButton jButtonbodyPropertyFont2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelAdd;
    private javax.swing.JLabel jLabelAddKey;
    private javax.swing.JLabel jLabelBody;
    private javax.swing.JLabel jLabelClass;
    private javax.swing.JLabel jLabelDataProperty;
    private javax.swing.JLabel jLabelDelete;
    private javax.swing.JLabel jLabelDeleteKey;
    private javax.swing.JLabel jLabelDependencies;
    private javax.swing.JLabel jLabelEditBody;
    private javax.swing.JLabel jLabelEditBodyKey;
    private javax.swing.JLabel jLabelEditHead;
    private javax.swing.JLabel jLabelEditHeadKey;
    private javax.swing.JLabel jLabelEditID;
    private javax.swing.JLabel jLabelEditIDKey;
    private javax.swing.JLabel jLabelFunctors;
    private javax.swing.JLabel jLabelID;
    private javax.swing.JLabel jLabelInvalidQuery;
    private javax.swing.JLabel jLabelObjectProperty;
    private javax.swing.JLabel jLabelPH;
    private javax.swing.JLabel jLabelPH1;
    private javax.swing.JLabel jLabelPH2;
    private javax.swing.JLabel jLabelPH3;
    private javax.swing.JLabel jLabelPH4;
    private javax.swing.JLabel jLabelParameters;
    private javax.swing.JLabel jLabelPlaceHolder;
    private javax.swing.JLabel jLabelPlaceHolder1;
    private javax.swing.JLabel jLabelVariables;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelBodyProperty1;
    private javax.swing.JPanel jPanelClassPreferences;
    private javax.swing.JPanel jPanelInvalidDependencies;
    private javax.swing.JPanel jPanelInvalidQuery;
    private javax.swing.JPanel jPanelVariablePreferences;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
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

    	        jScrollPane1 = new javax.swing.JScrollPane();
    	        fontNameChoice = new javax.swing.JList();
    	        jScrollPane2 = new javax.swing.JScrollPane();
    	        fontSizeChoice = new javax.swing.JList();
    	        bold = new javax.swing.JCheckBox();
    	        jPanel1 = new javax.swing.JPanel();
//    	        jButtonCancel = new javax.swing.JButton();
//    	        jButtonOK = new javax.swing.JButton();

    	        setMinimumSize(new java.awt.Dimension(400, 300));
    	        setLayout(new java.awt.GridBagLayout());

    	        fontNameChoice.setModel(new javax.swing.AbstractListModel() {
    	            String[] strings = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    	            public int getSize() { return strings.length; }
    	            public Object getElementAt(int i) { return strings[i]; }
    	        });
    	        jScrollPane1.setViewportView(fontNameChoice);

    	        gridBagConstraints = new java.awt.GridBagConstraints();
    	        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    	        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    	        gridBagConstraints.weightx = 1.0;
    	        gridBagConstraints.weighty = 1.0;
    	        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    	        add(jScrollPane1, gridBagConstraints);

    	        fontSizeChoice.setModel(new javax.swing.AbstractListModel() {
    	            String[] strings = { "8", "10", "11", "12", "14", "16", "18", "20", "24", "30", "36", "40", "48", "60", "72" };
    	            public int getSize() { return strings.length; }
    	            public Object getElementAt(int i) { return strings[i]; }
    	        });
    	        fontSizeChoice.setMaximumSize(new java.awt.Dimension(50, 285));
    	        fontSizeChoice.setMinimumSize(new java.awt.Dimension(50, 285));
    	        fontSizeChoice.setPreferredSize(new java.awt.Dimension(50, 285));
    	        jScrollPane2.setViewportView(fontSizeChoice);

    	        gridBagConstraints = new java.awt.GridBagConstraints();
    	        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    	        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    	        gridBagConstraints.weightx = 1.0;
    	        gridBagConstraints.weighty = 1.0;
    	        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    	        add(jScrollPane2, gridBagConstraints);

    	        bold.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
    	        bold.setText("Bold");
    	        bold.setBorder(null);
    	        gridBagConstraints = new java.awt.GridBagConstraints();
    	        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    	        gridBagConstraints.weightx = 1.0;
    	        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    	        add(bold, gridBagConstraints);

    	        jPanel1.setLayout(new java.awt.GridBagLayout());
    		  
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


