package it.unibz.inf.ontop.endpoint.controllers;

import com.google.gson.Gson;
import com.moandjiezana.toml.Toml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@RestController
public class PortalConfigController {

    private final Optional<String> portalConfigFile;
    // LAZY
    private String json;

    @Autowired
    public PortalConfigController(@Value("${portal:#{null}}") Optional<String> portalConfigFile) {
        this.portalConfigFile = portalConfigFile;
        this.json = null;
    }

    @RequestMapping(value = "/ontop/portalConfig")
    @ResponseBody
    public ResponseEntity<String> reformulate() throws FileNotFoundException {
        if ((json == null) && portalConfigFile.isPresent()) {
            Toml toml = new Toml().read(new FileReader(portalConfigFile.get()));
            Gson gson = new Gson();
            json = gson.toJson(toml.toMap());
        } else if (json == null) {
            json = "{}";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, "application/json; charset=UTF-8");
        return new ResponseEntity<>(json, headers, HttpStatus.OK);
    }

}
