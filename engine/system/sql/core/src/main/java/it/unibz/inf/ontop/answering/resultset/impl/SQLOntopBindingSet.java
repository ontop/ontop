package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public class SQLOntopBindingSet extends AbstractOntopBindingSet implements OntopBindingSet {


    private final ImmutableList<Variable> signature;
    private final ImmutableSubstitution<DBConstant> sqlVar2DBConstant;
    private final ImmutableSubstitution<ImmutableTerm> sparqlVar2Term;


    SQLOntopBindingSet(ImmutableList<Variable> signature,
                       ImmutableMap<String, Integer> bindingName2Index,
                       ImmutableSubstitution<DBConstant> sqlVar2DBConstant,
                       ImmutableSubstitution<ImmutableTerm> sparqlVar2Term) {
        super(bindingName2Index);
        this.signature = signature;
        this.sqlVar2DBConstant = sqlVar2DBConstant;
        this.sparqlVar2Term = sparqlVar2Term;
    }

    @Override
    public ImmutableList<RDFConstant> computeValues() {
        ImmutableSubstitution<ImmutableTerm> composition = sqlVar2DBConstant.composeWith(sparqlVar2Term);
        return signature.stream()
                .map(composition::apply)
                .map(this::evaluate)
                .collect(ImmutableCollectors.toList());
    }

    private RDFConstant evaluate(ImmutableTerm term) {
        ImmutableTerm constant = term.simplify(false);
        if (constant instanceof RDFConstant) {
            return (RDFConstant) constant;
        }
        throw new InvalidTermAsResultException(constant);
    }

    public static class InvalidTermAsResultException extends OntopInternalBugException {
        InvalidTermAsResultException(ImmutableTerm term) {
            super("Term " + term + " does not evaluate to an RDF constant");
        }
    }
}
