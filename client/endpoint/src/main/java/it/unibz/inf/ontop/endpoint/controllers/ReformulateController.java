package it.unibz.inf.ontop.endpoint.controllers;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepositoryConnection;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;


@RestController
@ConditionalOnExpression("${dev:false}")
public class ReformulateController {

    private OntopVirtualRepository repository;

    @Autowired
    public ReformulateController(OntopVirtualRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/ontop/reformulate")
    @ResponseBody
    public ResponseEntity<String> reformulate(@RequestParam(value = "query") String query)
            throws OntopConnectionException, OntopReformulationException {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        try (OntopRepositoryConnection connection = repository.getConnection()) {
            return new ResponseEntity<>(connection.reformulate(query), headers, HttpStatus.OK);
        }
    }
}
