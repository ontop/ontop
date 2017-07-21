package it.unibz.inf.ontop.mapping.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.mapping.SQLPPMappingConverter;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.EQNormalizer;
import it.unibz.inf.ontop.pp.PPTriplesMapProvenance;
import it.unibz.inf.ontop.spec.impl.LegacyIsNotNullDatalogMappingFiller;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.SQLPPMapping2DatalogConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATA_FACTORY;


/**
 * SQLPPMapping -> Datalog -> MappingWithProvenance
 */
public class LegacySQLPPMappingConverter implements SQLPPMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacySQLPPMappingConverter.class);
    private final Datalog2QueryMappingConverter mappingConverter;

    @Inject
    private LegacySQLPPMappingConverter(Datalog2QueryMappingConverter mappingConverter) {
        this.mappingConverter = mappingConverter;
    }

    @Override
    public MappingWithProvenance convert(SQLPPMapping ppMapping, DBMetadata dbMetadata,
                                         ExecutorRegistry executorRegistry) throws InvalidMappingSourceQueriesException {
        ImmutableMap<CQIE, PPTriplesMapProvenance> datalogMap = convertIntoDatalog(ppMapping, dbMetadata);

        return mappingConverter.convertMappingRules(datalogMap, dbMetadata, executorRegistry, ppMapping.getMetadata());
    }

    /**
     * Assumption: one CQIE per mapping axiom (no nested union)
     */
    private ImmutableMap<CQIE, PPTriplesMapProvenance> convertIntoDatalog(SQLPPMapping ppMapping, DBMetadata dbMetadata)
            throws InvalidMappingSourceQueriesException {

        /*
         * May also add views in the DBMetadata!
         */
        ImmutableMap<CQIE, PPTriplesMapProvenance> datalogMap = SQLPPMapping2DatalogConverter.convert(
                ppMapping.getTripleMaps(), dbMetadata);

        LOGGER.debug("Original mapping size: {}", datalogMap.size());

        // Normalizing language tags and equalities (SIDE-EFFECT!)
        normalizeMapping(datalogMap.keySet());

        return datalogMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> LegacyIsNotNullDatalogMappingFiller.addNotNull(e.getKey(), dbMetadata),
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
                if (typedTerm.getTerms().size() == 2 && typedTerm.getFunctionSymbol().getName().equals(OBDAVocabulary.RDFS_LITERAL_URI)) {
                    // changing the language, its always the second inner term (literal,lang)
                    Term originalLangTag = typedTerm.getTerm(1);
                    if (originalLangTag instanceof ValueConstant) {
                        ValueConstant originalLangConstant = (ValueConstant) originalLangTag;
                        Term normalizedLangTag = DATA_FACTORY.getConstantLiteral(originalLangConstant.getValue().toLowerCase(),
                                originalLangConstant.getType());
                        typedTerm.setTerm(1, normalizedLangTag);
                    }
                }
            }
        }

        // Normalizing equalities
        for (CQIE cq: unfoldingProgram)
            EQNormalizer.enforceEqualities(cq);
    }
}
