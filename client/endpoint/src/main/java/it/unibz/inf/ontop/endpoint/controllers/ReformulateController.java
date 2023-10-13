package it.unibz.inf.ontop.endpoint.controllers;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.endpoint.processor.SparqlQueryExecutor;
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

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;


@RestController
@ConditionalOnExpression("${dev:false}")
public class ReformulateController {

    private final OntopVirtualRepository repository;

    @Autowired
    public ReformulateController(OntopVirtualRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/ontop/reformulate")
    @ResponseBody
    public ResponseEntity<String> reformulate(@RequestParam(value = "query") String query,
                                              HttpServletRequest request)
            throws OntopConnectionException, OntopReformulationException {

        ImmutableMultimap<String, String> inputHeaders = SparqlQueryExecutor.extractHttpHeaders(request);

        try (OntopRepositoryConnection connection = repository.getConnection()) {
            String reformulation = connection.reformulate(query, inputHeaders);

            HttpHeaders returnedHeaders = new HttpHeaders();
            returnedHeaders.set(CONTENT_TYPE, "text/plain; charset=UTF-8");
            return new ResponseEntity<>(reformulation, returnedHeaders, HttpStatus.OK);
        }
    }
}
