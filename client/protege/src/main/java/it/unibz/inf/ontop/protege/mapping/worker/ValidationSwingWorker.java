package it.unibz.inf.ontop.protege.mapping.worker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.mapping.TriplesMap;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.SwingWorkerWithCompletionPercentageMonitor;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

import java.awt.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.HTML_TAB;

public class ValidationSwingWorker extends SwingWorkerWithCompletionPercentageMonitor<Void, ValidationSwingWorker.ValidationReport> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationSwingWorker.class);
    
    private final Component parent;
    private final List<TriplesMap> triplesMapList;
    private final OBDAModel obdaModel;
    private int invalidTriplesMapCount;

    private static final String DIALOG_TITLE = "Triples Maps Validation";

    public ValidationSwingWorker(Component parent, List<TriplesMap> triplesMapList, OBDAModel obdaModel) {
        super(parent, "<html><h3>Validating Triples Maps:</h3></html>");
        this.parent = parent;
        this.obdaModel = obdaModel;
        this.triplesMapList = triplesMapList;
    }

    @Override
    protected Void doInBackground() throws Exception {
        start("initializing...");

        setMaxTicks(triplesMapList.size());
        startLoop(this::getCompletionPercentage, () -> String.format("%d%% completed.", getCompletionPercentage()));

        try (Connection conn = obdaModel.getDataSource().getConnection();
             Statement statement = conn.createStatement()) {
            statement.setMaxRows(1);
            for (TriplesMap triplesMap : triplesMapList) {
                try (ResultSet rs = statement.executeQuery(triplesMap.getSqlQuery())) {
                    ImmutableList<String> invalidPlaceholders = getInvalidPlaceholders(rs, triplesMap.getTargetAtoms());
                    if (invalidPlaceholders.isEmpty())
                        publish(new ValidationReport(triplesMap.getId(), TriplesMap.Status.VALID));
                    else
                        publish(new ValidationReport(triplesMap.getId(), invalidPlaceholders));
                }
                catch (SQLException e) {
                    publish(new ValidationReport(triplesMap.getId(), e.getMessage()));
                }
                tick();
            }
        }
        endLoop("");
        end();
        return null;
    }

    protected void process(List<ValidationReport> reports) {
        for (ValidationReport report : reports) {
            if (report.status == TriplesMap.Status.INVALID)
                invalidTriplesMapCount++;

            obdaModel.getTriplesMapManager().setStatus(
                    report.id,
                    report.status,
                    report.sqlErrorMessage,
                    report.invalidPlaceholders);
        }
    }

    @Override
    protected void done() {
        try {
            complete();
            String message = invalidTriplesMapCount == 0
                    ? (triplesMapList.size() == 1
                    ? "The only triples map selected has been found valid."
                    : "All <b>" + triplesMapList.size() + "</b> triples map have been found valid.")
                    : "<b>" + invalidTriplesMapCount + "</b> triples map" + (invalidTriplesMapCount > 1 ? "s" : "") + " (out of <b>" +
                    triplesMapList.size() + "</b>) have been found invalid.";

            DialogUtils.showInfoDialog(parent,
                    "<html><h3>Validation of the triples maps is complete.</h3><br>" +
                            HTML_TAB + message + "<br></html>",
                    DIALOG_TITLE);
        }
        catch (CancellationException | InterruptedException ignore) {
        }
        catch (ExecutionException e) {
            DialogUtils.showErrorDialog(parent, DIALOG_TITLE, DIALOG_TITLE + " error.", LOGGER, e, obdaModel.getDataSource());
        }
    }

    static final class ValidationReport {
        private final String id;
        private final TriplesMap.Status status;
        private final String sqlErrorMessage;
        private final ImmutableList<String> invalidPlaceholders;

        ValidationReport(String id, TriplesMap.Status status) {
            this.id = id;
            this.status = status;
            this.sqlErrorMessage = null;
            this.invalidPlaceholders = ImmutableList.of();
        }

        ValidationReport(String id, String sqlErrorMessage) {
            this.id = id;
            this.status = TriplesMap.Status.INVALID;
            this.sqlErrorMessage = sqlErrorMessage;
            this.invalidPlaceholders = ImmutableList.of();
        }

        ValidationReport(String id, ImmutableList<String> invalidPlaceholders) {
            this.id = id;
            this.status = TriplesMap.Status.INVALID;
            this.sqlErrorMessage = null;
            this.invalidPlaceholders = invalidPlaceholders;
        }
    }

    private static ImmutableList<String> getInvalidPlaceholders(ResultSet rs, ImmutableList<TargetAtom> targetAtoms) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (int i = 1; i <= md.getColumnCount(); i++)
            builder.add(md.getColumnLabel(i).toUpperCase());
        ImmutableSet<String> normalizedColumnNames = builder.build();

        // a very lax version of column matching - quotation and case are ignored
        return  targetAtoms.stream()
                .flatMap(a -> a.getSubstitution().getImmutableMap().values().stream())
                .flatMap(ImmutableTerm::getVariableStream)
                .map(Variable::getName)
                .distinct()
                .filter(p -> !normalizedColumnNames.contains(stripOffQuotationMarks(p).toUpperCase()))
                .collect(ImmutableCollectors.toList());
    }

    private static String stripOffQuotationMarks(String placeholder) {
        char first = placeholder.charAt(0), last = placeholder.charAt(placeholder.length() - 1);
        if (first == '`' && last == '`'
                || first == '"' && last == '"'
                || first == '[' && last == ']')
            return placeholder.substring(1, placeholder.length() - 1);

        return placeholder;
    }

}
