package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.*;
import org.semanticweb.ontop.pivotalrepr.FunctionFreeDataAtom;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.ProjectionNode;
import org.semanticweb.ontop.pivotalrepr.PureDataAtom;

/**
 * TODO: explain
 *
 */
public class ProjectionNodeUnification {

    private final ImmutableList<VariableImpl> newDataAtomArguments;
    /**
     * { conflicting var : newly generated var }
     */
    private final InjectiveVar2VarSubstitution conflictMitigatingSubstitution;
    private final ImmutableSubstitution<Constant> imposedConstantSubstitution;
    /**
     * { new var : target (used as another argument) }
     */
    private final Var2VarSubstitution argumentEqualitySubstitution;
    private final ProjectionNode originalProjectionNode;


    protected ProjectionNodeUnification(ProjectionNode originalProjectionNode,
                                        IntermediateQuery originalQuery,
                                        FunctionFreeDataAtom targetDataAtom,
                                        ImmutableSet<VariableImpl> reservedVariables,
                                        VariableGenerator variableGenerator) {

        this.originalProjectionNode = originalProjectionNode;

        ArgumentsRenaming argumentRenaming = new ArgumentsRenaming(originalProjectionNode, originalQuery, targetDataAtom,
                reservedVariables, variableGenerator);

        newDataAtomArguments = argumentRenaming.getNewDataAtomArguments();
        conflictMitigatingSubstitution = argumentRenaming.getConflictMitigatingSubstitution();
        imposedConstantSubstitution = argumentRenaming.getImposedConstantSubstitution();
        argumentEqualitySubstitution = argumentRenaming.getArgumentEqualitySubstitution();
    }


    public ProjectionNode generateNewProjectionNode() {

        PureDataAtom newDataAtom = new PureDataAtomImpl(originalProjectionNode.getHeadAtom().getPredicate(),
                newDataAtomArguments);

        return new ProjectionNodeImpl(newDataAtom, generateAliasSubstitution());
    }

    private ImmutableSubstitution generateAliasSubstitution() {

        Var2VarSubstitution safeArgumentRenamingSubstitution = generateSafeArgumentRenamingSubstitution();

        ImmutableSubstitution renamedFormerAliasSubstitution = safeArgumentRenamingSubstitution.composeWith(
                conflictMitigatingSubstitution.applyRenaming(
                        originalProjectionNode.getAliasDefinition()));

        // TODO: now consider constants

        // TODO: continue

        throw new RuntimeException("Not fully implemented yet");
    }

    /**
     * TODO: explain what we mean by safe.
     */
    private Var2VarSubstitution generateSafeArgumentRenamingSubstitution() {

        ImmutableMap.Builder<VariableImpl, VariableImpl> substitutionMapBuilder = ImmutableMap.builder();

        ImmutableList<VariableImpl> originalArguments = originalProjectionNode.getHeadAtom().getVariableTerms();
        int arity = originalArguments.size();

        for (int i=0; i < arity ; i++) {
            substitutionMapBuilder.put(
                    conflictMitigatingSubstitution.applyToVariable(originalArguments.get(i)),
                    conflictMitigatingSubstitution.applyToVariable(newDataAtomArguments.get(i)));
        }

        return new Var2VarSubstitutionImpl(substitutionMapBuilder.build());
    }


    public ImmutableSubstitution generateNormalSubstitutionForSubTree() {
        throw new RuntimeException("Not fully implemented yet");
    }

    public Var2VarSubstitution generateConflictSubstitutionForSubTree() {
        throw new RuntimeException("Not fully implemented yet");
    }

    private ImmutableList<VariableImpl> getFormerLocalVariables() {
        return originalProjectionNode.getHeadAtom().getVariableTerms();
    }

//    /**
//     * Applies two substitutions (a conflict mitigating and then a regular one) on the variables and target terms
//     * of a given ImmutableSubstitution
//     *
//     * Returns the generated substitution.
//     *
//     * TODO: delete it
//     */
//    @Deprecated
//    private static ImmutableSubstitution applySubstitutionsOnSubstitutionVariableAndTerms(
//            ImmutableSubstitution substitutionToUpdate, Var2VarSubstitution conflictMitigatingSubstitution,
//            ImmutableSubstitution regularSubstitutionToApply) {
//
//        Var2VarSubstitution secondVar2VarSubstitution = ImmutableSubstitutionUtilities.extractVar2VarSubstitution(
//                regularSubstitutionToApply);
//
//        ImmutableMap.Builder<VariableImpl, ImmutableTerm> substitutionMap = ImmutableMap.builder();
//        for (Map.Entry<VariableImpl, ImmutableTerm> originalEntry : substitutionToUpdate.getImmutableMap().entrySet()) {
//
//            // Converted variable
//            VariableImpl convertedVariable = secondVar2VarSubstitution.applyToVariable(
//                    conflictMitigatingSubstitution.applyToVariable(
//                            originalEntry.getKey()));
//
//            // Converted target term
//            ImmutableTerm convertedTargetTerm = regularSubstitutionToApply.apply(
//                    conflictMitigatingSubstitution.apply(
//                            originalEntry.getValue()));
//
//            substitutionMap.put(convertedVariable, convertedTargetTerm);
//        }
//
//        return new ImmutableSubstitutionImpl(substitutionMap.build());
//    }

