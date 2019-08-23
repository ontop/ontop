package it.unibz.inf.ontop.iq.tools.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.ImmutableQueryModifiers;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.FunctionalTools;
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

    @Inject
    private UnionBasedQueryMergerImpl(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                                      CoreUtilsFactory coreUtilsFactory, QueryTransformerFactory transformerFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.transformerFactory = transformerFactory;
    }

    @Override
    public Optional<IQ> mergeDefinitions(Collection<IQ> predicateDefinitions) {
        return mergeDefinitions(predicateDefinitions, Optional.empty());
    }

    @Override
    public Optional<IQ> mergeDefinitions(Collection<IQ> predicateDefinitions,
                                                        ImmutableQueryModifiers topModifiers) {
        return mergeDefinitions(predicateDefinitions, Optional.of(topModifiers));
    }

    /**
     * The optional modifiers are for the top construction node above the UNION (if any).
     *
     * TODO: refactor it so that the definitive intermediate query is directly constructed.
     */
    private Optional<IQ> mergeDefinitions(Collection<IQ> predicateDefinitions,
                                          Optional<ImmutableQueryModifiers> optionalTopModifiers) {
        if (predicateDefinitions.isEmpty())
            return Optional.empty();

        IQ firstDefinition = predicateDefinitions.iterator().next();
        if (predicateDefinitions.size() == 1) {
            if (optionalTopModifiers.isPresent())
                return Optional.of(iqFactory.createIQ(firstDefinition.getProjectionAtom(),
                        optionalTopModifiers.get().insertAbove(firstDefinition.getTree(), iqFactory)));
            else
                return Optional.of(firstDefinition);
        }

        DistinctVariableOnlyDataAtom projectionAtom = firstDefinition.getProjectionAtom();

        VariableGenerator variableGenerator =  coreUtilsFactory.createVariableGenerator(firstDefinition.getTree().getKnownVariables());

        Stream<IQTree> renamedDefinitions = predicateDefinitions.stream()
                .skip(1)
                .map(def -> {
                    // Updates the variable generator
                    InjectiveVar2VarSubstitution disjointVariableSetRenaming = substitutionFactory.generateNotConflictingRenaming(
                            variableGenerator, def.getTree().getKnownVariables());

                    ImmutableSet<Variable> freshVariables = ImmutableSet.copyOf(
                            disjointVariableSetRenaming.getImmutableMap().values());

                    InjectiveVar2VarSubstitution headSubstitution = computeRenamingSubstitution(
                            disjointVariableSetRenaming.applyToDistinctVariableOnlyDataAtom(def.getProjectionAtom()),
                            projectionAtom)
                            .orElseThrow(() -> new IllegalStateException("Bug: unexpected incompatible atoms"));

                    InjectiveVar2VarSubstitution renamingSubstitution =
                            /*
                              fresh variables are excluded from the domain of the renaming substitution
                               since they are in use in the sub-query.

                               NB: this guarantees that the renaming substitution is injective
                             */
                            headSubstitution.composeWithAndPreserveInjectivity(disjointVariableSetRenaming, freshVariables)
                                    .orElseThrow(() -> new IllegalStateException("Bug: the renaming substitution is not injective"));

                    QueryRenamer queryRenamer = transformerFactory.createRenamer(renamingSubstitution);
                    return queryRenamer.transform(def).getTree();
                });

        ImmutableList<IQTree> unionChildren = Stream.concat(Stream.of(firstDefinition.getTree()), renamedDefinitions)
                .collect(ImmutableCollectors.toList());

        IQTree unionTree = iqFactory.createNaryIQTree(iqFactory.createUnionNode(projectionAtom.getVariables()),
                unionChildren);

        IQTree tree = optionalTopModifiers
                .map(m -> m.insertAbove(unionTree, iqFactory))
                .orElse(unionTree);

        return Optional.of(iqFactory.createIQ(projectionAtom, tree));
    }

    /**
     * When such substitution DO NOT EXIST, returns an EMPTY OPTIONAL.
     * When NO renaming is NEEDED returns an EMPTY SUBSTITUTION.
     *
     */
    private Optional<InjectiveVar2VarSubstitution> computeRenamingSubstitution(
            DistinctVariableOnlyDataAtom sourceProjectionAtom,
            DistinctVariableOnlyDataAtom targetProjectionAtom) {

        int arity = sourceProjectionAtom.getEffectiveArity();

        if (!sourceProjectionAtom.getPredicate().equals(targetProjectionAtom.getPredicate())
                || (arity != targetProjectionAtom.getEffectiveArity())) {
            return Optional.empty();
        }
        else {
            ImmutableMap<Variable, Variable> newMap = FunctionalTools.zip(
                    sourceProjectionAtom.getArguments(),
                    targetProjectionAtom.getArguments()).stream()
                    .distinct()
                    .filter(e -> !e.getKey().equals(e.getValue()))
                    .collect(ImmutableCollectors.toMap());

            return Optional.of(substitutionFactory.getInjectiveVar2VarSubstitution(newMap));
        }
    }
}
