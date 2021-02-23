package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.TargetQueryParserFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.protege.connection.DataSource;
import it.unibz.inf.ontop.protege.mapping.DuplicateTriplesMapException;
import it.unibz.inf.ontop.protege.mapping.TriplesMapCollection;
import it.unibz.inf.ontop.protege.query.QueryManager;
import it.unibz.inf.ontop.protege.query.QueryManagerEventListener;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.util.MappingOntologyUtils;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import org.apache.commons.rdf.api.RDF;
import org.protege.editor.core.ui.util.UIUtil;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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
    private final OntologySignature signature = new OntologySignature();
    // mutable
    private final QueryManager queryManager = new QueryManager();

    private final OntopConfigurationManager configurationManager;


    private final OBDAModelManager obdaModelManager;

    private final SQLPPMappingFactory ppMappingFactory;
    private final TypeFactory typeFactory;
    private final TermFactory termFactory;
    private final TargetAtomFactory targetAtomFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TargetQueryParserFactory targetQueryParserFactory;
    private final SQLPPSourceQueryFactory sourceQueryFactory;
    private final RDF rdfFactory;

    OBDAModel(OWLOntology ontology,
              OBDAModelManager obdaModelManager) {

        this.ontology = ontology;
        this.obdaModelManager = obdaModelManager;

        /*
         * TODO: avoid using Default injector
         */
        Injector defaultInjector = OntopMappingSQLAllConfiguration.defaultBuilder()
                .jdbcDriver("")
                .jdbcUrl("")
                .jdbcUser("")
                .jdbcPassword("")
                .build().getInjector();

        rdfFactory = defaultInjector.getInstance(RDF.class);
        typeFactory = defaultInjector.getInstance(TypeFactory.class);
        ppMappingFactory = defaultInjector.getInstance(SQLPPMappingFactory.class);
        termFactory = defaultInjector.getInstance(TermFactory.class);
        targetAtomFactory = defaultInjector.getInstance(TargetAtomFactory.class);
        substitutionFactory = defaultInjector.getInstance(SubstitutionFactory.class);
        targetQueryParserFactory = defaultInjector.getInstance(TargetQueryParserFactory.class);
        sourceQueryFactory = defaultInjector.getInstance(SQLPPSourceQueryFactory.class);

        configurationManager = new OntopConfigurationManager(obdaModelManager);

        triplesMapCollection = new TriplesMapCollection(ontology, ppMappingFactory, termFactory,
                targetAtomFactory, substitutionFactory, targetQueryParserFactory, sourceQueryFactory);

        signature.reset(ontology);

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

    public RDF getRdfFactory() { return rdfFactory; }

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
        getTriplesMapCollection().addAll(triplesMaps);

        return MappingOntologyUtils.extractAndInsertDeclarationAxioms(
                ontology,
                triplesMaps,
                typeFactory,
                bootstraped);
    }

    public void addAxiomsToOntology(Set<? extends OWLAxiom> axioms) {
        obdaModelManager.getOntologyManager().addAxioms(ontology, axioms);
    }


    public OntopSQLOWLAPIConfiguration getConfigurationForOntology() {
        return configurationManager.buildOntopSQLOWLAPIConfiguration(ontology);
    }

    public SQLPPMapping parseR2RML(File file) throws MappingException {
        OntopMappingSQLAllConfiguration configuration = configurationManager.buildR2RMLConfiguration(datasource, file);
        return configuration.loadProvidedPPMapping();
    }

    public SQLPPMapping parseOBDA(String mapping) throws MappingIOException, InvalidMappingException {
        Reader mappingReader = new StringReader(mapping);
        SQLMappingParser mappingParser = configurationManager.getSQLMappingParser(datasource, mappingReader);
        return mappingParser.parse(mappingReader);
    }


}
