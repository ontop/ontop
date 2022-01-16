package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.PostProcessableFunctionLifter;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Singleton
public class PostProcessableFunctionLifterImpl implements PostProcessableFunctionLifter {

    protected final OptimizationSingletons optimizationSingletons;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected PostProcessableFunctionLifterImpl(OptimizationSingletons optimizationSingletons,
                                                IntermediateQueryFactory iqFactory) {
        this.optimizationSingletons = optimizationSingletons;
        this.iqFactory = iqFactory;
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
        return new FunctionLifterTransformer(variableGenerator, optimizationSingletons);
    }


    public static class FunctionLifterTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        protected static final int LOOPING_BOUND = 1000000;
        protected final VariableGenerator variableGenerator;
        protected final OptimizationSingletons optimizationSingletons;
        private final int maxNbChildrenForLiftingDBFunctionSymbol;

        protected FunctionLifterTransformer(VariableGenerator variableGenerator, OptimizationSingletons optimizationSingletons) {
            super(optimizationSingletons.getCoreSingletons().getIQFactory());
            this.variableGenerator = variableGenerator;
            this.optimizationSingletons = optimizationSingletons;
            this.maxNbChildrenForLiftingDBFunctionSymbol = optimizationSingletons.getSettings()
                    .getMaxNbChildrenForLiftingDBFunctionSymbol();
        }

        @Override
        protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            return super.transformUnaryNode(tree, rootNode, child)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            return super.transformNaryCommutativeNode(tree, rootNode, children)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                           IQTree leftChild, IQTree rightChild) {
            return super.transformBinaryNonCommutativeNode(tree, rootNode, leftChild, rightChild)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            IQTree normalizedTree = transformNaryCommutativeNode(tree, rootNode, children);

            // Fix-point before pursing (recursive, potentially dangerous!)
            if (!normalizedTree.equals(tree)) {
                return normalizedTree.acceptTransformer(this);
            }

            return lift(new LiftState(children, rootNode.getVariables(), variableGenerator,
                        optimizationSingletons.getCoreSingletons()))
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
                    .anyMatch(t -> shouldBeLifted(t, children.size()));
        }

        /**
         * Recursive
         */
        protected boolean shouldBeLifted(ImmutableFunctionalTerm functionalTerm, int nbChildren) {
            FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();
            if (!(functionSymbol instanceof DBFunctionSymbol)
                || ((nbChildren < maxNbChildrenForLiftingDBFunctionSymbol)
                    && ((DBFunctionSymbol) functionSymbol).isPreferringToBePostProcessedOverBeingBlocked()))
                return true;

            return functionalTerm.getTerms().stream()
                    .filter(t -> t instanceof ImmutableFunctionalTerm)
                    .map(t -> (ImmutableFunctionalTerm) t)
                    .anyMatch(t -> shouldBeLifted(t, nbChildren));
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
        private final SingleTermTypeExtractor typeExtractor;
        private final CoreSingletons coreSingletons;

        /**
         * Initial constructor
         */
        public LiftState(ImmutableList<IQTree> children, ImmutableSet<Variable> unionVariables,
                         VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
            this(children, unionVariables, ImmutableList.of(), null, variableGenerator, coreSingletons);
        }

        protected LiftState(ImmutableList<IQTree> children, ImmutableSet<Variable> unionVariables,
                            ImmutableList<ConstructionNode> ancestors, @Nullable Variable childIdVariable,
                            VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
            this.children = children;
            this.unionVariables = unionVariables;
            this.ancestors = ancestors;
            this.childIdVariable = childIdVariable;
            this.variableGenerator = variableGenerator;
            this.iqFactory = coreSingletons.getIQFactory();
            this.substitutionFactory = coreSingletons.getSubstitutionFactory();
            this.termFactory = coreSingletons.getTermFactory();
            this.typeExtractor = coreSingletons.getUniqueTermTypeExtractor();
            this.coreSingletons = coreSingletons;
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
                    .mapToObj(i -> liftDefinition(children.get(i), i, variable, unionVariables, idVariable))
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

            ImmutableMap<Variable, Optional<DBTermType>> newVarTypeMap = newUnionVariables.stream()
                    .collect(ImmutableCollectors.toMap(
                            v -> v,
                            v -> extractType(v, childDefinitionLifts)));

            ImmutableList<IQTree> newChildren = childDefinitionLifts.stream()
                    .map(l -> padChild(l.getPartiallyPaddedChild(), newVarTypeMap))
                    .map(t -> t.normalizeForOptimization(variableGenerator))
                    .collect(ImmutableCollectors.toList());

            ConstructionNode newConstructionNode = iqFactory.createConstructionNode(unionVariables,
                    substitutionFactory.getSubstitution(variable, newDefinition));

            ImmutableList<ConstructionNode> newAncestors = Stream.concat(
                    ancestors.stream(),
                    Stream.of(newConstructionNode))
                    .collect(ImmutableCollectors.toList());

            return new LiftState(newChildren, newUnionVariables, newAncestors, idVariable, variableGenerator,
                    coreSingletons);
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
                            .distinct(),
                    variableGenerator::generateNewVariableFromVar);

            boolean isVariableNotDefinedInSubstitution = originalDefinition.equals(variable);

            ImmutableSet<Variable> projectedVariablesBeforeRenaming = Stream.concat(
                    Stream.concat(
                            unionVariables.stream(),
                            Stream.of(idVariable)),
                    originalDefinition.getVariableStream())
                    .filter(v -> isVariableNotDefinedInSubstitution || !v.equals(variable))
                    .collect(ImmutableCollectors.toSet());

            ImmutableSubstitution<ImmutableTerm> positionSubstitution =
                    substitutionFactory.getSubstitution(idVariable, termFactory.getDBIntegerConstant(position));

            ImmutableSubstitution<ImmutableTerm> substitutionBeforeRenaming = originalSubstitution
                    .flatMap(s -> s.union(positionSubstitution))
                    .map(s -> s.filter(projectedVariablesBeforeRenaming::contains))
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
            ImmutableList<ImmutableTerm> values = childDefinitionLifts.stream()
                    .map(ChildDefinitionLift::getLiftedDefinition)
                    .collect(ImmutableCollectors.toList());

            return termFactory.getDBIntIndex(idVariable, values);
        }

        protected Optional<DBTermType> extractType(Variable variable, ImmutableList<ChildDefinitionLift> childDefinitionLifts) {
            return childDefinitionLifts.stream()
                    .map(ChildDefinitionLift::getPartiallyPaddedChild)
                    .filter(c -> c.getVariables().contains(variable))
                    .findAny()
                    .flatMap(t -> typeExtractor.extractSingleTermType(variable, t))
                    .filter(t -> t instanceof DBTermType)
                    .map(t -> (DBTermType) t);
        }

        protected IQTree padChild(IQTree partiallyPaddedChild, ImmutableMap<Variable, Optional<DBTermType>> newVarTypeMap) {
            ImmutableSet<Variable> childVariables = partiallyPaddedChild.getVariables();

            ImmutableSubstitution<ImmutableTerm> paddingSubstitution = substitutionFactory.getSubstitution(
                    newVarTypeMap.entrySet().stream()
                            .filter(v -> !childVariables.contains(v.getKey()))
                            .collect(ImmutableCollectors.toMap(
                                    Map.Entry::getKey,
                                    e -> e.getValue()
                                            .map(t -> termFactory.getTypedNull(t).simplify())
                                            .orElseGet(termFactory::getNullConstant))));

            return paddingSubstitution.isEmpty()
                    ? partiallyPaddedChild
                    : iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(newVarTypeMap.keySet(), paddingSubstitution),
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
