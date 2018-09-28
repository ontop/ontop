package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
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

    public ExplicitEqualityTransformerImpl(IntermediateQueryFactory iqFactory,
                                           AtomFactory atomFactory,
                                           TermFactory termFactory,
                                           VariableGenerator variableGenerator,
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
     * - (left) join: if the same variable is returned by both operands (implicit equality),
     * rename it in each branch but the leftmost (with a different variable each time),
     * and make the corresponding equalities explicit.
     * - data node: create a variable and make the equality explicit (create a filter).
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
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            // One substitution per child
            ImmutableList<InjectiveVar2VarSubstitution> substitutions = computeSubstitutions(ImmutableList.of(leftChild, rightChild));
            if (substitutions.isEmpty())
                return tree;
            Iterator<InjectiveVar2VarSubstitution> it = substitutions.iterator();
            ImmutableList<IQTree> updatedChildren = (Stream.of(leftChild, rightChild)).sequential()
                    .map(t -> t.applyDescendingSubstitutionWithoutOptimizing(it.next()))
                    .collect(ImmutableCollectors.toList());

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
                throw new ExplicitEqualityTransformerException("at least 2 children are expected");
            }
            ImmutableSet<Variable> repeatedVariables = children.stream()
                    .flatMap(t -> t.getVariables().stream())
                    .collect(ImmutableCollectors.toMultiset()).entrySet().stream()
                    .filter(e -> e.getCount() > 1)
                    .map(e -> e.getElement())
                    .collect(ImmutableCollectors.toSet());

            return Stream.concat(
                    Stream.of(substitutionFactory.getInjectiveVar2VarSubstitution(ImmutableMap.of())),
                    children.stream()
                            .map(t -> computeSubstitution(repeatedVariables, t))
            ).collect(ImmutableCollectors.toList());
        }


        private InjectiveVar2VarSubstitution computeSubstitution(ImmutableSet<Variable> repeatedVars, IQTree tree) {
            return substitutionFactory.getInjectiveVar2VarSubstitution(
                    tree.getVariables().stream()
                            .filter(v -> repeatedVars.contains(v))
                            .collect(ImmutableCollectors.toMap(
                                    v -> v,
                                    v -> variableGenerator.generateNewVariable()
                    )));
        }

        private ImmutableExpression updateJoinCondition(Optional<ImmutableExpression> optionalFilterCondition, ImmutableList<InjectiveVar2VarSubstitution> substitutions) {
            ImmutableExpression varEqualities = extractEqualities(substitutions);
            return optionalFilterCondition.isPresent() ?
                    termFactory.getImmutableExpression(
                            AND,
                            optionalFilterCondition.get(),
                            varEqualities) :
                    varEqualities;
        }

        private ImmutableExpression extractEqualities(ImmutableList<InjectiveVar2VarSubstitution> substitutions) {

            return termFactory.getImmutableExpression(
                    AND,
                    substitutions.stream()
                            .flatMap(s -> s.getImmutableMap().entrySet().stream())
                            .collect(ImmutableCollectors.toMultimap(
                                    s -> s.getKey(),
                                    s -> s.getValue()
                            )).asMap().entrySet().stream()
                            .map(e -> getEquivalenceExp(e))
                            .collect(ImmutableCollectors.toList())
            );
        }

        private ImmutableExpression getEquivalenceExp(Map.Entry<Variable, Collection<Variable>> e) {
            if (e.getValue().isEmpty()) {
                throw new ExplicitEqualityTransformerException("This collection is expected to be nonEmpty");
            }
            Variable refVar = e.getKey();
            return termFactory.getImmutableExpression(
                    AND,
                    e.getValue().stream()
                            .map(v -> termFactory.getImmutableExpression(
                                    EQ,
                                    refVar,
                                    v
                            )).collect(ImmutableCollectors.toList()));
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
                    termFactory.getImmutableExpression(
                            AND,
                            groundTermSubstitution.entrySet().stream().
                                    map(s -> termFactory.getImmutableExpression(
                                            EQ,
                                            s.getKey(),
                                            s.getValue()
                                    ))
                                    .collect(ImmutableCollectors.toList())
                    ));
        }

    }

    /**
     * Affects filters and (left) joins.
     * For each child, deletes its root if it is a filter node.
     * Then:
     * - join and filter: merge the boolean expressions
     * - left join: merge boolean expressions coming from the right, and lift the ones coming from the left.
     * This lift is only performed for optimization purposes: may avoid a subquery during SQL generation.
     */
    class FilterChildNormalizer extends DefaultNonRecursiveIQTreeTransformer {

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        }

        @Override
        public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
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
                                            .map(t -> trimIdleCn(t))
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
                    .map(t -> getIdleCn(t))
                    .filter(o -> o.isPresent())
                    .map(o -> o.get())
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

    private class ExplicitEqualityTransformerException extends OntopInternalBugException {
        public ExplicitEqualityTransformerException (String message) {
            super(message);
        }
    }
}