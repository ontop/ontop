package it.unibz.inf.ontop.protege.workers;

import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.OntopQueryEvaluationException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.exception.OntopOWLException;
import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.core.OntopProtegeReasoner;
import it.unibz.inf.ontop.protege.utils.*;
import org.protege.editor.core.editorkit.EditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public abstract class OntopQuerySwingWorker<T, V> extends SwingWorkerWithTimeIntervalMonitor<Map.Entry<T, String>, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntopQuerySwingWorker.class);

    private static final int MONITOR_UPDATE_INTERVAL = 300;

    private final String query;
    private final Component parent;
    private final String title;
    private final OntopProtegeReasoner ontop;

    private OntopOWLStatement statement;

    protected OntopQuerySwingWorker(OntopProtegeReasoner ontop, String query, Component parent, String title) {
        this(ontop, query, parent, title, new DialogProgressMonitor(parent, "<html><h3>" + title + "</h3></html>", true));
    }

    protected OntopQuerySwingWorker(OntopProtegeReasoner ontop, String query, Component parent, String title, JButton startButton, JButton stopButton, JLabel statusLabel) {
        this(ontop, query, parent, title, new EmbeddedProgressMonitor(startButton, stopButton, statusLabel));
    }

    protected OntopQuerySwingWorker(OntopProtegeReasoner ontop, String query, Component parent, String title, AbstractProgressMonitor progressMonitor) {
        super(progressMonitor, MONITOR_UPDATE_INTERVAL);

        this.parent = parent;
        this.title = title;

        this.query = query;
        this.ontop = ontop;

        progressMonitor.setCancelAction(this::cancelStatementQuietly);
    }


    public static <T, V> void getOntopAndExecute(EditorKit editorKit, String query, OntopQuerySwingWorkerFactory<T, V> factory) {
        Optional<OntopProtegeReasoner> ontop = DialogUtils.getOntopProtegeReasoner(editorKit);
        if (!ontop.isPresent())
            return;

        OntopQuerySwingWorker<T, V> worker = factory.apply(ontop.get(), query);
        worker.execute();
    }


    abstract protected T runQuery(OntopOWLStatement statement, String query) throws Exception;

    abstract protected void onCompletion(T result, String sqlQuery);


    @Override
    protected Map.Entry<T, String> doInBackground() throws Exception {
        try {
            start("Rewriting the query...");
            statement = ontop.getStatement();
            if (statement == null)
                throw new NullPointerException("OntopQuerySwingWorker received a null OntopOWLStatement object from the reasoner");

            IQ sqlExecutableQuery = statement.getExecutableQuery(query);
            String sql = sqlExecutableQuery.toString();

            startLoop(() -> 50, () -> getCount() == 0
                    ? "Started retrieving results..."
                    : String.format("%d results retrieved...", getCount()));
            T value = runQuery(statement, query);
            endLoop("Completed results retrieval.");
            end();
            return Maps.immutableEntry(value, sql);
        }
        catch (OntopOWLException e) {
            if (e.getCause() instanceof OntopQueryEvaluationException &&
               "Query execution was cancelled".equals(e.getCause().getMessage()))
                throw new CancelActionException();

            throw e;
        }
        finally {
            closeStatementQuietly();
        }
    }

    @Override
    protected void done() {
        try {
            Map.Entry<T, String> result = complete();

            onCompletion(result.getKey(), result.getValue());
        }
        catch (CancellationException | InterruptedException ignore) {
            progressMonitor.setStatus("Query processing was cancelled.");
        }
        catch (ExecutionException e) {
            DialogUtils.showErrorDialog(parent, title, title + " error.", LOGGER, e, (OBDADataSource)null);
        }
        catch (Exception e) {
            DialogUtils.showQuickErrorDialog(parent, e, title + " error.");
        }
    }


    private void closeStatementQuietly() {
        try {
            if (statement != null && !statement.isClosed())
                statement.close();
        }
        catch (Exception ignore) {
        }
    }

    private void cancelStatementQuietly() {
        try {
            if (statement != null && !statement.isClosed())
                statement.cancel();
        }
        catch (Exception ignore) {
        }
    }
}
