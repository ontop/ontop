/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package inf.unibz.it.obda.gui.swing.dependencies.panel;

import inf.unibz.it.utils.swing.DialogUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;


/**
 *A simple dialog, showing the results of the assertion validation
 *
  * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
 */
public class ValidateDepenencyDialog extends JDialog{
	
	
	private static final long		serialVersionUID	= -3099215855478663834L;
	private JDialog					myself				= null;
	private DefaultStyledDocument	doc					= null;
	private JTree					parent				= null;
	private int						index				= 0;
	
	public Style					VALID				= null;
	public Style					ERROR				= null;
	public Style					NORMAL				= null;
	
	public boolean					closed				= false;
	
	public ValidateDepenencyDialog(JTree tree){
		super();
		myself = this;
		doc = new DefaultStyledDocument();
		parent = tree;
		createStyles();
		createContent();
		DialogUtils.centerDialogWRTParent(tree.getParent(), this);
	} 
	
	private void createStyles() {

		StyleContext context = new StyleContext();
		VALID = context.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(VALID, "Arial");
		StyleConstants.setFontSize(VALID, 12);
		StyleConstants.setForeground(VALID, Color.GREEN.darker());

		StyleContext context1 = new StyleContext();
		ERROR = context1.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(ERROR, "Arial");
		StyleConstants.setFontSize(ERROR, 12);
		StyleConstants.setForeground(ERROR, Color.RED);
		
		StyleContext context3 = new StyleContext();
		NORMAL = context3.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(NORMAL, "Arial");
		StyleConstants.setFontSize(NORMAL, 11);
		StyleConstants.setForeground(NORMAL, Color.BLACK);
	}
	
	private void createContent() {

		this.setTitle("Validate Dependencies...");
		this.setSize(new Dimension(700, 400));
		Container panel = this.getContentPane();
		panel.setLayout(new BorderLayout());
		JTextPane area = new JTextPane();
		area.setBounds(0, 0, 298, 273);
		area.setEditable(false);
		area.setBackground(Color.WHITE);
		area.setDocument(doc);
		JScrollPane areaScrollPane = new JScrollPane(area);
		areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setBounds(0, 0, 300, 275);

		JButton button = new JButton();
		button.setText("OK");
		button.setBounds(120, 290, 60, 25);
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				closed = true;
				myself.dispose();
			}

		});
		button.requestFocus();

		panel.add(areaScrollPane, BorderLayout.CENTER);
		panel.add(button, BorderLayout.SOUTH);
		// this.setLocationRelativeTo(parent);
		this.setResizable(true);
	}

	/***
	 * Adds the text synchorniously. Do not call from the Event thread. Use a
	 * working thread.
	 * 
	 * @param text
	 * @param style
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	public void addText(final String text, final Style style) {

		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					try {
						doc.insertString(index, text, style);
						index = index + text.length();
						invalidate();
						repaint();

					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}

			});
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
