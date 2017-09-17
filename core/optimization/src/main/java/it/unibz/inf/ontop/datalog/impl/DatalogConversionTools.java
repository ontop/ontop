package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.DataNode;
import it.unibz.inf.ontop.datalog.TargetAtom;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;

import static it.unibz.inf.ontop.model.OntopModelSingletons.ATOM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.SUBSTITUTION_FACTORY;
import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.castIntoGroundTerm;
import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.isGroundTerm;
import static it.unibz.inf.ontop.model.term.impl.ImmutabilityTools.convertIntoImmutableTerm;

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
     */
    public static TargetAtom convertFromDatalogDataAtom(
            Function datalogDataAtom)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {

        Predicate datalogAtomPredicate = datalogDataAtom.getFunctionSymbol();
        AtomPredicate atomPredicate = (datalogAtomPredicate instanceof AtomPredicate)
                ? (AtomPredicate) datalogAtomPredicate
                : ATOM_FACTORY.getAtomPredicate(datalogAtomPredicate);

        ImmutableList.Builder<Variable> argListBuilder = ImmutableList.builder();
        ImmutableMap.Builder<Variable, ImmutableTerm> bindingBuilder = ImmutableMap.builder();

        /*
         * Replaces all the arguments by variables.
         * Makes sure the projected variables are unique.
         *
         * Creates allBindings entries if needed (in case of constant of a functional term)
         */
        VariableGenerator projectedVariableGenerator = new VariableGenerator(ImmutableSet.of());
        for (Term term : datalogDataAtom.getTerms()) {
            Variable newArgument;

            /*
             * If a projected variable occurs multiple times as an head argument,
             * rename it and keep track of the equivalence.
             *
             */
            if (term instanceof Variable) {
                Variable originalVariable = (Variable) term;
                newArgument = projectedVariableGenerator.generateNewVariableIfConflicting(originalVariable);
                if (!newArgument.equals(originalVariable)) {
                    bindingBuilder.put(newArgument, originalVariable);
                }
            }
            /*
             * Ground-term: replace by a variable and add a binding.
             * (easier to merge than putting the ground term in the data atom).
             */
            else if (isGroundTerm(term)) {
                Variable newVariable = projectedVariableGenerator.generateNewVariable();
                newArgument = newVariable;
                bindingBuilder.put(newVariable, castIntoGroundTerm(term));
            }
            /*
             * Non-ground functional term
             */
            else {
                ImmutableTerm nonVariableTerm = convertIntoImmutableTerm(term);
                Variable newVariable = projectedVariableGenerator.generateNewVariable();
                newArgument = newVariable;
                bindingBuilder.put(newVariable, nonVariableTerm);
            }
            argListBuilder.add(newArgument);
        }

        DistinctVariableOnlyDataAtom dataAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(atomPredicate, argListBuilder.build());
        ImmutableSubstitution<ImmutableTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(bindingBuilder.build());


        return new TargetAtomImpl(dataAtom, substitution);
    }
}
