package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import java.util.List;

public class JsonLenses extends JsonOpenObject {
    @Nonnull
    public final List<? extends JsonLens> relations;

    @JsonCreator
    public JsonLenses(@JsonProperty("relations") List<JsonLens> relations) {
        this.relations = relations;
    }
}
