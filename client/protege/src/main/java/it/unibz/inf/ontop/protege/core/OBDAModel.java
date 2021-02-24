package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.protege.connection.DataSource;
import it.unibz.inf.ontop.protege.mapping.DuplicateTriplesMapException;
import it.unibz.inf.ontop.protege.mapping.TriplesMapCollection;
import it.unibz.inf.ontop.protege.query.QueryManager;
import it.unibz.inf.ontop.protege.query.QueryManagerEventListener;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.util.MappingOntologyUtils;
import org.apache.commons.rdf.api.RDF;
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

    // mutable
    private final DataSource datasource = new DataSource();
    // mutable
    private final TriplesMapCollection triplesMapCollection;
    // Mutable: the ontology inside is replaced
    private final OntologySignature signature;
    // mutable
    private final QueryManager queryManager = new QueryManager();

    private final OntopConfigurationManager configurationManager;

    private final OBDAModelManager obdaModelManager;

    OBDAModel(OWLOntology ontology, OBDAModelManager obdaModelManager) {

        this.ontology = ontology;
        this.obdaModelManager = obdaModelManager;

        configurationManager = new OntopConfigurationManager(obdaModelManager, obdaModelManager.getStandardProperties());

        triplesMapCollection = new TriplesMapCollection(ontology);

        signature = new OntologySignature(ontology);

        triplesMapCollection.addMappingsListener(s -> triggerOntologyChanged());

        queryManager.addListener(new QueryManagerEventListener() {
            @Override
            public void inserted(QueryManager.Item group, int indexInParent) {
                triggerOntologyChanged();
            }
            @Override
            public void removed(QueryManager.Item group, int indexInParent) {
                triggerOntologyChanged();
            }
            @Override
            public void renamed(QueryManager.Item group, int indexInParent) {
                triggerOntologyChanged();
            }
            @Override
            public void changed(QueryManager.Item group, int indexInParent) {
                triggerOntologyChanged();
            }
        });

        datasource.addListener(s -> triggerOntologyChanged());
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

    public MutablePrefixManager getMutablePrefixManager() { return triplesMapCollection.getMutablePrefixManager(); }

    public OBDAModelManager getObdaModelManager() { return obdaModelManager; }

    public OntopConfigurationManager getConfigurationManager() { return configurationManager; }

    public void load() throws Exception {
        String owlName = getActiveOntologyOwlFilename();
        if (owlName == null)
            return;

        File obdaFile = new File(URI.create(owlName + OBDA_EXT));
        if (obdaFile.exists()) {
            configurationManager.load(owlName);
            datasource.load(new File(URI.create(owlName + PROPERTY_EXT)));
            triplesMapCollection.load(obdaFile, this); // can update datasource!
            queryManager.load(new File(URI.create(owlName + QUERY_EXT)));
            obdaModelManager.getModelManager().setClean(ontology);
        }
        else {
            LOGGER.warn("No OBDA model was loaded because no .obda file exists in the same location as the .owl file");
        }
    }

    public void store() throws IOException {
        String owlName = getActiveOntologyOwlFilename();
        if (owlName == null)
            return;

        try {
            triplesMapCollection.store(new File(URI.create(owlName + OBDA_EXT)));
            queryManager.store(new File(URI.create(owlName + QUERY_EXT)));
            datasource.store(new File(URI.create(owlName + PROPERTY_EXT)));
        }
        catch (Exception e) {
            triggerOntologyChanged();
            throw e;
        }
    }

    private String getActiveOntologyOwlFilename() {
        IRI documentIRI = obdaModelManager.getOntologyManager().getOntologyDocumentIRI(ontology);

        if (!UIUtil.isLocalFile(documentIRI.toURI()))
            return null;

        String owlDocumentIriString = documentIRI.toString();
        int i = owlDocumentIriString.lastIndexOf(".");
        return owlDocumentIriString.substring(0, i);
    }



    private void triggerOntologyChanged() {
        obdaModelManager.getModelManager().setDirty(ontology);
    }

    public Set<OWLDeclarationAxiom> insertTriplesMaps(ImmutableList<SQLPPTriplesMap> triplesMaps, boolean bootstraped) throws DuplicateTriplesMapException {
        OntopSQLOWLAPIConfiguration configuration = configurationManager.getBasicConfiguration(this);
        TypeFactory typeFactory = configuration.getTypeFactory();

        getTriplesMapCollection().addAll(triplesMaps);
        return MappingOntologyUtils.extractAndInsertDeclarationAxioms(ontology, triplesMaps, typeFactory, bootstraped);
    }

    public void addAxiomsToOntology(Set<? extends OWLAxiom> axioms) {
        obdaModelManager.getOntologyManager().addAxioms(ontology, axioms);
    }


    public OntopSQLOWLAPIConfiguration getConfigurationForOntology() {
        return configurationManager.buildOntopSQLOWLAPIConfiguration(ontology);
    }
}
