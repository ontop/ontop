package it.unibz.inf.ontop.iq.executor.leftjoin;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.DataNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;


@Singleton
public class LeftJoinRightChildNormalizationAnalyzerImpl implements LeftJoinRightChildNormalizationAnalyzer {

    private final TermFactory termFactory;
    private final AtomFactory atomFactory;
    private final ImmutabilityTools immutabilityTools;

    @Inject
    private LeftJoinRightChildNormalizationAnalyzerImpl(TermFactory termFactory, AtomFactory atomFactory,
                                                        ImmutabilityTools immutabilityTools) {
        this.termFactory = termFactory;
        this.atomFactory = atomFactory;
        this.immutabilityTools = immutabilityTools;
    }

    @Override
    public LeftJoinRightChildNormalizationAnalysis analyze(ImmutableSet<Variable> leftVariables,
                                                           ImmutableList<ExtensionalDataNode> leftDataNodes,
                                                           ExtensionalDataNode rightDataNode,
                                                           VariableGenerator variableGenerator) {

        ImmutableMultimap<RelationDefinition, ImmutableList<? extends VariableOrGroundTerm>> leftRelationArgumentMultimap
                = leftDataNodes.stream()
                .map(ExtensionalDataNode::getProjectionAtom)
                .map(a -> Maps.immutableEntry(
                        a.getPredicate().getRelationDefinition(), a.getArguments()))
                .collect(ImmutableCollectors.toMultimap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));

        DataAtom<RelationPredicate> rightProjectionAtom = rightDataNode.getProjectionAtom();
        ImmutableList<? extends VariableOrGroundTerm> rightArguments = rightProjectionAtom.getArguments();
        if (leftRelationArgumentMultimap.isEmpty()) {
            // TODO: print a warning
            return new LeftJoinRightChildNormalizationAnalysisImpl(false);
        }
        RelationDefinition rightRelation = rightDataNode.getProjectionAtom().getPredicate().getRelationDefinition();

        /*
         * Matched UCs and FKs
         */
        ImmutableSet<UniqueConstraint> matchedUCs = extractMatchedUCs(leftRelationArgumentMultimap, rightArguments,
                rightRelation);
        ImmutableSet<ForeignKeyConstraint> matchedFKs = extractMatchedFKs(leftRelationArgumentMultimap, rightArguments,
                rightRelation);

        if (matchedUCs.isEmpty() && matchedFKs.isEmpty()) {
            return new LeftJoinRightChildNormalizationAnalysisImpl(false);
        }

        ImmutableSet<Integer> nonMatchedRightAttributeIndexes = extractNonMatchedRightAttributeIndexes(matchedUCs,
                matchedFKs, rightArguments.size());
        ImmutableList<Integer> conflictingRightArgumentIndexes = nonMatchedRightAttributeIndexes.stream()
                .filter(i -> isRightArgumentConflicting(i, leftVariables, rightArguments, nonMatchedRightAttributeIndexes))
                .collect(ImmutableCollectors.toList());

