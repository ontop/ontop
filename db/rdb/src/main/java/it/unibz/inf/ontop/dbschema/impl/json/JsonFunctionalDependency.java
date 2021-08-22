package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.*;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "determinants",
        "dependents"
})
public class JsonFunctionalDependency extends JsonOpenObject {
    public final List<String> determinants, dependents;

    @JsonCreator
    public JsonFunctionalDependency(@JsonProperty("determinants") List<String> determinants,
                                    @JsonProperty("dependents") List<String> dependents) {
        this.determinants = determinants;
        this.dependents = dependents;
    }

    public JsonFunctionalDependency(FunctionalDependency fd) {
        this.determinants = JsonMetadata.serializeAttributeList(fd.getDeterminants().stream());
        this.dependents = JsonMetadata.serializeAttributeList(fd.getDependents().stream());
    }

    public void insert(NamedRelationDefinition relation, QuotedIDFactory idFactory) throws MetadataExtractionException {
        FunctionalDependency.Builder builder = FunctionalDependency.defaultBuilder(relation);
        JsonMetadata.deserializeAttributeList(idFactory, determinants, builder::addDeterminant);
        JsonMetadata.deserializeAttributeList(idFactory, dependents, builder::addDependent);
        builder.build();
    }
}
