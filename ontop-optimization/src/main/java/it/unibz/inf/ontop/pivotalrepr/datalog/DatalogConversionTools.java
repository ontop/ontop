package it.unibz.inf.ontop.pivotalrepr.datalog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fj.P;
import fj.P2;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.DataNode;
import it.unibz.inf.ontop.pivotalrepr.impl.IntensionalDataNodeImpl;

import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.ExtensionalDataNodeImpl;

import java.util.Collection;

import static it.unibz.inf.ontop.model.impl.GroundTermTools.castIntoGroundTerm;
import static it.unibz.inf.ontop.model.impl.GroundTermTools.isGroundTerm;
import static it.unibz.inf.ontop.model.impl.ImmutabilityTools.convertIntoImmutableTerm;
import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class DatalogConversionTools {

    /**
     * TODO: explain
     */
    public static DataNode createDataNode(DataAtom dataAtom, Collection<Predicate> tablePredicates) {

        if (tablePredicates.contains(dataAtom.getPredicate())) {
            return new ExtensionalDataNodeImpl(dataAtom);
        }

        return new IntensionalDataNodeImpl(dataAtom);
    }


    /**
     * TODO: explain
     *
     * TODO: deal with multiple occurences of the same variable in the head of the DatalogProgram
     */
    public static P2<DistinctVariableOnlyDataAtom, ImmutableSubstitution<ImmutableTerm>> convertFromDatalogDataAtom(
            Function datalogDataAtom)
            throws DatalogProgram2QueryConverter.InvalidDatalogProgramException {

        Predicate datalogAtomPredicate = datalogDataAtom.getFunctionSymbol();
        AtomPredicate atomPredicate = (datalogAtomPredicate instanceof AtomPredicate)
                ? (AtomPredicate) datalogAtomPredicate
                : new AtomPredicateImpl(datalogAtomPredicate);

        ImmutableList.Builder<Variable> argListBuilder = ImmutableList.builder();
        ImmutableMap.Builder<Variable, ImmutableTerm> allBindingBuilder = ImmutableMap.builder();

        /**
         * Replaces all the terms by variables.
         * Makes sure these variables are unique.
         *
         * Creates allBindings entries if needed (in case of constant of a functional term)
         */
        VariableGenerator variableGenerator = new VariableGenerator(ImmutableSet.of());
        for (Term term : datalogDataAtom.getTerms()) {
            Variable newArgument;

            /**
             * If a variable occurs multiple times, rename it and keep track of the equivalence.
             *
             */
            if (term instanceof Variable) {
                Variable originalVariable = (Variable) term;
                newArgument = variableGenerator.generateNewVariableIfConflicting(originalVariable);
                if (!newArgument.equals(originalVariable)) {
                    allBindingBuilder.put(newArgument, originalVariable);
                }
            }
            /**
             * Ground-term: replace by a variable and add a binding.
             * (easier to merge than putting the ground term in the data atom).
             */
            else if (isGroundTerm(term)) {
                Variable newVariable = variableGenerator.generateNewVariable();
                newArgument = newVariable;
                allBindingBuilder.put(newVariable, castIntoGroundTerm(term));
            }
            /**
             * Non-ground functional term
             */
            else {
                Variable newVariable = variableGenerator.generateNewVariable();
                newArgument = newVariable;
                allBindingBuilder.put(newVariable, convertIntoImmutableTerm(term));
            }
            argListBuilder.add(newArgument);
        }

        DistinctVariableOnlyDataAtom dataAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(atomPredicate, argListBuilder.build());
        ImmutableSubstitution<ImmutableTerm> substitution = new ImmutableSubstitutionImpl<>(allBindingBuilder.build());


        return P.p(dataAtom, substitution);
    }
}
