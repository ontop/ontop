package it.unibz.inf.ontop.endpoint.controllers;

import it.unibz.inf.ontop.endpoint.beans.PredefinedQueryComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@RestController
public class PredefinedQueryController {

    private final PredefinedQueryComponent.PredefinedQueries predefinedQueries;

    @Autowired
    public PredefinedQueryController(PredefinedQueryComponent.PredefinedQueries predefinedQueries) {
        this.predefinedQueries = predefinedQueries;
    }

    // TODO: extract parameters
    @RequestMapping(value = "/predefined")
    @ResponseBody
    public ResponseEntity<String> answer() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, "application/json; charset=UTF-8");
        // TODO: continue
        return new ResponseEntity<>("TODO: continue", headers, HttpStatus.OK);
    }

}
