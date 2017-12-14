package it.unibz.inf.ontop.spec.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.spec.TOBDASpecInput;
import org.apache.commons.rdf.api.Graph;

import java.io.File;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TOBDASpecInputImpl implements TOBDASpecInput {

    private final ImmutableMap<String, File> files;
    private final ImmutableMap<String, Reader> readers;
    private final ImmutableMap<String, Graph> graphs;

    private TOBDASpecInputImpl(ImmutableMap<String, File> files,
                              ImmutableMap<String, Reader> readers,
                              ImmutableMap<String, Graph> graphs) {
        this.files = files;
        this.readers = readers;
        this.graphs = graphs;
    }


    @Override
    public Optional<File> getFile(String key) {
        return Optional.ofNullable(files.get(key));
    }

    @Override
    public Optional<Reader> getReader(String key) {
        return Optional.ofNullable(readers.get(key));
    }

    @Override
    public Optional<Graph> getGraph(String key) {
        return Optional.ofNullable(graphs.get(key));
    }

    public Optional<File> getTemporalFile(String key) {
        return Optional.ofNullable(files.get(key));
    }

    public Optional<Reader> getTemporalReader(String key) {
        return Optional.ofNullable(readers.get(key));
    }

    public Optional<Graph> getTemporalGraph(String key) {
        return Optional.ofNullable(graphs.get(key));
    }


    public static class BuilderImpl implements Builder {

        private final Map<String, File> files = new HashMap<>();
        private final Map<String, Reader> readers = new HashMap<>();
        private final Map<String, Graph> graphs = new HashMap<>();

        @Override
        public Builder addFile(String key, File file) {
            files.put(key, file);
            return this;
        }

        @Override
        public Builder addReader(String key, Reader reader) {
            readers.put(key, reader);
            return this;
        }

        @Override
        public Builder addGraph(String key, Graph graph) {
            graphs.put(key, graph);
            return this;
        }

        @Override
        public TOBDASpecInput build() {
            return new TOBDASpecInputImpl(
                    ImmutableMap.copyOf(files),
                    ImmutableMap.copyOf(readers),
                    ImmutableMap.copyOf(graphs));
        }
    }
}
