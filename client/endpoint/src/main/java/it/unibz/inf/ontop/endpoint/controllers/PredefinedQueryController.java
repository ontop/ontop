package it.unibz.inf.ontop.endpoint.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.rdf4j.predefined.LateEvaluationOrConversionException;
import it.unibz.inf.ontop.rdf4j.predefined.OntopRDF4JPredefinedQueryEngine;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@RestController
public class PredefinedQueryController {

    private final OntopRDF4JPredefinedQueryEngine engine;

    @Autowired
    public PredefinedQueryController(OntopRDF4JPredefinedQueryEngine engine) {
        this.engine = engine;
    }

    @RequestMapping(value = "/predefined/{id}")
    @ResponseBody
    public void answer(@PathVariable("id") String id,
                       @RequestParam Map<String,String> allRequestParams,
                       @RequestHeader(ACCEPT) String accept,
                       HttpServletRequest request, HttpServletResponse response) throws IOException, LateEvaluationOrConversionException {

        response.setCharacterEncoding("UTF-8");

        List<MediaType> mediaTypes = MediaType.parseMediaTypes(accept);
        MediaType.sortBySpecificity(mediaTypes);
        ImmutableList<String> acceptMediaTypes = mediaTypes.stream()
                .map(m -> m.getType() + "/" + m.getSubtype())
                .collect(ImmutableCollectors.toList());

        ImmutableMultimap<String, String> httpHeaders = Collections.list(request.getHeaderNames()).stream()
                .flatMap(k -> Collections.list(request.getHeaders(k)).stream()
                        .map(v -> Maps.immutableEntry(k, v)))
                .collect(ImmutableCollectors.toMultimap());

        ImmutableMap<String, String> requestParams = ImmutableMap.copyOf(allRequestParams);
        ServletOutputStream responseOutputStream = response.getOutputStream();

        if (engine.shouldStream(id)) {

            engine.evaluate(id, requestParams, acceptMediaTypes, httpHeaders,
                    response::setStatus, response::setHeader, responseOutputStream);
        }
        else {
            ByteArrayOutputStream payloadOutputStream = new ByteArrayOutputStream();
            engine.evaluate(id, requestParams, acceptMediaTypes, httpHeaders,
                    response::setStatus, response::setHeader, payloadOutputStream);
            response.setContentLength(payloadOutputStream.size());
            payloadOutputStream.writeTo(responseOutputStream);
        }
        responseOutputStream.flush();
    }

    @ExceptionHandler({LateEvaluationOrConversionException.class})
    public ResponseEntity<String> handleLateException(Exception ex) {
        ex.printStackTrace();
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(ex.getMessage(), headers, status);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<String> handleUnexpectedException(Exception ex) {
        ex.printStackTrace();
        String message = "Unexpected exception: " + ex.getMessage();
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(message, headers, status);
    }

}
