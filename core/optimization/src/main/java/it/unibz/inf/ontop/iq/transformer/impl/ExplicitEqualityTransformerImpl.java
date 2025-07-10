package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transformer.ExplicitEqualityTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ArgumentSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;


public class ExplicitEqualityTransformerImpl implements ExplicitEqualityTransformer {

    private final IntermediateQueryFactory iqFactory;
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final VariableGenerator variableGenerator;
    private final SubstitutionFactory substitutionFactory;
    private final IQTreeTools iqTreeTools;

    @AssistedInject
    public ExplicitEqualityTransformerImpl(@Assisted VariableGenerator variableGenerator,
                                           IntermediateQueryFactory iqFactory,
                                           AtomFactory atomFactory,
                                           TermFactory termFactory,
                                           SubstitutionFactory substitutionFactory,
                                           IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.variableGenerator = variableGenerator;
        this.substitutionFactory = substitutionFactory;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public IQTree transform(IQTree tree) {
        var transformer0 = new LocalExplicitEqualityEnforcer();
        IQTree newTree0 = tree.acceptVisitor(transformer0);
        var transformer1 = new CnLifter();
        IQTree newTree1 = newTree0.acceptVisitor(transformer1);
        var transformer2 = new FilterChildNormalizer();
        IQTree newTree2 = newTree1.acceptVisitor(transformer2);
        return newTree2;
    }


    /**
     * Affects left joins, inner joins and data nodes.
     *
     * - left join: if the same variable is returned by both operands (implicit equality),
     * rename it (with a fresh variable) in the left branch, and make the corresponding equality explicit.
     *
     * - inner join: identical to left join, except that renaming is performed in each branch but the first where the variable appears.
     *
     * - data node: if the data atom contains a ground term or a duplicate variable,
     * create a variable and make the equality explicit by creating a filter.
     *
     * If needed, create a root projection to ensure that the transformed query has the same signature as the input one.
     */
    private class LocalExplicitEqualityEnforcer extends DefaultRecursiveIQTreeVisitingTransformer {

        LocalExplicitEqualityEnforcer() {
            super(ExplicitEqualityTransformerImpl.this.iqFactory);
        }

        @Override
        public IQTree transformIntensionalData(IntensionalDataNode dn) {
            DataAtom<AtomPredicate> projectionAtom = dn.getProjectionAtom();
            ImmutableList<? extends VariableOrGroundTerm> argumentList = projectionAtom.getArguments();
            ArgumentSubstitution<VariableOrGroundTerm> replacementVars = getArgumentReplacement(ArgumentSubstitution.stream(argumentList));
            if (replacementVars.isEmpty())
                return dn;

            ImmutableExpression filterExpression = replacementVars.getConjunction(termFactory, argumentList::get);
            DataAtom<AtomPredicate> atom = atomFactory.getDataAtom(
                    projectionAtom.getPredicate(),
                    replacementVars.replaceTerms(projectionAtom.getArguments()));

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(iqFactory.createConstructionNode(dn.getVariables()))
                    .append(iqFactory.createFilterNode(filterExpression))
                    .build(dn.newAtom(atom));
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode dn) {
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> initialArgumentMap = dn.getArgumentMap();
            ArgumentSubstitution<VariableOrGroundTerm> replacementVars = getArgumentReplacement(ArgumentSubstitution.stream(initialArgumentMap));
            if (replacementVars.isEmpty())
                return dn;

            ImmutableExpression filterExpression = replacementVars.getConjunction(termFactory, initialArgumentMap::get);
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> newArgumentMap = replacementVars.replaceTerms(initialArgumentMap);

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(iqFactory.createConstructionNode(dn.getVariables()))
                    .append(iqFactory.createFilterNode(filterExpression))
                    .build(iqFactory.createExtensionalDataNode(dn.getRelationDefinition(), newArgumentMap));
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<InjectiveSubstitution<Variable>> substitutions = computeSubstitutions(children);
            if (substitutions.stream().allMatch(Substitution::isEmpty))
                return super.transformInnerJoin(tree, rootNode, children);

            Optional<ImmutableExpression> updatedJoinCondition = updateJoinCondition(rootNode, substitutions);
            ImmutableList<IQTree> updatedChildren = updateJoinChildren(substitutions, children);

            return  iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(tree.getVariables()),
                        iqTreeTools.createInnerJoinTree(updatedJoinCondition, updatedChildren));
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            ImmutableList<IQTree> children = ImmutableList.of(leftChild, rightChild);
            ImmutableList<InjectiveSubstitution<Variable>> substitutions = computeSubstitutions(children);
            if (substitutions.stream().allMatch(Substitution::isEmpty))
                return super.transformLeftJoin(tree, rootNode, leftChild, rightChild);

            Optional<ImmutableExpression> updatedJoinCondition = updateJoinCondition(rootNode, substitutions);
            ImmutableList<IQTree> updatedChildren = updateJoinChildren(substitutions, children);

            return iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(tree.getVariables()),
                        iqTreeTools.createLeftJoinTree(updatedJoinCondition, updatedChildren.get(0), updatedChildren.get(1)));
        }

