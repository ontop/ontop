package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
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
        ImmutableList<IQTreeTransformer> postTransformers = ImmutableList.of(new CnLifter(), new FilterChildNormalizer(iqFactory));
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
            return transformIntensionalDataNode(dn);
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode dn) {
            return transformExtensionalDataNode(dn);
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
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
                                            substitutions ))),
                            updatedChildren));
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
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
            Iterator<IQTree> it = children.iterator();
            return substitutions.stream()
                    .map(s -> it.next().applyDescendingSubstitutionWithoutOptimizing(s, variableGenerator))
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
                    .flatMap(Substitution.Builder::toStrictEqualities);
            
            return termFactory.getConjunction(optionalFilterCondition, varEqualities).get();
        }

        private IQTree transformIntensionalDataNode(IntensionalDataNode dn) {
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
                    iqFactory.createUnaryIQTree(
                            filter,
                            dn.newAtom(atom)));
        }

        private IQTree transformExtensionalDataNode(ExtensionalDataNode dn) {
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

        public FilterChildNormalizer(IntermediateQueryFactory iqFactory) {
            this.childTransformer = new ChildTransformer(iqFactory, this);
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {

            Optional<ImmutableExpression> leftChildChildExpression = getOptionalChildExpression(leftChild);
            Optional<ImmutableExpression> rightChildExpression = getOptionalChildExpression(rightChild);

            if(leftChildChildExpression.isPresent() || rightChildExpression.isPresent()) {
                Optional<ImmutableExpression> joinCondition = rootNode.getOptionalFilterCondition();
                Stream<ImmutableExpression> additionalConditions = rightChildExpression.stream();
                IQTree leftJoinTree = iqFactory.createBinaryNonCommutativeIQTree(
                        rightChildExpression.isPresent() ?
                                iqFactory.createLeftJoinNode(termFactory.getConjunction(joinCondition, additionalConditions)) :
                                rootNode,
                        trimRootFilter(leftChild),
                        trimRootFilter(rightChild));

                return leftChildChildExpression.isPresent() ?
                        iqFactory.createUnaryIQTree(iqFactory.createFilterNode(leftChildChildExpression.get()), leftJoinTree) :
                        leftJoinTree;
            }
            return tree;
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<ImmutableExpression> filterChildExpressions = children.stream()
                    .flatMap(c -> getOptionalChildExpression(c).stream())
                    .collect(ImmutableCollectors.toList());
            if (filterChildExpressions.isEmpty())
                return tree;

            Optional<ImmutableExpression> joinCondition = rootNode.getOptionalFilterCondition();
            return iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(termFactory.getConjunction(joinCondition, filterChildExpressions.stream())),
                    children.stream()
                            .map(this::trimRootFilter)
                            .collect(ImmutableCollectors.toList()));
        }

        @Override
        public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
            Optional<ImmutableExpression> filterChildExpressions = getOptionalChildExpression(child);
            if (filterChildExpressions.isEmpty())
                return tree;

            Optional<ImmutableExpression> joinCondition = Optional.of(rootNode.getFilterCondition());
            Stream<ImmutableExpression> additionalConditions = filterChildExpressions.stream();
            return iqFactory.createUnaryIQTree(
                    iqFactory.createFilterNode(termFactory.getConjunction(joinCondition, additionalConditions).get()),
                    trimRootFilter(child));
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

        private Optional<ImmutableExpression> getOptionalChildExpression(IQTree child) {
            return Optional.of(child)
                    .map(IQTree::getRootNode)
                    .filter(n -> n instanceof FilterNode)
                    .map(n -> (FilterNode)n)
                    .map(FilterNode::getFilterCondition);
        }

        private IQTree trimRootFilter(IQTree tree) {
            return tree.getRootNode() instanceof FilterNode ?
                    ((UnaryIQTree) tree).getChild() :
                    tree;
        }

        protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            return childTransformer.transform(tree);
        }

        protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            return childTransformer.transform(tree);
        }

        protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
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
        public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
            return tree;
        }

        @Override
        public IQTree transformSlice(IQTree tree, SliceNode rootNode, IQTree child) {
            return tree;
        }

        @Override
        public IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            ImmutableList<ConstructionNode> idleCns = getIdleCns(Stream.of(child));
            return idleCns.isEmpty() ?
                    tree :
                    getProjection(
                            tree.getVariables(),
                            iqFactory.createUnaryIQTree(
                                    rootNode,
                                    trimIdleCn(child)));
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<ConstructionNode> idleCns = getIdleCns(children.stream());
            return idleCns.isEmpty() ?
                    tree :
                    getProjection(
                            tree.getVariables(),
                            iqFactory.createNaryIQTree(
                                    rootNode,
                                    children.stream()
                                            .map(this::trimIdleCn)
                                            .collect(ImmutableCollectors.toList())));
        }

        @Override
        protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
            ImmutableList<ConstructionNode> idleCns = getIdleCns(Stream.of(leftChild, rightChild));
            return idleCns.isEmpty() ?
                    tree :
                    getProjection(
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
            return Optional.of(tree.getRootNode())
                    .filter(r -> r instanceof ConstructionNode)
                    .map(r -> (ConstructionNode) r)
                    .filter(cn -> cn.getSubstitution().isEmpty());
        }

        private IQTree trimIdleCn(IQTree tree) {
            return getIdleCn(tree).isPresent() ?
                    ((UnaryIQTree) tree).getChild() :
                    tree;
        }

        private IQTree getProjection(ImmutableSet<Variable> signature, IQTree tree) {
            QueryNode root = tree.getRootNode();
            if (root instanceof ConstructionNode){
                return iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(signature, ((ConstructionNode)root).getSubstitution()),
                        ((UnaryIQTree)tree).getChild());
            }
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(signature),
                    tree);
        }
    }

    private static class ExplicitEqualityTransformerInternalException extends OntopInternalBugException {
        ExplicitEqualityTransformerInternalException(String message) {
            super(message);
        }
    }
}
