package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.PostProcessableFunctionLifter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.iq.visit.NormalizationState;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;


@Singleton
public class PostProcessableFunctionLifterImpl implements PostProcessableFunctionLifter {

    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final SingleTermTypeExtractor typeExtractor;

    private final int maxNbChildrenForLiftingDBFunctionSymbol;

    protected static final int LOOPING_BOUND = 1000000;

    @Inject
    protected PostProcessableFunctionLifterImpl(OptimizationSingletons optimizationSingletons) {
        CoreSingletons coreSingletons = optimizationSingletons.getCoreSingletons();
        this.iqFactory = coreSingletons.getIQFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.typeExtractor = coreSingletons.getUniqueTermTypeExtractor();

        this.maxNbChildrenForLiftingDBFunctionSymbol = optimizationSingletons.getSettings()
                .getMaxNbChildrenForLiftingDBFunctionSymbol();
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree newTree = query.getTree().acceptTransformer(
                new FunctionLifterTransformer(query.getVariableGenerator()));
        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }


    private class FunctionLifterTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        protected final VariableGenerator variableGenerator;

        protected FunctionLifterTransformer(VariableGenerator variableGenerator) {
            super(PostProcessableFunctionLifterImpl.this.iqFactory);
            this.variableGenerator = variableGenerator;
        }

