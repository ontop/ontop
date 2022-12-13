package it.unibz.inf.ontop.injection.impl;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.injection.OntopMappingOntologyConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.OntopOntologyOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.impl.OntopMappingOntologyBuilders.OntopMappingOntologyOptions;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.protege.xmlcatalog.owlapi.XMLCatalogIRIMapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

public class OntopMappingOntologyConfigurationImpl extends OntopMappingConfigurationImpl
        implements OntopMappingOntologyConfiguration, OntopOntologyOWLAPIConfiguration {

    private final OntopMappingOntologyOptions options;
    private Optional<OWLOntology> owlOntology;

    protected OntopMappingOntologyConfigurationImpl(OntopMappingSettings settings, OntopMappingOntologyOptions options) {
        super(settings, options.mappingOptions);
        this.options = options;
        this.owlOntology = Optional.empty();
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
