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

    BindingSet validateAndConvertBindings(ImmutableMap<String, String> bindings) throws InvalidBindingSetException;

    /**
     * Remove irrelevant bindings and replace the values of "safe" parameters by the reference ones
     *
     */
    ImmutableMap<String, String> replaceWithReferenceValues(ImmutableMap<String, String> bindings);
}
