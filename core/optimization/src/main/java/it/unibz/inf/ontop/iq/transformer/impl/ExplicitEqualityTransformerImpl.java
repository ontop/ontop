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
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.CompositeIQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultNonRecursiveIQTreeTransformer;
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
    private final CompositeIQTreeTransformer compositeTransformer;
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
        ImmutableList<IQTreeTransformer> preTransformers = ImmutableList.of(new LocalExplicitEqualityEnforcer());
        ImmutableList<IQTreeTransformer> postTransformers = ImmutableList.of(new CnLifter(), new FilterChildNormalizer());
        this.compositeTransformer = new CompositeIQTreeTransformer(preTransformers, postTransformers, iqFactory);
    }

    @Override
    public IQTree transform(IQTree tree) {
          return compositeTransformer.transform(tree);
    }


    /**
     * Affects left joins, inner joins and data nodes.
     *
     * - left join: if the same variable is returned by both operands (implicit equality),
     * rename it (with a fresh variable) in the left branch, and make the corresponding equality explicit.
     *
     * - inner join: identical to left join, except that renaming is performed in each branch but the first where the variable appears.
     *
     * - data node or: if the data atom contains a ground term or a duplicate variable,
     * create a variable and make the equality explicit by creating a filter.
     *
     * If needed, create a root projection to ensure that the transformed query has the same signature as the input one.
     */
    class LocalExplicitEqualityEnforcer extends DefaultNonRecursiveIQTreeTransformer {

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

            return iqTreeTools.createIQTreeWithSignature(dn.getVariables(),
                    iqFactory.createUnaryIQTree(
                            iqFactory.createFilterNode(filterExpression),
                            dn.newAtom(atom)));
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode dn) {
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> initialArgumentMap = dn.getArgumentMap();
            ArgumentSubstitution<VariableOrGroundTerm> replacementVars = getArgumentReplacement(ArgumentSubstitution.stream(initialArgumentMap));
            if (replacementVars.isEmpty())
                return dn;

            ImmutableExpression filterExpression = replacementVars.getConjunction(termFactory, initialArgumentMap::get);
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> newArgumentMap = replacementVars.replaceTerms(initialArgumentMap);

            return iqTreeTools.createIQTreeWithSignature(dn.getVariables(),
                    iqFactory.createUnaryIQTree(
                            iqFactory.createFilterNode(filterExpression),
                            iqFactory.createExtensionalDataNode(dn.getRelationDefinition(), newArgumentMap)));
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<InjectiveSubstitution<Variable>> substitutions = computeSubstitutions(children);
            if (substitutions.stream().allMatch(Substitution::isEmpty))
                return tree;

            Optional<ImmutableExpression> updatedJoinCondition = updateJoinCondition(rootNode, substitutions);
            ImmutableList<IQTree> updatedChildren = updateJoinChildren(substitutions, children);

            return iqTreeTools.createIQTreeWithSignature(tree.getVariables(),
                    iqFactory.createNaryIQTree(
                            iqFactory.createInnerJoinNode(updatedJoinCondition),
                            updatedChildren));
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            ImmutableList<IQTree> children = ImmutableList.of(leftChild, rightChild);
            ImmutableList<InjectiveSubstitution<Variable>> substitutions = computeSubstitutions(children);
            if (substitutions.stream().allMatch(Substitution::isEmpty))
                return tree;

            Optional<ImmutableExpression> updatedJoinCondition = updateJoinCondition(rootNode, substitutions);
            ImmutableList<IQTree> updatedChildren = updateJoinChildren(substitutions, children);

            return iqTreeTools.createIQTreeWithSignature(tree.getVariables(),
                    iqFactory.createBinaryNonCommutativeIQTree(
                            iqFactory.createLeftJoinNode(updatedJoinCondition),
                            updatedChildren.get(0),
                            updatedChildren.get(1)));
        }

        private ImmutableList<InjectiveSubstitution<Variable>> computeSubstitutions(ImmutableList<IQTree> children) {
            if (children.size() < 2) {
                throw new ExplicitEqualityTransformerInternalException("At least 2 children are expected");
            }
            ImmutableSet<Variable> repeatedVariables = children.stream()
                    .flatMap(t -> t.getVariables().stream())
                    .collect(ImmutableCollectors.toMultiset()).entrySet().stream()
                    .filter(e -> e.getCount() > 1)
                    .map(Multiset.Entry::getElement)
                    .collect(ImmutableCollectors.toSet());

            return children.stream()
                    .map(t -> Sets.intersection(t.getVariables(), repeatedVariables).stream()
                            .filter(v -> !isFirstOcc(v, children, t))
                            .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator)))
                    .collect(ImmutableCollectors.toList());
        }

        private ImmutableList<IQTree> updateJoinChildren(ImmutableList<InjectiveSubstitution<Variable>> substitutions, ImmutableList<IQTree> children) {
            return IntStream.range(0, substitutions.size())
                    .mapToObj(i -> children.get(i).applyDescendingSubstitutionWithoutOptimizing(substitutions.get(i), variableGenerator))
                    .collect(ImmutableCollectors.toList());
        }

        private boolean isFirstOcc(Variable variable, ImmutableList<IQTree> children, IQTree tree) {
            return children.stream()
                    .filter(t -> t.getVariables().contains(variable))
                    .findFirst()
                    .orElseThrow(() -> new MinorOntopInternalBugException("Should be present"))
                    == tree;
        }

        private Optional<ImmutableExpression> updateJoinCondition(JoinOrFilterNode node, ImmutableList<InjectiveSubstitution<Variable>> substitutions) {
            Optional<ImmutableExpression> optionalFilterCondition = node.getOptionalFilterCondition();
            Stream<ImmutableExpression> varEqualities = substitutions.stream()
                    .map(Substitution::builder)
                    .flatMap(b -> b.toStream(termFactory::getStrictEquality));
            
            return termFactory.getConjunction(optionalFilterCondition, varEqualities);
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
     * Affects each **outermost** filter or (left) join n in the tree.
     * For each child of n, deletes its root if it is a filter node.
     * Then:
     * - if n is a join or filter: merge the boolean expressions
     * - if n is a left join: merge boolean expressions coming from the right, and lift the ones coming from the left.
     * This lift is only performed for optimization purposes: may save a subquery during SQL generation.
     */
    class FilterChildNormalizer extends DefaultNonRecursiveIQTreeTransformer {

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {

            var leftFilter = UnaryIQTreeDecomposition.of(leftChild, FilterNode.class);
            var rightFilter = UnaryIQTreeDecomposition.of(rightChild, FilterNode.class);

            if (leftFilter.isPresent() || rightFilter.isPresent()) {
                IQTree leftJoinTree = iqFactory.createBinaryNonCommutativeIQTree(
                        iqTreeTools.updateLeftJoinNodeWithConjunct(
                                rootNode,
                                rightFilter.getOptionalNode()
                                        .map(FilterNode::getFilterCondition)),
                        leftFilter.getChild(),
                        rightFilter.getChild());

                return iqTreeTools.createOptionalUnaryIQTree(
                        leftFilter.getOptionalNode()
                                .map(FilterNode::getFilterCondition)
                                .map(iqFactory::createFilterNode),
                        leftJoinTree);
            }
            return tree;
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            var childrenFilters = UnaryIQTreeDecomposition.of(children, FilterNode.class);

            ImmutableList<ImmutableExpression> filterChildExpressions = childrenFilters.stream()
                    .map(UnaryIQTreeDecomposition::getOptionalNode)
                    .flatMap(Optional::stream)
                    .map(FilterNode::getFilterCondition)
                    .collect(ImmutableCollectors.toList());

            if (filterChildExpressions.isEmpty())
                return tree;

            return iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(termFactory.getConjunction(
                            rootNode.getOptionalFilterCondition(),
                            filterChildExpressions.stream())),
                    UnaryIQTreeDecomposition.getChildren(childrenFilters));
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            var filterChild = UnaryIQTreeDecomposition.of(child, FilterNode.class);
            return filterChild
                    .<IQTree>map((f, t) -> iqFactory.createUnaryIQTree(
                            iqFactory.createFilterNode(termFactory.getConjunction(
                                    rootNode.getFilterCondition(),
                                    f.getFilterCondition())),
                            t))
                    .orElse(tree);
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

        @Override
        protected IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            return iqFactory.createUnaryIQTree(rootNode, transformChild(child));
        }

        @Override
        public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<IQTree> newChildren = transformChildren(children);
            return iqFactory.createNaryIQTree(rootNode, newChildren);
        }

    }

    /**
     * - Default behavior: for each child, deletes its root if it is a substitution-free construction node (i.e. a simple projection),
     * and lift the projection if needed
     * - Distinct or slice nodes: does not apply
     */
    class CnLifter extends DefaultNonRecursiveIQTreeTransformer {

        @Override
        public IQTree transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
            return tree;
        }

        @Override
        public IQTree transformSlice(UnaryIQTree tree, SliceNode rootNode, IQTree child) {
            return tree;
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree child) {
            var construction = UnaryIQTreeDecomposition.of(child, ConstructionNode.class);
            return !isIdleCn(construction)
                    ? tree
                    : iqFactory.createUnaryIQTree(node, construction.getChild());
        }

        // covers remaining unary nodes: filter, order by, aggregation, flatten?
        @Override
        protected IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode node, IQTree child) {
            var construction = UnaryIQTreeDecomposition.of(child, ConstructionNode.class);
            return !isIdleCn(construction)
                    ? tree
                    : iqTreeTools.createIQTreeWithSignature(tree.getVariables(),
                            iqFactory.createUnaryIQTree(node, construction.getChild()));
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode node, ImmutableList<IQTree> children) {
            var childrenConstruction = UnaryIQTreeDecomposition.of(children, ConstructionNode.class);

            return childrenConstruction.stream().noneMatch(this::isIdleCn)
                    ? tree
                    : iqTreeTools.createIQTreeWithSignature(tree.getVariables(),
                            iqFactory.createNaryIQTree(node,
                                    childrenConstruction.stream()
                                            .map(this::trimIdleCn)
                                            .collect(ImmutableCollectors.toList())));
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode node, IQTree leftChild, IQTree rightChild) {
            var leftConstruction = UnaryIQTreeDecomposition.of(leftChild, ConstructionNode.class);
            var rightConstruction = UnaryIQTreeDecomposition.of(rightChild, ConstructionNode.class);
            return !isIdleCn(leftConstruction) && !isIdleCn(rightConstruction)
                    ? tree
                    : iqTreeTools.createIQTreeWithSignature(tree.getVariables(),
                            iqFactory.createBinaryNonCommutativeIQTree(node,
                                    trimIdleCn(leftConstruction),
                                    trimIdleCn(rightConstruction)));
        }

        private boolean isIdleCn(UnaryIQTreeDecomposition<ConstructionNode> decomposition) {
           return decomposition.getOptionalNode()
                   .map(ConstructionNode::getSubstitution)
                   .filter(Substitution::isEmpty)
                   .isPresent();
        }

        private IQTree trimIdleCn(UnaryIQTreeDecomposition<ConstructionNode> decomposition) {
            if (isIdleCn(decomposition))
                return decomposition.getChild();

            return decomposition
                    .<IQTree>map(iqFactory::createUnaryIQTree) // not ideal as it creates a new tree
                    .orElseGet(decomposition::getChild);
        }
    }

    private static class ExplicitEqualityTransformerInternalException extends OntopInternalBugException {
        ExplicitEqualityTransformerInternalException(String message) {
            super(message);
        }
    }
}
