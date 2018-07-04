package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public class SQLOntopBindingSet extends AbstractOntopBindingSet implements OntopBindingSet {


    private final SQLConstantRetriever constantRetriever;
    private final ImmutableList<String> rawValues;
    private final ImmutableSubstitution substitution;


    SQLOntopBindingSet(ImmutableList<String> rawValues, ImmutableList<String> signature, SQLConstantRetriever constantRetriever, ImmutableSubstitution substitution) {
        super(signature);
        this.rawValues = rawValues;
        this.constantRetriever = constantRetriever;
        this.substitution = substitution;
    }

    @Override
    public ImmutableList<RDFConstant> computeValues() {
        ImmutableSubstitution<ImmutableFunctionalTerm> composition = substitution.composeWith(
                constantRetriever.retrieveAllConstants(rawValues)
        );
        return composition.getImmutableMap().values().stream()
                .map(this::evaluate)
                .collect(ImmutableCollectors.toList());
    }

    private RDFConstant evaluate(ImmutableFunctionalTerm term) {
        ImmutableTerm constant = term.simplify(false);
        if (constant instanceof RDFConstant) {
            return (RDFConstant) constant;
        }
        throw new InvalidTermAsResultException(term);
    }

    public static class InvalidTermAsResultException extends OntopInternalBugException {
        InvalidTermAsResultException(ImmutableFunctionalTerm term) {
            super("Term " + term + " does not evaluate to an RDF constant");
        }
    }
}
