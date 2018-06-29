package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.stream.Stream;

public class PostProcessedIriSQLBindingSet extends AbstractOntopBindingSet implements OntopBindingSet {


    private final ImmutableList<String> rawValues;
    private final SQLConstantRetriever constantRetriever;
    private final ImmutableSubstitution substitution;


    public PostProcessedIriSQLBindingSet(ImmutableList<String> rawValues, ImmutableMap<String, Integer> signature,
                                         SQLConstantRetriever constantRetriever, ImmutableSubstitution substitution) {
        super(signature);
        this.rawValues = rawValues;
        this.constantRetriever = constantRetriever;
        this.substitution = substitution;
    }

    public Stream<OntopBinding> computeBindings() {
        ImmutableSubstitution<ImmutableFunctionalTerm> composition = substitution.composeWith(
                constantRetriever.retrieveAllConstants(rawValues)
        );
        return composition
                .getImmutableMap().entrySet().stream()
                .map(e -> new OntopBindingImpl(e.getKey(), evaluate(e.getValue())));
    }

    private RDFConstant evaluate(ImmutableFunctionalTerm term) {
        ImmutableTerm constant = term.evaluate(false);
        if (constant instanceof RDFConstant) {
            return (RDFConstant) constant;
        }
        throw new InvalidTermAsResultException(term);
    }

    public static class InvalidTermAsResultException extends OntopInternalBugException {
        public InvalidTermAsResultException(ImmutableFunctionalTerm term) {
            super("Term " + term + " does not evaluate to an RDF constant");
        }
    }

}
