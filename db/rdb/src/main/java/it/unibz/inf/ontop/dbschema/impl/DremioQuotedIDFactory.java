package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

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

        System.out.println("DEBUG: " + Arrays.toString(components));

        Stream<String> normalisedComponentsStream = components.length <= 2
                ? Stream.of(components)
                : Stream.of(
                Stream.of(components)
                        .limit(components.length - 1) // first (n-1) components are the schema name
                        .map(name -> name.replace("\"", ""))
                        .collect(Collectors.joining(".")),
                components[0]); //  last is the table name

        ImmutableList<QuotedID> r = normalisedComponentsStream
                .map(this::createFromString)
                .collect(ImmutableCollectors.toList());

        System.out.println("DEBUG2: " + r);

        return new RelationIDImpl(r);
    }
}
