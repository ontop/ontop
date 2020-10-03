package it.unibz.inf.ontop.rdf4j.predefined.parsing.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.moandjiezana.toml.Toml;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructQuery;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedGraphQuery;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedQueries;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedTupleQuery;
import it.unibz.inf.ontop.rdf4j.predefined.impl.PredefinedGraphQueryImpl;
import it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryConfigEntry;
import it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryConfigException;
import it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryParser;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class PredefinedQueryParserImpl implements PredefinedQueryParser {

    @Override
    public PredefinedQueries parse(@Nonnull Reader configReader, @Nonnull Reader queryReader) throws PredefinedQueryConfigException {
        return parse(configReader, queryReader, null);
    }

    /**
     * TODO: handle exceptions from TOML and Jackson
     */
    @Override
    public PredefinedQueries parse(@Nonnull Reader configReader, @Nonnull Reader queryReader, @Nullable Reader contextReader)
            throws PredefinedQueryConfigException {
        try {
            Toml toml = new Toml().read(queryReader);
            ImmutableMap<String, String> queryMap = toml.entrySet().stream()
                    .filter(e -> e.getValue() instanceof Map)
                    .flatMap(e -> Optional.ofNullable(((Map) e.getValue()).get("query"))
                            .filter(v -> v instanceof String)
                            .map(v -> (String) v)
                            .map(v -> Maps.immutableEntry(e.getKey(), v))
                            .map(Stream::of)
                            .orElseGet(Stream::empty))
                    .collect(ImmutableCollectors.toMap());

            ObjectMapper mapper = new ObjectMapper();
            ImmutableMap<String, PredefinedQueryConfigEntry> configEntries = mapper.readValue(configReader, Config.class)
                    .getQueries();

            Sets.SetView<String> missingQueries = Sets.difference(configEntries.keySet(), queryMap.keySet());
            if (!missingQueries.isEmpty())
                throw new PredefinedQueryConfigException("Missing query entries for " + missingQueries);

            // TODO: consider contexts

            ImmutableMap<String, PredefinedGraphQuery> graphQueries = configEntries.entrySet().stream()
                    .filter(e -> e.getValue().getQueryType().equals(Query.QueryType.GRAPH))
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            // TODO: consider contexts
                            e -> createPredefinedGraphQuery(e.getKey(), e.getValue(),
                                    queryMap.get(e.getKey()))));

            ImmutableMap<String, PredefinedTupleQuery> tupleQueries = configEntries.entrySet().stream()
                    .filter(e -> e.getValue().getQueryType().equals(Query.QueryType.TUPLE))
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> createPredefinedTupleQuery(e.getKey(), e.getValue(),
                                    queryMap.get(e.getKey()))));

            return PredefinedQueries.defaultPredefinedQueries(tupleQueries, graphQueries);

        } catch (IOException e) {
            throw new PredefinedQueryConfigException(e);
        }
    }

    private PredefinedGraphQuery createPredefinedGraphQuery(String id, PredefinedQueryConfigEntry queryConfigEntry,
                                                            String queryString) {
        throw new RuntimeException("TODO: continue with graph queries");
        // return new PredefinedGraphQueryImpl(id, queryString, constructQuery, queryConfigEntry);
    }

    private PredefinedTupleQuery createPredefinedTupleQuery(String id, PredefinedQueryConfigEntry queryConfigEntry,
                                                            String queryString) {
        throw new RuntimeException("TODO: support tuple queries");
    }

    private static class Config {
        private final ImmutableMap<String, PredefinedQueryConfigEntry> queries;

        @JsonCreator
        public Config(@JsonProperty("queries") Map<String, PredefinedQueryConfigEntry> queries) {
            this.queries = ImmutableMap.copyOf(queries);
        }

        public ImmutableMap<String, PredefinedQueryConfigEntry> getQueries() {
            return queries;
        }
    }
}
