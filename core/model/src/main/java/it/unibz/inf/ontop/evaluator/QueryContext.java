package it.unibz.inf.ontop.evaluator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;
import java.util.UUID;

/**
 * Contextual information that is provided on a per-query basis.
 * The QueryContext is typically constructed out of the HTTP headers.
 */
public interface QueryContext {

    Optional<String> getUsername();

    ImmutableSet<String> getRolesOrGroups();

    ImmutableSet<String> getRoles();

    ImmutableSet<String> getGroups();

    UUID getSalt();

    interface Factory {
        QueryContext create(ImmutableMap<String, String> normalizedHttpHeaders);
    }

}
