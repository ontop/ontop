package federationOptimization;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
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
import it.unibz.inf.ontop.iq.optimizer.FederationOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.FederationOptimizerImpl;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.query.KGQueryFactory;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class Tester {

    private final FederationOptimizer federationOptimizer;

    private final QueryReformulator reformulator;

    private final KGQueryFactory kgQueryFactory;

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
        federationOptimizer = new FederationOptimizerImpl(
                injector.getInstance(IntermediateQueryFactory.class),
                injector.getInstance(AtomFactory.class),
                injector.getInstance(TermFactory.class),
                injector.getInstance(CoreSingletons.class),
                sourceFile != null,
                sourceFile,
                effLabelFile,
                hintFile);
    }

    public static Tester create(
            Federator federator, Setting setting)
            throws OBDASpecificationException {

        return create(federator, setting, null);
    }

    public static Tester create(
            Federator federator, Setting setting, @Nullable Optimization opt)
            throws OBDASpecificationException {

        Objects.requireNonNull(federator);
        Objects.requireNonNull(setting);

        String BASE = "src/test/resources/federation/";
        String f = federator.name().toLowerCase();
        String s = setting.name().toLowerCase();
        String o = opt == null ? null : opt.name().toLowerCase();
        return create(
                BASE + "system-" + f + "-" + s + ".properties",
                BASE + "ontology.owl",
                BASE + "mappings.fed" + (federator == Federator.TEIID ? ".teiid" : "") + ".obda",
                BASE + "constraints.fed" + (federator == Federator.TEIID ? ".teiid" : "") + ".txt",
                BASE + "system-" + f + "-" + s + ".metadata.json",
                opt == null ? null : BASE + "source_relations." + s + ".txt",
                opt == null ? null : BASE + "source_efficiency_labels." + s + ".txt",
                opt == null ? null : BASE + "hints." + f + "-" + o + (opt == Optimization.OPTMATV ? "." + s : "") + ".txt");
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

        if (sourceFile != null || effLabelFile != null || hintFile != null) {
            Objects.requireNonNull(sourceFile);
            Objects.requireNonNull(effLabelFile);
            Objects.requireNonNull(hintFile);
        }

        return new Tester(
                propertyFile, ontologyFile, mappingFile, constraintFile, metadataFile,
                sourceFile, effLabelFile, hintFile);
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

        IQ optimizedIQ = federationOptimizer.optimize(inputIQ);
        listener.onOptimizedIQ(optimizedIQ);

        IQ executableIQ = reformulator.generateExecutableQuery(optimizedIQ);
        listener.onExecutableIQ(executableIQ);

        // Handling final SQL query
        String sqlQuery = ((NativeNodeImpl) executableIQ.getTree().getChildren().get(0)).getNativeQueryString();
        listener.onSqlQuery(sqlQuery);
    }

    public enum Federator {
        TEIID, DENODO, DREMIO
    }

    public enum Setting {
        HOM, HET
    }

    public enum Optimization {
        OPT, OPTMATV
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
                    sqlQuery -> handler.accept("SQL query", sqlQuery)
            );
        }

        static Listener create(
                @Nullable Consumer<? super String> onSparqlQuery,
                @Nullable Consumer<? super IQ> onInputIQ,
                @Nullable Consumer<? super IQ> onOptimizedIQ,
                @Nullable Consumer<? super IQ> onExecutableIQ,
                @Nullable Consumer<? super String> onSqlQuery) {

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

                private <T> void call(@Nullable Consumer<T> callback, T object) {
                    if (callback != null) {
                        callback.accept(object);
                    }
                }

            };
        }

    }

}
