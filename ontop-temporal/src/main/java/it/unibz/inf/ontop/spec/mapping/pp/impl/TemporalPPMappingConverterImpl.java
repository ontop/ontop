package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.EQNormalizer;
import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.spec.impl.LegacyIsNotNullDatalogMappingFiller;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.TemporalPPMappingConverter;
import it.unibz.inf.ontop.temporal.datalog.TemporalDatalog2QueryMappingConverter;
import it.unibz.inf.ontop.temporal.mapping.TemporalSQLPPMapping2DatalogConverter;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TemporalPPMappingConverterImpl implements TemporalPPMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemporalPPMappingConverter.class);
    private final SpecificationFactory specificationFactory;
    private final TemporalIntermediateQueryFactory iqFactory;
    private final ProvenanceMappingFactory provMappingFactory;
    private final TemporalDatalog2QueryMappingConverter mappingConverter;
    private final TermFactory termFactory;
    private final EQNormalizer eqNormalizer;
    private final LegacyIsNotNullDatalogMappingFiller isNotNullDatalogMappingFiller;
    private final TemporalSQLPPMapping2DatalogConverter temporalSQLPPMapping2DatalogConverter;



    @Inject
    private TemporalPPMappingConverterImpl(SpecificationFactory specificationFactory,
                                           TemporalIntermediateQueryFactory iqFactory,
                                           ProvenanceMappingFactory provMappingFactory,
                                           TemporalDatalog2QueryMappingConverter mappingConverter,
                                           TermFactory termFactory, EQNormalizer eqNormalizer,
                                           LegacyIsNotNullDatalogMappingFiller isNotNullDatalogMappingFiller,
                                           TemporalSQLPPMapping2DatalogConverter temporalSQLPPMapping2DatalogConverter) {
        this.specificationFactory = specificationFactory;
        this.iqFactory = iqFactory;
        this.provMappingFactory = provMappingFactory;
        this.mappingConverter = mappingConverter;

        this.termFactory = termFactory;
        this.eqNormalizer = eqNormalizer;
        this.isNotNullDatalogMappingFiller = isNotNullDatalogMappingFiller;
        this.temporalSQLPPMapping2DatalogConverter = temporalSQLPPMapping2DatalogConverter;
    }
    @Override
    public MappingWithProvenance convert(SQLPPMapping ppMapping, RDBMetadata dbMetadata, ExecutorRegistry executorRegistry) throws InvalidMappingSourceQueriesException {

        ImmutableMap<CQIE, PPMappingAssertionProvenance> datalogMap = convertIntoDatalog(ppMapping, dbMetadata);

        return mappingConverter.convertMappingRules(datalogMap, dbMetadata, executorRegistry, ppMapping.getMetadata());
//
//        try {
//            return convertIntoMappingWithProvenance(ppMapping, dbMetadata);
//        } catch (InvalidSelectQueryException e) {
//            e.printStackTrace();
//        }
//        return null;
    }

    /**
     * Assumption: one CQIE per mapping axiom (no nested union)
     */
    private ImmutableMap<CQIE, PPMappingAssertionProvenance> convertIntoDatalog(SQLPPMapping ppMapping, RDBMetadata dbMetadata)
            throws InvalidMappingSourceQueriesException {

        /*
         * May also add views in the DBMetadata!
         */
        ImmutableMap<CQIE, PPMappingAssertionProvenance> datalogMap = temporalSQLPPMapping2DatalogConverter.convert(
                ppMapping.getTripleMaps(), dbMetadata);

        LOGGER.debug("Original mapping size: {}", datalogMap.size());

        // Normalizing language tags and equalities (SIDE-EFFECT!)
        normalizeMapping(datalogMap.keySet());

        return datalogMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> isNotNullDatalogMappingFiller.addNotNull(e.getKey(), dbMetadata),
                        Map.Entry::getValue));
    }

    /**
     * Normalize language tags (make them lower-case) and equalities
     * (remove them by replacing all equivalent terms with one representative)
     */

    private void normalizeMapping(ImmutableSet<CQIE> unfoldingProgram) {

        // Normalizing language tags. Making all LOWER CASE

        for (CQIE mapping : unfoldingProgram) {
            Function head = mapping.getHead();
            for (Term term : head.getTerms()) {
                if (!(term instanceof Function))
                    continue;

                Function typedTerm = (Function) term;
                if (typedTerm.getTerms().size() == 2 && typedTerm.getFunctionSymbol().getName().equals(IriConstants.RDFS_LITERAL_URI)) {
                    // changing the language, its always the second inner term (literal,lang)
                    Term originalLangTag = typedTerm.getTerm(1);
                    if (originalLangTag instanceof ValueConstant) {
                        ValueConstant originalLangConstant = (ValueConstant) originalLangTag;
                        Term normalizedLangTag = termFactory.getConstantLiteral(originalLangConstant.getValue().toLowerCase(),
                                originalLangConstant.getType());
                        typedTerm.setTerm(1, normalizedLangTag);
                    }
                }
            }
        }

        // Normalizing equalities
        for (CQIE cq: unfoldingProgram)
            eqNormalizer.enforceEqualities(cq);
    }

}
