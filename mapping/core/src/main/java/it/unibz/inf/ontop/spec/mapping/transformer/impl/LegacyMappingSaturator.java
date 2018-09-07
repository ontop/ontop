package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingSaturator;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses the old Datalog-based mapping saturation code
 */
@Singleton
public class LegacyMappingSaturator implements MappingSaturator {

    private final TMappingExclusionConfig tMappingExclusionConfig;
    private final Mapping2DatalogConverter mapping2DatalogConverter;
    private final Datalog2QueryMappingConverter datalog2MappingConverter;
    private final TermFactory termFactory;
    private final TMappingProcessor tMappingProcessor;
    private final DatalogFactory datalogFactory;
    private final UnifierUtilities unifierUtilities;
    private final SubstitutionUtilities substitutionUtilities;

    @Inject
    private LegacyMappingSaturator(TMappingExclusionConfig tMappingExclusionConfig,
                                   Mapping2DatalogConverter mapping2DatalogConverter,
                                   Datalog2QueryMappingConverter datalog2MappingConverter, TermFactory termFactory,
                                   TMappingProcessor tMappingProcessor, DatalogFactory datalogFactory,
                                   UnifierUtilities unifierUtilities, SubstitutionUtilities substitutionUtilities) {
        this.tMappingExclusionConfig = tMappingExclusionConfig;
        this.mapping2DatalogConverter = mapping2DatalogConverter;
        this.datalog2MappingConverter = datalog2MappingConverter;
        this.termFactory = termFactory;
        this.tMappingProcessor = tMappingProcessor;
        this.datalogFactory = datalogFactory;
        this.unifierUtilities = unifierUtilities;
        this.substitutionUtilities = substitutionUtilities;
    }

    @Override
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, ClassifiedTBox saturatedTBox) {

        LinearInclusionDependencies foreignKeyRules = new LinearInclusionDependencies(dbMetadata.generateFKRules());
        CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(foreignKeyRules, datalogFactory,
                unifierUtilities, substitutionUtilities, termFactory);

        ImmutableList<CQIE> initialMappingRules = mapping2DatalogConverter.convert(mapping)
                .collect(ImmutableCollectors.toList());

        ImmutableSet<CQIE> saturatedMappingRules = ImmutableSet.copyOf(
                tMappingProcessor.getTMappings(initialMappingRules, saturatedTBox, foreignKeyCQC, tMappingExclusionConfig));

        return datalog2MappingConverter.convertMappingRules(ImmutableList.copyOf(saturatedMappingRules), mapping.getMetadata());
    }

}
