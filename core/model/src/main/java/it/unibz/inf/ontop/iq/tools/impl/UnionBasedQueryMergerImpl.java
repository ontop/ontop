package it.unibz.inf.ontop.iq.tools.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class UnionBasedQueryMergerImpl implements UnionBasedQueryMerger {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    private UnionBasedQueryMergerImpl(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                                      CoreUtilsFactory coreUtilsFactory, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public Optional<IQ> mergeDefinitions(Collection<IQ> predicateDefinitions) {

        if (predicateDefinitions.isEmpty())
            return Optional.empty();

        IQ firstDefinition = predicateDefinitions.iterator().next();
        if (predicateDefinitions.size() == 1) {
            return Optional.of(firstDefinition);
        }

        DistinctVariableOnlyDataAtom projectionAtom = firstDefinition.getProjectionAtom();

        VariableGenerator variableGenerator =  coreUtilsFactory.createVariableGenerator(firstDefinition.getTree().getKnownVariables());

        ImmutableList<IQTree> renamedDefinitions = Stream.concat(
                Stream.of(firstDefinition.getTree()),
                predicateDefinitions.stream()
                .skip(1)
                .map(def -> {
                    if (!def.getProjectionAtom().getPredicate().equals(projectionAtom.getPredicate()))
                        throw new IllegalStateException("Bug: unexpected incompatible atoms");

                    IQ freshDef = iqTreeTools.getFreshInstance(def, variableGenerator);

                    InjectiveSubstitution<Variable> headSubstitution = substitutionFactory.getSubstitution(
                            freshDef.getProjectionAtom().getArguments(), projectionAtom.getArguments())
                                    .injective();

                    return iqTreeTools.applyDownPropagation(headSubstitution, freshDef.getTree());
                }))
                .collect(ImmutableCollectors.toList());

        ImmutableSet<Variable> unionVariables = projectionAtom.getVariables();

        IQTree unionTree = iqTreeTools.createUnionTree(unionVariables,
                NaryIQTreeTools.transformChildren(renamedDefinitions,
                                c -> iqTreeTools.unaryIQTreeBuilder(unionVariables).build(c)));

        return Optional.of(iqFactory.createIQ(projectionAtom, unionTree));
    }
}
