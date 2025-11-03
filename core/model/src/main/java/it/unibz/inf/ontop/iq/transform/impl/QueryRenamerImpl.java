package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.node.DefaultQueryNodeTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;


@Singleton
public class QueryRenamerImpl implements QueryRenamer {

    private final IntermediateQueryFactory iqFactory;
    private final AtomFactory atomFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private QueryRenamerImpl(IntermediateQueryFactory iqFactory, AtomFactory atomFactory, SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
        this.atomFactory = atomFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public IQ applyInDepthRenaming(InjectiveSubstitution<Variable> renaming, IQ originalQuery) {
        if (renaming.isEmpty())
            return originalQuery;

        IQTree newIQTree = applyRenaming(renaming, originalQuery.getTree());

        DistinctVariableOnlyDataAtom atom = originalQuery.getProjectionAtom();
        ImmutableList<Variable> newArguments = substitutionFactory.apply(renaming, atom.getArguments());
        DistinctVariableOnlyDataAtom newProjectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(atom.getPredicate(), newArguments);

        return iqFactory.createIQ(newProjectionAtom, newIQTree);
    }

    @Override
    public IQTree applyInDepthRenaming(InjectiveSubstitution<Variable> renaming, IQTree originalTree) {
        if (renaming.isEmpty())
            return originalTree;

        return applyRenaming(renaming, originalTree);
    }

    private IQTree applyRenaming(InjectiveSubstitution<Variable> renamingSubstitution, IQTree tree) {
        QueryNodeRenamer nodeTransformer = new QueryNodeRenamer(renamingSubstitution);
        return nodeTransformer.treeTransformer().transform(tree);
    }

    /**
     * Renames query nodes according to one renaming substitution.
     */
    private class QueryNodeRenamer extends DefaultQueryNodeTransformer {

        private final InjectiveSubstitution<Variable> renamingSubstitution;

        public QueryNodeRenamer(InjectiveSubstitution<Variable> renamingSubstitution) {
            super(QueryRenamerImpl.this.iqFactory);
            this.renamingSubstitution = renamingSubstitution;
        }

        @Override
        public FilterNode transform(FilterNode filterNode, UnaryIQTree tree) {
            return filterNode.applyFreshRenaming(renamingSubstitution);
        }

        @Override
        public ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode) {
            return extensionalDataNode.applyFreshRenaming(renamingSubstitution);
        }

        @Override
        public LeftJoinNode transform(LeftJoinNode leftJoinNode, BinaryNonCommutativeIQTree tree) {
            return leftJoinNode.applyFreshRenaming(renamingSubstitution);
        }

        @Override
        public UnionNode transform(UnionNode unionNode, NaryIQTree tree) {
            return unionNode.applyFreshRenaming(renamingSubstitution);
        }

        @Override
        public IntensionalDataNode transform(IntensionalDataNode intensionalDataNode) {
            return intensionalDataNode.applyFreshRenaming(renamingSubstitution);
        }

        @Override
        public InnerJoinNode transform(InnerJoinNode innerJoinNode, NaryIQTree tree) {
            return innerJoinNode.applyFreshRenaming(renamingSubstitution);
        }

        @Override
        public ConstructionNode transform(ConstructionNode constructionNode, UnaryIQTree tree) {
            return constructionNode.applyFreshRenaming(renamingSubstitution);
        }

        @Override
        public AggregationNode transform(AggregationNode aggregationNode, UnaryIQTree tree) {
            return aggregationNode.applyFreshRenaming(renamingSubstitution);
        }

        @Override
        public FlattenNode transform(FlattenNode flattenNode, UnaryIQTree tree) {
            return flattenNode.applyFreshRenaming(renamingSubstitution);
        }

        @Override
        public EmptyNode transform(EmptyNode emptyNode) {
            return emptyNode.applyFreshRenaming(renamingSubstitution);
        }

        @Override
        public TrueNode transform(TrueNode trueNode) {
            return trueNode.applyFreshRenaming(renamingSubstitution);
        }

        @Override
        public ValuesNode transform(ValuesNode valuesNode) {
            return valuesNode.applyFreshRenaming(renamingSubstitution);
        }

        @Override
        public DistinctNode transform(DistinctNode distinctNode, UnaryIQTree tree) {
            return distinctNode.applyFreshRenaming(renamingSubstitution);
        }

        @Override
        public SliceNode transform(SliceNode sliceNode, UnaryIQTree tree) {
            return sliceNode.applyFreshRenaming(renamingSubstitution);
        }

        @Override
        public OrderByNode transform(OrderByNode orderByNode, UnaryIQTree tree) {
            return orderByNode.applyFreshRenaming(renamingSubstitution);
        }
    }
}