        @Override
        protected IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            return super.transformUnaryNode(tree, rootNode, child)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            return super.transformInnerJoin(tree, rootNode, children)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode,
                                                           IQTree leftChild, IQTree rightChild) {
            return super.transformLeftJoin(tree, rootNode, leftChild, rightChild)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            IQTree normalizedTree = super.transformUnion(tree, rootNode, children)
                    .normalizeForOptimization(variableGenerator);

            // Fix-point before pursing (recursive, potentially dangerous!)
            if (!normalizedTree.equals(tree)) {
                return transform(normalizedTree);
            }

            return NormalizationState.reachFixedPoint(
                            new LiftState(children, rootNode.getVariables(), UnaryOperatorSequence.of(), Optional.empty()),
                            LOOPING_BOUND)
                    .asIQTree()
                    .normalizeForOptimization(variableGenerator);
        }


        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private class LiftState implements NormalizationState<LiftState> {
            private final UnaryOperatorSequence<ConstructionNode> ancestors;
            private final ImmutableSet<Variable> unionVariables;
            private final ImmutableList<IQTree> children;

            private final Optional<Variable> childIdVariable;

            LiftState(ImmutableList<IQTree> children, ImmutableSet<Variable> unionVariables,
                      UnaryOperatorSequence<ConstructionNode> ancestors, Optional<Variable> childIdVariable) {
                this.children = children;
                this.unionVariables = unionVariables;
                this.ancestors = ancestors;
                this.childIdVariable = childIdVariable;
            }

            public IQTree asIQTree() {
                IQTree unionTree = iqFactory.createNaryIQTree(
                        iqFactory.createUnionNode(unionVariables),
                        children);
                return iqTreeTools.createAncestorsUnaryIQTree(ancestors, unionTree);
            }

            @Override
            public Optional<LiftState> next() {
                return unionVariables.stream()
                        .filter(v -> shouldBeLifted(v, children))
                        // select any variable to lift
                        .findAny()
                        .map(this::liftVariable);
            }

            private boolean shouldBeLifted(Variable variable, ImmutableList<IQTree> children) {
                return children.stream()
                        .map(c -> UnaryIQTreeDecomposition.of(c, ConstructionNode.class))
                        .flatMap(d -> d.getOptionalNode().stream())
                        .map(n -> n.getSubstitution().get(variable))
                        .filter(d -> d instanceof ImmutableFunctionalTerm)
                        .map(d -> (ImmutableFunctionalTerm) d)
                        .anyMatch(t -> shouldBeLifted(t, children.size()));
            }

            /**
             * Recursive
             */
            private boolean shouldBeLifted(ImmutableFunctionalTerm functionalTerm, int nbChildren) {
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


            private LiftState liftVariable(Variable variable) {
                Variable idVariable = childIdVariable
                        .orElseGet(variableGenerator::generateNewVariable);

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

                return new LiftState(newChildren, newUnionVariables, ancestors.append(newConstructionNode), Optional.of(idVariable));
            }

            private ChildDefinitionLift liftDefinition(IQTree childTree, int position, Variable variable,
                                                         ImmutableSet<Variable> unionVariables, Variable idVariable) {

                var construction = UnaryIQTreeDecomposition.of(childTree, ConstructionNode.class);
                Optional<Substitution<ImmutableTerm>> originalSubstitution = construction.getOptionalNode()
                        .map(ConstructionNode::getSubstitution);

                ImmutableTerm originalDefinition = originalSubstitution
                        .map(s -> s.apply(variable))
                        .orElse(variable);

                InjectiveSubstitution<Variable> renamingSubstitution = originalDefinition.getVariableStream()
                        .filter(v -> v.equals(variable) || (!unionVariables.contains(v)))
                        .distinct()
                        .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

                boolean isVariableNotDefinedInSubstitution = originalDefinition.equals(variable);

                ImmutableSet<Variable> projectedVariablesBeforeRenaming = Stream.concat(
                                Stream.concat(
                                        unionVariables.stream(),
                                        Stream.of(idVariable)),
                                originalDefinition.getVariableStream())
                        .filter(v -> isVariableNotDefinedInSubstitution || !v.equals(variable))
                        .collect(ImmutableCollectors.toSet());

                Substitution<ImmutableTerm> positionSubstitution =
                        substitutionFactory.getSubstitution(idVariable, termFactory.getDBIntegerConstant(position));

                Substitution<ImmutableTerm> substitutionBeforeRenaming = originalSubstitution
                        .map(s -> substitutionFactory.union(s, positionSubstitution))
                        .map(s -> s.restrictDomainTo(projectedVariablesBeforeRenaming))
                        .orElse(positionSubstitution);

                UnaryIQTree childBeforeRenaming = iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(projectedVariablesBeforeRenaming, substitutionBeforeRenaming),
                        construction.getTail());

                IQTree partiallyPaddedChild = childBeforeRenaming.applyDescendingSubstitution(renamingSubstitution, Optional.empty(), variableGenerator);
                ImmutableTerm liftedDefinition = renamingSubstitution.applyToTerm(originalDefinition);

                return new ChildDefinitionLift(partiallyPaddedChild, renamingSubstitution.getRangeSet(), liftedDefinition);
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

                Substitution<ImmutableTerm> paddingSubstitution = newVarTypeMap.entrySet().stream()
                        .filter(v -> !childVariables.contains(v.getKey()))
                        .collect(substitutionFactory.toSubstitution(
                                Map.Entry::getKey,
                                e -> e.getValue()
                                        .map(t -> termFactory.getTypedNull(t).simplify())
                                        .orElseGet(termFactory::getNullConstant)));

                return iqTreeTools.createConstructionNodeTreeIfNontrivial(partiallyPaddedChild, paddingSubstitution, newVarTypeMap::keySet);
            }
        }
    }

    private static class ChildDefinitionLift {
        private final IQTree partiallyPaddedChild;
        private final ImmutableSet<Variable> freshlyCreatedVariables;
        private final ImmutableTerm liftedDefinition;

        ChildDefinitionLift(IQTree partiallyPaddedChild, ImmutableSet<Variable> freshlyCreatedVariables,
                                   ImmutableTerm liftedDefinition) {
            this.partiallyPaddedChild = partiallyPaddedChild;
            this.freshlyCreatedVariables = freshlyCreatedVariables;
            this.liftedDefinition = liftedDefinition;
        }

        ImmutableSet<Variable> getFreshlyCreatedVariables() {
            return freshlyCreatedVariables;
        }

        IQTree getPartiallyPaddedChild() {
            return partiallyPaddedChild;
        }

        ImmutableTerm getLiftedDefinition() {
            return liftedDefinition;
        }
    }
}
