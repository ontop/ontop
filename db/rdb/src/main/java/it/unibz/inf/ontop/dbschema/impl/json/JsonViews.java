package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.Nonnull;
import java.util.List;

@JsonPropertyOrder({
        "relations"
})
public class JsonViews extends JsonOpenObject {
    @Nonnull
    public final List<? extends JsonView> relations;

    /**
     * TODO: allow all sorts of JSONView
     */
    @JsonCreator
    public JsonViews(@JsonProperty("relations") List<JsonBasicView> relations) {
        this.relations = relations;
    }
}
