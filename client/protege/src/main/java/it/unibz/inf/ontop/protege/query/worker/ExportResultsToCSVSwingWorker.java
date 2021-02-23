package it.unibz.inf.ontop.protege.query.worker;

import it.unibz.inf.ontop.protege.connection.DataSource;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.SwingWorkerWithCompletionPercentageMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


public class ExportResultsToCSVSwingWorker extends SwingWorkerWithCompletionPercentageMonitor<Void, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportResultsToCSVSwingWorker.class);

    private final Component parent;
    private final File file;
    private final DefaultTableModel tableModel;

    private static final String DIALOG_TITLE = "Export to CSV";

    public ExportResultsToCSVSwingWorker(Component parent, File file, DefaultTableModel tableModel) {
        super(parent, "<html><h3>Exporting results to CSV file:</h3></html>");
        this.parent = parent;
        this.file = file;
        this.tableModel = tableModel;
    }

    @Override
    protected Void doInBackground() throws Exception {
        start("initializing...");

        Vector<Vector> data = tableModel.getDataVector();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            setMaxTicks(data.size());
            startLoop(this::getCompletionPercentage, () -> String.format("%d%% completed.", getCompletionPercentage()));

            for (Vector<?> row : data) {
                String line = row.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",", "", "\n"));
                writer.write(line);
                writer.flush();
                tick();
            }
            endLoop("");
        }
        end();
        return null;
    }

    @Override
    protected void done() {
        try {
            complete();

            DialogUtils.showInfoDialog(parent,
                    "<html><h3>Export to CSV file is complete.</h3><br></html>",
                    DIALOG_TITLE);
        }
        catch (CancellationException | InterruptedException ignore) {
        }
        catch (ExecutionException e) {
            DialogUtils.showErrorDialog(parent, DIALOG_TITLE, DIALOG_TITLE + " error.", LOGGER, e, (DataSource)null);
        }
    }
}

