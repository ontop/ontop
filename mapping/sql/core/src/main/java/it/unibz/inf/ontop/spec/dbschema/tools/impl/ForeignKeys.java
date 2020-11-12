package it.unibz.inf.ontop.spec.dbschema.tools.impl;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "from", "to" })
public class ForeignKeys {
    @JsonProperty("name")
    private String name;
    @JsonProperty("from")
    private List<FromToObject> from;
    @JsonProperty("to")
    private List<FromToObject> to;

    /*@JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();*/

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("from")
    public List<FromToObject> getFrom() { return from; }

    @JsonProperty("from")
    public void setFrom(List<FromToObject> from) { this.from = from; }

    @JsonProperty("to")
    public List<FromToObject> getTo() {
        return to;
    }

    @JsonProperty("to")
    public void setTo(List<FromToObject> to) {
        this.to = to;
    }

    /*@JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }*/
}
