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
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SQLPPMapping -> Datalog -> MappingWithProvenance
 */
public class LegacySQLPPMappingConverter implements SQLPPMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacySQLPPMappingConverter.class);
    private final Datalog2QueryMappingConverter mappingConverter;
    private final SQLPPMapping2DatalogConverter ppMapping2DatalogConverter;
    private final EQNormalizer eqNormalizer;

    @Inject
    private LegacySQLPPMappingConverter(Datalog2QueryMappingConverter mappingConverter,
                                        SQLPPMapping2DatalogConverter ppMapping2DatalogConverter,
                                        EQNormalizer eqNormalizer) {
        this.mappingConverter = mappingConverter;
        this.ppMapping2DatalogConverter = ppMapping2DatalogConverter;
        this.eqNormalizer = eqNormalizer;
    }

    @Override
    public MappingWithProvenance convert(SQLPPMapping ppMapping, RDBMetadata dbMetadata,
                                         ExecutorRegistry executorRegistry) throws InvalidMappingSourceQueriesException {
        ImmutableMap<CQIE, PPMappingAssertionProvenance> datalogMap = convertIntoDatalog(ppMapping, dbMetadata);

        return mappingConverter.convertMappingRules(datalogMap, ppMapping.getMetadata());
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
     * Normalize equalities
     */
    private void normalizeMapping(ImmutableSet<CQIE> unfoldingProgram) {

        // Normalizing equalities
        for (CQIE cq: unfoldingProgram)
            eqNormalizer.enforceEqualities(cq);
    }
}
