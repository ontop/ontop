package it.unibz.inf.ontop.protege.gui.action;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.protege.core.OntopProtegeReasoner;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OBDAProgressListener;
import it.unibz.inf.ontop.protege.utils.OBDAProgressMonitor;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CountDownLatch;

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

public abstract class OBDADataQueryAction<T> implements OBDAProgressListener {

	// The string shown to the user in the dialog during execution of action
	private final String msg;

	// The result object
	private T result;

	// Translated SQL query
	private String sqlQuery;

	// THe time used by the execution
	private long time;

	private boolean queryExecError = false;
	private OntopOWLStatement statement;
	private CountDownLatch latch;
	private String queryString;

	private boolean isCanceled = false;
	private boolean actionStarted = false;
	private OntopProtegeReasoner reasoner;
	private final Component rootView;

	private static final String QUEST_START_MESSAGE = "Ontop reasoner must be started before using this feature. To proceed \n * select Ontop in the \"Reasoners\" menu and \n * click \"Start reasoner\" in the same menu.";

	private static final Logger log = LoggerFactory.getLogger(OBDADataQueryAction.class);


	public OBDADataQueryAction(String msg, Component rootView){
		this.msg = msg;
		this.rootView = rootView;
	}

	/**
	 *  This function must do the call to e.g. statement.query()
	 */
	public abstract T executeQuery(OntopOWLStatement st, String queryString) throws OWLException;

	/**
	 * Must be implemented by the subclass for getting the current reasoner
	 *
	 */
	public abstract OWLEditorKit getEditorKit();

	/**
	 * This function displays or handles the result
	 * @param res THe result object ot be handles
	 */
	public abstract void handleResult(T res) throws OWLException;

	public abstract void handleSQLTranslation(String sqlQuery);

	public void run(String query) {
		this.queryString = query;
		this.actionStarted = true;
		this.isCanceled = false;
		this.queryExecError = false;
		OBDAProgressMonitor monitor = null;
		try {
			monitor = new OBDAProgressMonitor(msg, getEditorKit().getWorkspace());
			monitor.start();
			latch = new CountDownLatch(1);
			OWLEditorKit kit = this.getEditorKit();
			OWLReasoner r = kit.getModelManager().getOWLReasonerManager().getCurrentReasoner();
			if (r instanceof OntopProtegeReasoner) {
				this.reasoner = (OntopProtegeReasoner) r;
				monitor.addProgressListener(this);
				long startTime = System.currentTimeMillis();
				runAction();
				latch.await();
				monitor.stop();
				if(!this.isCancelled() && !this.isErrorShown()){
					this.time = System.currentTimeMillis() - startTime;
					handleSQLTranslation(sqlQuery);
					handleResult(result);
				}
			}
			else /* reasoner not OntopProtegeReasoner */ {
				JOptionPane.showMessageDialog(rootView, QUEST_START_MESSAGE);
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(rootView, e);
		}
		finally {
			latch.countDown();
			monitor.stop();
		}
	}


	public long getExecutionTime(){
		return this.time;
	}

	public abstract int getNumberOfRows();

	public abstract boolean isRunning();


	private void runAction() {
		// FIXME
		// sqlQuery = sqlExecutableQuery.getSQL();
		Thread thread = new Thread(() -> {
			try {
				statement = reasoner.getStatement();
				if (statement == null)
					throw new NullPointerException("QuestQueryAction received a null QuestOWLStatement object from the reasoner");

				final IQ sqlExecutableQuery = statement.getExecutableQuery(queryString);
				// FIXME
				// sqlQuery = sqlExecutableQuery.getSQL();
				sqlQuery = sqlExecutableQuery.toString();
				actionStarted = true;
				result = executeQuery(statement, queryString);
				latch.countDown();
			}
			catch (Exception e) {
				if (!isCancelled()) {
					latch.countDown();
					queryExecError = true;
					log.error(e.getMessage(), e);
					DialogUtils.showQuickErrorDialog(rootView, e, "Error executing query");
				}
			}
		});
		thread.start();
	}

	/**
	 * This thread handles the cancelling of a request.
	 * To speed up the process for the user, this thread is given the old connection, statement and latch,
	 * while these are replaced in the containing class
	 * @author Dag Hovland
	 *
	 */
	private class Canceller extends Thread {
		private final CountDownLatch old_latch;
		private final OWLConnection old_conn;
		private final OntopOWLStatement old_stmt;

		Canceller() throws OntopConnectionException {
			this.old_latch = latch;
			this.old_stmt = statement;
			this.old_conn = reasoner.replaceConnection();
		}

		public void run() {
			try {
				old_stmt.cancel();
				old_stmt.close();
				old_conn.close();
				old_latch.countDown();
			}
			catch (Exception e) {
				this.old_latch.countDown();
				DialogUtils.showQuickErrorDialog(rootView, e, "Error cancelling query.");
			}
		}
	}

	public void actionCanceled() {
		isCanceled = true;
		if (!actionStarted)
			throw new Error("Query execution has not been started, and cannot be cancelled.");
		try {
			Canceller canceller = new Canceller();
			canceller.start();
		}
		catch (OntopConnectionException e) {
			DialogUtils.showQuickErrorDialog(rootView, e, "Error creating new database connection.");
		}
		finally {
			actionStarted = false;
		}
	}

	public boolean isCancelled(){
		return this.isCanceled;
	}

	public boolean isErrorShown(){
		return this.queryExecError;
	}
}
