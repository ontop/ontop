package it.unibz.inf.ontop.materialization;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.substitution.Substitution;

import java.util.stream.Stream;

public interface RDFFactTemplates {

    /**
     * Each list is an ordered list of either 3 or 4 variables, representing the subject, predicate, object, and graph
     */
    ImmutableCollection<ImmutableList<Variable>> getTriplesOrQuadsVariables();

    RDFFactTemplates apply(Substitution<Variable> substitution);

    RDFFactTemplates merge(RDFFactTemplates templates);

    Stream<RDFFact> convert(Substitution<ImmutableTerm> tupleSubstitution);

    /**
     * Each list contains variables in a substitution that correspond to the same ImmutableTerm
     */
    RDFFactTemplates compress(ImmutableSet<ImmutableCollection<Variable>> equivalentVariables);

    ImmutableSet<Variable> getVariables();
}
