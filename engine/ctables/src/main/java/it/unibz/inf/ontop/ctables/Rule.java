package it.unibz.inf.ontop.ctables;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Rule implements Comparable<Rule>, Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;

    private final String source;

    private final String target;

    private Rule(final String id, final String source, final String target) {
        this.id = id;
        this.source = source;
        this.target = target;
    }

    @JsonCreator
    public static Rule create(@JsonProperty("id") final String id,
            @JsonProperty("source") final String source,
            @JsonProperty("target") final String target) {

        Objects.requireNonNull(id);
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);

        return new Rule(id, source, target);
    }

    public String getId() {
        return this.id;
    }

    public String getSource() {
        return this.source;
    }

    public String getTarget() {
        return this.target;
    }

    @Override
    public int compareTo(final Rule other) {
        return this.id.compareTo(other.id);
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Rule)) {
            return false;
        }
        final Rule other = (Rule) object;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public String toString(final boolean includeDetails) {
        return !includeDetails ? this.id
                : this.id + ":  " + this.target + " := "
                        + this.source.replaceAll("[ \\t\\n\\r]+", " ");
    }

    @Override
    public String toString() {
        return toString(false);
    }

}
