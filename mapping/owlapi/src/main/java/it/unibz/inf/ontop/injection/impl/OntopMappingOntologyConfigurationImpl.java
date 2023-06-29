package it.unibz.inf.ontop.injection.impl;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.injection.OntopMappingOntologyConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.OntopOntologyOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.impl.OntopMappingOntologyBuilders.OntopMappingOntologyOptions;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.spec.ontology.impl.RDFFactImpl;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.protege.xmlcatalog.owlapi.XMLCatalogIRIMapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<ImmutableSet<RDFFact>> loadInputFacts() throws OBDASpecificationException {
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

    private RDFFormat toRDFFormat(String format) {
        Optional<RDFFormat> guessedFormat = Rio.getParserFormatForFileName(format);
        // the else case should never happen.
        return guessedFormat.orElse(RDFFormat.TURTLE);
    }

    private Optional<ImmutableSet<RDFFact>> loadFactsFromFile() throws FactsException {
        Optional<RDFFormat> format = options.factFormat
                .map(this::toRDFFormat)
                .or(() -> options.factsFile.map(f -> Rio.getParserFormatForFileName(f.getName())).orElse(Optional.empty()));
        if (options.factsFile.isEmpty() && options.factsURL.isEmpty() && options.factsReader.isEmpty())
            return factsFile;
        if(format.isEmpty()) {
            throw new FactsException("No valid fact file format was provided, and a format could not be inferred from the file name.");
        }

        try {
            if (options.factsFile.isPresent()) {
                this.factsFile = Optional.of(loadFactsWithReader(new FileReader(options.factsFile.get()), format.get()));
            } else if (options.factsReader.isPresent()) {
                this.factsFile = Optional.of(loadFactsWithReader(options.factsReader.get(), format.get()));
            } else if (options.factsURL.isPresent()) {
                this.factsFile = Optional.of(loadFactsWithReader(new InputStreamReader(options.factsURL.get().openStream()), format.get()));
            }
        } catch (IOException e) {
            throw new FactsException(e);
        } catch (RDFParseException | RDFHandlerException | IllegalArgumentException e) {
            throw new FactsException("An error occured while parsing the facts file: " + e.getMessage(), e);
        }

        return factsFile;
    }

    /**
     * Reads a set of RDFFacts from a given reader.
     */
    private ImmutableSet<RDFFact> loadFactsWithReader(Reader reader, RDFFormat format) throws FactsException {
        RDFParser parser = Rio.createParser(format);
        Model model = new LinkedHashModel();
        parser.setRDFHandler(new StatementCollector(model));
        try {
            parser.parse(reader, options.factsBaseIRI.orElseGet(() -> String.format("http://%s.example.org/data/", UUID.randomUUID())));
        } catch (IOException e) {
            throw new FactsException(e);
        }

        return model.stream()
                .map(this::statementToFact)
                .collect(ImmutableCollectors.toSet());
    }

    /**
     * Converts RDF4J triples/quadruples of the type `Statement` to Ontop triples/quadruples of the type `RDFFact`.
     */
    private RDFFact statementToFact(Statement statement) {
        var sub = statement.getSubject();
        var factSubject = sub.isBNode() ? getTermFactory().getConstantBNode(sub.stringValue()) : getTermFactory().getConstantIRI(sub.stringValue());
        var pred = statement.getPredicate();
        var factPredicate = getTermFactory().getConstantIRI(pred.stringValue());
        var obj = statement.getObject();
        var factObject = obj.isBNode()
                ? getTermFactory().getConstantBNode(obj.stringValue())
                : obj.isIRI()
                    ? getTermFactory().getConstantIRI(obj.stringValue())
                    : ((Literal)obj).getLanguage().isPresent()
                        ? getTermFactory().getRDFLiteralConstant(((Literal)obj).getLabel(), ((Literal)obj).getLanguage().get())
                        : getTermFactory().getRDFLiteralConstant(((Literal)obj).getLabel(), new SimpleRDF().createIRI(((Literal)obj).getDatatype().stringValue()));
        var ctxt = Optional.ofNullable(statement.getContext());
        var factContext = ctxt.map(c -> c.isBNode() ? getTermFactory().getConstantBNode(c.stringValue()) : getTermFactory().getConstantIRI(c.stringValue()));

        return factContext.map(objectConstant -> RDFFactImpl.createQuadFact(factSubject, factPredicate, factObject, objectConstant))
                .orElseGet(() -> RDFFactImpl.createTripleFact(factSubject, factPredicate, factObject));
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
