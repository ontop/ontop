package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Var2VarSubstitution;
import org.semanticweb.ontop.pivotalrepr.FunctionFreeDataAtom;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.ProjectionNode;

/**
 * TODO: explain
 */
public class SubQueryUnificationTools {

    /**
     * TODO: explain
     *
     * Returns a new IntermediateQuery (the original one is untouched).
     */
    public static IntermediateQuery unifySubQuery(final IntermediateQuery originalSubQuery,
                                                  final FunctionFreeDataAtom targetDataAtom,
                                                  final ImmutableSet<VariableImpl> reservedVariables)
            throws SubQueryUnificationException {

        ProjectionNode originalRootNode = originalSubQuery.getRootProjectionNode();
        ImmutableSet<VariableImpl> originalVariables = VariableCollector.collectVariables(originalSubQuery);
        ImmutableSet<Variable> allKnownVariables = ImmutableSet.<Variable>builder()
                .addAll(reservedVariables)
                .addAll(originalVariables)
                .build();

        /**
         * Should have already been checked.
         */
        if (!originalRootNode.getHeadAtom().subsumes(targetDataAtom)) {
            throw new IllegalArgumentException("The target data atom is not compatible with the query");
        }

        VariableGenerator variableGenerator = new VariableGenerator(allKnownVariables);

        ProjectionNodeUnification rootRenaming = new ProjectionNodeUnification(originalRootNode, originalSubQuery,
                targetDataAtom, reservedVariables, variableGenerator);

        ProjectionNode newRootNode = rootRenaming.generateNewProjectionNode();
        Var2VarSubstitution conflictSubstitution =  rootRenaming.generateConflictSubstitutionForSubTree();
        // TODO: create a sub-type of immutable substitution
        ImmutableSubstitution normalSubstitution =  rootRenaming.generateNormalSubstitutionForSubTree();


        // TODO: continue


        throw new RuntimeException("Not fully implemented yet");
    }


    /**
     * TODO: explain
     */
    public class SubQueryUnificationException extends Exception {
        protected SubQueryUnificationException(String message) {
            super(message);
        }
    }




}
