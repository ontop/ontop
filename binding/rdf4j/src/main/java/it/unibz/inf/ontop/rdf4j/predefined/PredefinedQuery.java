package it.unibz.inf.ontop.rdf4j.predefined;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQuery;
import org.eclipse.rdf4j.query.BindingSet;

import java.util.Map;
import java.util.Optional;

public interface PredefinedQuery<Q extends RDF4JInputQuery> {

    Q getInputQuery();

    String getId();

    Optional<String> getName();
    Optional<String> getDescription();

    boolean shouldReturn404IfEmpty();

    boolean isResultStreamingEnabled();

    void validate(ImmutableMap<String, String> bindings) throws InvalidBindingSetException;

    /**
     * NB: the bindings should have been validated before
     */
    BindingSet convertBindings(ImmutableMap<String, String> bindings);

    /**
     * Removes irrelevant bindings and replace the values of "safe" parameters by the reference ones
     *
     * NB: the bindings should have been validated before
     */
    ImmutableMap<String, String> replaceWithReferenceValues(ImmutableMap<String, String> bindings);
}
