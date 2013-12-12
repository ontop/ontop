package it.unibz.krdb.obda.protege4.utils;

import it.unibz.krdb.obda.protege4.utils.OBDAProgessMonitor;
import it.unibz.krdb.obda.protege4.utils.OBDAProgressListener;
import it.unibz.krdb.obda.protege4.utils.ProgressPanel;

import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

public class OBDAProgessMonitor {

	private JDialog parent = new JDialog();
	
	private boolean	bCancel = false;
	private boolean bFinish = false;
	
	private String msg = null;

	private Vector<OBDAProgressListener> listeners = new Vector<OBDAProgressListener>();
	
	public OBDAProgessMonitor(String msg) {
		this.msg = msg;
	}
	
	public void start() {	
		Runnable action = new Runnable() {
			@Override
			public void run() {
				if (bFinish) {
					return;
				}
				ProgressPanel panel = new ProgressPanel(OBDAProgessMonitor.this, msg);
				parent.setModal(true);
				parent.setContentPane(panel);
				parent.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				parent.setSize(280, 140);
				parent.setLocationRelativeTo(null);
				parent.setVisible(true);
			}
		};
		SwingUtilities.invokeLater(action);
	}

	public void stop() {
		bFinish = true;
		bCancel = false;
		parent.setVisible(false);
		parent.dispose();
	}

	public void cancel() throws Exception {
		bFinish = false;
		bCancel = true;
		parent.setVisible(false);
		parent.dispose();
		
		for (OBDAProgressListener pl : listeners) {
			pl.actionCanceled();
		}
	}
	
	public void addProgressListener(OBDAProgressListener list) {
		listeners.add(list);
	}

	public void removeProgressListener(OBDAProgressListener list) {
		listeners.remove(list);
	}

	public boolean isFinished() {
		return bFinish;
	}
	
	public boolean isCanceled() {
		return bCancel;
	}
}
