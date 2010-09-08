package inf.unibz.it.obda.gui.swing.dependencies.treemodel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSInclusionDependency;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
* A customized tree cell renderer to show the results of an assertion
* validation
* 
* @author Manfred Gerstgrasser
* 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
*
*/

public class ValidationTreeCellRenderer extends
DefaultTreeCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5069319474074524380L;
	private JLabel						label							= null;
	private JTextPane					area							= null;
	private JPanel						panel	=null;
	private APIController	apic;
	
	public ValidationTreeCellRenderer(APIController apic){
		
			this.apic = apic;
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		return createComponent(node, tree, sel);
	}
	
	private JPanel createComponent(DefaultMutableTreeNode node, JTree t, boolean selected) {
		
		try {
			java.awt.GridBagConstraints grid;
			GridBagLayout l = new GridBagLayout();
			panel = new JPanel();
			panel.setLayout(l);
			label = new JLabel();
			area = new JTextPane();
			panel.setBackground(Color.white);
			label.setBackground(Color.white);
			label.setHorizontalAlignment(SwingConstants.LEFT);
			JLabel ph = new JLabel();
			ph.setVisible(false);
			ph.setHorizontalAlignment(SwingConstants.LEADING);		
			
			if(node.getUserObject() instanceof Object[]){
				Object[] ob = (Object[]) node.getUserObject();
				if(ob[0] instanceof RDBMSInclusionDependency){
					RDBMSInclusionDependency inc = (RDBMSInclusionDependency) ob[0];
		    		Boolean bool = (Boolean) ob[1];
					if(bool.booleanValue()){
						String txt = inc.toString() + " is satisfied";
						area.setForeground(Color.GREEN.darker());
						area.setText(txt);
					}else{
						String txt = inc.toString() + " is not satisfied";
						area.setForeground(Color.RED.brighter());
						area.setText(txt);
					}
				}else if(ob[0] instanceof RDBMSDisjointnessDependency){
					RDBMSDisjointnessDependency dis = (RDBMSDisjointnessDependency)ob[0];
					Boolean bool = (Boolean) ob[1];
					if(bool.booleanValue()){
						String txt = dis.toString() + " is satisfied";
						area.setForeground(Color.GREEN.darker());
						area.setText(txt);
					}else{
						String txt = dis.toString() + " is not satisfied";
						area.setForeground(Color.RED.brighter());
						area.setText(txt);
					}
				}else {
					area.setText(ob[0].toString());
				}
			}else{
				area.setText(node.getUserObject().toString());
			}
			label.setIcon(super.getIcon());
			grid = new java.awt.GridBagConstraints();
			grid.gridx = 0;
			grid.gridy = 0;
			grid.gridwidth = 1;
			grid.weightx = 0;
			grid.weighty = 0;
			grid.fill = java.awt.GridBagConstraints.VERTICAL;
			panel.add(label, grid);

			grid = new java.awt.GridBagConstraints();
			grid.gridx = 1;
			grid.gridy = 0;
			grid.gridwidth = java.awt.GridBagConstraints.REMAINDER;
			grid.fill = java.awt.GridBagConstraints.BOTH;
			grid.weightx = 1.0;
			grid.weighty = 1.0;
			panel.add(area, grid);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return panel;
	}
}
