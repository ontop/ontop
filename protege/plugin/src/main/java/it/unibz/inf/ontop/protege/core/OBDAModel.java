package it.unibz.inf.ontop.protege.core;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.shaded.com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.shaded.com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.answering.connection.pool.JDBCConnectionPool;
import it.unibz.inf.ontop.answering.connection.pool.impl.ConnectionGenerator;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.protege.connection.DataSource;
import it.unibz.inf.ontop.protege.mapping.DuplicateTriplesMapException;
import it.unibz.inf.ontop.protege.mapping.TriplesMapManager;
import it.unibz.inf.ontop.protege.mapping.TriplesMapFactory;
import it.unibz.inf.ontop.protege.query.QueryManager;
import it.unibz.inf.ontop.protege.query.QueryManagerListener;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.protege.editor.core.ui.util.UIUtil;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * An OBDAModel is created for an ontology.
 *
 * OntologySignature and OntologyPrefixManager simply redirect
 * all requests to the ontology.
 *
 * An OBDAModel contains 3 main components:
 *     - DataSource,
 *     - TriplesMapManager,
 *     - QueryManager.
 * Each of these has a respective listener interface,
 * and OBDAModel subscribes to them (simply tags the ontology as dirty).
 *
 * TriplesMapFactoryImpl is a helper class collecting required factories.
 * It is fully mutable and is updated every time a DataSource is changed.
 *
 * DataSource also uses an auxiliary object JDBCConnectionManager,
 * which needs to be disposed (and so is DataSource).
 * When a DataSource is updated, the factories need to be reset.
 *
 * OBDAModelManager listens to the 3 components of each OBDAModel and
 * broadcasts the changes to any of the subscribers.
 * It also has its own listener which is fired when a new active ontology
 * is selected.
 *
 * 1. DataSource has no subscribers: DataSourcePanel listens to the activeOntologyChange
 *    and OntopPropertiesModel simply reflects the properties in DataSource.
 *
 * 2. MappingManagerPanel listens to the activeOntologyChange.
 *    MappingFilteredListModel reflects the list of mappings but
 *    listens to TriplesMapManager to notify the UI.
 *
 * 3. QueryManager  listens to the activeOntologyChange.
 */

