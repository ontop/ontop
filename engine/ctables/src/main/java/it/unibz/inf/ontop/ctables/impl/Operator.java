package it.unibz.inf.ontop.ctables.impl;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibz.inf.ontop.ctables.spec.Rule;
import it.unibz.inf.ontop.ctables.spec.Ruleset;

public abstract class Operator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCTablesEngine.class);

    private static final int MAX_BATCH_SIZE = 100; // TODO: make it configurable?

    public abstract boolean run(OperatorContext context, boolean incremental) throws SQLException;

    public static Operator newRuleOperator(final Rule rule) {
        return new Operator() {

            private HashCode hash(final Object[] tuple) {

                // TODO: optimize hashing
                final Hasher hasher = Hashing.goodFastHash(128).newHasher();
                hasher.putInt(tuple.length);
                for (final Object attribute : tuple) {
                    if (attribute != null) {
                        hasher.putUnencodedChars(attribute.toString());
                    }
                    hasher.putByte((byte) 0);
                }
                return hasher.hash();
            }

            @Override
            public boolean run(final OperatorContext context, final boolean incremental)
                    throws SQLException {

                // Evaluate the source part of the rule, obtaining a relation
                long ts = System.currentTimeMillis();
                LOGGER.debug("[rule {}] source query evaluation started", rule.getId());
                final Entry<String[], List<Object[]>> relation = context
                        .evalQuery(rule.getSource());
                final String[] signature = relation.getKey();
                final List<Object[]> tuples = relation.getValue();
                LOGGER.debug("[rule {}] source query evaluated in {} ms, {} tuples", rule.getId(),
                        System.currentTimeMillis() - ts, tuples.size());

                // In incremental mode, drop tuples already stored in previous runs
                if (incremental) {
                    ts = System.currentTimeMillis();
                    Set<HashCode> hashes = context.getAttribute(this, "hashes");
                    if (hashes == null) {
                        hashes = Sets.newHashSet();
                        context.setAttribute(this, "hashes", hashes);
                    }
                    final int initialSize = tuples.size();
                    for (final Iterator<Object[]> i = tuples.iterator(); i.hasNext();) {
                        final Object[] tuple = i.next();
                        final HashCode hash = hash(tuple);
                        if (!hashes.add(hash)) {
                            i.remove(); // already added
                        }
                    }
                    LOGGER.debug("[rule {}] dropped {} tuples already upserted in {} ms",
                            rule.getId(), initialSize - tuples.size(),
                            System.currentTimeMillis() - ts, tuples.size());
                }

                // Generate a SQL upsert statement to update the target computed table
                if (!tuples.isEmpty()) {
                    final String updateSql = new StringBuilder() //
                            .append("UPSERT INTO ").append(rule.getTarget()).append(" (") //
                            .append(Joiner.on(", ").join(Iterables.transform( //
                                    Arrays.asList(signature), a -> '"' + a + '"'))) //
                            .append(") VALUES (")
                            .append(Joiner.on(", ")
                                    .join(Collections.nCopies(signature.length, "?")))
                            .append(")").toString();

                    // Execute the upsert statement in batches
                    ts = System.currentTimeMillis();
                    LOGGER.debug("[rule {}] upsert of {} tuples into {}", rule.getId(),
                            tuples.size(), rule.getTarget());
                    for (final List<Object[]> batch : Iterables.partition(tuples,
                            MAX_BATCH_SIZE)) {
                        context.evalUpdate(updateSql, batch);
                    }
                    LOGGER.debug("[rule {}] upsert completed in {} ms", rule.getId(),
                            System.currentTimeMillis() - ts);
                }

                // Return true if not running incrementally or if there were no tuples to store
                return !incremental || tuples.isEmpty();
            }

            @Override
            public String toString() {
                return rule.getId();
            }

        };
    }

    public static Operator newSequenceOperator(final Iterable<Operator> children) {
        final Operator[] theChildren = Iterables.toArray(children, Operator.class);
        return theChildren.length == 1 ? theChildren[0] : new Operator() {

            @Override
            public boolean run(final OperatorContext context, final boolean incremental)
                    throws SQLException {
                boolean done = true;
                for (final Operator child : theChildren) {
                    final boolean childDone = child.run(context, incremental);
                    done &= childDone;
                }
                return done;
            }

            @Override
            public String toString() {
                return "sequence(" + Joiner.on(", ").join(children) + ")";
            }

        };
    }

    public static Operator newParallelOperator(final Iterable<Operator> children) {
        final Operator[] theChildren = Iterables.toArray(children, Operator.class);
        return theChildren.length == 1 ? theChildren[0] : new Operator() {

            @Override
            public boolean run(final OperatorContext context, final boolean incremental)
                    throws SQLException {

                final AtomicBoolean done = new AtomicBoolean(true);
                final List<Throwable> exceptions = Lists.newArrayList();
                final CountDownLatch latch = new CountDownLatch(theChildren.length);

                for (final Operator child : theChildren) {
                    ForkJoinPool.commonPool().submit(() -> {
                        try {
                            final boolean childDone = child.run(context, incremental);
                            synchronized (done) {
                                done.set(done.get() & childDone);
                            }
                        } catch (final Throwable ex) {
                            synchronized (exceptions) {
                                exceptions.add(ex);
                            }
                        } finally {
                            latch.countDown();
                        }
                    });
                }

                try {
                    latch.await();
                } catch (final Throwable ex) {
                    exceptions.add(ex);
                }

                if (!exceptions.isEmpty()) {
                    final SQLException exception = new SQLException();
                    for (final Throwable ex : exceptions) {
                        exception.addSuppressed(ex);
                    }
                    throw exception;
                }

                return done.get();
            }

            @Override
            public String toString() {
                return "parallel(" + Joiner.on(", ").join(children) + ")";
            }

        };
    }

    public static Operator newFixpointOperator(final Operator child) {
        return new Operator() {

            @Override
            public boolean run(final OperatorContext context, final boolean incremental)
                    throws SQLException {
                boolean done = false;
                while (!done) {
                    done = child.run(context, true);
                }
                return true;
            }

            @Override
            public String toString() {
                return "fixpoint(" + child + ")";
            }

        };
    }

    public static Operator compile(final Ruleset ruleset) {

        // TODO: this method needs further unit tests

        final Multimap<String, Rule> rulesByTarget = HashMultimap.create();
        for (final Rule rule : ruleset.getRules()) {
            rulesByTarget.put(rule.getTarget(), rule);
        }

        final DirectedGraph<Operator, DefaultEdge> graph = new DefaultDirectedGraph<>(
                DefaultEdge.class);

        final Map<Rule, Operator> ruleOperators = Maps.newHashMap();
        for (final Rule rule : ruleset.getRules()) {
            final Operator op = newRuleOperator(rule);
            graph.addVertex(op);
            ruleOperators.put(rule, op);
        }

        for (final Rule rule : ruleset.getRules()) {
            final Operator targetOp = ruleOperators.get(rule);
            for (final String target : rule.getDependencies()) {
                for (final Rule depRule : rulesByTarget.get(target)) {
                    final Operator sourceOp = ruleOperators.get(depRule);
                    graph.addEdge(sourceOp, targetOp);
                }
            }
        }

        for (final Set<Operator> component : new StrongConnectivityInspector<>(graph)
                .stronglyConnectedSets()) {

            if (component.size() <= 1) {
                continue;
            }

            // TODO: extract further structure?
            final Operator fixpointOp = newFixpointOperator(newParallelOperator(component));
            graph.addVertex(fixpointOp);

            for (final Operator op : component) {
                for (final DefaultEdge edge : graph.incomingEdgesOf(op)) {
                    final Operator sourceOp = graph.getEdgeSource(edge);
                    if (!component.contains(sourceOp)) {
                        graph.addEdge(sourceOp, fixpointOp);
                    }
                }
                for (final DefaultEdge edge : graph.outgoingEdgesOf(op)) {
                    final Operator targetOp = graph.getEdgeTarget(edge);
                    if (!component.contains(targetOp)) {
                        graph.addEdge(fixpointOp, targetOp);
                    }
                }
            }

            graph.removeAllVertices(component);
        }

        final List<Operator> sequenceOpChildren = Lists.newArrayList();
        while (!graph.vertexSet().isEmpty()) {
            final List<Operator> parallelOpChildren = Lists.newArrayList();
            for (final Operator op : graph.vertexSet()) {
                if (graph.incomingEdgesOf(op).isEmpty()) {
                    parallelOpChildren.add(op);
                }
            }
            graph.removeAllVertices(parallelOpChildren);
            sequenceOpChildren.add(newParallelOperator(parallelOpChildren));
        }
        final Operator sequenceOp = newSequenceOperator(sequenceOpChildren);
        LOGGER.debug("Compiled plan: {}", sequenceOp);
        return sequenceOp;
    }

}
