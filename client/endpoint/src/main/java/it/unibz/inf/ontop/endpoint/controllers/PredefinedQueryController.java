package it.unibz.inf.ontop.endpoint.controllers;

import it.unibz.inf.ontop.endpoint.beans.PredefinedQueryComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@RestController
public class PredefinedQueryController {

    private final PredefinedQueryComponent.PredefinedQueries predefinedQueries;

    @Autowired
    public PredefinedQueryController(PredefinedQueryComponent.PredefinedQueries predefinedQueries) {
        this.predefinedQueries = predefinedQueries;
    }

    @RequestMapping(value = "/predefined/{id}")
    @ResponseBody
    public ResponseEntity<String> answer(@PathVariable("id") String id,
                                         @RequestParam Map<String,String> allRequestParams) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, "application/json; charset=UTF-8");
        // TODO: continue
        return new ResponseEntity<>("TODO: continue for " + id + " " + allRequestParams, headers, HttpStatus.OK);
    }

}
