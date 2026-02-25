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
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.UnaryIQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.node.normalization.impl.NormalizationContext.State;


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
        // no equality check
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
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        Context context = new Context(variableGenerator);
        return context.transformer.transform(tree);
    }


    private class Context {
        private final VariableGenerator variableGenerator;
        private final Transformer transformer;

        Context(VariableGenerator variableGenerator) {
            this.variableGenerator = variableGenerator;
            this.transformer = new Transformer();
        }

        private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
            Transformer() {
                super(PostProcessableFunctionLifterImpl.this.iqFactory);
            }

            @Override
            public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
                IQTree normalizedTree = super.transformUnion(tree, rootNode, children)
                        .normalizeForOptimization(variableGenerator);

                // Fix-point before pursing (recursive, potentially dangerous!)
                if (!normalizedTree.equals(tree)) {
                    return transform(normalizedTree);
                }

                var initial = State.<ConstructionNode, UnionSubTree>initial(
                        new UnionSubTree(rootNode.getVariables(), children, Optional.empty()));

                var state = initial.reachFinal(LOOPING_BOUND, s -> liftAnyVariable(s));

                return asIQTree(state)
                        .normalizeForOptimization(variableGenerator);
            }
        }

        private class UnionSubTree {
            private final ImmutableSet<Variable> unionVariables;
            private final ImmutableList<IQTree> children;

            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            private final Optional<Variable> childIdVariable;

            private UnionSubTree(ImmutableSet<Variable> unionVariables, ImmutableList<IQTree> children, Optional<Variable> childIdVariable) {
                this.unionVariables = unionVariables;
                this.children = children;
                this.childIdVariable = childIdVariable;
            }
        }

        /**
         * A sequence of ConstructionNodes, followed by a UnionNode
         */

        IQTree asIQTree(State<ConstructionNode, UnionSubTree> state) {
            return iqTreeTools.unaryIQTreeBuilder()
                    .append(state.getAncestors())
                    .build(iqTreeTools.createUnionTree(state.getSubTree().unionVariables, state.getSubTree().children));
        }

        Optional<State<ConstructionNode, UnionSubTree>> liftAnyVariable(State<ConstructionNode, UnionSubTree> state) {
            UnionSubTree subTree = state.getSubTree();
            return subTree.unionVariables.stream()
                    .filter(v -> shouldBeLifted(v, subTree.children))
                    .findAny()
                    .map(v -> liftVariable(state, v));
        }

        boolean shouldBeLifted(Variable variable, ImmutableList<IQTree> children) {
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
        boolean shouldBeLifted(ImmutableFunctionalTerm functionalTerm, int nbChildren) {
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


        State<ConstructionNode, UnionSubTree> liftVariable(State<ConstructionNode, UnionSubTree> state, Variable variable) {
            UnionSubTree subTree = state.getSubTree();
            Variable idVariable = subTree.childIdVariable
                    .orElseGet(variableGenerator::generateNewVariable);

            ImmutableList<ChildDefinitionLift> childDefinitionLifts = IntStream.range(0, subTree.children.size())
                    .mapToObj(i -> liftDefinition(subTree.children.get(i), i, variable, subTree.unionVariables, idVariable))
                    .collect(ImmutableCollectors.toList());

            ImmutableFunctionalTerm newDefinition = mergeDefinitions(idVariable, childDefinitionLifts);

            ImmutableSet<Variable> newUnionVariables = Stream.concat(
                            Stream.concat(
                                    subTree.unionVariables.stream(),
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

            ConstructionNode newConstructionNode = iqFactory.createConstructionNode(subTree.unionVariables,
                    substitutionFactory.getSubstitution(variable, newDefinition));

            return state.lift(newConstructionNode, new UnionSubTree(newUnionVariables, newChildren, Optional.of(idVariable)));
        }

        ChildDefinitionLift liftDefinition(IQTree childTree, int position, Variable variable,
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

            IQTree partiallyPaddedChild = iqTreeTools.applyDownPropagation(renamingSubstitution, childBeforeRenaming);
            ImmutableTerm liftedDefinition = renamingSubstitution.applyToTerm(originalDefinition);

            return new ChildDefinitionLift(partiallyPaddedChild, renamingSubstitution.getRangeSet(), liftedDefinition);
        }

        ImmutableFunctionalTerm mergeDefinitions(Variable idVariable,
                                                 ImmutableList<ChildDefinitionLift> childDefinitionLifts) {
            ImmutableList<ImmutableTerm> values = childDefinitionLifts.stream()
                    .map(ChildDefinitionLift::getLiftedDefinition)
                    .collect(ImmutableCollectors.toList());

            return termFactory.getDBIntIndex(idVariable, values);
        }

        Optional<DBTermType> extractType(Variable variable, ImmutableList<ChildDefinitionLift> childDefinitionLifts) {
            return childDefinitionLifts.stream()
                    .map(ChildDefinitionLift::getPartiallyPaddedChild)
                    .filter(c -> c.getVariables().contains(variable))
                    .findAny()
                    .flatMap(t -> typeExtractor.extractSingleTermType(variable, t))
                    .filter(t -> t instanceof DBTermType)
                    .map(t -> (DBTermType) t);
        }

        IQTree padChild(IQTree partiallyPaddedChild, ImmutableMap<Variable, Optional<DBTermType>> newVarTypeMap) {
            ImmutableSet<Variable> childVariables = partiallyPaddedChild.getVariables();

            Substitution<ImmutableTerm> paddingSubstitution = newVarTypeMap.entrySet().stream()
                    .filter(v -> !childVariables.contains(v.getKey()))
                    .collect(substitutionFactory.toSubstitution(
                            Map.Entry::getKey,
                            e -> e.getValue()
                                    .map(t -> termFactory.getTypedNull(t).simplify())
                                    .orElseGet(termFactory::getNullConstant)));

            var optionalConstructionNode = iqTreeTools.createOptionalConstructionNode(newVarTypeMap::keySet, paddingSubstitution);
            return iqTreeTools.unaryIQTreeBuilder()
                    .append(optionalConstructionNode)
                    .build(partiallyPaddedChild);
        }
    }

    private static class ChildDefinitionLift {
        private final IQTree partiallyPaddedChild;
        private final ImmutableSet<Variable> freshlyCreatedVariables;
        private final ImmutableTerm liftedDefinition;

        ChildDefinitionLift(IQTree partiallyPaddedChild,
                            ImmutableSet<Variable> freshlyCreatedVariables,
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
