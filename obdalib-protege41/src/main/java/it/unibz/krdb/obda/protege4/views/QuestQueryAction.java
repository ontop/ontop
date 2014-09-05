package it.unibz.krdb.obda.protege4.views;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SPARQLQueryUtility;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.obda.protege4.utils.DialogUtils;
import it.unibz.krdb.obda.protege4.utils.OBDAProgressListener;

import java.util.concurrent.CountDownLatch;

import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class is extended by most of the OBDAProgressListeners in QueryInterfaceView.
 * Puts the execution of the query into another thread, and handles cancellation and errors of this thread
 * @author Dag Hovland
 *
 * @param <T> The type of the result returned from this form of execution. 
 */
public abstract class QuestQueryAction<T> implements OBDAProgressListener {

	private boolean errorShown = false;
	private QuestOWLStatement statement = null;
	private QuestOWLConnection oldconn = null;
	private CountDownLatch latch = null;
	private Thread thread = null;
	private T result = null;
	private String queryString = null;
	private boolean isCanceled = false;
	private boolean actionStarted = false;
	private QuestOWL reasoner;

	private static String QUEST_START_MESSAGE = "Quest must be started before using this feature. To proceed \n * select Quest in the \"Reasoners\" menu and \n * click \"Start reasoner\" in the same menu.";


	private static final Logger log = LoggerFactory.getLogger(QuestQueryAction.class);

	public QuestQueryAction(CountDownLatch l, SPARQLQueryUtility q, QuestOWL r) {
		this(l,q.getQueryString(), r);
	}


	public QuestQueryAction(CountDownLatch l, String q, QuestOWL r) {
		this.reasoner = r;
		this.latch = l;
		this.queryString = q;
	}


	/**
	 * This function must do the call to e.g. statement.query()
	 * @param queryString
	 * @return
	 */
	public abstract T executeQuery(QuestOWLStatement st, String queryString) throws OWLException;

	/**
	 * Returns results from executing query.
	 */
	public T getResult(){
		return result;
	}

	public void run() {

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
					latch.countDown();
					if(!isCancelled()){
						errorShown = true;
						log.error(e.getMessage(), e);
						DialogUtils.showQuickErrorDialog(null, e);
					}
				}

			}
		};
		thread.start();
	}

	@Override
	public void actionCanceled() {
		this.isCanceled = true;
		if(!actionStarted)
			throw new Error("Query execution has not been started, and cannot be cancelled. The statement object is null");
		try {
			oldconn = reasoner.replaceConnection();
			Thread canceller = new Thread() {
				public void run(){
					try {
						statement.cancel();
						closeConnection();
						oldconn.close();
						latch.countDown();
					} catch (Exception e) {
						latch.countDown();
						DialogUtils.showQuickErrorDialog(null, e, "Error executing query.");
					}
				}
			};
			canceller.start();
		} catch (OBDAException e) {
			DialogUtils.showQuickErrorDialog(null, e, "Error creating new database connection.");
		}
	}

	@Override
	public boolean isCancelled(){
		return this.isCanceled;
	}

	@Override
	public boolean isErrorShown(){
		return this.errorShown;
	}

	public void closeConnection() throws OWLException {
		if (statement != null) {
			statement.close();
		}
	}

}
