package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import javax.annotation.Nonnull;
import java.util.List;

@JsonDeserialize(as = JsonJoinView.class)
public class JsonJoinView extends JsonBasicOrJoinView {

    @Nonnull
    public final JoinPart joinPart;

    protected JsonJoinView(@JsonProperty("columns") JsonBasicOrJoinView.Columns columns, @JsonProperty("name") List<String> name,
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
    protected ImmutableList<ParentDefinition> extractParentDefinitions(
            DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup) throws MetadataExtractionException {
        QuotedIDFactory quotedIDFactory = dbParameters.getQuotedIDFactory();

        if (joinPart.columnPrefixes.size() != joinPart.relations.size()){
            throw new MetadataExtractionException("Exactly one column prefix must be defined per parent relation");
        }

        if (joinPart.relations.size() < 2)
            throw new MetadataExtractionException("At least two relations are expected");

        ImmutableList.Builder<ParentDefinition> builder = ImmutableList.builder();
        for(int i=0 ; i < joinPart.relations.size(); i++) {
            NamedRelationDefinition parentDefinition = parentCacheMetadataLookup.getRelation(quotedIDFactory.createRelationID(
                    joinPart.relations.get(i).toArray(new String[0])));

            builder.add(new ParentDefinition(parentDefinition, joinPart.columnPrefixes.get(i)));
        }

        return builder.build();
    }

    /**
     * TODO: consider implementing it (using FKs between parents)
     */
    @Override
    public ImmutableList<ImmutableList<Attribute>> getAttributesIncludingParentOnes(Lens lens, ImmutableList<Attribute> parentAttributes) {
        return ImmutableList.of();
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
