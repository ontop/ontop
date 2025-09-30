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
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.PostProcessableFunctionLifter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.iq.visit.impl.IQStateOptionalTransformer;
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
        return context.lift(tree);
    }


    private class Context {
        private final VariableGenerator variableGenerator;
        private final Transformer transformer;

        Context(VariableGenerator variableGenerator) {
            this.variableGenerator = variableGenerator;
            this.transformer = new Transformer();
        }

        IQTree lift(IQTree tree) {
            return tree.acceptVisitor(transformer);
        }

        private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
            Transformer() {
                super(PostProcessableFunctionLifterImpl.this.iqFactory,
                        t -> t.normalizeForOptimization(variableGenerator));
            }

            @Override
            public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
                IQTree normalizedTree = super.transformUnion(tree, rootNode, children);

                // Fix-point before pursing (recursive, potentially dangerous!)
                if (!normalizedTree.equals(tree)) {
                    return lift(normalizedTree);
                }

                return IQStateOptionalTransformer.reachFinalState(
                                new State(
                                        UnaryOperatorSequence.of(),
                                        rootNode.getVariables(),
                                        children,
                                        Optional.empty()),
                                Context.State::liftAnyVariable,
                                LOOPING_BOUND)
                        .asIQTree()
                        .normalizeForOptimization(variableGenerator);
            }
        }

        /**
         * A sequence of ConstructionNodes, followed by a UnionNode
         */

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private class State {
            private final UnaryOperatorSequence<ConstructionNode> ancestors;
            private final ImmutableSet<Variable> unionVariables;
            private final ImmutableList<IQTree> children;

            private final Optional<Variable> childIdVariable;

            State(UnaryOperatorSequence<ConstructionNode> ancestors, ImmutableSet<Variable> unionVariables, ImmutableList<IQTree> children,
                  Optional<Variable> childIdVariable) {
                this.ancestors = ancestors;
                this.unionVariables = unionVariables;
                this.children = children;
                this.childIdVariable = childIdVariable;
            }

            IQTree asIQTree() {
                return iqTreeTools.unaryIQTreeBuilder()
                        .append(ancestors)
                        .build(iqTreeTools.createUnionTree(unionVariables, children));
            }

            Optional<State> liftAnyVariable() {
                return unionVariables.stream()
                        .filter(v -> shouldBeLifted(v, children))
                        .findAny()
                        .map(this::liftVariable);
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


            State liftVariable(Variable variable) {
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

                return new State(ancestors.append(newConstructionNode), newUnionVariables, newChildren, Optional.of(idVariable));
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

                DownPropagation dp = iqTreeTools.createDownPropagation(renamingSubstitution, childBeforeRenaming.getVariables());
                IQTree partiallyPaddedChild = dp.propagate(childBeforeRenaming);
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
