package it.unibz.inf.ontop.protege.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

import javax.annotation.Nonnull;

public class JsonPropertyDescription {
    @Nonnull
    private final String type, description;

    @JsonCreator
    JsonPropertyDescription(@Nonnull @JsonProperty("type") String type,
                            @Nonnull @JsonProperty("description") String description) {
        this.type = type;
        this.description = description;
    }

    @Nonnull
    public String getType() { return type; }

    @Nonnull
    public String getDescription() { return description; }

    public boolean isValidValue(String value) {
        switch (type) {
            case "Boolean":
                return "true".equals(value) || "false".equals(value);
            case "Integer":
                try {
                    Integer.parseInt(value);
                }
                catch (NumberFormatException e) {
                    return false;
                }
                return true;
            case "IRI":
                RDF rdf = new SimpleRDF();
                try {
                    rdf.createIRI(value);
                }
                catch (IllegalArgumentException e) {
                    return false;
                }
                return true;
            case "String":
            case "Enum":
            default:
                return true;
        }
    }

    @Override
    public String toString() { return "Ontop Property: " + type + ", " + description; }
}
