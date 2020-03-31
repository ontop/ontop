package it.unibz.inf.ontop.iq.executor.leftjoin;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@Singleton
public class LeftJoinRightChildNormalizationAnalyzerImpl implements LeftJoinRightChildNormalizationAnalyzer {

    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private LeftJoinRightChildNormalizationAnalyzerImpl(TermFactory termFactory,
                                                        IntermediateQueryFactory iqFactory) {
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
    }

    @Override
    public LeftJoinRightChildNormalizationAnalysis analyze(ImmutableSet<Variable> leftVariables,
                                                           ImmutableList<ExtensionalDataNode> leftDataNodes,
                                                           ExtensionalDataNode rightDataNode,
                                                           VariableGenerator variableGenerator,
                                                           VariableNullability variableNullability) {

        ImmutableMultimap<RelationDefinition, ImmutableMap<Integer, ? extends VariableOrGroundTerm>> leftRelationArgumentMultimap = leftDataNodes.stream()
                .collect(ImmutableCollectors.toMultimap(
                        ExtensionalDataNode::getRelationDefinition,
                        ExtensionalDataNode::getArgumentMap));

        ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap = rightDataNode.getArgumentMap();
        if (leftRelationArgumentMultimap.isEmpty()) {
            // TODO: print a warning
            return new LeftJoinRightChildNormalizationAnalysisImpl(false);
        }
        RelationDefinition rightRelation = rightDataNode.getRelationDefinition();

        /*
         * Matched UCs and FKs
         */
        ImmutableSet<UniqueConstraint> matchedUCs = extractMatchedUCs(leftRelationArgumentMultimap, rightArgumentMap,
                rightRelation, variableNullability);
        ImmutableSet<ForeignKeyConstraint> matchedFKs = extractMatchedFKs(leftRelationArgumentMultimap, rightArgumentMap,
                rightRelation, variableNullability);

        if (matchedUCs.isEmpty() && matchedFKs.isEmpty()) {
            return new LeftJoinRightChildNormalizationAnalysisImpl(false);
        }

        int rightArity = rightDataNode.getRelationDefinition().getAttributes().size();
        ImmutableSet<Integer> nonMatchedRightAttributeIndexes = extractNonMatchedRightAttributeIndexes(matchedUCs,
                matchedFKs, rightArity);
        ImmutableList<Integer> conflictingRightArgumentIndexes = nonMatchedRightAttributeIndexes.stream()
                .filter(i -> isRightArgumentConflicting(i, leftVariables, rightArgumentMap, nonMatchedRightAttributeIndexes))
                .collect(ImmutableCollectors.toList());

        if (!conflictingRightArgumentIndexes.isEmpty()) {
            ExtensionalDataNode newRightDataNode = iqFactory.createExtensionalDataNode(
                    rightRelation,
                    computeNewRightArgumentMap(rightArgumentMap, conflictingRightArgumentIndexes, variableGenerator));
            ImmutableExpression newExpression = computeExpression(rightArgumentMap,
                    newRightDataNode.getArgumentMap());

            return new LeftJoinRightChildNormalizationAnalysisImpl(newRightDataNode, newExpression);
        }
        else {
            return new LeftJoinRightChildNormalizationAnalysisImpl(true);
        }
    }

    private ImmutableSet<UniqueConstraint> extractMatchedUCs(
            ImmutableMultimap<RelationDefinition, ImmutableMap<Integer, ? extends VariableOrGroundTerm>> leftRelationArgumentMultimap,
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap,
            RelationDefinition rightRelation, VariableNullability variableNullability) {
        /*
         * When the left and right relations are the same
         */
        return leftRelationArgumentMultimap.get(rightRelation).stream()
                .flatMap(leftArgumentMap -> rightRelation.getUniqueConstraints().stream()
                        .filter(uc -> isUcMatching(uc, leftArgumentMap, rightArgumentMap, variableNullability)))
                .collect(ImmutableCollectors.toSet());
    }

    private boolean isUcMatching(UniqueConstraint uniqueConstraint,
                                 ImmutableMap<Integer, ? extends VariableOrGroundTerm> leftArgumentMap,
                                 ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap, VariableNullability variableNullability) {
        return uniqueConstraint.getAttributes().stream()
                .allMatch(a -> {
                    Optional<? extends VariableOrGroundTerm> leftArg = Optional.ofNullable(leftArgumentMap.get(a.getIndex() - 1));

                    return leftArg.isPresent()
                            && leftArg.equals(Optional.ofNullable(rightArgumentMap.get(a.getIndex() - 1)))
                        // Non-null term (at the level of the LJ tree)
                       && (!leftArg.get().isNullable(variableNullability.getNullableVariables()));
                });
    }

    private ImmutableSet<ForeignKeyConstraint> extractMatchedFKs(
            ImmutableMultimap<RelationDefinition, ImmutableMap<Integer, ? extends VariableOrGroundTerm>> leftRelationArgumentMultimap,
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap,
            RelationDefinition rightRelation, VariableNullability variableNullability) {

        return leftRelationArgumentMultimap.asMap().entrySet().stream()
                .flatMap(e -> extractMatchedFKsForARelation(e.getKey(), e.getValue(), rightArgumentMap, rightRelation, variableNullability))
                .collect(ImmutableCollectors.toSet());
    }

