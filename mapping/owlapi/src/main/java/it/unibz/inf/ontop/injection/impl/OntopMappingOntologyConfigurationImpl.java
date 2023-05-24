package it.unibz.inf.ontop.injection.impl;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.injection.OntopMappingOntologyConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.OntopOntologyOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.impl.OntopMappingOntologyBuilders.OntopMappingOntologyOptions;
import it.unibz.inf.ontop.spec.fact.impl.FactExtractorWithSaturatedTBox;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.protege.xmlcatalog.owlapi.XMLCatalogIRIMapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class OntopMappingOntologyConfigurationImpl extends OntopMappingConfigurationImpl
        implements OntopMappingOntologyConfiguration, OntopOntologyOWLAPIConfiguration {

    private final OntopMappingOntologyOptions options;
    private Optional<OWLOntology> owlOntology;
    private Optional<ImmutableSet<RDFFact>> factsFile;

    protected OntopMappingOntologyConfigurationImpl(OntopMappingSettings settings, OntopMappingOntologyOptions options) {
        super(settings, options.mappingOptions);
        this.options = options;
        this.owlOntology = Optional.empty();
        this.factsFile = Optional.empty();
    }

    @Override
    public Optional<ImmutableSet<RDFFact>> loadInputFacts() throws OWLOntologyCreationException {
        if (factsFile.isPresent()){
            return factsFile;
        }

        return loadFactsFromFile();
    }

    /**
     * TODO: cache the ontology
     */
    @Override
    public Optional<OWLOntology> loadInputOntology() throws OWLOntologyCreationException {
        if (owlOntology.isPresent()){
            return owlOntology;
        }
        return loadOntologyFromFile();


    }

    private Optional<OWLOntology> loadOntologyFromFile() throws OWLOntologyCreationException {
        /*
         * File
         */
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        if (options.xmlCatalogFile.isPresent()) {
            OWLOntologyIRIMapper iriMapper;
            try {
                iriMapper = new XMLCatalogIRIMapper(new File(options.xmlCatalogFile.get()));
            }
            catch (IOException e) {
                throw new OWLOntologyCreationException(e.getMessage());
            }
            manager.setIRIMappers(ImmutableSet.of(iriMapper));
        }

        if (options.ontologyFile.isPresent()) {
            owlOntology = Optional.of(manager.loadOntologyFromOntologyDocument(options.ontologyFile.get()));
        }
        else if (options.ontologyReader.isPresent()) {
            try {
                InputStream inputStream = new ByteArrayInputStream(
                        CharStreams.toString(options.ontologyReader.get())
                        .getBytes(Charsets.UTF_8));
                owlOntology = Optional.of(manager.loadOntologyFromOntologyDocument(inputStream));
            } catch (IOException e) {
                throw new OWLOntologyCreationException(e.getMessage());
            }

        }

        /*
         * URL
         */
        Optional<URL> optionalURL = options.ontologyURL;
        if (optionalURL.isPresent()) {
            try (InputStream is = optionalURL.get().openStream()) {
                owlOntology = Optional.of(manager.loadOntologyFromOntologyDocument(is));
            }
            catch (MalformedURLException e ) {
                throw new OWLOntologyCreationException("Invalid URI: " + e.getMessage());
            }
            catch (IOException e) {
                throw new OWLOntologyCreationException(e.getMessage());
            }
        }

        return owlOntology;
    }

    private ImmutableSet<RDFFact> axiomsToFacts(InputStream file, OWLOntologyManager manager) throws OWLOntologyCreationException {
        var factOntology = manager.loadOntologyFromOntologyDocument(file);
        OWLAPITranslatorOWL2QL translator = getInjector().getInstance(OWLAPITranslatorOWL2QL.class);
        var translated = translator.translateAndClassify(factOntology);
        var extractor = getInjector().getInstance(FactExtractorWithSaturatedTBox.class);
        return extractor.extractAndSelect(Optional.of(translated), true);
    }

    private Optional<ImmutableSet<RDFFact>> loadFactsFromFile() throws OWLOntologyCreationException {
        /*
         * File
         */
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        //var e = new FactExtractorWithSaturatedTBox(getTermFactory(), this);

        if (options.factsFile.isPresent()) {
            try {
                factsFile = Optional.of(axiomsToFacts(new FileInputStream(options.factsFile.get()), manager));
            } catch (IOException e) {
                throw new OWLOntologyCreationException(e.getMessage());
            }
        }
        else if (options.factsReader.isPresent()) {
            try {
                InputStream inputStream = new ByteArrayInputStream(
                        CharStreams.toString(options.factsReader.get())
                                .getBytes(Charsets.UTF_8));
                factsFile = Optional.of(axiomsToFacts(inputStream, manager));
            } catch (IOException e) {
                throw new OWLOntologyCreationException(e.getMessage());
            }

        }

        /*
         * URL
         */
        Optional<URL> optionalURL = options.factsURL;
        if (optionalURL.isPresent()) {
            try (InputStream is = optionalURL.get().openStream()) {
                factsFile = Optional.of(axiomsToFacts(is, manager));
            }
            catch (MalformedURLException e ) {
                throw new OWLOntologyCreationException("Invalid URI: " + e.getMessage());
            }
            catch (IOException e) {
                throw new OWLOntologyCreationException(e.getMessage());
            }
        }

        return factsFile;
    }

    Optional<Ontology> loadOntology() throws OntologyException {
        OWLAPITranslatorOWL2QL translator = getInjector().getInstance(OWLAPITranslatorOWL2QL.class);
        try {
            return loadInputOntology()
                    .map(translator::translateAndClassify);
        }
        catch (OWLOntologyCreationException e) {
            throw new OntologyException(e.getMessage());
        }
    }

}
