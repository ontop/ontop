package it.unibz.inf.ontop.spec;

import it.unibz.inf.ontop.spec.impl.OBDASpecInputImpl;
import org.eclipse.rdf4j.model.Model;

import java.io.File;
import java.io.Reader;
import java.util.Optional;

/**
 * TODO: find a better name
 *
 * Files, readers and RDF graphs
 *
 */
public interface OBDASpecInput {

    Optional<File> getFile(String key);

    Optional<Reader> getReader(String key);

    /**
     * TODO: use an abstraction independent of RDF4J
     */
    Optional<Model> getGraph(String key);


    static Builder defaultBuilder() {
        return new OBDASpecInputImpl.BuilderImpl();
    }

    //-----------------
    // Default methods
    //-----------------


    default Optional<File> getMappingFile() {
        return getFile(MAPPING_KEY);
    }

    default Optional<Reader> getMappingReader() {
        return getReader(MAPPING_KEY);
    }

    /**
     * TODO: use an abstraction independent of RDF4J
     */
    default Optional<Model> getMappingGraph() {
        return getGraph(MAPPING_KEY);
    }

    default Optional<File> getConstraintFile() {
        return getFile(CONSTRAINT_KEY);
    }


    interface Builder {

        Builder addFile(String key, File file);
        Builder addReader(String key, Reader reader);
        Builder addGraph(String key, Model graph);

        OBDASpecInput build();

        default Builder addMappingFile(File mappingFile) {
            return addFile(MAPPING_KEY, mappingFile);
        }

        default Builder addMappingReader(Reader mappingReader) {
            return addReader(MAPPING_KEY, mappingReader);
        }

        default Builder addMappingGraph(Model mappingGraph) {
            return addGraph(MAPPING_KEY, mappingGraph);
        }

        default Builder addConstraintFile(File constraintFile) {
            return addFile(CONSTRAINT_KEY, constraintFile);
        }
    }


    //---------------
    // Standard keys
    //---------------

    String MAPPING_KEY = "mapping";
    String CONSTRAINT_KEY = "constraint";



}
