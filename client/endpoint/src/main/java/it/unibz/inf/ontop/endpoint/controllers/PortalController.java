package it.unibz.inf.ontop.endpoint.controllers;

import it.unibz.inf.ontop.utils.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@RestController
@ConditionalOnExpression("${disable-portal-page}==false")
public class PortalController {

    private static final Logger log = LoggerFactory.getLogger(SparqlQueryController.class);

    @GetMapping(value = "/")
    public ModelAndView home() {
        Map<String, String> model = new HashMap<>();
        model.put("version", VersionInfo.getVersionInfo().getVersion());
        return new ModelAndView("portal", model);
    }
}