    private Stream<ForeignKeyConstraint> extractMatchedFKsForARelation(
            RelationDefinition leftRelation,
            Collection<ImmutableMap<Integer, ? extends VariableOrGroundTerm>> leftArgumentLists,
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap,
            RelationDefinition rightRelation, VariableNullability variableNullability) {

        return leftRelation.getForeignKeys().stream()
             .filter(fk -> fk.getReferencedRelation().equals(rightRelation))
             .filter(fk -> leftArgumentLists.stream()
                     .anyMatch(leftArgumentMap -> isFkMatching(fk, leftArgumentMap, rightArgumentMap, variableNullability)));
    }


    private boolean isFkMatching(ForeignKeyConstraint foreignKey,
                                 ImmutableMap<Integer, ? extends VariableOrGroundTerm> leftArgumentMap,
                                 ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap,
                                 VariableNullability variableNullability) {
        return foreignKey.getComponents().stream()
                .allMatch(c -> {
                    Optional<? extends VariableOrGroundTerm> leftArg = Optional.ofNullable(
                            leftArgumentMap.get(c.getAttribute().getIndex() - 1));

                        return leftArg.isPresent()
                                && leftArg.equals(Optional.ofNullable(
                                        rightArgumentMap.get(c.getReference().getIndex() - 1)))
                        // Non-nullable term
                        &&  (!leftArg.get().isNullable(variableNullability.getNullableVariables()));
                });
    }


    private ImmutableSet<Integer> extractNonMatchedRightAttributeIndexes(ImmutableCollection<UniqueConstraint> matchedUCs,
                                                                         ImmutableCollection<ForeignKeyConstraint> matchedFKs,
                                                                         int arity) {
        return IntStream.range(0, arity)
                .filter(i -> (matchedUCs.stream()
                        .noneMatch(uc ->
                                uc.getAttributes().stream()
                                        .anyMatch(a -> a.getIndex() == (i + 1)))))
                .filter(i -> (matchedFKs.stream()
                        .noneMatch(fk ->
                                fk.getComponents().stream()
                                        .anyMatch(c -> c.getReference().getIndex() == (i + 1)))))
                .boxed()
                .collect(ImmutableCollectors.toSet());
    }

    private boolean isRightArgumentConflicting(int rightArgumentIndex, ImmutableCollection<Variable> leftVariables,
                                               ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap,
                                               ImmutableSet<Integer> nonMatchedRightAttributeIndexes) {
        VariableOrGroundTerm rightArgument = rightArgumentMap.get(rightArgumentIndex);
        /*
         * Ground term -> pulled out as an equality
         */
        if (rightArgument instanceof GroundTerm)
            return true;
        Variable rightVariable = (Variable) rightArgument;

        /*
         * Is conflicting if the variable occurs in the left atom or occurs more than once in the right atom.
         */
        if (leftVariables.contains(rightVariable))
            return true;
        return rightArgumentMap.keySet().stream()
                // In case of an equality between two nonMatchedRightAttributeIndexes: count it once
                // (thanks to this order relation)
                .filter(i -> (i < rightArgumentIndex) || (!nonMatchedRightAttributeIndexes.contains(i)))
                .anyMatch(i -> rightArgumentMap.get(i).equals(rightVariable));
    }

    private ImmutableMap<Integer, VariableOrGroundTerm> computeNewRightArgumentMap(ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap,
                                                                                   ImmutableList<Integer> conflictingRightArgumentIndexes, VariableGenerator variableGenerator) {
        return rightArgumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> conflictingRightArgumentIndexes.contains(e.getKey())
                                ? variableGenerator.generateNewVariable()
                                : e.getValue()
                ));
    }

    private ImmutableExpression computeExpression(ImmutableMap<Integer, ? extends VariableOrGroundTerm> formerRightArgumentMap,
                                                  ImmutableMap<Integer, ? extends VariableOrGroundTerm> newRightArgumentMap) {
        Stream<ImmutableExpression> expressions = formerRightArgumentMap.entrySet().stream()
                .filter(e -> !e.getValue().equals(newRightArgumentMap.get(e.getKey())))
                .map(e -> termFactory.getStrictEquality(newRightArgumentMap.get(e.getKey()), e.getValue()));

        return termFactory.getConjunction(expressions)
                .orElseThrow(() -> new MinorOntopInternalBugException("A boolean expression was expected"));
    }



    public static class LeftJoinRightChildNormalizationAnalysisImpl implements LeftJoinRightChildNormalizationAnalysis {

        @Nullable
        private final ExtensionalDataNode newRightDataNode;
        @Nullable
        private final ImmutableExpression expression;
        private final boolean isMatchingAConstraint;

        private LeftJoinRightChildNormalizationAnalysisImpl(ExtensionalDataNode newRightDataNode, ImmutableExpression expression) {
            this.newRightDataNode = newRightDataNode;
            this.expression = expression;
            this.isMatchingAConstraint = true;
        }

        private LeftJoinRightChildNormalizationAnalysisImpl(boolean isMatchingAConstraint) {
            this.newRightDataNode = null;
            this.expression = null;
            this.isMatchingAConstraint = isMatchingAConstraint;
        }

        @Override
        public boolean isMatchingAConstraint() {
            return isMatchingAConstraint;
        }

        @Override
        public Optional<ExtensionalDataNode> getProposedRightDataNode() {
            return Optional.ofNullable(newRightDataNode);
        }

        @Override
        public Optional<ImmutableExpression> getAdditionalExpression() {
            return Optional.ofNullable(expression);
        }
    }


}
