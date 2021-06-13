package it.unibz.inf.ontop.protege.mapping.worker;

import it.unibz.inf.ontop.protege.connection.DataSource;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.SwingWorkerWithCompletionPercentageMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class ExecuteSQLQuerySwingWorker extends SwingWorkerWithCompletionPercentageMonitor<DefaultTableModel, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteSQLQuerySwingWorker.class);

    private final Dialog dialog;
    private final String sqlQuery;
    private final int maxRows;
    private final DataSource dataSource;
    private final Consumer<DefaultTableModel> tableModelConsumer;

    public ExecuteSQLQuerySwingWorker(Dialog dialog, DataSource dataSource, String sqlQuery, int maxRows, Consumer<DefaultTableModel> tableModelConsumer) {
        super(dialog, "<html><h3>Executing SQL Query:</h3></html>");
        this.dialog = dialog;
        this.dataSource = dataSource;
        this.sqlQuery = sqlQuery;
        this.maxRows = maxRows;
        this.tableModelConsumer = tableModelConsumer;

        progressMonitor.setCancelAction(this::doCancel);
    }

    private Statement statement;

    private void doCancel() {
        try {
            if (statement != null && !statement.isClosed())
                statement.cancel();
        }
        catch (Exception ignore) {
        }
    }

    @Override
    protected DefaultTableModel doInBackground() throws Exception {
        start("initializing...");
        setMaxTicks(maxRows);
        try (Connection conn = dataSource.getConnection()) {
            statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.setMaxRows(maxRows);
            try (ResultSet rs = statement.executeQuery(sqlQuery)) {
                ResultSetMetaData metadata = rs.getMetaData();
                int numcols = metadata.getColumnCount();
                String[] columns = new String[numcols];
                for (int i = 1; i <= numcols; i++)
                    columns[i - 1] = metadata.getColumnLabel(i);
                DefaultTableModel tableModel = DialogUtils.createNonEditableTableModel(columns);
                startLoop(this::getCompletionPercentage, () -> String.format("%d%% rows retrieved...", getCompletionPercentage()));
                while (rs.next()) {
                    String[] values = new String[numcols];
                    for (int i = 1; i <= numcols; i++)
                        try {
                            values[i - 1] = rs.getString(i);
                        } catch (Exception ex) {
                            // E.g., Teiid cannot display the geometry value as a BLOB
                            // Such values normally should not be used directly in the mapping target
                            values[i - 1] = "[[NON-PRINTABLE]]";
                        }
                    tableModel.addRow(values);
                    tick();
                }
                endLoop("generating table...");
                end();
                return tableModel;
            }
        }
        finally {
            try {
                if (statement != null && !statement.isClosed())
                    statement.close();
            }
            catch (Exception ignore) {
            }
        }
    }

    @Override
    public void done() {
        try {
            tableModelConsumer.accept(complete());
        }
        catch (CancellationException | InterruptedException ignore) {
        }
        catch (ExecutionException e) {
            DialogUtils.showErrorDialog(dialog, dialog.getTitle(), "Error executing SQL Query: " + sqlQuery, LOGGER, e, dataSource);
        }
    }
}
