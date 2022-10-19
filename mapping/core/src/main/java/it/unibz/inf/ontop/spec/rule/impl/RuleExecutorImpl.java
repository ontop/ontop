package it.unibz.inf.ontop.spec.rule.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.GeneralStructuralAndSemanticIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.rule.RuleExecutor;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;

import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class RuleExecutorImpl implements RuleExecutor {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final QueryTransformerFactory transformerFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final AtomFactory atomFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final GeneralStructuralAndSemanticIQOptimizer generalStructuralAndSemanticIQOptimizer;

    @Inject
    protected RuleExecutorImpl(IntermediateQueryFactory iqFactory,
                               SubstitutionFactory substitutionFactory, QueryTransformerFactory transformerFactory,
                               CoreUtilsFactory coreUtilsFactory, AtomFactory atomFactory,
                               UnionBasedQueryMerger queryMerger,
                               GeneralStructuralAndSemanticIQOptimizer generalStructuralAndSemanticIQOptimizer) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.transformerFactory = transformerFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.atomFactory = atomFactory;
        this.queryMerger = queryMerger;
        this.generalStructuralAndSemanticIQOptimizer = generalStructuralAndSemanticIQOptimizer;
    }

    @Override
    public ImmutableList<MappingAssertion> apply(ImmutableList<MappingAssertion> mapping, ImmutableList<IQ> rules) {
        if (rules.isEmpty())
            return mapping;

        // Assumes that indexes are unique for each mapping assertion at that stage
        Map<MappingAssertionIndex, MappingAssertion> mutableMappingMap = mapping.stream()
                .collect(Collectors.toMap(
                        MappingAssertion::getIndex,
                        a -> a));

        IQOptimizer mappingUnfolder = new MutableQueryUnfolder(mutableMappingMap, iqFactory, substitutionFactory,
                transformerFactory, coreUtilsFactory, atomFactory);

        for (IQ rule : rules) {
            IQ additionalDefinition = optimize(mappingUnfolder.optimize(rule));
            // Mutation
            updateMapping(mutableMappingMap, additionalDefinition);
        }

        return ImmutableList.copyOf(mutableMappingMap.values());
    }

    private IQ optimize(IQ rule) {
        return generalStructuralAndSemanticIQOptimizer.optimize(rule);
    }

    private void updateMapping(Map<MappingAssertionIndex, MappingAssertion> mutableMappingMap, IQ additionalDefinition) {
        MappingAssertion additionalAssertion = new MappingAssertion(additionalDefinition, null);
        mutableMappingMap.merge(additionalAssertion.getIndex(), additionalAssertion, this::merge);
    }

    private MappingAssertion merge(MappingAssertion existingAssertion, MappingAssertion additionalAssertion) {
        IQ mergedDefinition = queryMerger.mergeDefinitions(
                        ImmutableSet.of(existingAssertion.getQuery(), additionalAssertion.getQuery()))
                .orElseThrow(() -> new MinorOntopInternalBugException("Cannot merge the definitions"));

        return new MappingAssertion(mergedDefinition, null);
    }
}
