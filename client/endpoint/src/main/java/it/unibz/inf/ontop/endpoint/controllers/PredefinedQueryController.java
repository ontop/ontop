package it.unibz.inf.ontop.endpoint.controllers;

import it.unibz.inf.ontop.endpoint.beans.PredefinedQueryComponent;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import org.apache.tomcat.websocket.Util;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriterFactory;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.ACCEPT;
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
    public void answer(@PathVariable("id") String id,
                       @RequestParam Map<String,String> allRequestParams,
                       @RequestHeader(ACCEPT) String accept,
                       HttpServletRequest request, HttpServletResponse response) throws IOException {

        RDFWriterRegistry registry = RDFWriterRegistry.getInstance();

        // TODO: consider select/ask queries

        // JSON-LD by default
        List<MediaType> mediaTypes = MediaType.parseMediaTypes(accept);
        MediaType.sortBySpecificity(mediaTypes);
        Optional<RDFFormat> optionalFormat = mediaTypes.stream()
                .map(m -> m.getType() + "/" + m.getSubtype())
                .map(m -> m.equals("*/*")
                        ? Optional.of(RDFFormat.JSONLD)
                        : registry.getFileFormatForMIMEType(m)
                        .map(Optional::of)
                        .orElseGet(() -> Optional.of(m)
                                .filter(a -> a.contains("json"))
                                .map(a -> org.eclipse.rdf4j.rio.RDFFormat.JSONLD)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(f -> registry.get(f).isPresent())
                .findFirst();

        if (!optionalFormat.isPresent()) {
            // 406: Not acceptable
            response.setStatus(406);
            return;
        }

        RDFFormat format = optionalFormat.get();
        RDFWriterFactory writerFactory = registry.get(format)
                .orElseThrow(() -> new MinorOntopInternalBugException("Should have been checked before"));


        response.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");
        response.setStatus(200);
        ServletOutputStream outputStream = response.getOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        writer.println("TODO: continue for " + id + " " + allRequestParams);
        outputStream.flush();



    }

}
