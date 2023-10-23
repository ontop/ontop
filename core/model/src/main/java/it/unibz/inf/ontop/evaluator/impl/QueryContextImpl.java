package it.unibz.inf.ontop.evaluator.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class QueryContextImpl implements QueryContext {

    public static final String USER_HTTP_HEADER_LOWERCASE = "x-user";
    public static final String GROUPS_HTTP_HEADER_LOWERCASE = "x-groups";
    public static final String ROLES_HTTP_HEADER_LOWERCASE = "x-roles";

    @Nullable
    private final String username;
    private final ImmutableSet<String> rolesOrGroups;
    private final byte[] salt;

    @AssistedInject
    protected QueryContextImpl(@Assisted ImmutableMap<String, String> normalizedHttpHeaders,
                               OntopModelSettings settings) {
        if (settings.isAuthorizationEnabled()) {
            var commaSplitter = Splitter.on(",");
            // TODO: validate user name
            username = normalizedHttpHeaders.get(USER_HTTP_HEADER_LOWERCASE);
            rolesOrGroups = Stream.of(GROUPS_HTTP_HEADER_LOWERCASE, ROLES_HTTP_HEADER_LOWERCASE)
                    .flatMap(h -> Optional.ofNullable(normalizedHttpHeaders.get(h)).stream())
                    .flatMap(commaSplitter::splitToStream)
                    .collect(ImmutableCollectors.toSet());
        }
        else {
            username = null;
            rolesOrGroups = ImmutableSet.of();
        }

        SecureRandom random = new SecureRandom();
        this.salt = new byte[20];
        random.nextBytes(salt);
    }

    @Override
    public Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    @Override
    public ImmutableSet<String> getRolesOrGroups() {
        return rolesOrGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryContextImpl that = (QueryContextImpl) o;
        return Objects.equals(username, that.username) && Objects.equals(rolesOrGroups, that.rolesOrGroups);
    }

    @Override
    public byte[] getSalt() {
        return salt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, rolesOrGroups);
    }
}