        if (!conflictingRightArgumentIndexes.isEmpty()) {
            ExtensionalDataNode newRightDataNode = rightDataNode.newAtom(computeNewRightAtom(rightProjectionAtom.getPredicate(),
                    rightArguments, conflictingRightArgumentIndexes, variableGenerator));
            ImmutableExpression newExpression = computeExpression(rightArguments,
                    newRightDataNode.getProjectionAtom().getArguments());

            return new LeftJoinRightChildNormalizationAnalysisImpl(newRightDataNode, newExpression);
        }
        else {
            return new LeftJoinRightChildNormalizationAnalysisImpl(true);
        }
    }

    private ImmutableSet<UniqueConstraint> extractMatchedUCs(
            ImmutableMultimap<RelationDefinition, ImmutableList<? extends VariableOrGroundTerm>> leftRelationArgumentMultimap,
            ImmutableList<? extends VariableOrGroundTerm> rightArguments,
            RelationDefinition rightRelation) {
        /*
         * When the left and right relations are the same
         */
        return leftRelationArgumentMultimap.get(rightRelation).stream()
                .flatMap(leftArguments -> rightRelation.getUniqueConstraints().stream()
                        .filter(uc -> isUcMatching(uc, leftArguments, rightArguments)))
                .collect(ImmutableCollectors.toSet());
    }

    private boolean isUcMatching(UniqueConstraint uniqueConstraint,
                                 ImmutableList<? extends VariableOrGroundTerm> leftArguments,
                                 ImmutableList<? extends VariableOrGroundTerm> rightArguments) {
        return uniqueConstraint.getAttributes().stream()
                .allMatch(a -> leftArguments.get(a.getIndex() -1)
                        .equals(rightArguments.get(a.getIndex() - 1))
                        // Excludes nullable attributes for the moment. TODO: reconsider it
                        && !a.canNull());
    }

    private ImmutableSet<ForeignKeyConstraint> extractMatchedFKs(
            ImmutableMultimap<RelationDefinition, ImmutableList<? extends VariableOrGroundTerm>> leftRelationArgumentMultimap,
            ImmutableList<? extends VariableOrGroundTerm> rightArguments,
            RelationDefinition rightRelation) {

        return leftRelationArgumentMultimap.asMap().entrySet().stream()
                .flatMap(e -> extractMatchedFKsForARelation(e.getKey(), e.getValue(), rightArguments, rightRelation))
                .collect(ImmutableCollectors.toSet());
    }

    private Stream<ForeignKeyConstraint> extractMatchedFKsForARelation(
            RelationDefinition leftRelation,
            Collection<ImmutableList<? extends VariableOrGroundTerm>> leftArgumentLists,
            ImmutableList<? extends VariableOrGroundTerm> rightArguments,
            RelationDefinition rightRelation) {

        return leftRelation.getForeignKeys().stream()
             .filter(fk -> fk.getReferencedRelation().equals(rightRelation))
             .filter(fk -> leftArgumentLists.stream()
                     .anyMatch(leftArguments -> isFkMatching(fk, leftArguments, rightArguments)));
    }


    private boolean isFkMatching(ForeignKeyConstraint foreignKey,
                                 ImmutableList<? extends VariableOrGroundTerm> leftArguments,
                                 ImmutableList<? extends VariableOrGroundTerm> rightArguments) {
        return foreignKey.getComponents().stream()
                .allMatch(c -> leftArguments.get(c.getAttribute().getIndex() - 1)
                        .equals(rightArguments.get(c.getReference().getIndex() - 1))
                        // Excludes nullable attributes for the moment. TODO: reconsider it
                        &&  (!c.getAttribute().canNull()));
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
                                               ImmutableList<? extends VariableOrGroundTerm> rightArguments,
                                               ImmutableSet<Integer> nonMatchedRightAttributeIndexes) {
        VariableOrGroundTerm rightArgument = rightArguments.get(rightArgumentIndex);
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
        return IntStream.range(0, rightArguments.size())
                // In case of an equality between two nonMatchedRightAttributeIndexes: count it once
                // (thanks to this order relation)
                .filter(i -> (i < rightArgumentIndex) || (!nonMatchedRightAttributeIndexes.contains(i)))
                .anyMatch(i -> rightArguments.get(i).equals(rightVariable));
    }

    private DataAtom<RelationPredicate> computeNewRightAtom(RelationPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> rightArguments,
                                         ImmutableList<Integer> conflictingRightArgumentIndexes, VariableGenerator variableGenerator) {
        ImmutableList<VariableOrGroundTerm> newArguments = IntStream.range(0, rightArguments.size())
                .boxed()
                .map(i -> conflictingRightArgumentIndexes.contains(i)
                        ? variableGenerator.generateNewVariable()
                        : rightArguments.get(i))
                .collect(ImmutableCollectors.toList());

        return atomFactory.getDataAtom(predicate, newArguments);
    }

    private ImmutableExpression computeExpression(ImmutableList<? extends VariableOrGroundTerm> formerRightArguments,
                                                  ImmutableList<? extends VariableOrGroundTerm> newRightArguments) {
        Stream<ImmutableExpression> expressions = IntStream.range(0, formerRightArguments.size())
                .filter(i -> !formerRightArguments.get(i).equals(newRightArguments.get(i)))
                .boxed()
                .map(i -> termFactory.getImmutableExpression(EQ, newRightArguments.get(i), formerRightArguments.get(i)));

        return immutabilityTools.foldBooleanExpressions(expressions)
                .orElseThrow(() -> new MinorOntopInternalBugException("A boolean expression was expected"));
    }



    public static class LeftJoinRightChildNormalizationAnalysisImpl implements LeftJoinRightChildNormalizationAnalysis {

        @Nullable
        private final DataNode newRightDataNode;
        @Nullable
        private final ImmutableExpression expression;
        private final boolean isMatchingAConstraint;

        private LeftJoinRightChildNormalizationAnalysisImpl(DataNode newRightDataNode, ImmutableExpression expression) {
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
        public Optional<DataNode> getProposedRightDataNode() {
            return Optional.ofNullable(newRightDataNode);
        }

        @Override
        public Optional<ImmutableExpression> getAdditionalExpression() {
            return Optional.ofNullable(expression);
        }
    }


}
