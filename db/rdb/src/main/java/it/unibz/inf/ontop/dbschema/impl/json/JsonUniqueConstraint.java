package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.*;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "determinants",
        "isPrimaryKey"
})
public class JsonUniqueConstraint extends JsonOpenObject {
    public final String name;
    public final List<String> determinants;
    public final Boolean isPrimaryKey;

    @JsonCreator
    public JsonUniqueConstraint(@JsonProperty("name") String name,
                                @JsonProperty("determinants") List<String> determinants,
                                @JsonProperty("isPrimaryKey") Boolean isPrimaryKey) {
        this.name = name;
        this.determinants = determinants;
        this.isPrimaryKey = isPrimaryKey;
    }

    public JsonUniqueConstraint(UniqueConstraint uc) {
        this.name = uc.getName();
        this.isPrimaryKey = uc.isPrimaryKey();
        this.determinants = JsonMetadata.serializeAttributeList(uc.getAttributes().stream());
    }

    public void insert(NamedRelationDefinition relation, QuotedIDFactory idFactory) throws MetadataExtractionException {
            FunctionalDependency.Builder builder = UniqueConstraint.builder(relation, name);

            JsonMetadata.deserializeAttributeList(idFactory, determinants, builder::addDeterminant);
            builder.build();
    }
}