public class OBDAModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(OBDAModel.class);

    private static final String OBDA_EXT = ".obda"; // The default OBDA file extension.
    private static final String QUERY_EXT = ".q"; // The default query file extension.
    public static final String PROPERTY_EXT = ".properties"; // The default property file extension.
    public static final String DBPREFS_EXT = ".db_prefs"; // The default db_prefs (currently only user constraints) file extension.
    public static final String DBMETADATA_EXT = ".json"; // The default db-metadata file extension.

    private final OWLOntology ontology;

    // the next 3 components are fully mutable and OBDAModel listens on them
    private final DataSource datasource;
    private final TriplesMapManager triplesMapManager;
    private final QueryManager queryManager;

    // these 2 components are immutable
    private final OntologyPrefixManager prefixManager; // can extend the list of the ontology prefixes!
    private final OntologySignature signature;

    // mutable
    private final TriplesMapFactoryImpl triplesMapFactory;

    private final OBDAModelManager obdaModelManager;

    @Nullable
    private File implicitDBConstraintFile;

    @Nullable
    private File dbMetadataFile;

    private TypeFactory typeFactory;

    OBDAModel(OWLOntology ontology, OBDAModelManager obdaModelManager) {

        this.ontology = ontology;
        this.obdaModelManager = obdaModelManager;

        datasource = new DataSource();
        datasource.addListener(s -> setOntologyDirtyFlag());
        datasource.addListener(s -> resetFactories());

        this.implicitDBConstraintFile = null;
        this.dbMetadataFile = null;

        signature = new OntologySignature(ontology);
        prefixManager = new OntologyPrefixManager(ontology);
        triplesMapFactory = new TriplesMapFactoryImpl(prefixManager);

        triplesMapManager = new TriplesMapManager(triplesMapFactory, prefixManager);
        triplesMapManager.addListener(s -> setOntologyDirtyFlag());

        queryManager = new QueryManager();
        queryManager.addListener(new QueryManagerListener() {
            @Override
            public void inserted(QueryManager.Item item, int indexInParent) {
                setOntologyDirtyFlag();
            }
            @Override
            public void removed(QueryManager.Item item, int indexInParent) {
                setOntologyDirtyFlag();
            }
            @Override
            public void renamed(QueryManager.Item item, int indexInParent) {
                setOntologyDirtyFlag();
            }
            @Override
            public void changed(QueryManager.Item query, int indexInParent) {
                setOntologyDirtyFlag();
            }
        });

        resetFactories();
    }

    void dispose() {
        datasource.dispose();
    }

    private void resetFactories() {
        OntopMappingSQLAllConfiguration configuration = constructBuilder(OntopMappingSQLAllOWLAPIConfiguration.defaultBuilder())
                .build();
        typeFactory = configuration.getTypeFactory();
        triplesMapFactory.reset(configuration);
    }



    public TriplesMapFactory getTriplesMapFactory() { return triplesMapFactory; }

    public DataSource getDataSource() { return datasource; }

    public TriplesMapManager getTriplesMapManager() { return triplesMapManager; }

    public QueryManager getQueryManager() { return queryManager; }

    public OntologySignature getOntologySignature() { return signature; }

    public OntologyPrefixManager getMutablePrefixManager() { return prefixManager; }

    public OBDAModelManager getObdaModelManager() { return obdaModelManager; }


    public void clear() {
        implicitDBConstraintFile = null;
        dbMetadataFile = null;

        datasource.clear();
        triplesMapManager.clear();
        queryManager.clear();
    }

    /**
        should not be called twice in a row without clear() in between
     */

    public void load() throws Exception {
        String owlFileBaseName = getOwlFileBaseName();
        if (owlFileBaseName == null)
            return;

        File obdaFile = fileOf(owlFileBaseName, OBDA_EXT);
        if (obdaFile.exists()) {
            File implicitDBConstraintFile = fileOf(owlFileBaseName, DBPREFS_EXT);
            this.implicitDBConstraintFile = implicitDBConstraintFile.exists()
                    ? implicitDBConstraintFile
                    : null;

            File dbMetadataFile = fileOf(owlFileBaseName, DBMETADATA_EXT);
            this.dbMetadataFile = dbMetadataFile.exists()
                    ? dbMetadataFile
                    : null;

            datasource.load(fileOf(owlFileBaseName, PROPERTY_EXT));
            triplesMapManager.load(obdaFile, this); // can update datasource!
            queryManager.load(fileOf(owlFileBaseName, QUERY_EXT));
            obdaModelManager.getModelManager().setClean(ontology);
        }
        else {
            LOGGER.warn("No OBDA model was loaded because no .obda file exists in the same location as the .owl file");
        }
    }

    public void store() throws IOException {
        String owlFilename = getOwlFileBaseName();
        if (owlFilename == null)
            return;

        try {
            triplesMapManager.store(fileOf(owlFilename, OBDA_EXT));
            queryManager.store(fileOf(owlFilename, QUERY_EXT));
            datasource.store(fileOf(owlFilename, PROPERTY_EXT));
        }
        catch (Exception e) {
            setOntologyDirtyFlag();
            throw e;
        }
    }

    private String getOwlFileBaseName() {
        String owlDocumentIriString = getOwlFileName();
        if (owlDocumentIriString == null)
            return null;
        int i = owlDocumentIriString.lastIndexOf(".");
        return owlDocumentIriString.substring(0, i);
    }

    private String getOwlFileName() {
        IRI documentIRI = ontology.getOWLOntologyManager().getOntologyDocumentIRI(ontology);

        if (!UIUtil.isLocalFile(documentIRI.toURI()))
            return null;

        return documentIRI.toString();
    }

    private static File fileOf(String owlFileName, String extension) {
        return new File(URI.create(owlFileName + extension));
    }

    private void setOntologyDirtyFlag() {
        obdaModelManager.getModelManager().setDirty(ontology);
    }

    public Set<OWLDeclarationAxiom> insertTriplesMaps(ImmutableList<SQLPPTriplesMap> triplesMaps, boolean bootstraped) throws DuplicateTriplesMapException {
        triplesMapManager.addAll(triplesMaps);
        return MappingOntologyUtils_OWLAIP4.extractAndInsertDeclarationAxioms(ontology, triplesMaps, typeFactory, bootstraped);
    }

    public void addAxiomsToOntology(Set<? extends OWLAxiom> axioms) {
        ontology.getOWLOntologyManager().addAxioms(ontology, axioms);
    }


    public SQLPPMapping parseNativeMapping(Reader mappingReader) throws MappingException {
        return constructBuilder(OntopMappingSQLAllOWLAPIConfiguration.defaultBuilder())
                .nativeOntopMappingReader(mappingReader)
                .build()
                .loadProvidedPPMapping();
    }

    public SQLPPMapping parseR2RMLMapping(File file) throws MappingException {
        return constructBuilder(OntopMappingSQLAllOWLAPIConfiguration.defaultBuilder())
                .r2rmlMappingFile(file)
                .build()
                .loadProvidedPPMapping();
    }

    public OntopSQLOWLAPIConfiguration getOntopConfiguration() {
            return constructBuilder(OntopSQLOWLAPIConfiguration.defaultBuilder())
                    .ppMapping(triplesMapManager.generatePPMapping())
                    .ontologyReader(createOntologyReader())
                    .build();
    }

    private Reader createOntologyReader() {
        OWLOntologyMerger ontologyMerger = new OWLOntologyMerger(ontology.getOWLOntologyManager());

        try {
            OWLOntology newOntology = ontologyMerger.createMergedOntology(OWLManager.createOWLOntologyManager(), null);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            newOntology.getOWLOntologyManager().saveOntology(newOntology, outputStream);
            return new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (OWLOntologyStorageException e) {
            // TODO: shall we throw a checked exception?
            throw new RuntimeException(e);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
    }

    private <B extends OntopMappingSQLAllOWLAPIConfiguration.Builder<?>> B constructBuilder(B builder) {

        Properties properties = new Properties();
        properties.put(JDBCConnectionPool.class.getCanonicalName(), ConnectionGenerator.class.getCanonicalName());
        properties.putAll(datasource.asProperties());

        builder.properties(properties);

        Optional.ofNullable(implicitDBConstraintFile)
                .ifPresent(builder::basicImplicitConstraintFile);

        Optional.ofNullable(dbMetadataFile)
                .ifPresent(builder::dbMetadataFile);

        return builder;
    }
    
}

// This is an Exact Copy of the class it.unibz.inf.ontop.spec.mapping.util.MappingOntologyUtils from the ontop/mapping/sql/owlapi module.
// It is needed here because the original class is compiled with OWLAPI v5 and the Protege plugin uses OWLAPI v4. 
// More specifically, the interface OWLDataFactory extends the DeclarationAxiomProvider interface in OWLAPI v5, but not in OWLAPI v4.
// Another tricky difference is that the code here imports the shaded version of the ImmutableSet/ImmutableList classes
class MappingOntologyUtils_OWLAIP4 {

    public static ImmutableSet<OWLDeclarationAxiom> extractAndInsertDeclarationAxioms(OWLOntology ontology,
                                                                                      ImmutableList<? extends SQLPPTriplesMap> tripleMaps,
                                                                                      TypeFactory typeFactory,
                                                                                      boolean bootstrappedMapping) {

        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();

        ImmutableSet<OWLDeclarationAxiom> declarationAxioms =
                tripleMaps.stream()
                        .flatMap(ax -> ax.getTargetAtoms().stream())
                        .map(ta -> extractEntity(ta, dataFactory, typeFactory, bootstrappedMapping))
                        .map(dataFactory::getOWLDeclarationAxiom)
                        .collect(ImmutableCollectors.toSet());

        manager.addAxioms(ontology, declarationAxioms);
        return declarationAxioms;
    }

    private static OWLEntity extractEntity(TargetAtom targetAtom, OWLDataFactory dataFactory,
                                           TypeFactory typeFactory, boolean bootstrappedMapping) {

        ImmutableList<ImmutableTerm> terms = targetAtom.getSubstitutedTerms();
        RDFAtomPredicate predicate = (RDFAtomPredicate) targetAtom.getProjectionAtom().getPredicate();

        Optional<org.apache.commons.rdf.api.IRI> classIRI = predicate.getClassIRI(terms);
        Optional<org.apache.commons.rdf.api.IRI> propertyIRI = predicate.getPropertyIRI(terms);

        if (classIRI.isPresent()) {
            return dataFactory.getOWLClass(IRI.create(classIRI.get().getIRIString()));
        }
        if (!propertyIRI.isPresent()) {
            throw new MinorOntopInternalBugException("No IRI could be extracted from " + targetAtom);
        }

        IRI iri = IRI.create(propertyIRI.get().getIRIString());

        ImmutableTerm objectTerm = predicate.getObject(terms);

        if (objectTerm instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm objectFunctionalTerm = (ImmutableFunctionalTerm) objectTerm;

            TermType termType = objectFunctionalTerm.inferType()
                    .flatMap(TermTypeInference::getTermType)
                    .filter(t -> t.isA(typeFactory.getAbstractRDFTermType()))
                    .orElseThrow(() -> new MinorOntopInternalBugException(
                            "Could not infer the RDF type of " + objectFunctionalTerm));

            return (termType.isA(typeFactory.getAbstractRDFSLiteral()))
                    ? dataFactory.getOWLDataProperty(iri)
                    : dataFactory.getOWLObjectProperty(iri);
        }
        if (bootstrappedMapping) {
            throw new MinorOntopInternalBugException("A functional term was expected for the object: " + objectTerm);
        }
        return dataFactory.getOWLDataProperty(iri);
    }
}

