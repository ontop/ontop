package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.checkerframework.checker.units.qual.A;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DremioQuotedIDFactory extends SQLStandardQuotedIDFactory {

    @Override
    protected QuotedID createFromString(@Nonnull String s) {
        Objects.requireNonNull(s);

        if (s.startsWith(QUOTATION_STRING) && s.endsWith(QUOTATION_STRING))
            return new QuotedIDImpl(s.substring(1, s.length() - 1), QUOTATION_STRING, false);

        return new QuotedIDImpl(s, NO_QUOTATION, false);
    }

    @Override
    public RelationID createRelationID(String... components) {
        Objects.requireNonNull(components[components.length - 1]);

        if (components.length <= 2)
            return new RelationIDImpl(Arrays.stream(components)
                    .map(this::createFromString)
                    .collect(ImmutableCollectors.toList())
                    .reverse());

        String schemaId = Arrays.stream(components)
                        .limit(components.length - 1) //First (N-1) components are schema, last is table name
                        .map(name -> name.replace("\"", "")) //Remove quotes in-between
                       .collect(Collectors.joining("."));

        return new RelationIDImpl(Stream.of(components[components.length - 1], schemaId).map(this::createFromString).collect(ImmutableCollectors.toList()));
    }
}
