package it.unibz.inf.ontop.ctables.spec;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

public final class Rule implements Comparable<Rule>, Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;

    private final String target;

    private final String source;

    @Nullable
    private transient Select sourceParsed;

    private final Set<String> uses;

    @Nullable
    private transient Set<String> dependencies;

    private Rule(final String id, final String target, final String source,
            final Set<String> uses) {
        this.id = id;
        this.target = target;
        this.source = source;
        this.uses = uses;
    }

    @JsonCreator
    public static Rule create(@JsonProperty("id") final String id,
            @JsonProperty("target") final String target,
            @JsonProperty("source") final String source,
            @JsonProperty("uses") @Nullable final Iterable<String> uses) {

        Objects.requireNonNull(id);
        Objects.requireNonNull(target);
        Objects.requireNonNull(source);

        return new Rule(id, target, source,
                uses == null ? ImmutableSet.of() : ImmutableSet.copyOf(uses));
    }

    public String getId() {
        return this.id;
    }

    public String getTarget() {
        return this.target;
    }

    public String getSource() {
        return this.source;
    }

    public Select getSourceParsed() {
        if (this.sourceParsed == null) {
            try {
                this.sourceParsed = (Select) CCJSqlParserUtil.parse(this.source,
                        parser -> parser.withSquareBracketQuotation(true));
            } catch (final JSQLParserException ex) {
                throw new RuntimeException(ex);
            }
        }
        return this.sourceParsed;
    }

    public Set<String> getUses() {
        return this.uses;
    }

    public Set<String> getDependencies() {
        if (this.dependencies == null) {
            final Set<String> deps = Sets.newLinkedHashSet();
            deps.addAll(new TablesNamesFinder().getTableList(getSourceParsed()));
            deps.addAll(this.uses);
            this.dependencies = ImmutableSet.copyOf(deps);
        }
        return this.dependencies;
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
