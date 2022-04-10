package it.unibz.inf.ontop.endpoint.controllers;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/**
 * @author Davide Lanti
 */

@RestController
@ConditionalOnExpression("${enable-download-ontology}==true")
public class OntologyFetcherController {

    private final OntopSQLOWLAPIConfiguration configuration;

    // Davide> The useless "repository" argument is apparently required by Spring. If not provided, then
    //         the instantiation of the configuration object fails. Can some expert of Spring explain me
    //         what is going on here?
    @Autowired
    public OntologyFetcherController(OntopVirtualRepository repository, OntopSQLOWLAPIConfiguration configuration) {
        this.configuration = configuration;
    }

    @RequestMapping(value = "/ontology")
    public ResponseEntity<String> ontology() {
        try {
            Optional<OWLOntology> optionalOntology = configuration.loadInputOntology();
            if (!optionalOntology.isPresent())
                return new ResponseEntity<>("No ontology found", HttpStatus.NOT_FOUND);
            OWLOntology ontology = optionalOntology.get();
            HttpHeaders headers = new HttpHeaders();
            // TODO: fix the content type (using TurtleDocumentFormat for instance). Consider content-negotiation?
            headers.set(CONTENT_TYPE, "text/plain;charset=UTF-8"); // The ontology could be a turtle, RDF/XML, ...
            OutputStream out = new ByteArrayOutputStream();
            ontology.getOWLOntologyManager().saveOntology(ontology, out);
            String output = out.toString();
            out.close();
            return new ResponseEntity<>(output, headers, HttpStatus.OK);
        } catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}