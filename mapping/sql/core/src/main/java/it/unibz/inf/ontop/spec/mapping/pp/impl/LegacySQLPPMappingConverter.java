package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.EQNormalizer;
import it.unibz.inf.ontop.datalog.SQLPPMapping2DatalogConverter;
import it.unibz.inf.ontop.datalog.impl.DatalogRule2QueryConverter;
import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * SQLPPMapping -> Datalog -> MappingWithProvenance
 */
public class LegacySQLPPMappingConverter implements SQLPPMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacySQLPPMappingConverter.class);

    private final TermFactory termFactory;
    private final SQLPPMapping2DatalogConverter ppMapping2DatalogConverter;
    private final EQNormalizer eqNormalizer;
    private final ProvenanceMappingFactory provMappingFactory;
    private final NoNullValueEnforcer noNullValueEnforcer;
    private final DatalogRule2QueryConverter datalogRule2QueryConverter;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private LegacySQLPPMappingConverter(TermFactory termFactory, SQLPPMapping2DatalogConverter ppMapping2DatalogConverter,
                                        EQNormalizer eqNormalizer, ProvenanceMappingFactory provMappingFactory, NoNullValueEnforcer noNullValueEnforcer, DatalogRule2QueryConverter datalogRule2QueryConverter, IntermediateQueryFactory iqFactory) {
        this.termFactory = termFactory;
        this.ppMapping2DatalogConverter = ppMapping2DatalogConverter;
        this.eqNormalizer = eqNormalizer;
        this.provMappingFactory = provMappingFactory;
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.datalogRule2QueryConverter = datalogRule2QueryConverter;
        this.iqFactory = iqFactory;
    }

    @Override
    public MappingWithProvenance convert(SQLPPMapping ppMapping, RDBMetadata dbMetadata,
                                         ExecutorRegistry executorRegistry) throws InvalidMappingSourceQueriesException {

        /**
         * Assumption: one CQIE per mapping axiom (no nested union)
         */
        /*
         * May also add views in the DBMetadata!
         */
        ImmutableMap<CQIE, PPMappingAssertionProvenance> datalogMap = ppMapping2DatalogConverter.convert(
                ppMapping.getTripleMaps(), dbMetadata);

        LOGGER.debug("Original mapping size: {}", datalogMap.size());

        // Normalizing language tags (SIDE-EFFECT!)
        for (CQIE cq: datalogMap.keySet())
            normalizeMapping(cq);

        // Normalizing equalities (SIDE-EFFECT!)
        for (CQIE cq: datalogMap.keySet())
            eqNormalizer.enforceEqualities(cq);


        ImmutableMap<IQ, PPMappingAssertionProvenance> iqMap = datalogMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> convertDatalogRule(e.getKey()),
                        Map.Entry::getValue));

        return provMappingFactory.create(iqMap, ppMapping.getMetadata());
    }

    private IQ convertDatalogRule(CQIE datalogRule) {

        IQ directlyConvertedIQ = datalogRule2QueryConverter.extractPredicatesAndConvertDatalogRule(
                datalogRule, iqFactory);

        return noNullValueEnforcer.transform(directlyConvertedIQ)
                .liftBinding();
    }


    /**
     * Normalize language tags (make them lower-case)
     */

    private void normalizeMapping(CQIE mapping) {
        Function head = mapping.getHead();
        for (Term term : head.getTerms()) {
            if (!(term instanceof Function))
                continue;

            Function typedTerm = (Function) term;
            if (typedTerm.getTerms().size() == 2 && typedTerm.getFunctionSymbol().getName().equals(RDF.LANGSTRING.getIRIString())) {
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
}
