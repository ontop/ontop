package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Iterator;
import java.util.Map;

class SQLConstantRetriever {

    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final ImmutableList<DBTermType> termTypes;
    /* maps each variable of the output substitution to the index (in the SQL tuple) of the value used to build it.
    * indices start at 0 */
    private final ImmutableMap<Variable, Integer> var2SQLIndex;

    SQLConstantRetriever(ImmutableSubstitution substitution, ImmutableMap<Variable, Integer> var2SQLIndex,
                         TermFactory termFactory, SubstitutionFactory substitutionFactory) {
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.var2SQLIndex = var2SQLIndex;
        this.termTypes = computeDBTypes(substitution.getImmutableMap().values());
    }


    ImmutableSubstitution<DBConstant> retrieveAllConstants(ImmutableList<String> rawValues) {
        Iterator<DBTermType> typesIt = termTypes.iterator();
        return substitutionFactory.getSubstitution(
                var2SQLIndex.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> termFactory.getDBConstant(
                                        rawValues.get(e.getValue()),
                                        typesIt.next()
                                ))));
    }

    private ImmutableList<DBTermType> computeDBTypes(ImmutableCollection<ImmutableTerm> terms) {
        return var2SQLIndex.keySet().stream()
                .map(v -> getRequiredType(v, terms))
                .collect(ImmutableCollectors.toList());
    }

    private DBTermType getRequiredType(Variable var, ImmutableCollection<ImmutableTerm> terms) {
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
        SQLVariableNameException(Variable var, ImmutableCollection<ImmutableTerm> terms) {
            super("Variable " + var + " should be required by exactly one term in " + terms);
        }
    }
}
