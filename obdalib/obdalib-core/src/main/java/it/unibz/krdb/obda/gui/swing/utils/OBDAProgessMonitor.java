package it.unibz.krdb.obda.gui.swing.utils;

import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

public class OBDAProgessMonitor {

	private Vector<OBDAProgressListener>	listeners	= null;
	private JDialog							parent		= new JDialog();

	private boolean							canceled	= false;
	
	private boolean finished = false;

	public OBDAProgessMonitor() {
		listeners = new Vector<OBDAProgressListener>();
	}

	public void start() {
		Runnable action = new Runnable() {

			@Override
			public void run() {
				if (finished)
					return;
				final ProgressPanel panel = new ProgressPanel(OBDAProgessMonitor.this);
				
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
		finished = true;
		parent.setVisible(false);
		parent.dispose();
		
	}

	public void addProgressListener(OBDAProgressListener list) {
		listeners.add(list);
	}

	public void removeProgressListener(OBDAProgressListener list) {
		listeners.remove(list);
	}

	public void triggerActionCanceled() {
		finished = true;
		canceled = true;
		parent.setVisible(false);
		for (OBDAProgressListener pl : listeners) {
			pl.actionCanceled();
		}
	}

	public boolean isCanceled() {
		return canceled;
	}
}
