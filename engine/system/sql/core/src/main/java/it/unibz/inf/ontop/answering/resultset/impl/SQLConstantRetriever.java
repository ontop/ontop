package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Iterator;

class SQLConstantRetriever {

    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final ImmutableList<DBTermType> termTypes;
    private final ImmutableList<Variable> SQLSignature;

    SQLConstantRetriever(ImmutableSubstitution<ImmutableFunctionalTerm> substitution, ImmutableList<Variable> SQLSignature,
                         TermFactory termFactory, SubstitutionFactory substitutionFactory) {
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.SQLSignature = SQLSignature;
        this.termTypes = computeDBTypes(substitution.getImmutableMap().values());
    }


    ImmutableSubstitution<DBConstant> retrieveAllConstants(ImmutableList<String> rawValues) {
        Iterator<DBTermType> termsIt = termTypes.iterator();
        Iterator<String> valuesIt = rawValues.iterator();
        return substitutionFactory.getSubstitution(
                SQLSignature.stream()
                        .collect(ImmutableCollectors.toMap(
                                v -> v,
                                v -> termFactory.getDBConstant(
                                        valuesIt.next(),
                                        termsIt.next()
                                ))));
    }

    private ImmutableList<DBTermType> computeDBTypes(ImmutableCollection<ImmutableFunctionalTerm> terms) {
        return SQLSignature.stream()
                .map(v -> getRequiredType(v, terms))
                .collect(ImmutableCollectors.toList());
    }

    private DBTermType getRequiredType(Variable var, ImmutableCollection<ImmutableFunctionalTerm> terms) {
        ImmutableSet<ImmutableTerm> filteredTerms = terms.stream()
                .filter(t -> (requires(t, var)))
                .collect(ImmutableCollectors.toSet());

        if (filteredTerms.size() != 1) {
            throw new SQLVariableNameException(var, terms);
        }
        return getRequiredType(var, terms.iterator().next());
    }

    private DBTermType getRequiredType(Variable var, ImmutableTerm term) {
        throw new RuntimeException("TODO: implement");
    }

    private boolean requires(ImmutableTerm t, Variable var) {
        return t.getVariableStream().anyMatch(v -> v.equals(var));
    }

    public static class SQLVariableNameException extends OntopInternalBugException {
        SQLVariableNameException(Variable var, ImmutableCollection<ImmutableFunctionalTerm> terms) {
            super("Variable " + var + " should be required by exactly one term in " + terms);
        }
    }
}
