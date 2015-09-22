package org.semanticweb.ontop.r2rml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDAFactoryWithException;
import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.io.InvalidDataSourceException;
import org.semanticweb.ontop.io.OBDADataSourceFromConfigExtractor;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

/**
 * High-level class that implements the MappingParser interface for R2RML.
 *
 * Guice-enabled, available through factories.
 */
public class R2RMLMappingParser implements MappingParser {

    private final OBDAProperties configuration;
    private final NativeQueryLanguageComponentFactory nativeQLFactory;
    private final OBDAFactoryWithException obdaFactory;
    /**
     * Data source given at construction time, not extracted from the preferences.
     */
    private final OBDADataSource predefinedDataSource;
    private OBDAModel obdaModel;

    /**
     * Either a file or a Sesame "model"
     */
    private final File mappingFile;
    private final Model mappingGraph;

    @AssistedInject
    private R2RMLMappingParser(@Assisted File mappingFile, NativeQueryLanguageComponentFactory nativeQLFactory,
                               OBDAFactoryWithException obdaFactory, OBDAProperties configuration) {
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
        this.configuration = configuration;
        this.mappingFile = mappingFile;
        this.mappingGraph = null;
        this.predefinedDataSource = null;

        /**
         * Computed lazily  (when requested for the first time).
         */
        this.obdaModel = null;
    }

    @AssistedInject
    private R2RMLMappingParser(@Assisted Model mappingGraph,
                               NativeQueryLanguageComponentFactory nativeQLFactory,
                               OBDAFactoryWithException obdaFactory, OBDAProperties configuration) {
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
        this.configuration = configuration;
        this.mappingGraph = mappingGraph;
        this.mappingFile = null;
        this.obdaModel = null;
        this.predefinedDataSource = null;
    }

    /**
     * Data source given from outside --> no need to extract it from the OBDA properties.
     */
    @AssistedInject
    private R2RMLMappingParser(@Assisted File mappingFile, @Assisted OBDADataSource dataSource,
                               NativeQueryLanguageComponentFactory nativeQLFactory,
                               OBDAFactoryWithException obdaFactory, OBDAProperties configuration) {
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
        this.configuration = configuration;
        this.mappingFile = mappingFile;
        this.mappingGraph = null;
        this.predefinedDataSource = dataSource;
    }

    @AssistedInject
    private R2RMLMappingParser(@Assisted Reader reader,
                               NativeQueryLanguageComponentFactory nativeQLFactory,
                               OBDAProperties configuration) {
        // TODO: support this
        throw new IllegalArgumentException("The R2RMLMappingParser does not support" +
                "yet the Reader interface.");
    }


    @Override
    public OBDAModel getOBDAModel() throws InvalidMappingException, IOException, InvalidDataSourceException,
            DuplicateMappingException {
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
                throw new InvalidDataSourceException(e.getMessage());
            }
        else if (mappingGraph != null)
            r2rmlManager = new R2RMLManager(mappingGraph, nativeQLFactory);
        else
            throw new RuntimeException("Internal inconsistency. A mappingFile or a mappingGraph should be defined.");


        OBDADataSource dataSource = this.predefinedDataSource;
        /**
         * If the data source is not already defined, extracts it from the preferences.
         */
        if (dataSource == null) {
            OBDADataSourceFromConfigExtractor dataSourceExtractor = new OBDADataSourceFromConfigExtractor(configuration);
            dataSource = dataSourceExtractor.getDataSource() ;
        }

        //TODO: make the R2RMLManager simpler.
        ImmutableList<OBDAMappingAxiom> sourceMappings = r2rmlManager.getMappings(r2rmlManager.getModel());

        //TODO: try to extract prefixes from the R2RML mappings
        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());

        obdaModel = obdaFactory.createOBDAModel(ImmutableSet.of(dataSource), ImmutableMap.of(dataSource.getSourceID(),
                        sourceMappings), prefixManager);

    return obdaModel;
    }
}
