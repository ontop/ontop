package it.unibz.inf.ontop.protege.workers;

import com.google.common.collect.Maps;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.core.OntopProtegeReasoner;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.SwingWorkerWithTimeIntervalMonitor;
import org.protege.editor.core.editorkit.EditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class OntopQuerySwingWorker<T> extends SwingWorkerWithTimeIntervalMonitor<Map.Entry<T, String>, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntopQuerySwingWorker.class);

    private static final int MONITOR_UPDATE_INTERVAL = 300;

    private final String query;
    private final Component parent;
    private final String title;
    private final OntopProtegeReasoner ontop;

    private OntopOWLStatement statement;

    protected OntopQuerySwingWorker(Component parent, OntopProtegeReasoner ontop, String title, String query) {
        super(parent, "<html><h3>" + title + "</h3></html>", MONITOR_UPDATE_INTERVAL);

        this.parent = parent;
        this.query = query;
        this.title = title;
        this.ontop = ontop;

        progressMonitor.setCancelAction(this::closeStatementQuietly);
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
        }
        catch (ExecutionException e) {
            DialogUtils.showErrorDialog(parent, title, title + " error.", LOGGER, e, (OBDADataSource)null);
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
}
