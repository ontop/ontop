package it.unibz.inf.ontop.spec;

import it.unibz.inf.ontop.spec.impl.OBDASpecInputImpl;
import org.apache.commons.rdf.api.Graph;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.Optional;

/**
 * TODO: find a better name
 *
 * Files, readers and RDF graphs
 *
 */
public interface OBDASpecInput {

    /**
     * Please consider using getReader()
     */
    @Deprecated
    Optional<File> getFile(String key);

    /**
     * The reader may have been created on the fly (e.g. from a file) or have been constructed before.
     *
     * In any case, the caller becomes responsible for closing the reader.
     */
    Optional<Reader> getReader(String key) throws FileNotFoundException;

    Optional<Graph> getGraph(String key);

    static Builder defaultBuilder() {
        return new OBDASpecInputImpl.BuilderImpl();
    }

    //-----------------
    // Default methods
    //-----------------


    default Optional<Reader> getMappingReader() throws FileNotFoundException {
        return getReader(MAPPING_KEY);
    }

    default Optional<Graph> getMappingGraph() {
        return getGraph(MAPPING_KEY);
    }

    default Optional<File> getConstraintFile() {
        return getFile(CONSTRAINT_KEY);
    }

    default Optional<Reader> getDBMetadataReader() throws FileNotFoundException { return getReader(DBMETADATA_KEY); }

    default Optional<Reader> getOntopViewReader() throws FileNotFoundException { return getReader(ONTOPVIEW_KEY); }


    interface Builder {

        Builder addFile(String key, File file);
        Builder addReader(String key, Reader reader);
        Builder addGraph(String key, Graph graph);

        OBDASpecInput build();

        default Builder addMappingFile(File mappingFile) {
            return addFile(MAPPING_KEY, mappingFile);
        }

        default Builder addMappingReader(Reader mappingReader) {
            return addReader(MAPPING_KEY, mappingReader);
        }

        default Builder addMappingGraph(Graph mappingGraph) {
            return addGraph(MAPPING_KEY, mappingGraph);
        }

        default Builder addConstraintFile(File constraintFile) {
            return addFile(CONSTRAINT_KEY, constraintFile);
        }

        default Builder addDBMetadataFile(File dbMetadataFile) {
            return addFile(DBMETADATA_KEY, dbMetadataFile);
        }

        default Builder addDBMetadataReader(Reader dbMetadataReader) {
            return addReader(DBMETADATA_KEY, dbMetadataReader);
        }

        default Builder addOntopViewFile(File ontopViewFile) {
            return addFile(ONTOPVIEW_KEY, ontopViewFile);
        }

        default Builder addOntopViewReader(Reader ontopViewReader) {
            return addReader(ONTOPVIEW_KEY, ontopViewReader);
        }
    }


    //---------------
    // Standard keys
    //---------------

    String MAPPING_KEY = "mapping";
    String CONSTRAINT_KEY = "constraint";
    String DBMETADATA_KEY = "db-metadata";
    String ONTOPVIEW_KEY = "ontop-view";


}
