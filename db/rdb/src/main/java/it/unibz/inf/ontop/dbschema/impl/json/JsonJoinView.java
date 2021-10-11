package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.MetadataLookup;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.IntStream;

@JsonDeserialize(as = JsonJoinView.class)
public class JsonJoinView extends JsonNonSQLView {

    @Nonnull
    public final JoinPart joinPart;

    protected JsonJoinView(@JsonProperty("columns") JsonNonSQLView.Columns columns, @JsonProperty("name") List<String> name,
                           @JsonProperty("join") JoinPart joinPart,
                           @JsonProperty("filterExpression") String filterExpression,
                           @JsonProperty("uniqueConstraints") UniqueConstraints uniqueConstraints,
                           @JsonProperty("otherFunctionalDependencies") OtherFunctionalDependencies otherFunctionalDependencies,
                           @JsonProperty("foreignKeys") ForeignKeys foreignKeys,
                           @JsonProperty("nonNullConstraints") NonNullConstraints nonNullConstraints) {
        super(name, uniqueConstraints, otherFunctionalDependencies, foreignKeys, nonNullConstraints, columns, filterExpression);
        this.joinPart = joinPart;
    }

    @Override
    protected ImmutableMap<NamedRelationDefinition, String> extractParentDefinitions(
            DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup) throws MetadataExtractionException {
        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();

        if (joinPart.columnPrefixes.size() != joinPart.relations.size()){
            throw new MetadataExtractionException("Exactly one column prefix must be defined per parent relation");
        }

        if (joinPart.relations.size() < 2)
            throw new MetadataExtractionException("At least two relations are expected");

        ImmutableMap.Builder<NamedRelationDefinition, String> builder = ImmutableMap.builder();
        for(int i=0 ; i < joinPart.relations.size(); i++) {
            NamedRelationDefinition parentDefinition = parentCacheMetadataLookup.getRelation(quotedIDFactory.createRelationID(
                    joinPart.relations.get(i).toArray(new String[0])));

            builder.put(parentDefinition, joinPart.columnPrefixes.get(i));
        }

        return builder.build();
    }

    protected static class JoinPart extends JsonOpenObject {
        public final List<List<String>> relations;
        public final List<String> columnPrefixes;

        @JsonCreator
        public JoinPart(@JsonProperty("relations") List<List<String>> relations,
                        @JsonProperty("columnPrefixes") List<String> columnPrefixes) {
            this.relations = relations;
            this.columnPrefixes = columnPrefixes;
        }
    }
}