        private ImmutableList<InjectiveSubstitution<Variable>> computeSubstitutions(ImmutableList<IQTree> children) {
            if (children.size() < 2) {
                throw new ExplicitEqualityTransformerInternalException("At least 2 children are expected");
            }
            ImmutableSet<Variable> repeatedVariables = NaryIQTreeTools.coOccurringVariablesStream(children)
                    .collect(ImmutableSet.toImmutableSet());

            return children.stream()
                    .map(t -> Sets.intersection(t.getVariables(), repeatedVariables).stream()
                            .filter(v -> !isFirstOcc(v, children, t))
                            .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator)))
                    .collect(ImmutableCollectors.toList());
        }

        private ImmutableList<IQTree> updateJoinChildren(ImmutableList<InjectiveSubstitution<Variable>> substitutions, ImmutableList<IQTree> children) {
            return IntStream.range(0, substitutions.size())
                    .mapToObj(i -> children.get(i).applyFreshRenaming(substitutions.get(i)))
                    .map(this::transformChild)
                    .collect(ImmutableCollectors.toList());
        }

        private boolean isFirstOcc(Variable variable, ImmutableList<IQTree> children, IQTree tree) {
            return tree == children.stream()
                    .filter(t -> t.getVariables().contains(variable))
                    .findFirst()
                    .orElseThrow(() -> new MinorOntopInternalBugException("Should be present"));
        }

        private Optional<ImmutableExpression> updateJoinCondition(JoinOrFilterNode node, ImmutableList<InjectiveSubstitution<Variable>> substitutions) {
            Stream<ImmutableExpression> varEqualities = substitutions.stream()
                    .map(Substitution::builder)
                    .flatMap(b -> b.toStream(termFactory::getStrictEquality));
            
            return termFactory.getConjunction(node.getOptionalFilterCondition(), varEqualities);
        }

        private <T extends VariableOrGroundTerm> ArgumentSubstitution<VariableOrGroundTerm> getArgumentReplacement(Stream<Map.Entry<Integer, T>> argumentStream) {
            Set<Variable> vars = new HashSet<>(); // mutable
            ImmutableMap<Integer, Variable> m = argumentStream
                    .flatMap(e -> getReplacement(e.getValue(), vars).stream()
                            .map(v -> Maps.immutableEntry(e.getKey(), v)))
                    .collect(ImmutableCollectors.toMap());

            return new ArgumentSubstitution<>(m, Optional::ofNullable);
        }

        private Optional<Variable> getReplacement(VariableOrGroundTerm term, Set<Variable> vars) { // vars is mutable
            if (term instanceof GroundTerm) {
                return Optional.of(variableGenerator.generateNewVariable());
            }
            else {
                Variable var = (Variable) term;
                if (vars.contains(var)) {
                    return Optional.of(variableGenerator.generateNewVariableFromVar(var));
                }
                else {
                    vars.add(var);
                    return Optional.empty();
                }
            }
        }
    }


    /**
     * Affects each filter or (left) join n in the tree.
     * For each child of n, deletes its root if it is a filter node.
     * Then:
     * - if n is a join or filter: merge the boolean expressions
     * - if n is a left join: merge boolean expressions coming from the right, and lift the ones coming from the left.
     * This lift is only performed for optimization purposes: may save a subquery during SQL generation.
     *
     * TODO: compare with FilterLifterImpl
     */
    private class FilterChildNormalizer extends DefaultRecursiveIQTreeVisitingTransformer {

        FilterChildNormalizer() {
            super(ExplicitEqualityTransformerImpl.this.iqFactory);
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {

            IQTree liftedLeftChild = transformChild(leftChild);
            IQTree liftedRightChild = transformChild(rightChild);

            var leftFilter = UnaryIQTreeDecomposition.of(liftedLeftChild, FilterNode.class);
            var rightFilter = UnaryIQTreeDecomposition.of(liftedRightChild, FilterNode.class);

            if (leftFilter.isPresent() || rightFilter.isPresent()) {
                IQTree leftJoinTree = iqTreeTools.createLeftJoinTree(
                       iqTreeTools.getConjunction(
                                        rootNode.getOptionalFilterCondition(),
                                        rightFilter.getOptionalNode().map(FilterNode::getFilterCondition)),
                        leftFilter.getTail(),
                        rightFilter.getTail());

                return iqTreeTools.unaryIQTreeBuilder()
                        .append(leftFilter.getOptionalNode())
                        .build(leftJoinTree);
            }
            return withTransformedChildren(tree, liftedLeftChild, liftedRightChild);
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<IQTree> liftedChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);
            var childrenFilters = UnaryIQTreeDecomposition.of(liftedChildren, FilterNode.class);

            ImmutableList<ImmutableExpression> filterChildExpressions = UnaryIQTreeDecomposition.getNodeStream(childrenFilters)
                    .map(FilterNode::getFilterCondition)
                    .collect(ImmutableCollectors.toList());

            if (!filterChildExpressions.isEmpty())
                return iqTreeTools.createInnerJoinTree(
                        termFactory.getConjunction(
                                rootNode.getOptionalFilterCondition(),
                                filterChildExpressions.stream()),
                        UnaryIQTreeDecomposition.getTails(childrenFilters));

            return withTransformedChildren(tree, liftedChildren);
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            IQTree liftedChild = transformChild(child);
            var filterChild = UnaryIQTreeDecomposition.of(liftedChild, FilterNode.class);
            if (filterChild.isPresent())
                    return iqFactory.createUnaryIQTree(iqFactory.createFilterNode(
                            termFactory.getConjunction(rootNode.getFilterCondition(), filterChild.getNode().getFilterCondition())),
                        filterChild.getChild());

            return withTransformedChild(tree,  liftedChild);
        }


//        private IQTree transformFlattenNode(IQTree tree, FlattenNode node, IQTree child) {
//
//            ImmutableList<Optional<Variable>> replacementVars = getArgumentReplacement(node.getDataAtom(), child.getVariables());
//
//            if (empt(replacementVars))
//                return tree;
//
//            FilterNode filter = createFilter(node.getDataAtom(), replacementVars);
//            DataAtom atom = replaceVars(node.getDataAtom(), replacementVars);
//            return iqFactory.createUnaryIQTree(
//                    iqFactory.createConstructionNode(node.getDataAtom().getVariables()),
//                    iqFactory.createUnaryIQTree(
//                            filter,
//                            iqFactory.createUnaryIQTree(
//                                    node.newNode(
//                                            node.getFlattenedVariable(),
//                                            node.getArrayIndexIndex(),
//                                            atom),
//                                    child
//                            )));
//        }
//    }
    }

    /**
     * - Default behavior: for each child, deletes its root if it is a substitution-free construction node
     *     (i.e. a simple projection), and lift the projection if needed
     * - Distinct or slice nodes: does not apply
     */
    class CnLifter extends DefaultRecursiveIQTreeVisitingTransformer {

        CnLifter() {
            super(ExplicitEqualityTransformerImpl.this.iqFactory);
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree child) {
            IQTree liftedChild = transformChild(child);
            var construction = UnaryIQTreeDecomposition.of(liftedChild, ConstructionNode.class);
            if (isProjectionConstructionNode(construction))
                return iqFactory.createUnaryIQTree(node, construction.getChild());
            return withTransformedChild(tree, liftedChild);
        }

        @Override
        public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
            return defaultTransformUnaryNode(tree, rootNode, child);
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            return defaultTransformUnaryNode(tree, rootNode, child);
        }

        @Override
        public IQTree transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
            return defaultTransformUnaryNode(tree, rootNode, child);
        }

        @Override
        public IQTree transformAggregation(UnaryIQTree tree, AggregationNode rootNode, IQTree child) {
            return defaultTransformUnaryNode(tree, rootNode, child);
        }

        private IQTree defaultTransformUnaryNode(UnaryIQTree tree, UnaryOperatorNode node, IQTree child) {
            IQTree liftedChild = transformChild(child);
            var construction = UnaryIQTreeDecomposition.of(liftedChild, ConstructionNode.class);
            if (isProjectionConstructionNode(construction))
                return iqTreeTools.unaryIQTreeBuilder()
                        .append(iqFactory.createConstructionNode(tree.getVariables()))
                        .append(node)
                        .build(construction.getChild());

            return withTransformedChild(tree, liftedChild);
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode node, ImmutableList<IQTree> children) {
            ImmutableList<IQTree> liftedChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);
            var childrenConstruction = UnaryIQTreeDecomposition.of(liftedChildren, ConstructionNode.class);
            if (childrenConstruction.stream().anyMatch(this::isProjectionConstructionNode))
                    return iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(tree.getVariables()),
                            iqFactory.createNaryIQTree(node,
                                    childrenConstruction.stream()
                                            .map(this::trimProjectionConstructionNode)
                                            .collect(ImmutableCollectors.toList())));

            return withTransformedChildren(tree, liftedChildren);
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode node, IQTree leftChild, IQTree rightChild) {
            IQTree liftedLeftChild = transformChild(leftChild);
            IQTree liftedRightChild = transformChild(rightChild);

            var leftConstruction = UnaryIQTreeDecomposition.of(liftedLeftChild, ConstructionNode.class);
            var rightConstruction = UnaryIQTreeDecomposition.of(liftedRightChild, ConstructionNode.class);

            if (isProjectionConstructionNode(leftConstruction) || isProjectionConstructionNode(rightConstruction))
                return iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(tree.getVariables()),
                        iqFactory.createBinaryNonCommutativeIQTree(node,
                                trimProjectionConstructionNode(leftConstruction),
                                trimProjectionConstructionNode(rightConstruction)));

            return withTransformedChildren(tree, liftedLeftChild, liftedRightChild);
        }

        private boolean isProjectionConstructionNode(UnaryIQTreeDecomposition<ConstructionNode> decomposition) {
           return decomposition.getOptionalNode()
                   .map(ConstructionNode::getSubstitution)
                   .filter(Substitution::isEmpty)
                   .isPresent();
        }

        private IQTree trimProjectionConstructionNode(UnaryIQTreeDecomposition<ConstructionNode> decomposition) {
            if (isProjectionConstructionNode(decomposition))
                return decomposition.getChild();

            return decomposition.getOptionalNode()
                    .<IQTree>map(n -> decomposition.getTree())
                    .orElseGet(decomposition::getTail);
        }
    }

    private static class ExplicitEqualityTransformerInternalException extends OntopInternalBugException {
        ExplicitEqualityTransformerInternalException(String message) {
            super(message);
        }
    }
}
