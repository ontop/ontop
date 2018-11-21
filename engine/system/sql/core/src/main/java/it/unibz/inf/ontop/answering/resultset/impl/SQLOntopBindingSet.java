package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public class SQLOntopBindingSet extends AbstractOntopBindingSet implements OntopBindingSet {


    private final ImmutableSubstitution<DBConstant> SQLVar2DBConstant;
    private final ImmutableSubstitution SPARQLVar2Term;


    SQLOntopBindingSet(ImmutableMap<String, Integer> bindingName2Index,
                       ImmutableSubstitution<DBConstant> SQLVar2DBConstant, ImmutableSubstitution SPARQLVar2Term) {
        super(bindingName2Index);
        this.SQLVar2DBConstant = SQLVar2DBConstant;
        this.SPARQLVar2Term = SPARQLVar2Term;
    }

    @Override
    public ImmutableList<RDFConstant> computeValues() {
        ImmutableSubstitution<ImmutableTerm> composition = SPARQLVar2Term.composeWith(SQLVar2DBConstant);
        return composition.getImmutableMap().values().stream()
                .map(t -> evaluate(t))
                .collect(ImmutableCollectors.toList());
    }

    private RDFConstant evaluate(ImmutableTerm term) {
        ImmutableTerm constant = term.simplify(false);
        if (constant instanceof RDFConstant) {
            return (RDFConstant) constant;
        }
        throw new InvalidTermAsResultException(term);
    }

    public static class InvalidTermAsResultException extends OntopInternalBugException {
        InvalidTermAsResultException(ImmutableTerm term) {
            super("Term " + term + " does not evaluate to an RDF constant");
        }
    }
}
