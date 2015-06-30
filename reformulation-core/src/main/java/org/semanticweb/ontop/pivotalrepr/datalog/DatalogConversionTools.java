package org.semanticweb.ontop.pivotalrepr.datalog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fj.P;
import fj.P2;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.AtomPredicateImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.VariableDispatcher;
import org.semanticweb.ontop.pivotalrepr.DataNode;
import org.semanticweb.ontop.pivotalrepr.impl.OrdinaryDataNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.TableNodeImpl;

import java.util.Collection;

import static org.semanticweb.ontop.model.impl.ImmutabilityTools.convertIntoImmutableTerm;

public class DatalogConversionTools {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

    /**
     * TODO: explain
     */
    public static DataNode createDataNode(DataAtom dataAtom, Collection<Predicate> tablePredicates) {

        if (tablePredicates.contains(dataAtom.getPredicate())) {
            return new TableNodeImpl(dataAtom);
        }

        return new OrdinaryDataNodeImpl(dataAtom);
    }


    /**
     * TODO: explain
     *
     * TODO: should we simplify it?
     */
    public static P2<DataAtom, ImmutableSubstitution<ImmutableTerm>> convertFromDatalogDataAtom(Function datalogDataAtom)
            throws DatalogProgram2QueryConverter.InvalidDatalogProgramException {

        Predicate datalogAtomPredicate = datalogDataAtom.getFunctionSymbol();
        AtomPredicate atomPredicate = new AtomPredicateImpl(datalogAtomPredicate);

        ImmutableList.Builder<VariableImpl> varListBuilder = ImmutableList.builder();
        ImmutableMap.Builder<VariableImpl, ImmutableTerm> allBindingBuilder = ImmutableMap.builder();

        /**
         * Replaces all the terms by variables.
         * Makes sure these variables are unique.
         *
         * Creates allBindings entries if needed (in case of constant of a functional term)
         */
        VariableDispatcher variableDispatcher = new VariableDispatcher();
        for (Term term : datalogDataAtom.getTerms()) {
            VariableImpl newVariableArgument;

            /**
             * Keep the same variable.
             */
            if (term instanceof VariableImpl) {
                newVariableArgument = (VariableImpl) term;
            }
            /**
             * TODO: could we consider a sub-class of Function instead?
             */
            else if ((term instanceof Constant) || (term instanceof Function)) {
                newVariableArgument = variableDispatcher.generateNewVariable();
                allBindingBuilder.put(newVariableArgument, convertIntoImmutableTerm(term));
            } else {
                throw new DatalogProgram2QueryConverter.InvalidDatalogProgramException("Unexpected term found in a data atom: " + term);
            }
            varListBuilder.add(newVariableArgument);
        }

        DataAtom dataAtom = DATA_FACTORY.getDataAtom(atomPredicate, varListBuilder.build());
        ImmutableSubstitution<ImmutableTerm> substitution = new ImmutableSubstitutionImpl<>(allBindingBuilder.build());


        return P.p(dataAtom, substitution);
    }
}
