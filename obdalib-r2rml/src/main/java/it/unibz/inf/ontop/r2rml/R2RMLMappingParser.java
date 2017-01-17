package it.unibz.inf.ontop.r2rml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.MappingFactory;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDAFactoryWithException;
import it.unibz.inf.ontop.injection.OBDASettings;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * High-level class that implements the MappingParser interface for R2RML.
 *
 * Guice-enabled, available through factories.
 */
public class R2RMLMappingParser implements MappingParser {

    private final OBDASettings configuration;
    private final NativeQueryLanguageComponentFactory nativeQLFactory;
    private final OBDAFactoryWithException obdaFactory;
    private final MappingFactory mappingFactory;
    private OBDAModel obdaModel;

    /**
     * Either a file or a Sesame "model"
     */
    private final File mappingFile;
    private final Model mappingGraph;

    @AssistedInject
    private R2RMLMappingParser(@Assisted File mappingFile, NativeQueryLanguageComponentFactory nativeQLFactory,
                               OBDAFactoryWithException obdaFactory, MappingFactory mappingFactory,
                               OBDASettings configuration) {
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
        this.configuration = configuration;
        this.mappingFile = mappingFile;
        this.mappingFactory = mappingFactory;
        this.mappingGraph = null;

        /**
         * Computed lazily  (when requested for the first time).
         */
        this.obdaModel = null;
    }

    @AssistedInject
    private R2RMLMappingParser(@Assisted Model mappingGraph, MappingFactory mappingFactory,
                               NativeQueryLanguageComponentFactory nativeQLFactory,
                               OBDAFactoryWithException obdaFactory, OBDASettings configuration) {
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
        this.mappingFactory = mappingFactory;
        this.configuration = configuration;
        this.mappingGraph = mappingGraph;
        this.mappingFile = null;
        this.obdaModel = null;
    }

    @AssistedInject
    private R2RMLMappingParser(@Assisted Reader reader,
                               NativeQueryLanguageComponentFactory nativeQLFactory,
                               OBDASettings configuration) {
        // TODO: support this
        throw new IllegalArgumentException("The R2RMLMappingParser does not support" +
                "yet the Reader interface.");
    }


    @Override
    public OBDAModel getOBDAModel() throws InvalidMappingException, IOException, DuplicateMappingException {
        /**
         * The OBDA model is only computed once.
         */
        if (obdaModel != null) {
            return obdaModel;
        }

        R2RMLManager r2rmlManager;
        if (mappingFile != null)
            try {
                r2rmlManager = new R2RMLManager(mappingFile, nativeQLFactory);
            } catch (RDFParseException | RDFHandlerException e) {
                throw new InvalidMappingException(e.getMessage());
            }
        else if (mappingGraph != null)
            r2rmlManager = new R2RMLManager(mappingGraph, nativeQLFactory);
        else
            throw new RuntimeException("Internal inconsistency. A mappingFile or a mappingGraph should be defined.");

        //TODO: make the R2RMLManager simpler.
        ImmutableList<OBDAMappingAxiom> sourceMappings = r2rmlManager.getMappings(r2rmlManager.getModel());

        //TODO: try to extract prefixes from the R2RML mappings
        PrefixManager prefixManager = mappingFactory.create(ImmutableMap.of());
        MappingMetadata mappingMetadata = mappingFactory.create(prefixManager);

        obdaModel = obdaFactory.createOBDAModel(sourceMappings, mappingMetadata);

    return obdaModel;
    }
}
