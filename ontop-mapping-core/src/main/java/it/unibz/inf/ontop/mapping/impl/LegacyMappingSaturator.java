package it.unibz.inf.ontop.mapping.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingSaturator;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.model.CQIE;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingProcessor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Uses the old Datalog-based mapping saturation code
 */
@Singleton
public class LegacyMappingSaturator implements MappingSaturator {

    private final TMappingExclusionConfig tMappingExclusionConfig;
    private final Mapping2DatalogConverter mapping2DatalogConverter;
    private final Datalog2QueryMappingConverter datalog2MappingConverter;

    @Inject
    private LegacyMappingSaturator(TMappingExclusionConfig tMappingExclusionConfig,
                                   Mapping2DatalogConverter mapping2DatalogConverter,
                                   Datalog2QueryMappingConverter datalog2MappingConverter) {
        this.tMappingExclusionConfig = tMappingExclusionConfig;
        this.mapping2DatalogConverter = mapping2DatalogConverter;
        this.datalog2MappingConverter = datalog2MappingConverter;
    }

    @Override
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, TBoxReasoner saturatedTBox) {

        LinearInclusionDependencies foreignKeyRules = new LinearInclusionDependencies(dbMetadata.generateFKRules());
        CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(foreignKeyRules);

        List<CQIE> inputMappingRules = mapping2DatalogConverter.convert(mapping)
                .collect(Collectors.toList());

        List<CQIE> saturatedMappingRules = TMappingProcessor.getTMappings(inputMappingRules, saturatedTBox, true,
                foreignKeyCQC, tMappingExclusionConfig);

        return datalog2MappingConverter.convertMappingRules(ImmutableList.copyOf(saturatedMappingRules),
                dbMetadata, mapping.getExecutorRegistry(), mapping.getMetadata());
    }
}
