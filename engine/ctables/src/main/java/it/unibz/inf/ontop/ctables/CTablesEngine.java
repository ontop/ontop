package it.unibz.inf.ontop.ctables;

import java.io.Closeable;
import java.sql.Connection;
import java.util.function.Supplier;

import it.unibz.inf.ontop.ctables.impl.DefaultCTablesEngine;
import it.unibz.inf.ontop.ctables.spec.Ruleset;
import it.unibz.inf.ontop.injection.OntopCTablesConfiguration;
import it.unibz.inf.ontop.injection.OntopCTablesSettings;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;

public interface CTablesEngine extends Closeable {

    void start() throws CTablesException;

    void refresh() throws CTablesException;

    void stop() throws CTablesException;

    void close() throws CTablesException;

    static CTablesEngine create(final OntopCTablesConfiguration configuration)
            throws CTablesException {

        // Retrieve the settings object associated to the configuration
        final OntopCTablesSettings settings = configuration.getSettings();

        // Retrieve the optional CRON schedule expression in the settings object
        final String schedule = settings.getCTablesRefreshSchedule();

        // Based on JDBC parameters in the settings object, setup a Connection supplier
        final Supplier<Connection> connectionFactory = () -> {
            try {
                return LocalJDBCConnectionUtils.createConnection(settings);
            } catch (final Throwable ex) {
                throw new CTablesException("Could not connect to database", ex);
            }
        };

        // Load the ruleset
        final Ruleset ruleset = configuration.loadRuleset();

        // Create and return a default engine for the configuration supplied
        return new DefaultCTablesEngine(connectionFactory, ruleset, schedule);
    }

}
