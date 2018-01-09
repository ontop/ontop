package it.unibz.inf.ontop.si;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.si.impl.RDF4JGraphLoading;
import it.unibz.inf.ontop.si.impl.OWLAPIABoxLoading;
import it.unibz.inf.ontop.si.impl.OntopRDFMaterializerLoading;
import org.eclipse.rdf4j.query.Dataset;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.Properties;

/**
 * Creates an in-memory DB and populates it.
 */
public interface OntopSemanticIndexLoader extends AutoCloseable {


    OntopSQLOWLAPIConfiguration getConfiguration();

    /**
     * Closes its connection to the in-memory DB that was kept
     * just for keeping the DB alive.
     *
     * After calling this method, the loader is not RESPONSIBLE
     * for keeping the DB alive anymore. This responsibility may be delegating
     * to another object: the latter just need to keep a connection alive.
     *
     * An in-memory DB like H2 is dropped when no-one connects to it.
     *
     */
    @Override
    void close();


    //-------------------------------
    // Default construction methods
    //-------------------------------


    /**
     * Loads the ABox of the ontology in an in-memory Semantic Index.
     */
    static OntopSemanticIndexLoader loadOntologyIndividuals(OWLOntology ontology,
                                                            Properties properties) throws SemanticIndexException {
        return OWLAPIABoxLoading.loadOntologyIndividuals(ontology, properties);
    }

    /**
     * Loads the ABox of the ontology in an in-memory Semantic Index.
     */
    static OntopSemanticIndexLoader loadOntologyIndividuals(String ontologyFilePath, Properties properties)
            throws SemanticIndexException {

        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(ontologyFilePath));
            return OWLAPIABoxLoading.loadOntologyIndividuals(ontology, properties);
        }
        catch (OWLOntologyCreationException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }

    /**
     * Loads the graph in an in-memory Semantic Index.
     */
    static OntopSemanticIndexLoader loadRDFGraph(Dataset dataset, Properties properties) throws SemanticIndexException {
        return RDF4JGraphLoading.loadRDFGraph(dataset, properties);
    }

    /**
     * Loads the virtual ABox of an OBDA system in an in-memory Semantic Index.
     */
    static OntopSemanticIndexLoader loadVirtualAbox(OntopSQLOWLAPIConfiguration obdaConfiguration, Properties properties)
            throws SemanticIndexException {
        return OntopRDFMaterializerLoading.loadVirtualAbox(obdaConfiguration, properties);
    }
}
