package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.ExplicitEqualityTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.AND;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;

public class ExplicitEqualityTransformerImpl implements ExplicitEqualityTransformer {

    private final ImmutableList<IQTreeTransformer> preTransformers;
    private final ImmutableList<IQTreeTransformer> postTransformers;
    private final IntermediateQueryFactory iqFactory;
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final VariableGenerator variableGenerator;
    private final SubstitutionFactory substitutionFactory;

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
        this.preTransformers = ImmutableList.of(new LocalExplicitEqualityEnforcer());
        this.postTransformers = ImmutableList.of(new CnLifter(), new FilterChildNormalizer());
    }

    @Override
    public IQTree transform(IQTree tree) {
        return new CompositeRecursiveIQTreeTransformer(preTransformers, postTransformers, iqFactory).transform(tree);
    }

    /**
     * Affects (left) joins and data nodes.
     * - left join: if the same variable is returned by both operands (implicit equality),
     * rename it (with a fresh variable each time) in each branch but the left one,
     * and make the corresponding equalities explicit.
     * - inner join: identical to left join, but renaming is performed in each branch but the first where the variable appears
     * - data node: if the data node contains a ground term, create a variable and make the equality explicit (create a filter).
     */
    class LocalExplicitEqualityEnforcer extends DefaultNonRecursiveIQTreeTransformer {

        @Override
        public IQTree transformIntensionalData(IntensionalDataNode dn) {
            return transformDataNode(dn);
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode dn) {
            return transformDataNode(dn);
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            // One substitution per child but the leftmost
            ImmutableList<InjectiveVar2VarSubstitution> substitutions = computeSubstitutions(children);
            if (substitutions.isEmpty())
                return tree;

            ImmutableList<IQTree> updatedChildren = updateJoinChildren(substitutions, children);
            return iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(
                            Optional.of(
                                    updateJoinCondition(
                                            rootNode.getOptionalFilterCondition(),
                                            substitutions
                                    ))),
                    updatedChildren
            );
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            ImmutableList<IQTree> children = ImmutableList.of(leftChild, rightChild);
            ImmutableList<InjectiveVar2VarSubstitution> substitutions = computeSubstitutions(children);
            if (substitutions.isEmpty())
                return tree;

            ImmutableList<IQTree> updatedChildren = updateJoinChildren(substitutions, children);
            return iqFactory.createBinaryNonCommutativeIQTree(
                    iqFactory.createLeftJoinNode(
                            Optional.of(
                                    updateJoinCondition(
                                            rootNode.getOptionalFilterCondition(),
                                            substitutions
                                    ))),
                    updatedChildren.get(0),
                    updatedChildren.get(1)
            );
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
                    .map(t -> computeSubstitution(
                                    repeatedVariables,
                                    children,
                                    t
                    ))
                    .collect(ImmutableCollectors.toList());
        }

        private ImmutableList<IQTree> updateJoinChildren(ImmutableList<InjectiveVar2VarSubstitution> substitutions, ImmutableList<IQTree> children) {
            Iterator<IQTree> it = children.iterator();
            return substitutions.stream().sequential()
                    .map(s -> it.next().applyDescendingSubstitutionWithoutOptimizing(s))
                    .collect(ImmutableCollectors.toList());
        }

        private InjectiveVar2VarSubstitution computeSubstitution(ImmutableSet<Variable> repeatedVars, ImmutableList<IQTree> children, IQTree tree) {
            return substitutionFactory.getInjectiveVar2VarSubstitution(
                    tree.getVariables().stream()
                            .filter(repeatedVars::contains)
                            .filter(v -> !isFirstOcc(v, children, tree))
                            .collect(ImmutableCollectors.toMap(
                                    v -> v,
                                    v -> variableGenerator.generateNewVariableFromVar(v)
                            )));
        }

        private boolean isFirstOcc(Variable variable, ImmutableList<IQTree> children, IQTree tree) {
            return children.stream().sequential()
                    .filter(t -> t.getVariables().contains(variable))
                    .findFirst().get()
                    .equals(tree);
        }


        private ImmutableExpression updateJoinCondition(Optional<ImmutableExpression> optionalFilterCondition, ImmutableList<InjectiveVar2VarSubstitution> substitutions) {
            Stream<ImmutableExpression> varEqualities = extractEqualities(substitutions);
            return getConjunction(optionalFilterCondition, varEqualities);
        }

        private Stream<ImmutableExpression> extractEqualities(ImmutableList<InjectiveVar2VarSubstitution> substitutions) {
            return substitutions.stream()
                    .flatMap(s -> s.getImmutableMap().entrySet().stream())
                    .map(e -> termFactory.getImmutableExpression(
                            EQ,
                            e.getKey(),
                            e.getValue()
                    ));
        }

        private IQTree transformDataNode(DataNode dn) {
            ImmutableMap<GroundTerm, Variable> groundTermSubstitution = getGroundTermSubstitution(dn);
            if (groundTermSubstitution.isEmpty())
                return dn;

            FilterNode filter = createFilter(groundTermSubstitution);
            return iqFactory.createUnaryIQTree(
                    filter,
                    dn.newAtom(updateDataAtom(
                            dn.getProjectionAtom(),
                            groundTermSubstitution
                    )));
        }

        private <P extends AtomPredicate> DataAtom<P> updateDataAtom(DataAtom<P> projectionAtom, ImmutableMap<GroundTerm, Variable> groundTermSubstitution) {
            return atomFactory.getDataAtom(
                    projectionAtom.getPredicate(),
                    projectionAtom.getArguments().stream()
                            .map(t -> replaceGT(
                                    t,
                                    groundTermSubstitution
                            ))
                            .collect(ImmutableCollectors.toList())
            );
        }

        private Variable replaceGT(VariableOrGroundTerm term, ImmutableMap<GroundTerm, Variable> groundTermSubstitution) {
            return term instanceof Variable ?
                    (Variable) term :
                    groundTermSubstitution.get(term);
        }

        private ImmutableMap<GroundTerm, Variable> getGroundTermSubstitution(DataNode dn) {
            return ((ImmutableList<VariableOrGroundTerm>) dn.getProjectionAtom().getArguments()).stream()
                    .filter(t -> t instanceof GroundTerm)
                    .map(t -> (GroundTerm) t)
                    .collect(ImmutableCollectors.toMap(
                            t -> t,
                            t -> variableGenerator.generateNewVariable()
                    ));
        }

        private FilterNode createFilter(ImmutableMap<GroundTerm, Variable> groundTermSubstitution) {
            return iqFactory.createFilterNode(
                    getConjunction(groundTermSubstitution.entrySet().stream().
                            map(s -> termFactory.getImmutableExpression(
                                    EQ,
                                    s.getKey(),
                                    s.getValue()
                            ))));
        }
    }

    /**
     * Affects filters and (left) joins.
     * For each child, deletes its root if it is a filter node.
     * Then:
     * - join and filter: merge the boolean expressions
     * - left join: merge boolean expressions coming from the right, and lift the ones coming from the left.
     * This lift is only performed for optimization purposes: may save a subquery during SQL generation.
     */
    class FilterChildNormalizer extends DefaultNonRecursiveIQTreeTransformer {

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            ImmutableList<IQTree> children = ImmutableList.of(leftChild, rightChild);
            ImmutableList<ImmutableExpression> filterChildExpressions = getFilterChildExpressions(children);
            if (filterChildExpressions.isEmpty())
                return tree;

            return iqFactory.createBinaryNonCommutativeIQTree(
                    iqFactory.createLeftJoinNode(
                            Optional.of(
                                    updateJoinCondition(
                                            rootNode.getOptionalFilterCondition(),
                                            filterChildExpressions
                                    ))),
                    trimRootFilter(leftChild),
                    trimRootFilter(rightChild)
            );
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<ImmutableExpression> filterChildExpressions = getFilterChildExpressions(children);
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
            ImmutableList<ImmutableExpression> filterChildExpressions = getFilterChildExpressions(ImmutableList.of(child));
            if (filterChildExpressions.isEmpty())
                return tree;
            return iqFactory.createUnaryIQTree(
                    iqFactory.createFilterNode(
                            updateJoinCondition(
                                    Optional.of(rootNode.getFilterCondition()),
                                    filterChildExpressions
                            )),
                    child
            );
        }

        private ImmutableList<ImmutableExpression> getFilterChildExpressions(ImmutableList<IQTree> children) {
            return children.stream()
                    .filter(t -> t.getRootNode() instanceof FilterNode)
                    .map(t -> ((FilterNode) t.getRootNode()).getFilterCondition())
                    .collect(ImmutableCollectors.toList());
        }

        private IQTree trimRootFilter(IQTree tree) {
            return tree.getRootNode() instanceof FilterNode ?
                    ((UnaryIQTree) tree).getChild() :
                    tree;
        }

        private ImmutableExpression updateJoinCondition(Optional<ImmutableExpression> joinCondition, ImmutableList<ImmutableExpression> additionalConditions) {
            if (additionalConditions.isEmpty())
                throw new ExplicitEqualityTransformerInternalException("Nonempty list of filters expected");
            return getConjunction(joinCondition, additionalConditions.stream());
        }
    }

    /**
     * - Default behavior:
     * a) for each child, deletes its root if it is a substitution-free construction node (i.e. a simple projection).
     * b) Then lift the projection if needed (i.e. create a substitution-free construction node above the current one)
     * - Construction node: perform only a)
     * - Distinct nodes: perform neither a) nor b)
     */
    class CnLifter extends DefaultNonRecursiveIQTreeTransformer {

        @Override
        public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
            return tree;
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
            ImmutableList<ConstructionNode> idleCns = getIdleCns(Stream.of(child));
            return idleCns.isEmpty() ?
                    tree :
                    iqFactory.createUnaryIQTree(
                            rootNode,
                            trimIdleCn(child)
                    );
        }

        @Override
        protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            ImmutableList<ConstructionNode> idleCns = getIdleCns(Stream.of(child));
            return idleCns.isEmpty() ?
                    tree :
                    iqFactory.createUnaryIQTree(
                            idleCns.iterator().next(),
                            iqFactory.createUnaryIQTree(
                                    rootNode,
                                    trimIdleCn(child)
                            ));
        }

        @Override
        protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<ConstructionNode> idleCns = getIdleCns(children.stream());
            return idleCns.isEmpty() ?
                    tree :
                    iqFactory.createUnaryIQTree(
                            mergeProjections(idleCns),
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
                    iqFactory.createUnaryIQTree(
                            mergeProjections(idleCns),
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

        private ConstructionNode mergeProjections(ImmutableList<ConstructionNode> idleCns) {
            return iqFactory.createConstructionNode(idleCns.stream()
                    .flatMap(c -> c.getVariables().stream())
                    .collect(ImmutableCollectors.toSet())
            );
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
    }

    private ImmutableExpression getConjunction(Stream<ImmutableExpression> expressions) {
        return expressions
                .reduce(null,
                        (a, b) -> (a == null) ?
                                b :
                                termFactory.getImmutableExpression(
                                        AND,
                                        a,
                                        b
                                ));
    }

    private ImmutableExpression getConjunction(Optional<ImmutableExpression> optExpression, Stream<ImmutableExpression> expressions) {
                return getConjunction(optExpression.isPresent()?
                        Stream.concat(
                                Stream.of(optExpression.get()),
                                expressions
                        ):
                        expressions
                );
    }

    private class ExplicitEqualityTransformerInternalException extends OntopInternalBugException {
        ExplicitEqualityTransformerInternalException(String message) {
            super(message);
        }
    }
}