package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.*;
import eu.optique.r2rml.api.model.impl.InvalidR2RMLMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.*;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * High-level class that implements the MappingParser interface for R2RML.
 */
public class R2RMLMappingParser implements SQLMappingParser {

    private final SQLPPMappingFactory ppMappingFactory;
    private final SpecificationFactory specificationFactory;
    private final R2RMLToSQLPPTriplesMapConverter transformer;
    private final RDF4JR2RMLMappingManager manager;

    @Inject
    private R2RMLMappingParser(SQLPPMappingFactory ppMappingFactory, SpecificationFactory specificationFactory,
                               R2RMLToSQLPPTriplesMapConverter transformer) {
        this.ppMappingFactory = ppMappingFactory;
        this.specificationFactory = specificationFactory;
        this.transformer = transformer;
        this.manager = RDF4JR2RMLMappingManager.getInstance();
    }


    @Override
    public SQLPPMapping parse(File mappingFile) throws InvalidMappingException, MappingIOException {
        LinkedHashModel rdf4jGraph = new LinkedHashModel();
        RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
        StatementCollector collector = new StatementCollector(rdf4jGraph);
        parser.setRDFHandler(collector);
        try (InputStream in = new FileInputStream(mappingFile)) {
            URL documentUrl = new URL("file://" + mappingFile);
            parser.parse(in, documentUrl.toString());
            return parse(new RDF4J().asGraph(rdf4jGraph));
        }
        catch (IOException e) {
            throw new MappingIOException(e);
        }
        catch (RDFParseException | RDFHandlerException e) {
            throw new InvalidMappingException(e.getMessage());
        }
    }


    @Override
    public SQLPPMapping parse(Reader reader) throws InvalidMappingException, MappingIOException {
        LinkedHashModel rdf4jGraph = new LinkedHashModel();
        RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
        StatementCollector collector = new StatementCollector(rdf4jGraph);
        parser.setRDFHandler(collector);
        try (Reader r = reader) {
            // TODO: get the base IRI in a proper way
            parser.parse(r, "http://example.org/baseIRI/");
            return parse(new RDF4J().asGraph(rdf4jGraph));
        }
        catch (IOException e) {
            throw new MappingIOException(e);
        }
        catch (RDFParseException | RDFHandlerException e) {
            throw new InvalidMappingException(e.getMessage());
        }
    }

    @Override
    public SQLPPMapping parse(Graph mappingGraph) throws InvalidMappingException {
        try {
            Collection<TriplesMap> tripleMaps = manager.importMappings(mappingGraph);

            ImmutableList<SQLPPTriplesMap> sourceMappings = transformer.convert(tripleMaps);

            //TODO: try to extract prefixes from the R2RML mappings
            PrefixManager prefixManager = specificationFactory.createPrefixManager(ImmutableMap.of());

            return ppMappingFactory.createSQLPreProcessedMapping(sourceMappings, prefixManager);
        }
        catch (InvalidR2RMLMappingException e) {
            throw new InvalidMappingException(e.getMessage());
        }
    }
}
