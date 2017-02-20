package it.unibz.inf.ontop.r2rml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.MappingFactory;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDAFactoryWithException;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * High-level class that implements the MappingParser interface for R2RML.
 */
public class R2RMLMappingParser implements MappingParser {

    private final NativeQueryLanguageComponentFactory nativeQLFactory;
    private final OBDAFactoryWithException obdaFactory;
    private final MappingFactory mappingFactory;


    @Inject
    private R2RMLMappingParser(NativeQueryLanguageComponentFactory nativeQLFactory,
                               OBDAFactoryWithException obdaFactory, MappingFactory mappingFactory) {
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
        this.mappingFactory = mappingFactory;
    }


    @Override
    public OBDAModel parse(File mappingFile) throws InvalidMappingException, IOException, DuplicateMappingException {

        try {
            R2RMLManager r2rmlManager = new R2RMLManager(mappingFile, nativeQLFactory);
            return parse(r2rmlManager);

        } catch (RDFParseException | RDFHandlerException e) {
            throw new InvalidMappingException(e.getMessage());
        }
    }

    @Override
    public OBDAModel parse(Reader reader) throws InvalidMappingException, IOException, DuplicateMappingException {
        // TODO: support this
        throw new UnsupportedOperationException("The R2RMLMappingParser does not support" +
                "yet the Reader interface.");
    }

    @Override
    public OBDAModel parse(Model mappingGraph) throws InvalidMappingException, IOException, DuplicateMappingException {
        R2RMLManager r2rmlManager = new R2RMLManager(mappingGraph, nativeQLFactory);
        return parse(r2rmlManager);
    }

    private OBDAModel parse(R2RMLManager manager) throws DuplicateMappingException {
        //TODO: make the R2RMLManager simpler.
        ImmutableList<OBDAMappingAxiom> sourceMappings = manager.getMappings(manager.getModel());

        //TODO: try to extract prefixes from the R2RML mappings
        PrefixManager prefixManager = mappingFactory.create(ImmutableMap.of());
        MappingMetadata mappingMetadata = mappingFactory.create(prefixManager);

        return obdaFactory.createOBDAModel(sourceMappings, mappingMetadata);
    }


}
