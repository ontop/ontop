package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory.IDFactoryType;
import it.unibz.inf.ontop.dbschema.RelationID;
import org.eclipse.jdt.annotation.NonNullByDefault;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@IDFactoryType("DREMIO")
@NonNullByDefault
public class DremioQuotedIDFactory extends SQLStandardQuotedIDFactory {

    @Override
    protected QuotedID createFromString(String s) {
        return createFromString(s, QUOTATION_STRING, i -> i, NO_QUOTATION, false);
    }

    @Override
    public RelationID createRelationID(@Nullable String... components) {
        Objects.requireNonNull(components[components.length - 1]);

        Stream<String> stream = components.length <= 2
                ? Arrays.stream(components).filter(Objects::nonNull)
                : Stream.of(Arrays.stream(components).filter(Objects::nonNull)
                        .limit(components.length - 1) // first (N-1) components are the schema
                        .map(name -> name.replace("\"", "")) // remove quotes in-between
                        .collect(Collectors.joining(".")),
                components[components.length - 1]); // last is the table name

        return new RelationIDImpl(stream
                .map(this::createFromString)
                .collect(ImmutableList.toImmutableList())
                .reverse());
    }

}
