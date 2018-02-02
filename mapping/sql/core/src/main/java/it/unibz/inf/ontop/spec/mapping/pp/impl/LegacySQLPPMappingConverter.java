package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.EQNormalizer;
import it.unibz.inf.ontop.datalog.SQLPPMapping2DatalogConverter;
import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SQLPPMapping -> Datalog -> MappingWithProvenance
 */
public class LegacySQLPPMappingConverter implements SQLPPMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacySQLPPMappingConverter.class);
    private final Datalog2QueryMappingConverter mappingConverter;
    private final TermFactory termFactory;
    private final SQLPPMapping2DatalogConverter ppMapping2DatalogConverter;
    private final EQNormalizer eqNormalizer;

    @Inject
    private LegacySQLPPMappingConverter(Datalog2QueryMappingConverter mappingConverter,
                                        TermFactory termFactory, SQLPPMapping2DatalogConverter ppMapping2DatalogConverter,
                                        EQNormalizer eqNormalizer) {
        this.mappingConverter = mappingConverter;
        this.termFactory = termFactory;
        this.ppMapping2DatalogConverter = ppMapping2DatalogConverter;
        this.eqNormalizer = eqNormalizer;
    }

    @Override
    public MappingWithProvenance convert(SQLPPMapping ppMapping, RDBMetadata dbMetadata,
                                         ExecutorRegistry executorRegistry) throws InvalidMappingSourceQueriesException {
        ImmutableMap<CQIE, PPMappingAssertionProvenance> datalogMap = convertIntoDatalog(ppMapping, dbMetadata);

        return mappingConverter.convertMappingRules(datalogMap, dbMetadata, executorRegistry, ppMapping.getMetadata());
    }

    /**
     * Assumption: one CQIE per mapping axiom (no nested union)
     */
    private ImmutableMap<CQIE, PPMappingAssertionProvenance> convertIntoDatalog(SQLPPMapping ppMapping, RDBMetadata dbMetadata)
            throws InvalidMappingSourceQueriesException {

        /*
         * May also add views in the DBMetadata!
         */
        ImmutableMap<CQIE, PPMappingAssertionProvenance> datalogMap = ppMapping2DatalogConverter.convert(
                ppMapping.getTripleMaps(), dbMetadata);

        LOGGER.debug("Original mapping size: {}", datalogMap.size());

        // Normalizing language tags and equalities (SIDE-EFFECT!)
        normalizeMapping(datalogMap.keySet());

        return datalogMap;
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
