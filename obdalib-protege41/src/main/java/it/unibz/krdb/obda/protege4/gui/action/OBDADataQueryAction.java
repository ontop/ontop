package it.unibz.krdb.obda.protege4.gui.action;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.obda.protege4.utils.DialogUtils;
import it.unibz.krdb.obda.protege4.utils.OBDAProgessMonitor;
import it.unibz.krdb.obda.protege4.utils.OBDAProgressListener;

import java.awt.Component;
import java.util.concurrent.CountDownLatch;

import javax.swing.JOptionPane;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public abstract class OBDADataQueryAction<T> implements OBDAProgressListener{

	// The string shown to the user in the dialog during execution of action
	String msg;


	// The result object
	T result;

	// THe time used by the execution
	private long time;

	private boolean errorShown = false;
	private QuestOWLStatement statement = null;
	private CountDownLatch latch = null;
	private Thread thread = null;
	private String queryString = null;
	private boolean isCanceled = false;
	private boolean actionStarted = false;
	private QuestOWL reasoner;
	private Component rootView;

	private static String QUEST_START_MESSAGE = "Quest must be started before using this feature. To proceed \n * select Quest in the \"Reasoners\" menu and \n * click \"Start reasoner\" in the same menu.";


	private static final Logger log = LoggerFactory.getLogger(OBDADataQueryAction.class);


	public OBDADataQueryAction(String msg, Component rootView){
		this.msg = msg;
		this.rootView = rootView;
	}

	/**
	 *  This function must do the call to e.g. statement.query()
	 * @param queryString
	 * @return
	 */
	public abstract T executeQuery(QuestOWLStatement st, String queryString) throws OWLException;

	/**
	 * Must be implemented by the subclass for getting the current reasoner
	 * 
	 */
	public abstract OWLEditorKit getEditorKit(); 

	/**
	 * This function displays or handles the result
	 * @param result THe result object ot be handles
	 */
	public abstract void handleResult(T res) throws OWLException;


	public void run(String query) {
		this.queryString = query;
		this.actionStarted = true;
		this.isCanceled = false;
		this.errorShown = false;
		OBDAProgessMonitor monitor = null;
		try {
			monitor = new OBDAProgessMonitor(this.msg);
			monitor.start();
			latch = new CountDownLatch(1);
			OWLEditorKit kit = this.getEditorKit();
			OWLReasoner r = kit.getModelManager().getOWLReasonerManager().getCurrentReasoner();
			if (r instanceof QuestOWL) {
				this.reasoner = (QuestOWL) r;
				monitor.addProgressListener(this);
				long startTime = System.currentTimeMillis();
				runAction();
				latch.await();
				monitor.stop();
				if(!this.isCancelled() && !(result == null && this.isErrorShown())){
					this.time = System.currentTimeMillis() - startTime;
					handleResult(result);
				}
			} else /* reasoner not QuestOWL */ {
				JOptionPane.showMessageDialog(
						rootView,
						QUEST_START_MESSAGE);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(
					rootView,
					e);
		} finally {
			monitor.stop();
		}
	}


	public long getExecutionTime(){
		return this.time;
	}

	public abstract int getNumberOfRows();


	public abstract boolean isRunning();


	private void runAction() {

		thread = new Thread() {

			@Override
			public void run() {

				try {
					statement = reasoner.getStatement();
					if(statement == null)
						throw new NullPointerException("QuestQueryAction received a null QuestOWLStatement object from the reasoner");
					actionStarted = true;
					result = executeQuery(statement, queryString);
					latch.countDown();
				} catch (Exception e) {
					if(!isCancelled()){
						/*try {
							Thread.sleep(50);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}*/
						errorShown = true;
						latch.countDown();
						log.error(e.getMessage(), e);
						//DialogUtils.showQuickErrorDialog(rootView, e, "Error executing query");
						JOptionPane.showMessageDialog(rootView, e,  "Error Executing Query", JOptionPane.ERROR_MESSAGE);
					}
				}	
			}
		};
		thread.start();
	}

	/**
	 * This thread handles the cancelling of a request. 
	 * To speed up the process for the user, this thread is given the old connection, statement and latch,
	 * while these are replaced in the containing class
	 * @author Dag Hovland
	 *
	 */
	private class Canceller extends Thread{
		private CountDownLatch old_latch;
		private QuestOWLConnection old_conn;
		private QuestOWLStatement old_stmt;

		Canceller() throws OBDAException{
			super();
			this.old_latch = latch;
			this.old_stmt = statement;
			this.old_conn = reasoner.replaceConnection();
		}


		public void run(){
			try {
				this.old_stmt.cancel();
				this.old_stmt.close();
				this.old_conn.close();
				this.old_latch.countDown();
			} catch (Exception e) {
				this.old_latch.countDown();
				DialogUtils.showQuickErrorDialog(rootView, e, "Error cancelling query.");
			}
		}
	};
	
	public void actionCanceled() {
		this.isCanceled = true;
		if(!actionStarted)
			throw new Error("Query execution has not been started, and cannot be cancelled.");
		try {
			Canceller canceller = new Canceller();
			canceller.start();
		} catch (OBDAException e) {
			DialogUtils.showQuickErrorDialog(rootView, e, "Error creating new database connection.");
		} finally {
			this.actionStarted = false;
		}
	}

	public boolean isCancelled(){
		return this.isCanceled;
	}

	public boolean isErrorShown(){
		return this.errorShown;
	}

	public void closeConnection() throws OWLException {
		if (statement != null) {
			statement.close();
		}
	}
}
