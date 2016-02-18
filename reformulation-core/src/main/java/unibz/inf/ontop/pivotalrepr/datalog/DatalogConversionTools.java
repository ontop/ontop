package unibz.inf.ontop.pivotalrepr.datalog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fj.P;
import fj.P2;
import unibz.inf.ontop.model.*;
import unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import unibz.inf.ontop.pivotalrepr.DataNode;
import unibz.inf.ontop.model.impl.AtomPredicateImpl;
import unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import unibz.inf.ontop.owlrefplatform.core.basicoperations.VariableDispatcher;
import unibz.inf.ontop.pivotalrepr.impl.IntensionalDataNodeImpl;
import unibz.inf.ontop.pivotalrepr.impl.ExtensionalDataNodeImpl;

import java.util.Collection;

import static unibz.inf.ontop.model.impl.GroundTermTools.castIntoGroundTerm;
import static unibz.inf.ontop.model.impl.GroundTermTools.isGroundTerm;
import static unibz.inf.ontop.model.impl.ImmutabilityTools.convertIntoImmutableTerm;

public class DatalogConversionTools {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

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
     * TODO: should we simplify it?
     */
    public static P2<DataAtom, ImmutableSubstitution<ImmutableTerm>> convertFromDatalogDataAtom(Function datalogDataAtom)
            throws DatalogProgram2QueryConverter.InvalidDatalogProgramException {

        Predicate datalogAtomPredicate = datalogDataAtom.getFunctionSymbol();
        AtomPredicate atomPredicate;
        if (datalogAtomPredicate instanceof AtomPredicate) {
            atomPredicate = (AtomPredicate) datalogAtomPredicate;
        }
        else {
            atomPredicate = new AtomPredicateImpl(datalogAtomPredicate);
        }

        ImmutableList.Builder<VariableOrGroundTerm> argListBuilder = ImmutableList.builder();
        ImmutableMap.Builder<Variable, ImmutableTerm> allBindingBuilder = ImmutableMap.builder();

        /**
         * Replaces all the terms by variables.
         * Makes sure these variables are unique.
         *
         * Creates allBindings entries if needed (in case of constant of a functional term)
         */
        VariableDispatcher variableDispatcher = new VariableDispatcher();
        for (Term term : datalogDataAtom.getTerms()) {
            VariableOrGroundTerm newArgument;

            /**
             * Keep the same variable.
             */
            if (term instanceof Variable) {
                newArgument = (Variable) term;
            }
            /**
             * Ground-term: replace by a variable and add a binding.
             * (easier to merge than putting the ground term in the data atom).
             */
            else if (isGroundTerm(term)) {
                Variable newVariable = variableDispatcher.generateNewVariable();
                newArgument = newVariable;
                allBindingBuilder.put(newVariable, castIntoGroundTerm(term));
            }
            /**
             * Non-ground functional term
             */
            else {
                Variable newVariable = variableDispatcher.generateNewVariable();
                newArgument = newVariable;
                allBindingBuilder.put(newVariable, convertIntoImmutableTerm(term));
            }
            argListBuilder.add(newArgument);
        }

        DataAtom dataAtom = DATA_FACTORY.getDataAtom(atomPredicate, argListBuilder.build());
        ImmutableSubstitution<ImmutableTerm> substitution = new ImmutableSubstitutionImpl<>(allBindingBuilder.build());


        return P.p(dataAtom, substitution);
    }
}
