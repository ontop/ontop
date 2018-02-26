package it.unibz.inf.ontop.answering.reformulation.generation.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.generation.TemporalNativeQueryGenerator;
import it.unibz.inf.ontop.answering.reformulation.generation.calcite.CalciteBasedSQLGenerator;
import it.unibz.inf.ontop.answering.reformulation.generation.calcite.TemporalCalciteBasedSQLGenerator;
import it.unibz.inf.ontop.answering.reformulation.impl.SQLExecutableQuery;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.TemporalTranslationFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;

public class TemporalSQLGenerator implements TemporalNativeQueryGenerator {
    /**
     * To be cloned for every non-empty query!
     */
    //private final TemporalOneShotSQLGeneratorEngine originalEngine;

    private final TemporalCalciteBasedSQLGenerator queryGenerator;
    private final DBMetadata dbMetadata;
    private final TemporalTranslationFactory temporalTranslationFactory;
    @AssistedInject
    private TemporalSQLGenerator(@Assisted DBMetadata metadata,
                                 TemporalTranslationFactory temporalTranslationFactory) {
        this.temporalTranslationFactory = temporalTranslationFactory;
        this.dbMetadata = metadata;
        this.queryGenerator = temporalTranslationFactory.createSQLGenerator(metadata);

//        originalEngine = new TemporalOneShotSQLGeneratorEngine(metadata, iriDictionary, settings, jdbcTypeMapper,
//                iq2DatalogTranslator, pullOutVariableOptimizer, typeExtractor, relation2Predicate,
//                datalogNormalizer, datalogFactory, typeFactory, termFactory);
    }

    @Override
    public ExecutableQuery generateSourceQuery(IntermediateQuery query, ImmutableList<String> signature)
            throws OntopReformulationException {
        return queryGenerator.clone(dbMetadata)
                .generateSourceQuery(query, signature);
        //return queryGenerator.clone().generateSourceQuery(query, signature);
    }

    @Override
    public ExecutableQuery generateEmptyQuery(ImmutableList<String> signatureContainer) {
        // Empty string query
        return new SQLExecutableQuery(signatureContainer);
    }

}