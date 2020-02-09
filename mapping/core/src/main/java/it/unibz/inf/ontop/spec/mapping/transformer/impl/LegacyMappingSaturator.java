package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.constraints.impl.DBLinearInclusionDependenciesImpl;
import it.unibz.inf.ontop.constraints.impl.ImmutableCQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingSaturator;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;

import java.util.List;

/**
 * Uses the old Datalog-based mapping saturation code
 */
@Singleton
public class LegacyMappingSaturator implements MappingSaturator {

    private final TMappingExclusionConfig tMappingExclusionConfig;
    private final TMappingProcessor tMappingProcessor;
    private final AtomFactory atomFactory;
    private final CoreUtilsFactory coreUtilsFactory;

    @Inject
    private LegacyMappingSaturator(TMappingExclusionConfig tMappingExclusionConfig,
                                   TMappingProcessor tMappingProcessor,
                                   AtomFactory atomFactory,
                                   CoreUtilsFactory coreUtilsFactory) {
        this.tMappingExclusionConfig = tMappingExclusionConfig;
        this.tMappingProcessor = tMappingProcessor;
        this.atomFactory = atomFactory;
        this.coreUtilsFactory = coreUtilsFactory;
    }

    @Override
    public ImmutableList<MappingAssertion> saturate(ImmutableList<MappingAssertion> mapping, ClassifiedTBox saturatedTBox) {

        ImmutableCQContainmentCheckUnderLIDs<RelationPredicate> cqContainmentCheck =
                new ImmutableCQContainmentCheckUnderLIDs<>(
                    new DBLinearInclusionDependenciesImpl(coreUtilsFactory, atomFactory));

        return tMappingProcessor.getTMappings(mapping, saturatedTBox, tMappingExclusionConfig, cqContainmentCheck);
    }

}
