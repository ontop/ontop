package it.unibz.inf.ontop.ctables.impl;

import java.sql.Connection;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibz.inf.ontop.ctables.CTablesEngine;
import it.unibz.inf.ontop.ctables.CTablesException;
import it.unibz.inf.ontop.ctables.spec.Ruleset;

public final class DefaultCTablesEngine implements CTablesEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCTablesEngine.class);

    private final Supplier<Connection> connectionFactory;

    private final Ruleset ruleset;

    private final Operator operator;

    private final AtomicInteger iteration;

    @Nullable
    private final LongSupplier scheduler;

    @Nullable
    private ScheduledExecutorService executor;

    public DefaultCTablesEngine(final Supplier<Connection> connectionFactory,
            final Ruleset ruleset, @Nullable final String schedule) {

        // Check arguments
        Objects.requireNonNull(connectionFactory);
        Objects.requireNonNull(ruleset);

        // Create a 'scheduler' object returning the time in ms to next refresh execution
        LongSupplier scheduler = null;
        if (schedule != null) {
            final CronDefinition cronDef = CronDefinitionBuilder
                    .instanceDefinitionFor(CronType.QUARTZ);
            final Cron cron = new CronParser(cronDef).parse(schedule);
            final ExecutionTime et = ExecutionTime.forCron(cron);
            scheduler = () -> et.timeToNextExecution(ZonedDateTime.now()).get().toMillis();
        }

        // Initialize object
        this.connectionFactory = connectionFactory;
        this.ruleset = ruleset;
        this.operator = Operator.compile(ruleset);
        this.iteration = new AtomicInteger();
        this.scheduler = scheduler;
        this.executor = null;
    }

    @Override
    public void start() throws CTablesException {

        // Do nothing in case scheduled refresh has not been configured
        if (this.scheduler == null) {
            return;
        }

        // Otherwise, setup scheduled refresh only if not already started
        synchronized (this.scheduler) {
            if (this.executor == null) {

                // Create a thread pool to schedule refresh executions
                final ThreadFactory tf = new ThreadFactoryBuilder() //
                        .setNameFormat("ctables-refresh-thread") //
                        .setDaemon(true) //
                        .build();
                final ScheduledExecutorService executor = Executors
                        .newSingleThreadScheduledExecutor(tf);
                this.executor = executor;

                // Create the refresh Runnable task, which will reschedule itself after completion
                final Runnable refreshTask = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            refresh();
                        } catch (final Throwable ex) {
                            LOGGER.error("CTables engine error while refreshing tables", ex);
                        }
                        synchronized (DefaultCTablesEngine.this.scheduler) {
                            if (executor == DefaultCTablesEngine.this.executor) {
                                final long delay = DefaultCTablesEngine.this.scheduler.getAsLong();
                                executor.schedule(this, delay, TimeUnit.MILLISECONDS);
                            }
                        }
                    }

                };

                // Schedule the first execution of the refresh task
                final long delay = this.scheduler.getAsLong();
                this.executor.schedule(refreshTask, delay, TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public void stop() throws CTablesException {

        // Do nothing in case scheduled refresh has not been configured
        if (this.scheduler == null) {
            return;
        }

        // Otherwise, disable scheduled refresh if previously started
        synchronized (this.scheduler) {
            if (this.executor != null) {
                try {
                    this.executor.shutdownNow();
                } finally {
                    this.executor = null;
                }
            }
        }
    }

    @Override
    public void close() {

        // Ensure to stop the engine when releasing this object
        try {
            stop();
        } catch (final Throwable ex) {
            LOGGER.warn("Ignoring stop error while closing CTables engine", ex);
        }

        // Wait for any pending refresh to complete, and disable further refresh requests
        synchronized (this.iteration) {
            this.iteration.set(Integer.MIN_VALUE);
        }
    }

    @Override
    public void refresh() throws CTablesException {

        // Ensure that concurrent calls with execute doRefresh only once
        final int iteration = this.iteration.get();
        Preconditions.checkState(iteration >= 0, "CTables engine has been closed");
        synchronized (this.iteration) {
            if (this.iteration.get() == iteration) {
                try {
                    final long ts = System.currentTimeMillis();
                    LOGGER.info("CTables engine refresh started");
                    doRefresh();
                    LOGGER.info("CTables engine refresh completed in {} ms",
                            System.currentTimeMillis() - ts);
                } catch (final CTablesException ex) {
                    throw ex;
                } catch (final Throwable ex) {
                    throw new CTablesException(ex);
                } finally {
                    this.iteration.incrementAndGet();
                }
            }
        }
    }

    private void doRefresh() throws Throwable {

        // Operate within a single connection
        try (Connection conn = this.connectionFactory.get()) {

            // Create operator context to store refresh-wide data
            final OperatorContext context = new OperatorContext(conn);

            // Wipe out the content of computed tables (TODO: incremental update)
            for (final String target : this.ruleset.getTargets()) {
                context.evalUpdate("DELETE FROM " + target);
            }

            // Execute rules
            this.operator.run(context, false);
        }
    }

}
