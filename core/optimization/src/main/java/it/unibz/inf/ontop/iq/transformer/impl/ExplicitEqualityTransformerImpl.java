package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
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
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
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
     * Affects (left) joins and data nodes.
     * - left join: if the same variable is returned by both operands (implicit equality),
     * rename it (with a fresh variable each time) in each branch but the left one,
     * and make the corresponding equalities explicit.
     * - inner join: identical to left join, but renaming is performed in each branch but the first where the variable appears
     * - data node: if the data node contains a ground term, create a variable and make the equality explicit (create a filter).
     *
     * If needed, creates a root projection to ensure that the transformed query has the same signature as the input one
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
            ImmutableList<InjectiveVar2VarSubstitution> substitutions = computeSubstitutions(children);
            if (substitutions.stream().allMatch(ImmutableSubstitution::isEmpty))
                return tree;

            ImmutableList<IQTree> updatedChildren = updateJoinChildren(substitutions, children);
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(tree.getVariables()),
                    iqFactory.createNaryIQTree(
                            iqFactory.createInnerJoinNode(
                                    Optional.of(updateJoinCondition(
                                            rootNode.getOptionalFilterCondition(),
                                            substitutions ))),
                            updatedChildren
                    ));
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            ImmutableList<IQTree> children = ImmutableList.of(leftChild, rightChild);
            ImmutableList<InjectiveVar2VarSubstitution> substitutions = computeSubstitutions(children);
            if (substitutions.stream().allMatch(ImmutableSubstitution::isEmpty))
                return tree;

           ImmutableList<IQTree> updatedChildren = updateJoinChildren(substitutions, children);


           return iqFactory.createUnaryIQTree(
                   iqFactory.createConstructionNode(tree.getVariables()),
                   iqFactory.createBinaryNonCommutativeIQTree(
                           iqFactory.createLeftJoinNode(
                                   Optional.of(
                                           updateJoinCondition(
                                                   rootNode.getOptionalFilterCondition(),
                                                   substitutions
                                           ))),
                           updatedChildren.get(0),
                           updatedChildren.get(1)
                   ));
        }

        private ImmutableList<InjectiveVar2VarSubstitution> computeSubstitutions(ImmutableList<IQTree> children) {
            if (children.size() < 2) {
                throw new ExplicitEqualityTransformerInternalException("At least 2 children are expected");
            }
            ImmutableSet<Variable> repeatedVariables = children.stream()
                    .flatMap(t -> t.getVariables().stream())
                    .collect(ImmutableCollectors.toMultiset()).entrySet().stream()
                    .filter(e -> e.getCount() > 1)
                    .map(Multiset.Entry::getElement)
                    .collect(ImmutableCollectors.toSet());

            return children.stream().sequential()
                    .map(t -> substitutionFactory.getInjectiveVar2VarSubstitution(
                                        t.getVariables().stream()
                                                .filter(repeatedVariables::contains)
                                                .filter(v -> !isFirstOcc(v, children, t)),
                                        variableGenerator::generateNewVariableFromVar))
                    .collect(ImmutableCollectors.toList());
        }

        private ImmutableList<IQTree> updateJoinChildren(ImmutableList<InjectiveVar2VarSubstitution> substitutions, ImmutableList<IQTree> children) {
            Iterator<IQTree> it = children.iterator();
            return substitutions.stream().sequential()
                    .map(s -> it.next().applyDescendingSubstitutionWithoutOptimizing(s))
                    .collect(ImmutableCollectors.toList());
        }

        private boolean isFirstOcc(Variable variable, ImmutableList<IQTree> children, IQTree tree) {
            return children.stream().sequential()
                    .filter(t -> t.getVariables().contains(variable))
                    .findFirst()
                    .orElseThrow(() -> new MinorOntopInternalBugException("Should be present"))
                    == tree;
        }

        private ImmutableExpression updateJoinCondition(Optional<ImmutableExpression> optionalFilterCondition, ImmutableList<InjectiveVar2VarSubstitution> substitutions) {
            Stream<ImmutableExpression> varEqualities = substitutions.stream()
                    .flatMap(s -> s.getImmutableMap().entrySet().stream())
                    .map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue()));
            return termFactory.getConjunction(optionalFilterCondition, varEqualities).get();
        }

        private IQTree transformIntensionalDataNode(IntensionalDataNode dn) {
            ImmutableList<Optional<Variable>> replacementVars = getArgumentReplacement(dn.getProjectionAtom());

            if (empt(replacementVars))
                return dn;

            FilterNode filter = createFilter(dn.getProjectionAtom(), replacementVars);
            DataAtom<AtomPredicate> atom = replaceVars(dn.getProjectionAtom(), replacementVars);
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(dn.getVariables()),
                    iqFactory.createUnaryIQTree(
                            filter,
                            dn.newAtom(atom))
            );
        }

        private IQTree transformExtensionalDataNode(ExtensionalDataNode dn) {
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> initialArgumentMap = dn.getArgumentMap();
            ImmutableMap<Integer, Variable> replacementVars = getArgumentReplacement(initialArgumentMap);

            if (replacementVars.isEmpty())
                return dn;

            FilterNode filter = createFilter(initialArgumentMap, replacementVars);
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> newArgumentMap = replaceVars(initialArgumentMap, replacementVars);
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(dn.getVariables()),
                    iqFactory.createUnaryIQTree(
                            filter,
                            iqFactory.createExtensionalDataNode(dn.getRelationDefinition(), newArgumentMap))
            );
        }



        private boolean empt(ImmutableList<Optional<Variable>> replacementVars) {
            return replacementVars.stream()
                    .noneMatch(Optional::isPresent);
        }

        private <P extends AtomPredicate> DataAtom<P> replaceVars(DataAtom<P> projectionAtom, ImmutableList<Optional<Variable>> replacements) {
            Iterator<Optional<Variable>> it = replacements.iterator();
            return atomFactory.getDataAtom(
                    projectionAtom.getPredicate(),
                    projectionAtom.getArguments().stream()
                            .map(a -> {
                                Optional<Variable> r = it.next();
                                return r.isPresent() ?
                                        r.get() :
                                        a;
                            })
                            .collect(ImmutableCollectors.toList())
            );
        }

        private ImmutableMap<Integer, ? extends VariableOrGroundTerm> replaceVars(
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                ImmutableMap<Integer, Variable> replacements) {
            return argumentMap.entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> Optional.ofNullable((VariableOrGroundTerm)replacements.get(e.getKey()))
                                    .orElseGet(e::getValue)));
        }

        private ImmutableList<Optional<Variable>> getArgumentReplacement(DataAtom<AtomPredicate> dataAtom) {
            Set<Variable> vars = new HashSet<>();
            List<Optional<Variable>> replacements = new ArrayList<>();
            for (VariableOrGroundTerm term: dataAtom.getArguments()) {
                if (term instanceof GroundTerm) {
                    replacements.add(Optional.of(variableGenerator.generateNewVariable()));
                } else if (term instanceof Variable) {
                    Variable var = (Variable) term;
                    if (vars.contains(var)) {
                        replacements.add(Optional.of(variableGenerator.generateNewVariableFromVar(var)));
                    } else {
                        replacements.add(Optional.empty());
                        vars.add(var);
                    }
                }
            }
            return ImmutableList.copyOf(replacements);
        }

        private ImmutableMap<Integer, Variable> getArgumentReplacement(
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap) {
            Set<Variable> vars = new HashSet<>();
            Map<Integer, Variable> replacementMap = new HashMap<>();
            for (Map.Entry<Integer, ? extends VariableOrGroundTerm> entry : argumentMap.entrySet()) {
                VariableOrGroundTerm term = entry.getValue();
                if (term instanceof GroundTerm) {
                    replacementMap.put(entry.getKey(), variableGenerator.generateNewVariable());
                } else if (term instanceof Variable) {
                    Variable var = (Variable) term;
                    if (vars.contains(var)) {
                        replacementMap.put(entry.getKey(), variableGenerator.generateNewVariableFromVar(var));
                    } else {
                        vars.add(var);
                    }
                }
            }
            return ImmutableMap.copyOf(replacementMap);
        }

        private FilterNode createFilter(DataAtom<AtomPredicate> da, ImmutableList<Optional<Variable>> replacementVars) {
            Iterator<Optional<Variable>> it = replacementVars.iterator();
            return iqFactory.createFilterNode(
                    termFactory.getConjunction(da.getArguments().stream()
                            .map(a -> getEquality(a, it.next()))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(ImmutableCollectors.toList())));
        }

        private FilterNode createFilter(ImmutableMap<Integer, ? extends VariableOrGroundTerm> initialArgumentMap,
                                        ImmutableMap<Integer, Variable> replacementVars) {
            return iqFactory.createFilterNode(
                    termFactory.getConjunction(replacementVars.entrySet().stream()
                            .map(e -> termFactory.getStrictEquality(initialArgumentMap.get(e.getKey()), e.getValue()))
                            .collect(ImmutableCollectors.toList())));
        }

        private Optional<ImmutableExpression> getEquality(VariableOrGroundTerm t, Optional<Variable> replacement) {
            return replacement
                    .map(variable -> termFactory.getStrictEquality(t, variable));
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
                IQTree leftJoinTree = iqFactory.createBinaryNonCommutativeIQTree(
                        rightChildExpression.isPresent() ?
                                iqFactory.createLeftJoinNode(
                                        Optional.of(
                                                updateJoinCondition(
                                                        rootNode.getOptionalFilterCondition(),
                                                        ImmutableList.of(rightChildExpression.get())
                                                ))) :
                                rootNode,
                        trimRootFilter(leftChild),
                        trimRootFilter(rightChild)
                );
                return leftChildChildExpression.isPresent() ?
                        iqFactory.createUnaryIQTree(iqFactory.createFilterNode(leftChildChildExpression.get()), leftJoinTree) :
                        leftJoinTree;
            }
            return tree;
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<ImmutableExpression> filterChildExpressions = getChildExpressions(children);
            if (filterChildExpressions.isEmpty())
                return tree;

            return iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(
                            Optional.of(
                                    updateJoinCondition(
                                            rootNode.getOptionalFilterCondition(),
                                            filterChildExpressions
                                    ))),
                    children.stream()
                            .map(this::trimRootFilter)
                            .collect(ImmutableCollectors.toList())
            );
        }

        @Override
        public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
            ImmutableList<ImmutableExpression> filterChildExpressions = getChildExpressions(ImmutableList.of(child));
            if (filterChildExpressions.isEmpty())
                return tree;
            return iqFactory.createUnaryIQTree(
                    iqFactory.createFilterNode(
                            updateJoinCondition(
                                    Optional.of(rootNode.getFilterCondition()),
                                    filterChildExpressions
                            )),
                    trimRootFilter(child)
            );
        }

        private ImmutableList<ImmutableExpression> getChildExpressions(ImmutableList<IQTree> children) {
            return children.stream()
                    .filter(t -> t.getRootNode() instanceof FilterNode)
                    .map(t -> ((FilterNode) t.getRootNode()).getFilterCondition())
                    .collect(ImmutableCollectors.toList());
        }

        private Optional<ImmutableExpression> getOptionalChildExpression(IQTree child) {
            QueryNode root = child.getRootNode();
            return root instanceof FilterNode?
                    Optional.of(((FilterNode) root).getFilterCondition()):
                    Optional.empty();

        }

        private IQTree trimRootFilter(IQTree tree) {
            return tree.getRootNode() instanceof FilterNode ?
                    ((UnaryIQTree) tree).getChild() :
                    tree;
        }

        private ImmutableExpression updateJoinCondition(Optional<ImmutableExpression> joinCondition, ImmutableList<ImmutableExpression> additionalConditions) {
            if (additionalConditions.isEmpty())
                throw new ExplicitEqualityTransformerInternalException("Nonempty list of filters expected");
            return termFactory.getConjunction(joinCondition, additionalConditions.stream()).get();
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
                                    trimIdleCn(child)
                    ));
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
                                            .collect(ImmutableCollectors.toList())
                            ));
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
                                    trimIdleCn(rightChild)
                            ));
        }

        private ImmutableList<ConstructionNode> getIdleCns(Stream<IQTree> trees) {
            return trees
                    .map(this::getIdleCn)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(ImmutableCollectors.toList());
        }

        private Optional<ConstructionNode> getIdleCn(IQTree tree) {
            QueryNode root = tree.getRootNode();
            if (root instanceof ConstructionNode) {
                ConstructionNode cn = ((ConstructionNode) root);
                if (cn.getSubstitution().isEmpty()) {
                    return Optional.of(cn);
                }
            }
            return Optional.empty();
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
                        ((UnaryIQTree)tree).getChild()
                );
            }
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(signature),
                    tree
            );
        }
    }

    private static class ExplicitEqualityTransformerInternalException extends OntopInternalBugException {
        ExplicitEqualityTransformerInternalException(String message) {
            super(message);
        }
    }
}
