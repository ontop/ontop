package inf.unibz.it.utils.swing;

import java.util.Vector;

import javax.swing.JDialog;

public class OBDAProgessMonitor{

	private Vector<OBDAProgressListener> listeners = null;
	private JDialog parent = null;
	
	public OBDAProgessMonitor(){
		listeners = new Vector<OBDAProgressListener>();
	}
	
	public void start(){
		ProgressPanel panel = new ProgressPanel(this);
		parent = new JDialog();
		parent.setContentPane(panel);
		parent.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		parent.setSize(280, 140);
		parent.setVisible(true);
		parent.setLocationRelativeTo(null);
	}
	
	public void stop(){
		parent.setVisible(false);
		parent.dispose();
	}
	
	public void addProgressListener(OBDAProgressListener list){
		listeners.add(list);
	}
	
	public void removeProgressListener(OBDAProgressListener list){
		listeners.remove(list);
	}
	
	public void triggerActionCanceled(){
		for(OBDAProgressListener pl : listeners){
			pl.actionCanceled();
		}
	}
}
