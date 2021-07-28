package it.unibz.inf.ontop.ctables.spec;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public final class Ruleset implements Serializable {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private static final long serialVersionUID = 1L;

    private final List<Rule> rules;

    private transient Map<String, Rule> rulesById;

    private transient List<String> targets;

    private Ruleset(final List<Rule> rules, @Nullable final Map<String, Rule> rulesById) {
        this.rules = rules;
        this.rulesById = rulesById;
    }

    @JsonCreator
    public static Ruleset create(@JsonProperty("rules") final Iterable<Rule> rules) {
        final List<Rule> ruleList = ImmutableList.copyOf(rules);
        final Map<String, Rule> rulesById = ImmutableMap
                .copyOf(ruleList.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)));
        return new Ruleset(ruleList, rulesById);
    }

    public static Ruleset create(final URL url) throws IOException {
        try (InputStream in = url.openStream()) {
            return MAPPER.readValue(in, Ruleset.class);
        }
    }

    public static Ruleset create(final Reader reader) throws IOException {
        return MAPPER.readValue(reader, Ruleset.class);
    }

    public List<Rule> getRules() {
        return this.rules;
    }

    @Nullable
    public Rule getRule(final String id) {
        if (this.rulesById == null) {
            this.rulesById = ImmutableMap
                    .copyOf(this.rules.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)));
        }
        return this.rulesById.get(id);
    }

    public List<String> getTargets() {
        if (this.targets == null) {
            this.targets = ImmutableList.copyOf(this.rules.stream().map(r -> r.getTarget())
                    .sorted().distinct().collect(Collectors.toList()));
        }
        return this.targets;
    }

    public String toString(final boolean includeDetails) {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.rules.size()).append(" rules");
        if (includeDetails) {
            sb.append(":");
            for (final Rule rule : this.rules) {
                sb.append("\n* ").append(rule.toString(true));
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.toString(false);
    }

}
