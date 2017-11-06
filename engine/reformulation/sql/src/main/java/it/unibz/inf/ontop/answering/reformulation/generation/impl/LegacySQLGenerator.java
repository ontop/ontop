package it.unibz.inf.ontop.answering.reformulation.generation.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.DatalogNormalizer;
import it.unibz.inf.ontop.datalog.IntermediateQuery2DatalogTranslator;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.Relation2Predicate;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.OntopReformulationSQLSettings;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.impl.SQLExecutableQuery;
import it.unibz.inf.ontop.dbschema.JdbcTypeMapper;
import it.unibz.inf.ontop.iq.optimizer.PullOutVariableOptimizer;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import javax.annotation.Nullable;

/**
 * Wrapper over a non thread-safe implementation
 *  --> the engine has to be cloned for every query
 */
public class LegacySQLGenerator implements NativeQueryGenerator {

    /**
     * To be cloned for every non-empty query!
     */
    private final OneShotSQLGeneratorEngine originalEngine;

    @AssistedInject
    private LegacySQLGenerator(@Assisted DBMetadata metadata,
                               @Nullable IRIDictionary iriDictionary,
                               OntopReformulationSQLSettings settings,
                               IntermediateQuery2DatalogTranslator iq2DatalogTranslator,
                               JdbcTypeMapper jdbcTypeMapper,
                               PullOutVariableOptimizer pullOutVariableOptimizer,
                               TypeExtractor typeExtractor, Relation2Predicate relation2Predicate,
                               DatalogNormalizer datalogNormalizer, DatalogFactory datalogFactory,
                               TypeFactory typeFactory, TermFactory termFactory) {
        originalEngine = new OneShotSQLGeneratorEngine(metadata, iriDictionary, settings, jdbcTypeMapper,
                iq2DatalogTranslator, pullOutVariableOptimizer, typeExtractor, relation2Predicate,
                datalogNormalizer, datalogFactory, typeFactory, termFactory);
    }

    @Override
    public ExecutableQuery generateSourceQuery(IntermediateQuery query, ImmutableList<String> signature)
            throws OntopReformulationException {
        return originalEngine.clone()
                .generateSourceQuery(query, signature);
    }

    @Override
    public ExecutableQuery generateEmptyQuery(ImmutableList<String> signatureContainer) {
        // Empty string query
        return new SQLExecutableQuery(signatureContainer);
    }
}
