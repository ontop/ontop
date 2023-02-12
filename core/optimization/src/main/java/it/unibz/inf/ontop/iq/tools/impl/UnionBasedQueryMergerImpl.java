package it.unibz.inf.ontop.iq.tools.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
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
    private final QueryTransformerFactory transformerFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    private UnionBasedQueryMergerImpl(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                                      CoreUtilsFactory coreUtilsFactory, QueryTransformerFactory transformerFactory, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.transformerFactory = transformerFactory;
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

        Stream<IQTree> renamedDefinitions = predicateDefinitions.stream()
                .skip(1)
                .map(def -> {
                    // Updates the variable generator
                    InjectiveSubstitution<Variable> disjointVariableSetRenaming =
                            substitutionFactory.generateNotConflictingRenaming(variableGenerator, def.getTree().getKnownVariables());

                    if (!def.getProjectionAtom().getPredicate().equals(projectionAtom.getPredicate()))
                        throw new IllegalStateException("Bug: unexpected incompatible atoms");

                    ImmutableList<Variable> sourceProjectionAtomArguments =
                            substitutionFactory.onVariables().apply(disjointVariableSetRenaming, def.getProjectionAtom().getArguments());

                    InjectiveSubstitution<Variable> headSubstitution =
                            substitutionFactory.getSubstitution(sourceProjectionAtomArguments, projectionAtom.getArguments())
                                    .injective();

                    InjectiveSubstitution<Variable> renamingSubstitution =
                            /*
                              fresh variables are excluded from the domain of the renaming substitution
                               since they are in use in the sub-query.

                               NB: this guarantees that the renaming substitution is injective
                             */
                            substitutionFactory.onVariables().compose(headSubstitution, disjointVariableSetRenaming)
                                    .removeFromDomain(disjointVariableSetRenaming.getRangeSet())
                                    .injective();

                    QueryRenamer queryRenamer = transformerFactory.createRenamer(renamingSubstitution);
                    return queryRenamer.transform(def).getTree();
                });

        ImmutableSet<Variable> unionVariables = projectionAtom.getVariables();

        ImmutableList<IQTree> unionChildren = Stream.concat(Stream.of(firstDefinition.getTree()), renamedDefinitions)
                .map(c -> iqTreeTools.createConstructionNodeTreeIfNontrivial(c, unionVariables))
                .collect(ImmutableCollectors.toList());

        IQTree unionTree = iqFactory.createNaryIQTree(iqFactory.createUnionNode(unionVariables),
                unionChildren);

        return Optional.of(iqFactory.createIQ(projectionAtom, unionTree));
    }

}
