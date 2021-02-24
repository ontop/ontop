package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.protege.connection.DataSource;
import it.unibz.inf.ontop.protege.mapping.DuplicateTriplesMapException;
import it.unibz.inf.ontop.protege.mapping.TriplesMapCollection;
import it.unibz.inf.ontop.protege.query.QueryManager;
import it.unibz.inf.ontop.protege.query.QueryManagerEventListener;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.util.MappingOntologyUtils;
import org.protege.editor.core.ui.util.UIUtil;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

public class OBDAModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(OBDAModel.class);

    private static final String OBDA_EXT = ".obda"; // The default OBDA file extension.
    private static final String QUERY_EXT = ".q"; // The default query file extension.
    public static final String PROPERTY_EXT = ".properties"; // The default property file extension.

    private final OWLOntology ontology;

    // the next 3 components are fully mutable and OBDAModel listens on them
    private final DataSource datasource;
    private final TriplesMapCollection triplesMapCollection;
    private final QueryManager queryManager;

    // these 2 components are immutable
    private final OntologyPrefixManager prefixManager; // can extend the list of the ontology prefixes!
    private final OntologySignature signature;

    private final OntopConfigurationManager configurationManager;

    private final OBDAModelManager obdaModelManager;

    OBDAModel(OWLOntology ontology, OBDAModelManager obdaModelManager) {

        this.ontology = ontology;
        this.obdaModelManager = obdaModelManager;

        datasource = new DataSource();
        datasource.addListener(s -> setOntologyDirtyFlag());

        configurationManager = new OntopConfigurationManager(obdaModelManager, obdaModelManager.getStandardProperties());

        signature = new OntologySignature(ontology);
        prefixManager = new OntologyPrefixManager(ontology);

        triplesMapCollection = new TriplesMapCollection(prefixManager);
        triplesMapCollection.addMappingsListener(s -> setOntologyDirtyFlag());

        queryManager = new QueryManager();
        queryManager.addListener(new QueryManagerEventListener() {
            @Override
            public void inserted(QueryManager.Item group, int indexInParent) {
                setOntologyDirtyFlag();
            }
            @Override
            public void removed(QueryManager.Item group, int indexInParent) {
                setOntologyDirtyFlag();
            }
            @Override
            public void renamed(QueryManager.Item group, int indexInParent) {
                setOntologyDirtyFlag();
            }
            @Override
            public void changed(QueryManager.Item group, int indexInParent) {
                setOntologyDirtyFlag();
            }
        });
    }

    void dispose() {
        datasource.dispose();
    }

    public OWLOntology getOntology() { return ontology; }

    public DataSource getDataSource() {
        return datasource;
    }

    public TriplesMapCollection getTriplesMapCollection() {
        return triplesMapCollection;
    }

    public QueryManager getQueryManager() {
        return queryManager;
    }

    public OntologySignature getOntologySignature() { return signature; }

    public OntologyPrefixManager getMutablePrefixManager() { return prefixManager; }

    public OBDAModelManager getObdaModelManager() { return obdaModelManager; }

    public OntopConfigurationManager getConfigurationManager() { return configurationManager; }

    public void load() throws Exception {
        String owlFilename = getOwlFilename();
        if (owlFilename == null)
            return;

        File obdaFile = fileOf(owlFilename, OBDA_EXT);
        if (obdaFile.exists()) {
            configurationManager.load(owlFilename);
            datasource.load(fileOf(owlFilename, PROPERTY_EXT));
            triplesMapCollection.load(obdaFile, this); // can update datasource!
            queryManager.load(fileOf(owlFilename, QUERY_EXT));
            obdaModelManager.getModelManager().setClean(ontology);
        }
        else {
            LOGGER.warn("No OBDA model was loaded because no .obda file exists in the same location as the .owl file");
        }
    }

    public void store() throws IOException {
        String owlFilename = getOwlFilename();
        if (owlFilename == null)
            return;

        try {
            triplesMapCollection.store(fileOf(owlFilename, OBDA_EXT));
            queryManager.store(fileOf(owlFilename, QUERY_EXT));
            datasource.store(fileOf(owlFilename, PROPERTY_EXT));
        }
        catch (Exception e) {
            setOntologyDirtyFlag();
            throw e;
        }
    }

    private String getOwlFilename() {
        IRI documentIRI = ontology.getOWLOntologyManager().getOntologyDocumentIRI(ontology);

        if (!UIUtil.isLocalFile(documentIRI.toURI()))
            return null;

        String owlDocumentIriString = documentIRI.toString();
        int i = owlDocumentIriString.lastIndexOf(".");
        return owlDocumentIriString.substring(0, i);
    }

    private static File fileOf(String owlFileName, String extension) {
        return new File(URI.create(owlFileName + extension));
    }

    private void setOntologyDirtyFlag() {
        obdaModelManager.getModelManager().setDirty(ontology);
    }

    public Set<OWLDeclarationAxiom> insertTriplesMaps(ImmutableList<SQLPPTriplesMap> triplesMaps, boolean bootstraped) throws DuplicateTriplesMapException {
        OntopSQLOWLAPIConfiguration configuration = configurationManager.getBasicConfiguration(this);
        TypeFactory typeFactory = configuration.getTypeFactory();

        getTriplesMapCollection().addAll(triplesMaps);
        return MappingOntologyUtils.extractAndInsertDeclarationAxioms(ontology, triplesMaps, typeFactory, bootstraped);
    }

    public void addAxiomsToOntology(Set<? extends OWLAxiom> axioms) {
        ontology.getOWLOntologyManager().addAxioms(ontology, axioms);
    }


    public OntopSQLOWLAPIConfiguration getConfigurationForOntology() {
        return configurationManager.buildOntopSQLOWLAPIConfiguration(ontology);
    }
}
