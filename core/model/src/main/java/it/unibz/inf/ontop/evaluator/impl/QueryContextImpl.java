package it.unibz.inf.ontop.evaluator.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class QueryContextImpl implements QueryContext {

    public static final String USER_HTTP_HEADER_LOWERCASE = "x-user";
    public static final String GROUPS_HTTP_HEADER_LOWERCASE = "x-groups";
    public static final String ROLES_HTTP_HEADER_LOWERCASE = "x-roles";

    @Nullable
    private final String username;
    private final ImmutableSet<String> roles;
    private final ImmutableSet<String> groups;
    private final UUID salt;

    @AssistedInject
    protected QueryContextImpl(@Assisted ImmutableMap<String, String> normalizedHttpHeaders,
                               OntopModelSettings settings) {
        if (settings.isAuthorizationEnabled()) {
            var commaSplitter = Splitter.on(",");
            // TODO: validate user name
            username = normalizedHttpHeaders.get(USER_HTTP_HEADER_LOWERCASE);
            roles = Optional.ofNullable(normalizedHttpHeaders.get(ROLES_HTTP_HEADER_LOWERCASE)).stream()
                    .flatMap(commaSplitter::splitToStream)
                    .collect(ImmutableCollectors.toSet());

            groups = Optional.ofNullable(normalizedHttpHeaders.get(GROUPS_HTTP_HEADER_LOWERCASE)).stream()
                    .flatMap(commaSplitter::splitToStream)
                    .collect(ImmutableCollectors.toSet());
        }
        else {
            username = null;
            roles = ImmutableSet.of();
            groups = ImmutableSet.of();
        }

        this.salt = UUID.randomUUID();
    }

    @Override
    public Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    @Override
    public ImmutableSet<String> getRolesOrGroups() {
        return Sets.union(roles, groups).immutableCopy();
    }

    @Override
    public ImmutableSet<String> getRoles() {
        return roles;
    }

    @Override
    public ImmutableSet<String> getGroups() {
        return groups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryContextImpl that = (QueryContextImpl) o;
        return Objects.equals(username, that.username) && Objects.equals(roles, that.roles) && Objects.equals(groups, that.groups);
    }

    @Override
    public UUID getSalt() {
        return salt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, roles, groups);
    }
}