    /**
     * TODO: explain
     */
    private static ImmutableSubstitution mergeSubstitutions(ImmutableSubstitution mainSubstitution,
                                                            ImmutableSubstitution secondarySubstitution) {
        throw new RuntimeException("Not yet implemented");
    }

    //        /**
//         * TODO: think more about possible conflicts to detect!
//         * Possibly conflicts due to equalities (and constants).
//         */
//        @Deprecated
//        private ImmutableSubstitution generateProjectionSubstitution() {
//
//            ImmutableMap<VariableImpl, ImmutableTerm> previousSubstitutionMap = originalProjectionNode.getAliasDefinition().getImmutableMap();
//
//
//            ImmutableList<VariableImpl> previousVariables = originalProjectionNode.getHeadAtom().getVariableTerms();
//
//            /**
//             * For each argument of the projection
//             */
//            ImmutableMap.Builder<VariableImpl, ImmutableTerm> substitutionMapBuilder = ImmutableMap.builder();
//            for (int i=0 ; i < previousVariables.size() ; i++ ) {
//                VariableImpl previousVariable = previousVariables.get(i);
//                VariableImpl newVariable = newDataAtomArguments.get(i);
//
//                boolean isNewImposedConstant = imposedConstants.containsKey(newVariable);
//                boolean wasAlias = originalProjectionNode.isAlias(previousVariable);
//
//                if (isNewImposedConstant) {
//                    Constant newConstant = imposedConstants.get(newVariable);
//
//                    if (wasAlias) {
//                        throw new RuntimeException("TODO: support the update of a projection substitution with a constant");
//                        // TODO: make sure the constant is propagated.
//                        // TODO: what about conflicts? Throw a specific exception
//                    }
//                    else {
//                        substitutionMapBuilder.put(newVariable, newConstant);
//                    }
//                }
//                /**
//                 * No constant
//                 */
//                else {
//                    boolean hasNotBeenRenamed = previousVariable.equals(newVariable);
//
//                    if (wasAlias) {
//                        /**
//                         * Keep the previous entry
//                         */
//                        if (hasNotBeenRenamed) {
//                            substitutionMapBuilder.put(previousVariable, previousSubstitutionMap.get(previousVariable));
//                        }
//                        /**
//                         * New equality: set it
//                         * TODO: check possible conflicts!
//                         */
//                        else if (argumentEqualitySubstitution.equals(newVariable)) {
//                            substitutionMapBuilder.put(newVariable, argumentEqualitySubstitution.get(newVariable));
//                        }
//                        /**
//                         * Just rename the variable in the substitution
//                         */
//                        else {
//                            substitutionMapBuilder.put(newVariable, previousSubstitutionMap.get(previousVariable));
//                        }
//
//                    }
//                    /**
//                     * No entry in the previous substitution
//                     */
//                    else {
//                        if (argumentEqualitySubstitution.equals(newVariable)) {
//                            substitutionMapBuilder.put(newVariable, argumentEqualitySubstitution.get(newVariable));
//                        }
//                    }
//                }
//            }
//
//            return new ImmutableSubstitutionImpl(substitutionMapBuilder.build());
//        }



    /**
     * ////////////////////////////
     *
     * SUBCLASS
     *
     * ///////////////////////////
     *
     * TODO: explain
     */
    private static class ArgumentsRenaming {
        private final ImmutableList.Builder<VariableImpl> newDataAtomArgumentBuilder;
        /**
         * { conflicting var : newly generated var }
         */
        private final ImmutableMap.Builder<VariableImpl, VariableImpl> conflictingImposedVariableBuilder;
        private final ImmutableMap.Builder<VariableImpl, Constant> imposedConstantBuilder;
        /**
         * { new var : target (used as another argument) }
         */
        private final ImmutableMap.Builder<VariableImpl, VariableImpl> variableEqualityBuilder;
        private final ImmutableSet<VariableImpl> subTreeVariables;

        private final ProjectionNode originalProjectionNode;
        private final FunctionFreeDataAtom targetDataAtom;
        private final ImmutableSet<VariableImpl> reservedVariables;
        private final VariableGenerator variableGenerator;


