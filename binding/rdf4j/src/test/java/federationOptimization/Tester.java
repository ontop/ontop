package federationOptimization;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.impl.QuestQueryProcessor;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.impl.NativeNodeImpl;
import it.unibz.inf.ontop.iq.optimizer.impl.FederationOptimizerImpl;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.query.KGQueryFactory;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class Tester {

    private final Optimizer federationOptimizer;

    private final QueryReformulator reformulator;

    private final KGQueryFactory kgQueryFactory;

    private final Map<String, String> sourceMap;

    private Tester(
            String propertyFile, String ontologyFile, String mappingFile,
            @Nullable String constraintFile, @Nullable String metadataFile,
            @Nullable String sourceFile, @Nullable String effLabelFile, @Nullable String hintFile)
            throws OBDASpecificationException {

        OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .propertyFile(propertyFile)
                .ontologyFile(ontologyFile)
                .nativeOntopMappingFile(mappingFile)
                .enableTestMode();

        if (constraintFile != null) {
            builder.basicImplicitConstraintFile(constraintFile);
        }

        if (metadataFile != null) {
            builder = builder.dbMetadataFile(metadataFile);
        }

        OntopSQLOWLAPIConfiguration configuration = builder.build();

        Injector injector = configuration.getInjector();

        kgQueryFactory = configuration.getKGQueryFactory();
        reformulator = configuration.loadQueryReformulator();
        federationOptimizer = new Optimizer(
                injector.getInstance(IntermediateQueryFactory.class),
                injector.getInstance(AtomFactory.class),
                injector.getInstance(TermFactory.class),
                injector.getInstance(CoreSingletons.class),
                hintFile != null,
                sourceFile,
                effLabelFile,
                hintFile);

        ImmutableMap.Builder<String, String> sourceMapBuilder = ImmutableMap.builder();
        if (sourceFile != null) {
            try {
                for (String line : Files.readAllLines(Paths.get(sourceFile))) {
                    String[] tokens = line.split("-");
                    if (tokens.length == 2) {
                        sourceMapBuilder.put(tokens[0], tokens[1]);
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        this.sourceMap = sourceMapBuilder.build();
    }

    public static Tester create(
            Federator federator, Setting setting)
            throws OBDASpecificationException {

        return create(federator, setting, Optimization.BASE);
    }

    public static Tester create(
            Federator federator, Setting setting, Optimization opt)
            throws OBDASpecificationException {

        Objects.requireNonNull(federator);
        Objects.requireNonNull(setting);
        Objects.requireNonNull(opt);

        Preconditions.checkArgument((setting == Setting.SC1 || setting == Setting.SC2) == (federator == Federator.NONE));
        Preconditions.checkArgument((setting != Setting.SC1 && setting != Setting.SC2) || (opt == Optimization.BASE));

        String BASE = "src/test/resources/federation/";
        String f = federator.name().toLowerCase();
        String s = setting.name().toLowerCase();
        String o = opt == Optimization.BASE ? null : opt.name().toLowerCase();
        String v = setting == Setting.SC1 ? "orig" : "fed";
        return create(
                BASE + "system-" + (federator != Federator.NONE ? f + "-" : "") + s + ".properties",
                BASE + "ontology.owl",
                BASE + "mappings." + v + (federator == Federator.TEIID ? ".teiid" : "") + ".obda",
                BASE + "constraints." + v + (federator == Federator.TEIID ? ".teiid" : "") + ".txt",
                BASE + "system-" + (federator != Federator.NONE ? f + "-" : "") + s + ".metadata.json",
                BASE + "source_relations." + s + ".txt",
                BASE + "source_efficiency_labels." + s + ".txt",
                opt == Optimization.BASE ? null : BASE + "hints." + f + "-" + o + (opt == Optimization.OPTMATV ? "." + s : "") + ".txt");
    }

    public static Tester create(
            String propertyFile, String ontologyFile, String mappingFile,
            @Nullable String constraintFile, @Nullable String metadataFile)
            throws OBDASpecificationException {

        return create(
                propertyFile, ontologyFile, mappingFile, constraintFile, metadataFile,
                null, null, null);
    }

    public static Tester create(
            String propertyFile, String ontologyFile, String mappingFile,
            @Nullable String constraintFile, @Nullable String metadataFile,
            @Nullable String sourceFile, @Nullable String effLabelFile, @Nullable String hintFile)
            throws OBDASpecificationException {

        Objects.requireNonNull(propertyFile);
        Objects.requireNonNull(ontologyFile);
        Objects.requireNonNull(mappingFile);

        return new Tester(
                propertyFile, ontologyFile, mappingFile, constraintFile, metadataFile,
                sourceFile, effLabelFile, hintFile);
    }

    public Map<String, String> getSourceMap() {
        return sourceMap;
    }

    public void reformulate(String sparqlQuery)
            throws OntopKGQueryException, OntopReformulationException {
        reformulate(sparqlQuery, Listener.logger());
    }

    public void reformulate(String sparqlQuery, Listener listener)
            throws OntopKGQueryException, OntopReformulationException {

        listener.onSparqlQuery(sparqlQuery);

        // Parse the SPARQL query into an IQ
        KGQuery<?> query = kgQueryFactory.createSPARQLQuery(sparqlQuery);
        QueryLogger queryLogger = reformulator.getQueryLoggerFactory().create(ImmutableMultimap.of());
        QueryContext emptyQueryContext = reformulator.getQueryContextFactory().create(ImmutableMap.of());
        QuestQueryProcessor.returnPlannedQuery = true;
        IQ inputIQ = reformulator.reformulateIntoNativeQuery(query, emptyQueryContext, queryLogger);
        QuestQueryProcessor.returnPlannedQuery = false;
        listener.onInputIQ(inputIQ);

        IQ optimizedIQ = federationOptimizer.optimize(inputIQ, listener);
        listener.onOptimizedIQ(optimizedIQ);

        IQ executableIQ = reformulator.generateExecutableQuery(optimizedIQ);
        listener.onExecutableIQ(executableIQ);

        // Handling final SQL query
        String sqlQuery = ((NativeNodeImpl) executableIQ.getTree().getChildren().get(0)).getNativeQueryString();
        listener.onSqlQuery(sqlQuery);
    }

    public enum Federator {
        NONE,
        TEIID,
        DENODO,
        DREMIO
    }

    public enum Setting {
        SC1,
        SC2,
        HOM,
        HET
    }

    public enum Optimization {
        BASE,
        OPT,
        OPTMATV
    }

    public enum Query {
        Q1, Q2, Q3, Q4, Q5, Q6, Q7, Q8, Q9, Q10, Q11, Q12;

        private @Nullable String cachedFile;
        private @Nullable String cachedSparql;

        public String getFile() {
            if (cachedFile == null) {
                cachedFile = String.format(
                        "src/test/resources/federation/bsbm-queries/%02d.rq",
                        Integer.parseInt(name().substring(1)));
            }
            return cachedFile;
        }

        public String getSparql() {
            if (cachedSparql == null) {
                try {
                    cachedSparql = Files.readString(Paths.get(getFile()));
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            return cachedSparql;
        }
    }

    public enum Rule {
        CE,
        EJE,
        SJE,
        MTR
    }

    public interface Listener {

        default void onSparqlQuery(String sparqlQuery) {
        }

        default void onInputIQ(IQ inputIQ) {
        }

        default void onOptimizedIQ(IQ optimizedIQ) {
        }

        default void onExecutableIQ(IQ executableIQ) {
        }

        default void onSqlQuery(String sqlQuery) {
        }

        default void onRuleApplied(Rule rule) {
        }

        static Listener nil() {
            return new Listener() {
            };
        }

        static Listener logger() {
            return logger(null, null);
        }

        static Listener logger(@Nullable Logger logger, @Nullable String prefix) {
            String p = prefix != null ? prefix : "";
            BiConsumer<String, Object> handler = (msg, object) -> {
                if (logger != null) {
                    logger.info("{}{}:\n{}", p, msg, object);
                } else {
                    System.out.printf("%s%s:\n%s%n%n", p, msg, object.toString());
                }
            };
            return create(
                    sparqlQuery -> handler.accept("SPARQL query", sparqlQuery),
                    inputIQ -> handler.accept("Input IQ", inputIQ),
                    optimizedIQ -> handler.accept("Optimized IQ", optimizedIQ),
                    executableIQ -> handler.accept("Executable IQ", executableIQ),
                    sqlQuery -> handler.accept("SQL query", sqlQuery),
                    rule -> handler.accept("Rule applued", rule)
            );
        }

        static Listener create(
                @Nullable Consumer<? super String> onSparqlQuery,
                @Nullable Consumer<? super IQ> onInputIQ,
                @Nullable Consumer<? super IQ> onOptimizedIQ,
                @Nullable Consumer<? super IQ> onExecutableIQ,
                @Nullable Consumer<? super String> onSqlQuery,
                @Nullable Consumer<? super Rule> onRuleApplied) {

            return new Listener() {
                @Override
                public void onSparqlQuery(String sparqlQuery) {
                    call(onSparqlQuery, sparqlQuery);
                }

                @Override
                public void onInputIQ(IQ inputIQ) {
                    call(onInputIQ, inputIQ);
                }

                @Override
                public void onOptimizedIQ(IQ optimizedIQ) {
                    call(onOptimizedIQ, optimizedIQ);
                }

                @Override
                public void onExecutableIQ(IQ executableIQ) {
                    call(onExecutableIQ, executableIQ);
                }

                @Override
                public void onSqlQuery(String sqlQuery) {
                    if (onSqlQuery != null) {
                        onSqlQuery.accept(sqlQuery);
                    }
                }

                @Override
                public void onRuleApplied(Rule rule) {
                    if (onRuleApplied != null) {
                        onRuleApplied.accept(rule);
                    }
                }

                private <T> void call(@Nullable Consumer<T> callback, T object) {
                    if (callback != null) {
                        callback.accept(object);
                    }
                }

            };
        }

    }

    public static class Optimizer extends FederationOptimizerImpl {

        private final ThreadLocal<@Nullable Listener> listener = new ThreadLocal<>();

        @Inject
        public Optimizer(
                IntermediateQueryFactory iqFactory,
                AtomFactory atomFactory,
                TermFactory termFactory,
                CoreSingletons coreSingletons) {
            super(iqFactory, atomFactory, termFactory, coreSingletons);
        }

        public Optimizer(
                IntermediateQueryFactory iqFactory,
                AtomFactory atomFactory,
                TermFactory termFactory,
                CoreSingletons coreSingletons,
                boolean enabled,
                String sourceFile,
                String effLabelFile,
                String hintFile) {
            super(iqFactory, atomFactory, termFactory, coreSingletons,
                    enabled, sourceFile, effLabelFile, hintFile);
        }

        @Override
        public IQ optimize(IQ query) {
            return optimize(query, null);
        }

        public IQ optimize(IQ query, @Nullable Listener listener) {
            this.listener.set(listener);
            IQ iq = super.optimize(query);
            this.listener.set(null);
            return iq;
        }

        @Override
        protected void onEquivalentRedundancyRuleApplied() {
            onRuleApplied(Rule.CE);
        }

        @Override
        protected void onEmptyJoinRuleApplied() {
            onRuleApplied(Rule.EJE);
        }

        @Override
        protected void onSelfJoinRuleApplied() {
            onRuleApplied(Rule.SJE);
        }

        @Override
        protected void onMatViewRuleApplied() {
            onRuleApplied(Rule.MTR);
        }

        private void onRuleApplied(Rule rule) {
            Optional.ofNullable(listener.get()).ifPresent(l -> l.onRuleApplied(rule));
        }

    }

}