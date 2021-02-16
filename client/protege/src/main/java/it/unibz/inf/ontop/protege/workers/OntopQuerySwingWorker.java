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
import java.util.function.Supplier;

public abstract class OntopQuerySwingWorker<T> extends SwingWorkerWithTimeIntervalMonitor<Map.Entry<T, String>, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntopQuerySwingWorker.class);

    private static final int MONITOR_UPDATE_INTERVAL = 300;

    private final String query;
    private final Component parent;
    private final String title;
    private final OntopProtegeReasoner ontop;

    private OntopOWLStatement statement;

    protected OntopQuerySwingWorker(Component parent, OntopProtegeReasoner ontop, String title, String query) {
        this(parent, ontop, title, query, () -> new DialogProgressMonitor(parent, "<html><h3>" + title + "</h3></html>", true));
    }

    protected OntopQuerySwingWorker(Component parent, OntopProtegeReasoner ontop, String title, String query, JButton startButton, JButton stopButton, JLabel statusLabel) {
        this(parent, ontop, title, query, () -> new EmbeddedProgressMonitor(startButton, stopButton, statusLabel));
    }

    protected OntopQuerySwingWorker(Component parent, OntopProtegeReasoner ontop, String title, String query, Supplier<AbstractProgressMonitor> progressMonitorSupplier) {
        super(progressMonitorSupplier, MONITOR_UPDATE_INTERVAL);

        this.parent = parent;
        this.title = title;

        this.query = query;
        this.ontop = ontop;

        progressMonitor.setCancelAction(this::cancelStatementQuietly);
    }


    public static <T> void getOntopAndExecute(EditorKit editorKit, String query, OntopQuerySwingWorkerFactory<T> factory) {
        Optional<OntopProtegeReasoner> ontop = DialogUtils.getOntopProtegeReasoner(editorKit);
        if (!ontop.isPresent())
            return;

        OntopQuerySwingWorker<T> worker = factory.apply(ontop.get(), query);
        worker.execute();
    }


    abstract protected T runQuery(OntopOWLStatement statement, String query) throws Exception;

    abstract protected void onCompletion(T result, String sqlQuery);


    @Override
    protected Map.Entry<T, String> doInBackground() throws Exception {
        try {
            start("initializing...");
            statement = ontop.getStatement();
            if (statement == null)
                throw new NullPointerException("OntopQuerySwingWorker received a null OntopOWLStatement object from the reasoner");

            IQ sqlExecutableQuery = statement.getExecutableQuery(query);
            String sql = sqlExecutableQuery.toString();

            startLoop(() -> 50, () -> String.format("%d results retrieved...", getCount()));
            T value = runQuery(statement, query);
            endLoop("");
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
