package it.unibz.inf.ontop.endpoint.controllers;

import it.unibz.inf.ontop.endpoint.beans.PortalConfigComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@RestController
public class PortalConfigController {

    private final String jsonString;

    @Autowired
    public PortalConfigController(PortalConfigComponent.PortalConfigSerialization portalConfigSerialization) {
        this.jsonString = portalConfigSerialization.jsonString;
    }

    @RequestMapping(value = "/ontop/portalConfig")
    @ResponseBody
    public ResponseEntity<String> reformulate() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, "application/json; charset=UTF-8");
        return new ResponseEntity<>(jsonString, headers, HttpStatus.OK);
    }

}
