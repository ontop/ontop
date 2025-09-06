package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.Predicate;

@Singleton
public class LeftJoinTools {
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;
    private final IntermediateQueryFactory iqFactory;
    private final AtomFactory atomFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final RightProvenanceNormalizer rightProvenanceNormalizer;

    @Inject
    public LeftJoinTools(CoreSingletons coreSingletons, CoreUtilsFactory coreUtilsFactory, RightProvenanceNormalizer rightProvenanceNormalizer) {
        this.termFactory = coreSingletons.getTermFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.iqFactory = coreSingletons.getIQFactory();
        this.atomFactory = coreSingletons.getAtomFactory();
        this.coreUtilsFactory = coreUtilsFactory;
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
    }

    public IQ constructMinusIQ(IQTree tree, IQTree otherTree, Predicate<Variable> isPossiblyNullable) {
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(
                Sets.union(otherTree.getKnownVariables(), tree.getKnownVariables()).immutableCopy());
        RightProvenanceNormalizer.RightProvenance rightProvenance = rightProvenanceNormalizer.normalizeRightProvenance(
                otherTree, tree.getVariables(), Optional.empty(), variableGenerator);

        Optional<ImmutableExpression> nonNullabilityCondition = termFactory.getConjunction(
                tree.getVariables().stream()
                        .filter(v -> !isPossiblyNullable.test(v))
                        .map(termFactory::getDBIsNotNull));

        ImmutableExpression isNullCondition = termFactory.getDBIsNull(rightProvenance.getProvenanceVariable());
        ImmutableExpression filterCondition = iqTreeTools.getConjunction(nonNullabilityCondition, isNullCondition);

        IQTree minusTree = iqTreeTools.unaryIQTreeBuilder()
                .append(iqFactory.createConstructionNode(ImmutableSet.of(rightProvenance.getProvenanceVariable())))
                .append(iqFactory.createFilterNode(filterCondition))
                .build(iqTreeTools.createLeftJoinTree(
                        Optional.empty(),
                        tree,
                        rightProvenance.getRightTree()));

        // Hack
        DistinctVariableOnlyDataAtom minusFakeProjectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(
                atomFactory.getRDFAnswerPredicate(1),
                ImmutableList.of(rightProvenance.getProvenanceVariable()));

        return iqFactory.createIQ(minusFakeProjectionAtom, minusTree);
    }
}
