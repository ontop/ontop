package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import java.util.List;

public class JsonViews extends JsonOpenObject {
    @Nonnull
    public final List<? extends JsonView> relations;

    @JsonCreator
    public JsonViews(@JsonProperty("relations") List<JsonView> relations) {
        this.relations = relations;
    }
}
