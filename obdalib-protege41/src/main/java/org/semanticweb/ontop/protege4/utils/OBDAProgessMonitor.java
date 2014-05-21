package org.semanticweb.ontop.protege4.utils;

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

import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.semanticweb.ontop.protege4.utils.OBDAProgessMonitor;
import org.semanticweb.ontop.protege4.utils.OBDAProgressListener;
import org.semanticweb.ontop.protege4.utils.ProgressPanel;

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
