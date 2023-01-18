package it.unibz.inf.ontop.rdf4j.predefined;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.query.RDF4JQuery;
import org.eclipse.rdf4j.query.BindingSet;

import java.util.Optional;

public interface PredefinedQuery<Q extends RDF4JQuery<?>> {

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
