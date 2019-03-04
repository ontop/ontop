package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.PostProcessableFunctionLifter;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PostProcessableFunctionLifterImpl implements PostProcessableFunctionLifter {

    protected final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @Inject
    protected PostProcessableFunctionLifterImpl(IntermediateQueryFactory iqFactory,
                                                SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree newTree = query.getTree().acceptTransformer(createTransformer(query.getVariableGenerator()));
        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    /**
     * TODO: refactor IQTreeVisitingTransformer so as avoid to create fresh transformers
     */
    protected IQTreeVisitingTransformer createTransformer(VariableGenerator variableGenerator) {
        return new FunctionLifterTransformer(iqFactory, variableGenerator, substitutionFactory, termFactory);
    }


    public static class FunctionLifterTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        protected static final int LOOPING_BOUND = 1000000;
        private final VariableGenerator variableGenerator;
        private final SubstitutionFactory substitutionFactory;
        private final TermFactory termFactory;

        protected FunctionLifterTransformer(IntermediateQueryFactory iqFactory, VariableGenerator variableGenerator,
                                            SubstitutionFactory substitutionFactory, TermFactory termFactory) {
            super(iqFactory);
            this.variableGenerator = variableGenerator;
            this.substitutionFactory = substitutionFactory;
            this.termFactory = termFactory;
        }

        @Override
        protected IQTree transformUnaryNode(UnaryOperatorNode rootNode, IQTree child) {
            return super.transformUnaryNode(rootNode, child)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        protected IQTree transformNaryCommutativeNode(NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            return super.transformNaryCommutativeNode(rootNode, children)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        protected IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeOperatorNode rootNode,
                                                           IQTree leftChild, IQTree rightChild) {
            return super.transformBinaryNonCommutativeNode(rootNode, leftChild, rightChild)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            IQTree normalizedTree = transformNaryCommutativeNode(rootNode, children);

            // Fix-point before pursing (recursive, potentially dangerous!)
            if (!normalizedTree.isEquivalentTo(tree)) {
                return normalizedTree.acceptTransformer(this);
            }

            return lift(new LiftState(children, rootNode.getVariables(), variableGenerator,
                        iqFactory, substitutionFactory, termFactory))
                    .generateTree(iqFactory)
                    .normalizeForOptimization(variableGenerator);
        }

        protected LiftState lift(LiftState initialState) {
            //Non-final
            LiftState state = initialState;

            for(int i =0; i < LOOPING_BOUND; i++) {
                LiftState newState = step(state);
                if (newState.equals(state))
                    return state;
                state = newState;
            }
            throw new MinorOntopInternalBugException(String.format("Has not converged in %d iterations", LOOPING_BOUND));
        }

        protected LiftState step(LiftState state) {
            return selectVariableToLift(state.getUnionVariables(), state.getChildren())
                    .map(state::liftVariable)
                    .orElse(state);
        }

        protected Optional<Variable> selectVariableToLift(ImmutableSet<Variable> unionVariables,
                                                          ImmutableList<IQTree> children) {
            return unionVariables.stream()
                    .filter(v -> shouldBeLifted(v, children))
                    .findAny();
        }

        protected boolean shouldBeLifted(Variable variable, ImmutableList<IQTree> children) {
            return children.stream()
                    .map(IQTree::getRootNode)
                    .filter(n -> n instanceof ConstructionNode)
                    .map(n -> (ConstructionNode)n)
                    .map(n -> n.getSubstitution().get(variable))
                    .filter(d -> d instanceof ImmutableFunctionalTerm)
                    .map(d -> (ImmutableFunctionalTerm) d)
                    .anyMatch(this::shouldBeLifted);
        }

        /**
         * Recursive
         */
        protected boolean shouldBeLifted(ImmutableFunctionalTerm functionalTerm) {
            FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();
            if (!(functionSymbol instanceof DBFunctionSymbol)
                || ((DBFunctionSymbol) functionSymbol).isPreferringToBePostProcessedOverBeingBlocked())
                return true;

            return functionalTerm.getTerms().stream()
                    .filter(t -> t instanceof ImmutableFunctionalTerm)
                    .map(t -> (ImmutableFunctionalTerm) t)
                    .anyMatch(this::shouldBeLifted);
        }

    }

    public static class LiftState {
        private final ImmutableList<IQTree> children;
        private final ImmutableSet<Variable> unionVariables;
        // Ancestor first
        private final ImmutableList<ConstructionNode> ancestors;

        @Nullable
        private final Variable childIdVariable;

        private final VariableGenerator variableGenerator;
        private final IntermediateQueryFactory iqFactory;
        private final SubstitutionFactory substitutionFactory;
        private final TermFactory termFactory;

        /**
         * Initial constructor
         */
        public LiftState(ImmutableList<IQTree> children, ImmutableSet<Variable> unionVariables,
                         VariableGenerator variableGenerator, IntermediateQueryFactory iqFactory,
                         SubstitutionFactory substitutionFactory, TermFactory termFactory) {
            this.children = children;
            this.unionVariables = unionVariables;
            this.variableGenerator = variableGenerator;
            this.iqFactory = iqFactory;
            this.substitutionFactory = substitutionFactory;
            this.termFactory = termFactory;
            this.ancestors = ImmutableList.of();
            this.childIdVariable = null;
        }

        protected LiftState(ImmutableList<IQTree> children, ImmutableSet<Variable> unionVariables,
                            ImmutableList<ConstructionNode> ancestors, Variable childIdVariable,
                            VariableGenerator variableGenerator, IntermediateQueryFactory iqFactory,
                            SubstitutionFactory substitutionFactory, TermFactory termFactory) {
            this.children = children;
            this.unionVariables = unionVariables;
            this.ancestors = ancestors;
            this.childIdVariable = childIdVariable;
            this.variableGenerator = variableGenerator;
            this.iqFactory = iqFactory;
            this.substitutionFactory = substitutionFactory;
            this.termFactory = termFactory;
        }

        public IQTree generateTree(IntermediateQueryFactory iqFactory) {
            IQTree unionTree = iqFactory.createNaryIQTree(
                    iqFactory.createUnionNode(unionVariables),
                    children);
            return ancestors.reverse().stream()
                    .reduce(unionTree,
                            (t, n) -> iqFactory.createUnaryIQTree(n, t),
                            (t1, t2) -> { throw new MinorOntopInternalBugException("this merging operation should never appear"); });

        }

        public ImmutableSet<Variable> getUnionVariables() {
            return unionVariables;
        }

        public ImmutableList<IQTree> getChildren() {
            return children;
        }

        public LiftState liftVariable(Variable variable) {
            Variable idVariable = (childIdVariable == null)
                    ? variableGenerator.generateNewVariable()
                    : childIdVariable;

            ImmutableList<ChildDefinitionLift> childDefinitionLifts = IntStream.range(0, children.size())
                    .boxed()
                    .map(i -> liftDefinition(children.get(i), i, variable, unionVariables, idVariable))
                    .collect(ImmutableCollectors.toList());

            ImmutableFunctionalTerm newDefinition = mergeDefinitions(idVariable, childDefinitionLifts);

            ImmutableSet<Variable> newUnionVariables = Stream.concat(
                    Stream.concat(
                            unionVariables.stream(),
                            Stream.of(idVariable)),
                    childDefinitionLifts.stream()
                            .flatMap(l -> l.getFreshlyCreatedVariables().stream()))
                    .filter(v -> !v.equals(variable))
                    .collect(ImmutableCollectors.toSet());

            ImmutableList<IQTree> newChildren = childDefinitionLifts.stream()
                    .map(l -> padChild(l.getPartiallyPaddedChild(), newUnionVariables))
                    .map(t -> t.normalizeForOptimization(variableGenerator))
                    .collect(ImmutableCollectors.toList());

            ConstructionNode newConstructionNode = iqFactory.createConstructionNode(unionVariables,
                    substitutionFactory.getSubstitution(variable, newDefinition));

            ImmutableList<ConstructionNode> newAncestors = Stream.concat(
                    ancestors.stream(),
                    Stream.of(newConstructionNode))
                    .collect(ImmutableCollectors.toList());

            return new LiftState(newChildren, newUnionVariables, newAncestors, idVariable, variableGenerator, iqFactory,
                    substitutionFactory, termFactory);
        }

        protected ChildDefinitionLift liftDefinition(IQTree childTree, int position, Variable variable,
                                                     ImmutableSet<Variable> unionVariables, Variable idVariable) {
            Optional<ImmutableSubstitution<ImmutableTerm>> originalSubstitution = Optional.of(childTree.getRootNode())
                    .filter(n -> n instanceof ConstructionNode)
                    .map(n -> (ConstructionNode) n)
                    .map(ConstructionNode::getSubstitution);

            ImmutableTerm originalDefinition = originalSubstitution
                    .filter(s -> s.isDefining(variable))
                    .map(s -> s.get(variable))
                    .orElse(variable);

            InjectiveVar2VarSubstitution renamingSubstitution = substitutionFactory.getInjectiveVar2VarSubstitution(
                    originalDefinition.getVariableStream()
                            .filter(v -> v.equals(variable) || (!unionVariables.contains(v)))
                            .distinct()
                            .collect(ImmutableCollectors.toMap(
                                    v -> v,
                                    variableGenerator::generateNewVariableFromVar)));

            ImmutableSet<Variable> projectedVariablesBeforeRenaming = Stream.concat(
                    Stream.concat(
                            unionVariables.stream(),
                            Stream.of(idVariable)),
                    originalDefinition.getVariableStream())
                    .filter(v -> !v.equals(variable))
                    .collect(ImmutableCollectors.toSet());

            ImmutableSubstitution<ImmutableTerm> positionSubstitution =
                    substitutionFactory.getSubstitution(idVariable, termFactory.getDBIntegerConstant(position));

            ImmutableSubstitution<ImmutableTerm> substitutionBeforeRenaming = originalSubstitution
                    .flatMap(s -> s.unionHeterogeneous(positionSubstitution))
                    .map(s -> (ImmutableSubstitution<ImmutableTerm>)
                            s.reduceDomainToIntersectionWith(projectedVariablesBeforeRenaming))
                    .orElse(positionSubstitution);


            IQTree childOfConstruction = Optional.of(childTree)
                    .filter(t -> t.getRootNode() instanceof ConstructionNode)
                    .map(t -> ((UnaryIQTree) t).getChild())
                    .orElse(childTree);

            UnaryIQTree childBeforeRenaming = iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(projectedVariablesBeforeRenaming, substitutionBeforeRenaming),
                    childOfConstruction);

            IQTree partiallyPaddedChild = childBeforeRenaming.applyDescendingSubstitution(renamingSubstitution, Optional.empty());
            ImmutableTerm liftedDefinition = renamingSubstitution.apply(originalDefinition);
            ImmutableSet<Variable> freshVariables = ImmutableSet.copyOf(renamingSubstitution.getImmutableMap().values());

            return new ChildDefinitionLift(partiallyPaddedChild, freshVariables, liftedDefinition);
        }

        protected ImmutableFunctionalTerm mergeDefinitions(Variable idVariable,
                                                           ImmutableList<ChildDefinitionLift> childDefinitionLifts) {
            return termFactory.getDBCase(
                    IntStream.range(0, childDefinitionLifts.size() - 1)
                            .boxed()
                            .map(i -> Maps.immutableEntry(
                                    termFactory.getStrictEquality(idVariable, termFactory.getDBIntegerConstant(i)),
                                    childDefinitionLifts.get(i).getLiftedDefinition())),
                    // Last child -> "default" value
                    childDefinitionLifts.get(childDefinitionLifts.size() - 1).getLiftedDefinition());
        }

        protected IQTree padChild(IQTree partiallyPaddedChild, ImmutableSet<Variable> unionVariables) {
            ImmutableSet<Variable> childVariables = partiallyPaddedChild.getVariables();
            Constant nullConstant = termFactory.getNullConstant();

            ImmutableSubstitution<ImmutableTerm> paddingSubstitution = substitutionFactory.getSubstitution(
                    unionVariables.stream()
                            .filter(v -> !childVariables.contains(v))
                            .collect(ImmutableCollectors.toMap(
                                    v -> v,
                                    v -> (ImmutableTerm) nullConstant)));

            return paddingSubstitution.isEmpty()
                    ? partiallyPaddedChild
                    : iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(unionVariables, paddingSubstitution),
                            partiallyPaddedChild);
        }
    }

    public static class ChildDefinitionLift {
        private final IQTree partiallyPaddedChild;
        private final ImmutableSet<Variable> freshlyCreatedVariables;
        private final ImmutableTerm liftedDefinition;

        public ChildDefinitionLift(IQTree partiallyPaddedChild, ImmutableSet<Variable> freshlyCreatedVariables,
                                   ImmutableTerm liftedDefinition) {
            this.partiallyPaddedChild = partiallyPaddedChild;
            this.freshlyCreatedVariables = freshlyCreatedVariables;
            this.liftedDefinition = liftedDefinition;
        }

        public ImmutableSet<Variable> getFreshlyCreatedVariables() {
            return freshlyCreatedVariables;
        }

        public IQTree getPartiallyPaddedChild() {
            return partiallyPaddedChild;
        }

        public ImmutableTerm getLiftedDefinition() {
            return liftedDefinition;
        }
    }
}
