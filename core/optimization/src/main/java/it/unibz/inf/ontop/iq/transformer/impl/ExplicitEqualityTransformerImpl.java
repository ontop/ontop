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
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.ChildTransformer;
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

    @AssistedInject
    public ExplicitEqualityTransformerImpl(@Assisted VariableGenerator variableGenerator,
                                           IntermediateQueryFactory iqFactory,
                                           AtomFactory atomFactory,
                                           TermFactory termFactory,
                                           SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.variableGenerator = variableGenerator;
        this.substitutionFactory = substitutionFactory;
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
            ArgumentSubstitution<VariableOrGroundTerm> replacementVars = getArgumentReplacement(projectionAtom.getArguments());
            if (replacementVars.isEmpty())
                return dn;

            FilterNode filter = iqFactory.createFilterNode(replacementVars.getConjunction(termFactory, projectionAtom.getArguments()));

            DataAtom<AtomPredicate> atom = atomFactory.getDataAtom(
                    projectionAtom.getPredicate(),
                    replacementVars.replaceTerms(projectionAtom.getArguments()));

            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(dn.getVariables()),
                    iqFactory.createUnaryIQTree(filter, dn.newAtom(atom)));
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode dn) {
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> initialArgumentMap = dn.getArgumentMap();
            ArgumentSubstitution<VariableOrGroundTerm> replacementVars = getArgumentReplacement(initialArgumentMap);
            if (replacementVars.isEmpty())
                return dn;

            FilterNode filter = iqFactory.createFilterNode(replacementVars.getConjunction(termFactory, initialArgumentMap));

            ImmutableMap<Integer, ? extends VariableOrGroundTerm> newArgumentMap = replacementVars.replaceTerms(initialArgumentMap);
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(dn.getVariables()),
                    iqFactory.createUnaryIQTree(
                            filter,
                            iqFactory.createExtensionalDataNode(dn.getRelationDefinition(), newArgumentMap)));
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<InjectiveSubstitution<Variable>> substitutions = computeSubstitutions(children);
            if (substitutions.stream().allMatch(Substitution::isEmpty))
                return tree;

            ImmutableList<IQTree> updatedChildren = updateJoinChildren(substitutions, children);
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(tree.getVariables()),
                    iqFactory.createNaryIQTree(
                            iqFactory.createInnerJoinNode(
                                    Optional.of(updateJoinCondition(
                                            rootNode.getOptionalFilterCondition(),
                                            substitutions))),
                            updatedChildren));
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            ImmutableList<IQTree> children = ImmutableList.of(leftChild, rightChild);
            ImmutableList<InjectiveSubstitution<Variable>> substitutions = computeSubstitutions(children);
            if (substitutions.stream().allMatch(Substitution::isEmpty))
                return tree;

           ImmutableList<IQTree> updatedChildren = updateJoinChildren(substitutions, children);

           return iqFactory.createUnaryIQTree(
                   iqFactory.createConstructionNode(tree.getVariables()),
                   iqFactory.createBinaryNonCommutativeIQTree(
                           iqFactory.createLeftJoinNode(
                                   Optional.of(
                                           updateJoinCondition(
                                                   rootNode.getOptionalFilterCondition(),
                                                   substitutions))),
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

        private ImmutableExpression updateJoinCondition(Optional<ImmutableExpression> optionalFilterCondition, ImmutableList<InjectiveSubstitution<Variable>> substitutions) {
            Stream<ImmutableExpression> varEqualities = substitutions.stream()
                    .map(Substitution::builder)
                    .flatMap(b -> b.toStream(termFactory::getStrictEquality));
            
            return termFactory.getConjunction(optionalFilterCondition, varEqualities).get();
        }


        private ArgumentSubstitution<VariableOrGroundTerm> getArgumentReplacement(ImmutableList<? extends VariableOrGroundTerm> argumentList) {
            Set<Variable> vars = new HashSet<>(); // mutable!
            ImmutableMap<Integer, Variable> m = IntStream.range(0, argumentList.size())
                    .mapToObj(i -> getReplacement(argumentList.get(i), vars).stream()
                            .map(v -> Maps.immutableEntry(i, v)))
                    .flatMap(e -> e)
                    .collect(ImmutableCollectors.toMap());

            return new ArgumentSubstitution<>(m, Optional::ofNullable);
        }

        private ArgumentSubstitution<VariableOrGroundTerm> getArgumentReplacement(ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap) {
            Set<Variable> vars = new HashSet<>(); // mutable
            ImmutableMap<Integer, Variable> m = argumentMap.entrySet().stream()
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
     * Affects each outermost filter or (left) join n in the tree.
     * For each child of n, deletes its root if it is a filter node.
     * Then:
     * - if n is a join or filter: merge the boolean expressions
     * - if n is a left join: merge boolean expressions coming from the right, and lift the ones coming from the left.
     * This lift is only performed for optimization purposes: may save a subquery during SQL generation.
     */
    class FilterChildNormalizer extends DefaultNonRecursiveIQTreeTransformer {

        private final ChildTransformer childTransformer;

        public FilterChildNormalizer() {
            this.childTransformer = new ChildTransformer(iqFactory, this);
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {

            var leftFilter = UnaryIQTreeDecomposition.of(leftChild, FilterNode.class);
            var rightFilter = UnaryIQTreeDecomposition.of(rightChild, FilterNode.class);

            if (leftFilter.isPresent() || rightFilter.isPresent()) {
                IQTree leftJoinTree = iqFactory.createBinaryNonCommutativeIQTree(
                        rightFilter.map((f, t) -> iqFactory.createLeftJoinNode(
                                        termFactory.getConjunction(
                                                rootNode.getOptionalFilterCondition(),
                                                Stream.of(f.getFilterCondition()))))
                                .orElse(rootNode),
                        leftFilter.getChild(),
                        rightFilter.getChild());

                return leftFilter
                        .<IQTree>map((f, t) ->
                                iqFactory.createUnaryIQTree(
                                        iqFactory.createFilterNode(f.getFilterCondition()),
                                        leftJoinTree))
                        .orElse(leftJoinTree);
            }
            return tree;
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            var childrenFilters = children.stream()
                    .map(c -> UnaryIQTreeDecomposition.of(c, FilterNode.class))
                    .collect(ImmutableCollectors.toList());

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
                    childrenFilters.stream()
                            .map(UnaryIQTreeDecomposition::getChild)
                            .collect(ImmutableCollectors.toList()));
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            return UnaryIQTreeDecomposition.of(child, FilterNode.class)
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
            return childTransformer.transform(tree);
        }

        @Override
        protected IQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            return childTransformer.transform(tree);
        }

        @Override
        protected IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
            return childTransformer.transform(tree);
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
        public IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            ImmutableList<ConstructionNode> idleCns = getIdleCns(Stream.of(child));
            return idleCns.isEmpty()
                    ? tree
                    : getProjection(
                            tree.getVariables(),
                            iqFactory.createUnaryIQTree(
                                    rootNode,
                                    trimIdleCn(child)));
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<ConstructionNode> idleCns = getIdleCns(children.stream());
            return idleCns.isEmpty()
                    ? tree
                    : getProjection(
                            tree.getVariables(),
                            iqFactory.createNaryIQTree(
                                    rootNode,
                                    children.stream()
                                            .map(this::trimIdleCn)
                                            .collect(ImmutableCollectors.toList())));
        }

        @Override
        protected IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
            ImmutableList<ConstructionNode> idleCns = getIdleCns(Stream.of(leftChild, rightChild));
            return idleCns.isEmpty()
                    ? tree
                    : getProjection(
                            tree.getVariables(),
                            iqFactory.createBinaryNonCommutativeIQTree(
                                    rootNode,
                                    trimIdleCn(leftChild),
                                    trimIdleCn(rightChild)));
        }

        private ImmutableList<ConstructionNode> getIdleCns(Stream<IQTree> trees) {
            return trees
                    .map(this::getIdleCn)
                    .flatMap(Optional::stream)
                    .collect(ImmutableCollectors.toList());
        }

        private Optional<ConstructionNode> getIdleCn(IQTree tree) {
            return UnaryIQTreeDecomposition.of(tree, ConstructionNode.class)
                    .getOptionalNode()
                    .filter(cn -> cn.getSubstitution().isEmpty());
        }

        private IQTree trimIdleCn(IQTree tree) {
            return getIdleCn(tree).isPresent() ?
                    ((UnaryIQTree) tree).getChild() :
                    tree;
        }

        private IQTree getProjection(ImmutableSet<Variable> signature, IQTree tree) {
            return UnaryIQTreeDecomposition.of(tree, ConstructionNode.class)
                    .map((cn, t) -> iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(signature, cn.getSubstitution()),
                            t))
                    .orElseGet(() -> iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(signature),
                            tree));
        }
    }

    private static class ExplicitEqualityTransformerInternalException extends OntopInternalBugException {
        ExplicitEqualityTransformerInternalException(String message) {
            super(message);
        }
    }
}
