package it.unibz.inf.ontop.protege.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

public class JsonPropertyDescription {
    @Nonnull
    private final String type, description;

    @JsonCreator
    JsonPropertyDescription(@JsonProperty("type") String type,
                            @JsonProperty("description") String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() { return type; }

    public String getDescription() { return description; }

    @Override
    public String toString() { return "J" + type + ", " + description; }
}
