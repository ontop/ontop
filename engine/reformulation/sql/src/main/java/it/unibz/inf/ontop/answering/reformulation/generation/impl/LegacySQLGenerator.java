package it.unibz.inf.ontop.answering.reformulation.generation.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.datalog.IntermediateQuery2DatalogTranslator;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopTranslationException;
import it.unibz.inf.ontop.injection.OntopTranslationSQLSettings;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.impl.SQLExecutableQuery;
import it.unibz.inf.ontop.dbschema.JdbcTypeMapper;

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
                               OntopTranslationSQLSettings settings,
                               IntermediateQuery2DatalogTranslator iq2DatalogTranslator,
                               JdbcTypeMapper jdbcTypeMapper) {
        originalEngine = new OneShotSQLGeneratorEngine(metadata, iriDictionary, settings, jdbcTypeMapper, iq2DatalogTranslator);
    }

    @Override
    public ExecutableQuery generateSourceQuery(IntermediateQuery query, ImmutableList<String> signature)
            throws OntopTranslationException {
        return originalEngine.clone()
                .generateSourceQuery(query, signature);
    }

    @Override
    public ExecutableQuery generateEmptyQuery(ImmutableList<String> signatureContainer) {
        // Empty string query
        return new SQLExecutableQuery(signatureContainer);
    }
}