        protected ArgumentsRenaming(ProjectionNode originalProjectionNode,
                                    IntermediateQuery originalQuery,
                                    FunctionFreeDataAtom targetDataAtom,
                                    ImmutableSet<VariableImpl> reservedVariables,
                                    VariableGenerator variableGenerator) {

            this.originalProjectionNode = originalProjectionNode;
            this.targetDataAtom = targetDataAtom;
            this.reservedVariables = reservedVariables;
            this.variableGenerator = variableGenerator;

            newDataAtomArgumentBuilder = ImmutableList.builder();
            conflictingImposedVariableBuilder = ImmutableMap.builder();
            imposedConstantBuilder = ImmutableMap.builder();
            variableEqualityBuilder = ImmutableMap.builder();

            subTreeVariables = VariableCollector.collectVariables(
                    originalQuery.getSubTreeNodesInTopDownOrder(this.originalProjectionNode));

            computeNewDataAtomArguments();
        }

        /**
         * TODO: explain
         *
         * TODO: list the structures that are filled.
         *
         */
        private void computeNewDataAtomArguments() {
            ImmutableList<VariableImpl> formerLocalVariables = originalProjectionNode.getHeadAtom().getVariableTerms();
            ImmutableList<NonFunctionalTerm> targetTerms = targetDataAtom.getNonFunctionalTerms();

            for (int i = 0; i < formerLocalVariables.size(); i++) {
                VariableImpl originalLocalVariable = formerLocalVariables.get(i);
                NonFunctionalTerm targetTerm = targetTerms.get(i);

                VariableImpl newLocalVariable;

                if (targetTerm instanceof Constant) {
                    newLocalVariable = renameVariableIfTargetConstant(originalLocalVariable);
                    imposedConstantBuilder.put(newLocalVariable, (Constant) targetTerm);
                }
                /**
                 * Variable
                 */
                else {
                    newLocalVariable = renameVariableFromTargetVariable((VariableImpl) targetTerm, i);
                }

                newDataAtomArgumentBuilder.add(newLocalVariable);
            }
        }


        /**
         * TODO: explain
         */
        private VariableImpl renameVariableFromTargetVariable(VariableImpl targetVariable, int index) {

            /**
             * First case: the target variable is already used as a previous argument
             *
             * Action:
             *   - Generates a new variable for this argument (variable uniqueness constraint)
             *   - Registers the equality between the new variable and the (already used) target one.
             */
            ImmutableList<VariableImpl> alreadyRenamedVariables = newDataAtomArgumentBuilder.build();
            if (alreadyRenamedVariables.contains(targetVariable)) {
                VariableImpl newVariable = variableGenerator.generateNewVariableFromVar(targetVariable);
                variableEqualityBuilder.put(newVariable, targetVariable);
                return newVariable;
            }

            /**
             * Second case: the (imposed) target variable conflicts with another variable
             * currently used in the sub-query.
             *
             * Action:
             *    - Use the target variable because it is imposed
             *    - Registers this conflicting variable
             */
            if (isConflictingWithRestOfSubQuery(targetVariable, index)) {
                conflictingImposedVariableBuilder.put(targetVariable, variableGenerator.generateNewVariable());
                return targetVariable;
            }

            /**
             * Otherwise, uses the target variable
             */
            return targetVariable;
        }

        /**
         * TODO: explain
         */
        private boolean isConflictingWithRestOfSubQuery(VariableImpl targetVariable, int restStartIndex) {
            ImmutableList<VariableImpl> formerLocalVariables = originalProjectionNode.getHeadAtom().getVariableTerms();

            /**
             * Checks the remaining former variables of the projection data atom
             */
            int arity = formerLocalVariables.size();
            if (restStartIndex < arity) {
                ImmutableList<VariableImpl> remainingFormerLocalVariables = formerLocalVariables.subList(restStartIndex,
                        arity);
                if (remainingFormerLocalVariables.contains(targetVariable))
                    return true;
            }

            /**
             * Checks the variables used in the other nodes of the sub-query
             */
            if (subTreeVariables.contains(targetVariable))
                return true;

            /**
             * No conflict
             */
            return false;
        }


        private VariableImpl renameVariableIfTargetConstant(VariableImpl originalLocalVariable) {
            if (reservedVariables.contains(originalLocalVariable)) {
                return variableGenerator.generateNewVariableFromVar(originalLocalVariable);
            }

            return originalLocalVariable;
        }


        public ImmutableList<VariableImpl> getNewDataAtomArguments() {
            return newDataAtomArgumentBuilder.build();
        }

        /**
         * { conflicting var : newly generated var }
         */
        public InjectiveVar2VarSubstitution getConflictMitigatingSubstitution() {
            return new InjectiveVar2VarSubstitutionImpl(conflictingImposedVariableBuilder.build());
        }

        public ImmutableSubstitution<Constant> getImposedConstantSubstitution() {
            return new ImmutableSubstitutionImpl<>(imposedConstantBuilder.build());
        }

        public Var2VarSubstitutionImpl getArgumentEqualitySubstitution() {
            return new Var2VarSubstitutionImpl(variableEqualityBuilder.build());
        }
    }

}
