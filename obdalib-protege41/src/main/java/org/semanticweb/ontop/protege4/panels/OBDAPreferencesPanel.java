package org.semanticweb.ontop.protege4.panels;

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

import org.semanticweb.ontop.utils.OBDAPreferences;

public class OBDAPreferencesPanel extends javax.swing.JPanel {
	
	private static final long serialVersionUID = 1L;
	private OBDAPreferences pref = null;
	private HashMap<String, KeyStroke> shortCuts = new HashMap<String, KeyStroke>();
	
	/**
	 * The constructor 
	 */
    public OBDAPreferencesPanel(OBDAPreferences preference) {
    	pref = preference;
        initComponents();
        addListener();
        applyPreferences();
    }

    private boolean isKeyStrokeAlreadyAssigned(KeyStroke stroke){
    	
    	return shortCuts.containsValue(stroke);
    }
    
    private void addListener(){    	
    	cmdClassColor.addActionListener(new ActionListener(){
    		@Override
			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(cmdClassColor, OBDAPreferences.CLASS_COLOR);
				cc.setVisible(true);
			}
    	});
    	
    	cmdDataPropertyColor.addActionListener(new ActionListener(){
    		@Override
			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(cmdDataPropertyColor, OBDAPreferences.DATAPROPERTY_COLOR);
				cc.setVisible(true);
			}
    	});
    	
    	cmdFunctorColor.addActionListener(new ActionListener(){
    		@Override
			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(cmdFunctorColor, OBDAPreferences.FUCNTOR_COLOR);
				cc.setVisible(true);
			}
    	});
    	
    	cmdObjectPropertyColor.addActionListener(new ActionListener(){
    		@Override
			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(cmdObjectPropertyColor, OBDAPreferences.OBJECTPROPTERTY_COLOR);
				cc.setVisible(true);
			}
    	});
    	
    	cmdParameterColor.addActionListener(new ActionListener(){
    		@Override
			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(cmdParameterColor, OBDAPreferences.PARAMETER_COLOR);
				cc.setVisible(true);
			}
    	});
    	
    	cmdVariableColor.addActionListener(new ActionListener(){
    		@Override
			public void actionPerformed(ActionEvent arg0) {
				ColorChooser cc = new ColorChooser(cmdVariableColor, OBDAPreferences.VARIABLE_COLOR);
				cc.setVisible(true);
			}
    	});
    	
    	String aux = pref.get(OBDAPreferences.ADD_MAPPING).toString();
    	KeyStroke ks = KeyStroke.getKeyStroke(aux);
    	lblAddMappingKey.setText(KeyEvent.getKeyModifiersText(ks.getModifiers()) + " + "+ KeyEvent.getKeyText(ks.getKeyCode()));
    	lblAddMappingKey.addMouseListener(new MouseListener(){
    		@Override
			public void mouseClicked(MouseEvent e) {
				lblAddMappingKey.setText("");
				lblAddMappingKey.requestFocus();
			}
    		@Override
			public void mouseEntered(MouseEvent e) {
    			// NO-OP
    		}
    		@Override
    		public void mouseExited(MouseEvent e) {
    			// NO-OP
    		}
    		@Override
    		public void mousePressed(MouseEvent e) {
    			// NO-OP
    		}
    		@Override
    		public void mouseReleased(MouseEvent e) {
    			// NO-OP
    		}
    	});
    	
    	lblAddMappingKey.addKeyListener(new KeyListener(){
    		@Override
			public void keyPressed(KeyEvent e) {
				int mod = e.getModifiers();
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_CONTROL || key ==KeyEvent.VK_CANCEL 
						|| key ==KeyEvent.VK_ALT || key ==KeyEvent.VK_ALT_GRAPH
						|| key ==KeyEvent.VK_SHIFT || key ==KeyEvent.VK_ESCAPE){
					return;
				}
				KeyStroke stroke = KeyStroke.getKeyStroke(key, mod);
				if (!isKeyStrokeAlreadyAssigned(stroke)){
					shortCuts.put(OBDAPreferences.ADD_MAPPING, stroke);
				    lblAddMappingKey.setText(KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + "+ KeyEvent.getKeyText(stroke.getKeyCode()));
					lblAddMappingKey.setToolTipText(stroke.toString());
					pref.put(OBDAPreferences.ADD_MAPPING, stroke.toString());
				} else {
					KeyStroke oldValue = shortCuts.get(OBDAPreferences.ADD_MAPPING);
					if(oldValue != null){
						lblAddMappingKey.setText(KeyEvent.getKeyModifiersText(oldValue.getModifiers()) + " + "+ KeyEvent.getKeyText(oldValue.getKeyCode()));
					}
					JOptionPane.showMessageDialog(null, "Key stroke already assigned. Please choose an other combination.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
    		@Override
			public void keyReleased(KeyEvent e) {
    			// NO-OP
    		}
    		@Override
			public void keyTyped(KeyEvent e) {
				// NO-OP
			}
    	});
    	
    	String aux2 = pref.get(OBDAPreferences.EDIT_BODY).toString();
    	KeyStroke ks2 = KeyStroke.getKeyStroke(aux2);
    	lblEditMappingBodyKey.setText(KeyEvent.getKeyModifiersText(ks2.getModifiers()) + " + "+ KeyEvent.getKeyText(ks2.getKeyCode()));
    	lblEditMappingBodyKey.addMouseListener(new MouseListener(){
    		@Override
			public void mouseClicked(MouseEvent e) {
				lblEditMappingBodyKey.setText("");
				lblEditMappingBodyKey.requestFocus();
			}
    		@Override
			public void mouseEntered(MouseEvent e) {
    			// NO-OP
    		}
    		@Override
    		public void mouseExited(MouseEvent e) {
    			// NO-OP
    		}
    		@Override
    		public void mousePressed(MouseEvent e) {
    			// NO-OP
    		}
    		@Override
    		public void mouseReleased(MouseEvent e) {
    			// NO-OP
    		}
    	});
    	lblEditMappingBodyKey.addKeyListener(new KeyListener(){
    		@Override
			public void keyPressed(KeyEvent e) {
				int mod = e.getModifiers();
				int key = e.getKeyCode();
				if(key == KeyEvent.VK_CONTROL || key ==KeyEvent.VK_CANCEL
						|| key ==KeyEvent.VK_ALT || key ==KeyEvent.VK_ALT_GRAPH
						|| key ==KeyEvent.VK_SHIFT || key ==KeyEvent.VK_ESCAPE){
					return;
				}
				KeyStroke stroke = KeyStroke.getKeyStroke(key, mod);
				if (!isKeyStrokeAlreadyAssigned(stroke)){
					shortCuts.put(OBDAPreferences.EDIT_BODY, stroke);
					lblEditMappingBodyKey.setText(KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + "+ KeyEvent.getKeyText(stroke.getKeyCode()));
					lblEditMappingBodyKey.setToolTipText(stroke.toString());
					pref.put(OBDAPreferences.EDIT_BODY, stroke.toString());
				} else {
					KeyStroke oldValue = shortCuts.get(OBDAPreferences.EDIT_BODY);
					if(oldValue != null){
						lblEditMappingBodyKey.setText(KeyEvent.getKeyModifiersText(oldValue.getModifiers()) + " + "+ KeyEvent.getKeyText(oldValue.getKeyCode()));
					}
					JOptionPane.showMessageDialog(null, "Key stroke already assigned. Please choose an other combination.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
    		@Override
			public void keyReleased(KeyEvent e) {
    			// NO-OP
    		}
    		@Override
			public void keyTyped(KeyEvent e) {
    			// NO-OP
    		}
    	});
    	
    	String aux3 = pref.get(OBDAPreferences.EDIT_HEAD).toString();
    	KeyStroke ks3 = KeyStroke.getKeyStroke(aux3);
    	lblEditMappingHeadKey.setText(KeyEvent.getKeyModifiersText(ks3.getModifiers()) + " + "+ KeyEvent.getKeyText(ks3.getKeyCode()));
    	lblEditMappingHeadKey.addMouseListener(new MouseListener(){
    		@Override
			public void mouseClicked(MouseEvent e) {
				lblEditMappingHeadKey.setText("");
				lblEditMappingHeadKey.requestFocus();
			}
    		@Override
			public void mouseEntered(MouseEvent e) {
    			// NO-OP
    		}
    		@Override
    		public void mouseExited(MouseEvent e) {
    			// NO-OP
    		}
    		@Override
    		public void mousePressed(MouseEvent e) {
    			// NO-OP
    		}
    		@Override
    		public void mouseReleased(MouseEvent e) {
    			// NO-OP
    		}
    	});
    	lblEditMappingHeadKey.addKeyListener(new KeyListener(){
    		@Override
			public void keyPressed(KeyEvent e) {
				int mod = e.getModifiers();
				int key = e.getKeyCode();
				if(key == KeyEvent.VK_CONTROL || key ==KeyEvent.VK_CANCEL 
						|| key ==KeyEvent.VK_ALT || key ==KeyEvent.VK_ALT_GRAPH
						|| key ==KeyEvent.VK_SHIFT || key ==KeyEvent.VK_ESCAPE){
					return;
				}
				KeyStroke stroke = KeyStroke.getKeyStroke(key, mod);
				if (!isKeyStrokeAlreadyAssigned(stroke)){
					shortCuts.put(OBDAPreferences.EDIT_HEAD, stroke);
					lblEditMappingHeadKey.setText(KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + "+ KeyEvent.getKeyText(stroke.getKeyCode()));
					lblEditMappingHeadKey.setToolTipText(stroke.toString());
					pref.put(OBDAPreferences.EDIT_HEAD, stroke.toString());
				} else {
					KeyStroke oldValue = shortCuts.get(OBDAPreferences.EDIT_HEAD);
					if(oldValue != null){
						lblEditMappingHeadKey.setText(KeyEvent.getKeyModifiersText(oldValue.getModifiers()) + " + "+ KeyEvent.getKeyText(oldValue.getKeyCode()));
					}
					JOptionPane.showMessageDialog(null, "Key stroke already assigned. Please choose an other combination.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
    		@Override
			public void keyReleased(KeyEvent e) {
    			// NO-OP
    		}
    		@Override
			public void keyTyped(KeyEvent e) {
				// NO-OP
			}
    	});
    	
    	String aux4 = pref.get(OBDAPreferences.EDIT_ID).toString();
    	KeyStroke ks4 = KeyStroke.getKeyStroke(aux4);
    	lblMappingIdKey.setText(KeyEvent.getKeyModifiersText(ks4.getModifiers()) + " + "+ KeyEvent.getKeyText(ks4.getKeyCode()));
    	lblMappingIdKey.addMouseListener(new MouseListener(){
    		@Override
			public void mouseClicked(MouseEvent e) {
				lblMappingIdKey.setText("");
				lblMappingIdKey.requestFocus();
			}
    		@Override
			public void mouseEntered(MouseEvent e) {
    			// NO-OP
    		}
    		@Override
    		public void mouseExited(MouseEvent e) {
    			// NO-OP
    		}
    		@Override
    		public void mousePressed(MouseEvent e) {
    			// NO-OP
    		}
    		@Override
    		public void mouseReleased(MouseEvent e) {
    			// NO-OP
    		}	
    	});
    	
    	lblMappingIdKey.addKeyListener(new KeyListener(){
    		@Override
			public void keyPressed(KeyEvent e) {
				int mod = e.getModifiers();
				int key = e.getKeyCode();
				if(key == KeyEvent.VK_CONTROL || key ==KeyEvent.VK_CANCEL 
						|| key ==KeyEvent.VK_ALT || key ==KeyEvent.VK_ALT_GRAPH
						|| key ==KeyEvent.VK_SHIFT || key ==KeyEvent.VK_ESCAPE){
					return;
				}
				KeyStroke stroke = KeyStroke.getKeyStroke(key, mod);
				if (!isKeyStrokeAlreadyAssigned(stroke)){
					shortCuts.put(OBDAPreferences.EDIT_ID, stroke);
					lblMappingIdKey.setText(KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + "+ KeyEvent.getKeyText(stroke.getKeyCode()));
					lblMappingIdKey.setToolTipText(stroke.toString());
					pref.put(OBDAPreferences.EDIT_ID, stroke.toString());
				} else {
					KeyStroke oldValue = shortCuts.get(OBDAPreferences.EDIT_ID);
					if(oldValue != null){
						lblMappingIdKey.setText(KeyEvent.getKeyModifiersText(oldValue.getModifiers()) + " + "+ KeyEvent.getKeyText(oldValue.getKeyCode()));
					}
					JOptionPane.showMessageDialog(null, "Key stroke already assigned. Please choose an other combination.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
    		@Override
			public void keyReleased(KeyEvent e) {
    			// NO-OP
    		}
    		@Override
			public void keyTyped(KeyEvent e) {
				// NO-OP
			}
    	});
    }
    
    private void applyPreferences(){
    	Color clazz = new Color(Integer.parseInt(pref.get(OBDAPreferences.CLASS_COLOR).toString()));
    	cmdClassColor.setBackground(clazz);
    	cmdClassColor.setOpaque(true);
    	cmdClassColor.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	
    	Color dp = new Color(Integer.parseInt(pref.get(OBDAPreferences.DATAPROPERTY_COLOR).toString()));
    	cmdDataPropertyColor.setBackground(dp);
    	cmdDataPropertyColor.setOpaque(true);
    	cmdDataPropertyColor.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	
    	Color op =new Color(Integer.parseInt(pref.get(OBDAPreferences.OBJECTPROPTERTY_COLOR).toString()));
    	cmdObjectPropertyColor.setBackground(op);
    	cmdObjectPropertyColor.setOpaque(true);
    	cmdObjectPropertyColor.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	
    	Color var = new Color(Integer.parseInt(pref.get(OBDAPreferences.VARIABLE_COLOR).toString()));
    	cmdVariableColor.setBackground(var);
    	cmdVariableColor.setOpaque(true);
    	cmdVariableColor.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	
    	Color par = new Color(Integer.parseInt(pref.get(OBDAPreferences.PARAMETER_COLOR).toString()));
    	cmdParameterColor.setBackground(par);
    	cmdParameterColor.setOpaque(true);
    	cmdParameterColor.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	
    	Color fun = new Color(Integer.parseInt(pref.get(OBDAPreferences.FUCNTOR_COLOR).toString()));
    	cmdFunctorColor.setBackground(fun);
    	cmdFunctorColor.setOpaque(true);
    	cmdFunctorColor.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
    	
    	String fontBody = pref.get(OBDAPreferences.OBDAPREFS_FONTFAMILY).toString();
    	int bodySize = Integer.parseInt(pref.get(OBDAPreferences.OBDAPREFS_FONTSIZE).toString());
    	cmdFontFamily.setText(fontBody + ", " + bodySize);
    	cmdFontFamily.setToolTipText(fontBody + ", " + bodySize);
    	
    	jCheckBoxUseDefault.setSelected(new Boolean(pref.get(OBDAPreferences.USE_DEAFAULT).toString()));
		if (jCheckBoxUseDefault.isSelected()) {
			cmdFontFamily.setEnabled(false);
		} else {
			cmdFontFamily.setEnabled(true);
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

        tabMainPanel = new javax.swing.JTabbedPane();
        pnlDisplayPreferencesTab = new javax.swing.JPanel();
        pnlClassPreferences = new javax.swing.JPanel();
        lblClass = new javax.swing.JLabel();
        cmdClassColor = new javax.swing.JButton();
        lblDataProperty = new javax.swing.JLabel();
        cmdDataPropertyColor = new javax.swing.JButton();
        lblObjectProperty = new javax.swing.JLabel();
        cmdObjectPropertyColor = new javax.swing.JButton();
        pnlVariablePreferences = new javax.swing.JPanel();
        lblParameter = new javax.swing.JLabel();
        cmdParameterColor = new javax.swing.JButton();
        lblVariable = new javax.swing.JLabel();
        cmdVariableColor = new javax.swing.JButton();
        lblFunctors = new javax.swing.JLabel();
        cmdFunctorColor = new javax.swing.JButton();
        pnlMappingPreferences = new javax.swing.JPanel();
        lblMappingBody = new javax.swing.JLabel();
        cmdFontFamily = new javax.swing.JButton();
        jCheckBoxUseDefault = new javax.swing.JCheckBox();
        jLabelplaceholder2 = new javax.swing.JLabel();
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
        jLabelPlaceholder = new javax.swing.JLabel();

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
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        pnlClassPreferences.add(lblClass, gridBagConstraints);

        cmdClassColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdClassColor.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdClassColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdClassColor.setPreferredSize(new java.awt.Dimension(120, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
        pnlClassPreferences.add(cmdClassColor, gridBagConstraints);

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
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        pnlClassPreferences.add(lblDataProperty, gridBagConstraints);

        cmdDataPropertyColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdDataPropertyColor.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdDataPropertyColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdDataPropertyColor.setPreferredSize(new java.awt.Dimension(120, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
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
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        pnlClassPreferences.add(lblObjectProperty, gridBagConstraints);

        cmdObjectPropertyColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdObjectPropertyColor.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdObjectPropertyColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdObjectPropertyColor.setPreferredSize(new java.awt.Dimension(120, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
        pnlClassPreferences.add(cmdObjectPropertyColor, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        pnlDisplayPreferencesTab.add(pnlClassPreferences, gridBagConstraints);

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
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        pnlVariablePreferences.add(lblParameter, gridBagConstraints);

        cmdParameterColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdParameterColor.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdParameterColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdParameterColor.setPreferredSize(new java.awt.Dimension(120, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
        pnlVariablePreferences.add(cmdParameterColor, gridBagConstraints);

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
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        pnlVariablePreferences.add(lblVariable, gridBagConstraints);

        cmdVariableColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdVariableColor.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdVariableColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdVariableColor.setPreferredSize(new java.awt.Dimension(120, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
        pnlVariablePreferences.add(cmdVariableColor, gridBagConstraints);

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
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        pnlVariablePreferences.add(lblFunctors, gridBagConstraints);

        cmdFunctorColor.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdFunctorColor.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdFunctorColor.setMinimumSize(new java.awt.Dimension(60, 17));
        cmdFunctorColor.setPreferredSize(new java.awt.Dimension(120, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
        pnlVariablePreferences.add(cmdFunctorColor, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        pnlDisplayPreferencesTab.add(pnlVariablePreferences, gridBagConstraints);

        pnlMappingPreferences.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.lightGray), "Font Preferences"));
        pnlMappingPreferences.setMinimumSize(new java.awt.Dimension(475, 90));
        pnlMappingPreferences.setPreferredSize(new java.awt.Dimension(475, 90));
        pnlMappingPreferences.setLayout(new java.awt.GridBagLayout());

        lblMappingBody.setText("Font Family:");
        lblMappingBody.setMaximumSize(new java.awt.Dimension(100, 20));
        lblMappingBody.setMinimumSize(new java.awt.Dimension(100, 20));
        lblMappingBody.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        pnlMappingPreferences.add(lblMappingBody, gridBagConstraints);

        cmdFontFamily.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        cmdFontFamily.setMaximumSize(new java.awt.Dimension(200, 17));
        cmdFontFamily.setMinimumSize(new java.awt.Dimension(90, 17));
        cmdFontFamily.setPreferredSize(new java.awt.Dimension(120, 17));
        cmdFontFamily.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdFontFamilyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
        pnlMappingPreferences.add(cmdFontFamily, gridBagConstraints);

        jCheckBoxUseDefault.setText("Use default fonts        ");
        jCheckBoxUseDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseDefaultActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 20);
        pnlMappingPreferences.add(jCheckBoxUseDefault, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        pnlDisplayPreferencesTab.add(pnlMappingPreferences, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weighty = 1.0;
        pnlDisplayPreferencesTab.add(jLabelplaceholder2, gridBagConstraints);

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

        lblEditMappingHead.setText("Edit Target Query: *");
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

        lblEditMappingBody.setText("Edit Source Query: *");
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weighty = 1.0;
        pnlEditingShortcutTab.add(jLabelPlaceholder, gridBagConstraints);

        tabMainPanel.addTab("Mapping Editing Shortcut", pnlEditingShortcutTab);

        add(tabMainPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void cmdFontFamilyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdFontFamilyActionPerformed
    	FontChooser2 fe = new FontChooser2(cmdFontFamily, OBDAPreferences.OBDAPREFS_FONTFAMILY, OBDAPreferences.OBDAPREFS_FONTSIZE, OBDAPreferences.OBDAPREFS_ISBOLD);
    }//GEN-LAST:event_cmdFontFamilyActionPerformed

    private void jCheckBoxUseDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUseDefaultActionPerformed
        
    	if(jCheckBoxUseDefault.isSelected()){
    		pref.put(OBDAPreferences.USE_DEAFAULT, "true");
    		cmdFontFamily.setEnabled(false);
    	}else{
    		pref.put(OBDAPreferences.USE_DEAFAULT, "false");
    		cmdFontFamily.setEnabled(true);
    	}
    	
    }//GEN-LAST:event_jCheckBoxUseDefaultActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdClassColor;
    private javax.swing.JButton cmdDataPropertyColor;
    private javax.swing.JButton cmdFontFamily;
    private javax.swing.JButton cmdFunctorColor;
    private javax.swing.JButton cmdObjectPropertyColor;
    private javax.swing.JButton cmdParameterColor;
    private javax.swing.JButton cmdVariableColor;
    private javax.swing.JCheckBox jCheckBoxUseDefault;
    private javax.swing.JLabel jLabelPlaceholder;
    private javax.swing.JLabel jLabelplaceholder2;
    private javax.swing.JLabel lblAddMapping;
    private javax.swing.JLabel lblAddMappingKey;
    private javax.swing.JLabel lblClass;
    private javax.swing.JLabel lblDataProperty;
    private javax.swing.JLabel lblDeleteMapping;
    private javax.swing.JLabel lblDeleteMappingKey;
    private javax.swing.JLabel lblEditMappingBody;
    private javax.swing.JLabel lblEditMappingBodyKey;
    private javax.swing.JLabel lblEditMappingHead;
    private javax.swing.JLabel lblEditMappingHeadKey;
    private javax.swing.JLabel lblEditMappingId;
    private javax.swing.JLabel lblFunctors;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblMappingBody;
    private javax.swing.JLabel lblMappingIdKey;
    private javax.swing.JLabel lblObjectProperty;
    private javax.swing.JLabel lblParameter;
    private javax.swing.JLabel lblVariable;
    private javax.swing.JPanel pnlClassPreferences;
    private javax.swing.JPanel pnlDisplayPreferencesTab;
    private javax.swing.JPanel pnlEditingShortcutTab;
    private javax.swing.JPanel pnlMappingPreferences;
    private javax.swing.JPanel pnlShortcutSettings;
    private javax.swing.JPanel pnlVariablePreferences;
    private javax.swing.JTabbedPane tabMainPanel;
    // End of variables declaration//GEN-END:variables
    
    private class ColorChooser extends JPanel {

		private static final long serialVersionUID = -5704647065277117955L;
		JButton button = null;
    	String key = null;
    	
    	private ColorChooser(JButton button, String key){
    		super();
    		this.button = button;
    		this.key = key;
    		this.setSize(500,500);
    		initComponents();
    		JOptionPane pane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    		JDialog d = pane.createDialog("Color Chooser");
    		d.setModal(true);
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
            jLabelplaceholder2 = new javax.swing.JLabel();
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
            add(jLabelplaceholder2, gridBagConstraints);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridheight = 3;
            gridBagConstraints.weightx = 1.0;
            add(jLabel2, gridBagConstraints);
        }// </editor-fold>

        private void applyPreferences() {
        	Color aux = jColorChooser1.getSelectionModel().getSelectedColor();
        	Integer rgb = aux.getRGB();
        	pref.put(key, rgb.toString());
        	button.setBackground(aux);
        	button.setOpaque(true);
        }

        // Variables declaration - do not modify
        private javax.swing.JColorChooser jColorChooser1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        // End of variables declaration
    }
    
	public class FontChooser2 extends JPanel {

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
		protected String fontSizes[] = { "8", "10", "11", "12", "14", "16",
				"18", "20", "24", "30", "36", "40", "48", "60", "72" };

		/** The index of the default size (e.g., 14 point == 4) */
		protected static final int DEFAULT_SIZE = 4;

		protected String fontfamily = null;

		protected String fontsize = null;

		protected String isbold = null;
   	  
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
			d.setModal(true);
			d.setSize(400, 300);
			d.setResizable(true);
			d.setVisible(true);
			Object o = pane.getValue();
			if (o != null) {
				int ret = (Integer) o;
				if (ret == JOptionPane.OK_OPTION) {
					applyChanges();
				}
			}
		}
    	  
		private void init() {

			java.awt.GridBagConstraints gridBagConstraints;
			javax.swing.JScrollPane scrFontNameList;
			javax.swing.JScrollPane scrFontSizeList;

			fontNameChoice = new javax.swing.JList();
			fontSizeChoice = new javax.swing.JList();
			scrFontNameList = new javax.swing.JScrollPane();
			scrFontSizeList = new javax.swing.JScrollPane();
			bold = new javax.swing.JCheckBox();
			pnlDisplayPreferencesTab = new javax.swing.JPanel();

			setMinimumSize(new java.awt.Dimension(400, 300));
			setLayout(new java.awt.GridBagLayout());

			fontNameChoice.setModel(new javax.swing.AbstractListModel() {
				private static final long serialVersionUID = -1794346954116474402L;
				String[] strings = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
				@Override
				public int getSize() {
					return strings.length;
				}
				@Override
				public Object getElementAt(int i) {
					return strings[i];
				}
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
				private static final long serialVersionUID = 4046867275908672992L;
				String[] strings = { "8", "10", "11", "12", "14", "16", "18",
						"20", "24", "30", "36", "40", "48", "60", "72" };
				@Override
				public int getSize() {
					return strings.length;
				}
				@Override
				public Object getElementAt(int i) {
					return strings[i];
				}
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

			String currentFont = pref.get(OBDAPreferences.OBDAPREFS_FONTFAMILY).toString();
			int currentSize = Integer.parseInt(pref.get(OBDAPreferences.OBDAPREFS_FONTSIZE).toString());
			Boolean isBold = new Boolean(pref.get(OBDAPreferences.OBDAPREFS_ISBOLD).toString());
			int i = getIndexOfFont(currentFont);
			int j = getIndexOfSize(String.valueOf(currentSize));
			fontNameChoice.setSelectedIndex(i);
			fontSizeChoice.setSelectedIndex(j);
			bold.setSelected(isBold);
		}
    	  
		private int getIndexOfFont(String font) {
			AbstractListModel model = (AbstractListModel) fontNameChoice.getModel();
			int size = model.getSize();
			for (int i = 0; i < size; i++) {
				if (model.getElementAt(i).equals(font)) {
					return i;
				}
			}
			return 0;
		}
    	  
		private int getIndexOfSize(String size) {
			AbstractListModel model = (AbstractListModel) fontSizeChoice.getModel();
			int length = model.getSize();
			for (int i = 0; i < length; i++) {
				if (model.getElementAt(i).equals(size)) {
					return i;
				}
			}
			return 0;
		}

		protected void applyChanges() {
			resultName = (String) fontNameChoice.getSelectedValue();
			String resultSizeName = (String) fontSizeChoice.getSelectedValue();
			isBold = bold.isSelected();
			pref.put(OBDAPreferences.OBDAPREFS_FONTFAMILY, resultName);
			pref.put(OBDAPreferences.OBDAPREFS_FONTSIZE, resultSizeName);
			pref.put(OBDAPreferences.OBDAPREFS_ISBOLD, new Boolean(isBold).toString());
			button.setText(resultName + ", " + resultSizeName);
			button.setToolTipText(resultName + ", " + resultSizeName);
		}
    	  
		/**
		 * Called from the action handlers to get the font info, build a font,
		 * and set it.
		 */
		protected void previewFont() {
			resultName = (String) fontNameChoice.getSelectedValue();
			String resultSizeName = (String) fontSizeChoice.getSelectedValue();
			int resultSize = Integer.parseInt(resultSizeName);
			isBold = bold.isSelected();
			int attrs = Font.PLAIN;
			if (isBold) {
				attrs = Font.BOLD;
			}
			if (isItalic) {
				attrs |= Font.ITALIC;
			}
			resultFont = new Font(resultName, attrs, resultSize);
		}

		/** 
		 * Retrieve the selected font name. 
		 */
		public String getSelectedName() {
			return resultName;
		}

		/**
		 * Retrieve the selected size 
		 */
		public int getSelectedSize() {
			return resultSize;
		}

		/** 
		 * Retrieve the selected font, or null
		 */
		public Font getSelectedFont() {
			return resultFont;
		}
	}
}
