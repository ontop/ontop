package it.unibz.inf.ontop.iq.datalog.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.DataNode;
import it.unibz.inf.ontop.iq.mapping.TargetAtom;
import it.unibz.inf.ontop.iq.mapping.impl.TargetAtomImpl;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;

import static it.unibz.inf.ontop.model.OntopModelSingletons.ATOM_FACTORY;
import static it.unibz.inf.ontop.model.impl.GroundTermTools.castIntoGroundTerm;
import static it.unibz.inf.ontop.model.impl.GroundTermTools.isGroundTerm;
import static it.unibz.inf.ontop.model.impl.ImmutabilityTools.convertIntoImmutableTerm;
import static it.unibz.inf.ontop.model.OntopModelSingletons.DATA_FACTORY;

public class DatalogConversionTools {

    /**
     * TODO: explain
     */
    public static DataNode createDataNode(IntermediateQueryFactory iqFactory, DataAtom dataAtom,
                                          Collection<Predicate> tablePredicates) {

        if (tablePredicates.contains(dataAtom.getPredicate())) {
            return iqFactory.createExtensionalDataNode(dataAtom);
        }

        return iqFactory.createIntensionalDataNode(dataAtom);
    }


    /**
     * TODO: explain
     *
     * TODO: deal with multiple occurences of the same variable in the head of the DatalogProgram
     */
    public static TargetAtom convertFromDatalogDataAtom(
            Function datalogDataAtom)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {

        Predicate datalogAtomPredicate = datalogDataAtom.getFunctionSymbol();
        AtomPredicate atomPredicate = (datalogAtomPredicate instanceof AtomPredicate)
                ? (AtomPredicate) datalogAtomPredicate
                : DATA_FACTORY.getAtomPredicate(datalogAtomPredicate);

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

        DistinctVariableOnlyDataAtom dataAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(atomPredicate, argListBuilder.build());
        ImmutableSubstitution<ImmutableTerm> substitution = DATA_FACTORY.getSubstitution(allBindingBuilder.build());


        return new TargetAtomImpl(dataAtom, substitution);
    }
}
