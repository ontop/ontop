package it.unibz.inf.ontop.spec.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.rdf.api.Graph;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OBDASpecInputImpl implements OBDASpecInput {

    private final ImmutableMap<String, File> files;
    private final ImmutableMap<String, Reader> readers;
    private final ImmutableMap<String, Graph> graphs;

    private OBDASpecInputImpl(ImmutableMap<String, File> files,
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
    public Optional<Reader> getReader(String key) throws FileNotFoundException {
        if (readers.containsKey(key))
            return Optional.of(readers.get(key));
        else if (files.containsKey(key)) {
            // Eliminates BOM from the file (typically comes from Windows files)
            return Optional.of(new InputStreamReader(new BOMInputStream(new FileInputStream(files.get(key)))));
        }
        else
            return Optional.empty();
    }

    @Override
    public Optional<Graph> getGraph(String key) {
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
        public OBDASpecInput build() {
            return new OBDASpecInputImpl(
                    ImmutableMap.copyOf(files),
                    ImmutableMap.copyOf(readers),
                    ImmutableMap.copyOf(graphs));
        }
    }
}
