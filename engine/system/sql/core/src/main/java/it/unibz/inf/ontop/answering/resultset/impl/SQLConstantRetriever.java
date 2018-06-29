package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;


public class SQLConstantRetriever {

    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final ImmutableMap<Variable, IndexTypePair> variableMap;

    public SQLConstantRetriever(ImmutableSubstitution substitution, ImmutableMap<Variable, Integer> var2SQLIndexMap,
            TermFactory termFactory, SubstitutionFactory substitutionFactory) {
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.variableMap = computeVariableMap(substitution, var2SQLIndexMap);
    }


    protected ImmutableSubstitution<Constant> retrieveAllConstants(ImmutableList<String> rawValues) {
        return substitutionFactory.getSubstitution(
                variableMap.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                e -> e.getKey(),
                                e -> getConstant(e.getValue(), rawValues)
                        )));
    }

    private ValueConstant getConstant(IndexTypePair p, ImmutableList<String> rawValues) {
        return termFactory.getConstantLiteral(rawValues.get(p.getIndex()), p.type);
    }

    private ImmutableMap<Variable,IndexTypePair> computeVariableMap(ImmutableSubstitution substitution,
                                                                    ImmutableMap<Variable, Integer> var2SQLIndexMap) {
        return var2SQLIndexMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> e.getKey(),
                        e -> new IndexTypePair(e.getValue(), getRequiredType(e.getKey(), substitution))
                ));
    }

    private IRI getRequiredType(Variable var, ImmutableSubstitution subst) {
        ImmutableSet<ImmutableTerm> terms = (ImmutableSet<ImmutableTerm>) subst.getImmutableMap().values().stream()
                .filter(t -> (requires((ImmutableTerm) t, var)))
                .collect(ImmutableCollectors.toSet());

        if(terms.size() != 1){
            throw new SQLVariableNameException(var, subst);
        }
        return getRequiredType(var, terms.iterator().next());
    }

    private IRI getRequiredType(Variable var, ImmutableTerm term) {

    }

    private boolean requires(ImmutableTerm t, Variable var) {
        return t.getVariableStream().anyMatch(v -> v.equals(var));
    }

    private class IndexTypePair {
        private final int index;
        private final IRI type;

        public IndexTypePair(int index, IRI type) {
            this.index = index;
            this.type = type;
        }

        public int getIndex() {
            return index;
        }

        public IRI getType() {
            return type;
        }
    }

    public static class SQLVariableNameException extends OntopInternalBugException {
        public SQLVariableNameException (Variable var, ImmutableSubstitution subst) {
            super("Variable "+ var + " should be required by exactly one term in substitution "+ subst);
        }
    }
}
