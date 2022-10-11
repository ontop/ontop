package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;

import javax.annotation.Nonnull;
import java.util.Objects;

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
        ImmutableList.Builder<QuotedID> builder = ImmutableList.builder();

        builder.add(createFromString(components[components.length - 1]));

        StringBuilder current = new StringBuilder();
        for (int i = components.length - 2; i >= 0; i--) {
            if (components[i] == null)
                continue;
            if(current.length() > 0)
                current.insert(0, ".");
            current.insert(0, components[i].replace("\"", ""));
        }
        builder.add(createFromString(current.toString()));

        return new RelationIDImpl(builder.build());
    }
}
