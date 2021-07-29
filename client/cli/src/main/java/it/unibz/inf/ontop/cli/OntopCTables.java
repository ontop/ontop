package it.unibz.inf.ontop.cli;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibz.inf.ontop.ctables.CTablesEngine;
import it.unibz.inf.ontop.injection.OntopCTablesConfiguration;

@Command(name = "ctables", description = "Start the engine managing computed tables (ctables)")
public class OntopCTables implements OntopCommand {

    // TODO evaluate supporting --constraint, --db-metadata, --ontop-views

    private static final Logger LOGGER = LoggerFactory.getLogger(OntopCTables.class);

    @Option(type = OptionType.COMMAND, name = { "-p",
            "--properties" }, title = "properties file", description = "Properties file")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String propertiesFile;

    @Option(type = OptionType.COMMAND, name = {
            "--db-url" }, title = "DB URL", description = "DB URL (overrides the properties)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String dbUrl;

    @Option(type = OptionType.COMMAND, name = { "-u",
            "--db-user" }, title = "DB user", description = "DB user (overrides the properties)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String dbUser;

    @Option(type = OptionType.COMMAND, name = {
            "--db-password" }, title = "DB password", description = "DB password (overrides the properties)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String dbPassword;

    @Option(type = OptionType.COMMAND, name = {
            "--ctables-ruleset" }, title = "ruleset file", description = "ctables ruleset file (.yml)")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String ctablesRulesetFile;

    @Option(type = OptionType.COMMAND, name = {
            "--ctables-schedule" }, title = "CRON expression", description = "ctables refresh schedule")
    @BashCompletion(behaviour = CompletionBehaviour.NONE)
    String ctablesRefreshSchedule;

    @Option(type = OptionType.COMMAND, name = {
            "--ctables-refresh-at-start" }, title = "refresh-at-start", description = "whether to force a table refresh when the engine starts")
    private boolean ctablesRefreshAtStart = false;

    @Override
    public void run() {

        // Obtain the CTables configuration object from command line arguments
        @SuppressWarnings("rawtypes")
        final OntopCTablesConfiguration.Builder<? extends OntopCTablesConfiguration.Builder> builder = //
                OntopCTablesConfiguration.defaultBuilder().propertyFile(this.propertiesFile);
        if (this.dbUrl != null) {
            builder.jdbcUrl(this.dbUrl);
        }
        if (this.dbUser != null) {
            builder.jdbcUser(this.dbUser);
        }
        if (this.dbPassword != null) {
            builder.jdbcPassword(this.dbPassword);
        }
        if (this.ctablesRulesetFile != null) {
            builder.ctablesRulesetFile(this.ctablesRulesetFile);
        }
        if (this.ctablesRefreshSchedule != null) {
            builder.ctablesRefreshSchedule(this.ctablesRefreshSchedule);
        }
        final OntopCTablesConfiguration configuration = builder.build();

        // Define the shutdown hook
        final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
        final Thread mainThread = Thread.currentThread();
        final Lock lock = new ReentrantLock();
        final Thread shutdownHandler = new Thread("shutdown") {

            @Override
            public void run() {
                shutdownRequested.set(true);
                mainThread.interrupt(); // delegate processing to main thread
                lock.lock(); // wait for main thread to terminate
                lock.unlock();
            }

        };

        // Create and start the CTables engine
        LOGGER.info("CTables engine starting...");
        final CTablesEngine engine = CTablesEngine.create(configuration);
        engine.start();
        LOGGER.info(
                "CTables engine started (send SIGINT / CTRL-C / Q to stop, R to force table refresh)");

        try {
            // Acquire exclusive lock to synchronize with shutdown hook thread
            lock.lock();

            // Register shutdown hook and signal handler, if supported
            Runtime.getRuntime().addShutdownHook(shutdownHandler);

            // Force refresh if requested
            if (this.ctablesRefreshAtStart || "true".equalsIgnoreCase(configuration.getSettings()
                    .getProperty("ctables.refreshAtStart").orElse("false"))) {
                LOGGER.info("CTables engine refresh forced at startup as requested");
                engine.refresh();
            }

            // Enter loop where signals and terminal input are checked and processed
            outer: while (!shutdownRequested.get()) {
                try {
                    boolean refresh = false;
                    while (System.in.available() > 0) {
                        final char ch = (char) System.in.read();
                        if (ch == 'q' || ch == 'Q') {
                            break outer;
                        } else if (ch == 'r' || ch == 'R') {
                            refresh = true;
                        }
                    }
                    if (refresh) {
                        engine.refresh();
                    } else {
                        Thread.sleep(1000);
                    }
                } catch (final IOException | InterruptedException ex) {
                    // Ignore
                }
            }

        } finally {
            // Stop the CTables engine and release the lock
            try {
                LOGGER.info("CTables engine stopping...");
                engine.stop();
                LOGGER.info("CTables engine stopped");
            } finally {
                lock.unlock();
            }
        }
    }

}
