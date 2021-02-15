package it.unibz.inf.ontop.protege.workers;

import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.gui.models.OWLResultSetTableModel;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.IconLoader;
import it.unibz.inf.ontop.protege.utils.SwingWorkerWithCompletionPercentageMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;


public class ExportResultsToCSVSwingWorker extends SwingWorkerWithCompletionPercentageMonitor<Void, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportResultsToCSVSwingWorker.class);

    private final Component parent;
    private final File file;
    private final OWLResultSetTableModel tableModel;

    private static final String DIALOG_TITLE = "Export to CSV";

    public ExportResultsToCSVSwingWorker(Component parent, File file, OWLResultSetTableModel tableModel) {
        super(parent, "<html><h3>Exporting results to CSV file:</h3></html>");
        this.parent = parent;
        this.file = file;
        this.tableModel = tableModel;
    }

    @Override
    protected Void doInBackground() throws Exception {
        start("initializing...");

        List<String[]> data = tableModel.getTabularData();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            setMaxTicks(data.size());
            startLoop(this::getCompletionPercentage, () -> String.format("%d%% completed.", getCompletionPercentage()));

            for (String[] rows : data) {
                StringBuilder line = new StringBuilder();
                boolean needComma = false;
                for (String row : rows) {
                    if (needComma) {
                        line.append(",");
                    }
                    line.append(row);
                    needComma = true;
                }
                line.append("\n");
                writer.write(line.toString());
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

            JOptionPane.showMessageDialog(parent,
                    "<html><h3>Export to CSV file is complete.</h3><br></html>",
                    DIALOG_TITLE,
                    JOptionPane.INFORMATION_MESSAGE,
                    IconLoader.getOntopIcon());
        }
        catch (CancellationException | InterruptedException ignore) {
        }
        catch (ExecutionException e) {
            DialogUtils.showErrorDialog(parent, DIALOG_TITLE, DIALOG_TITLE + " error.", LOGGER, e, (OBDADataSource)null);
        }
    }
}

