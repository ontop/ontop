package it.unibz.inf.ontop.spec;

import it.unibz.inf.ontop.spec.impl.TOBDASpecInputImpl;
import org.apache.commons.rdf.api.Graph;

import java.io.File;
import java.io.Reader;
import java.util.Optional;

public interface TOBDASpecInput extends OBDASpecInput {

    static Builder defaultBuilder() {
        return (Builder) new TOBDASpecInputImpl.BuilderImpl();
    }

    default Optional<File> getTemporalMappingFile() {
        return getFile(TEMPORAL_MAPPING_KEY);
    }

    interface Builder {

        TOBDASpecInput.Builder addFile(String key, File file);
        TOBDASpecInput.Builder addReader(String key, Reader reader);
        TOBDASpecInput.Builder addGraph(String key, Graph graph);

        TOBDASpecInput build();

        default TOBDASpecInput.Builder addMappingFile(File mappingFile) {
            return addFile(MAPPING_KEY, mappingFile);
        }

        default TOBDASpecInput.Builder addTemporalMappingFile(File temporalMappingFile) {
            return addFile(TEMPORAL_MAPPING_KEY, temporalMappingFile);
        }

        default TOBDASpecInput.Builder addMappingReader(Reader mappingReader) {
            return addReader(MAPPING_KEY, mappingReader);
        }

        default TOBDASpecInput.Builder addMappingGraph(Graph mappingGraph) {
            return addGraph(MAPPING_KEY, mappingGraph);
        }

        default TOBDASpecInput.Builder addConstraintFile(File constraintFile) {
            return addFile(CONSTRAINT_KEY, constraintFile);
        }
    }

    String TEMPORAL_MAPPING_KEY = "temporalMapping";
}
