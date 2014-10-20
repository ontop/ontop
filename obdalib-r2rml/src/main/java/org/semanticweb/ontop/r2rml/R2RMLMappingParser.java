package org.semanticweb.ontop.r2rml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.openrdf.model.Model;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDAProperties;
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
import java.util.Properties;

/**
 * High-level class that implements the MappingParser interface for R2RML.
 */
public class R2RMLMappingParser implements MappingParser {

    private final OBDAProperties configuration;
    private final NativeQueryLanguageComponentFactory nativeQLFactory;
    private OBDAModel obdaModel;

    /**
     * Either a file or a Sesame "model"
     */
    private final File mappingFile;
    private final Model mappingGraph;

    @AssistedInject
    private R2RMLMappingParser(@Assisted File mappingFile, NativeQueryLanguageComponentFactory nativeQLFactory,
                               OBDAProperties configuration) {
        //TODO: complete
        this.nativeQLFactory = nativeQLFactory;
        this.configuration = configuration;
        this.mappingFile = mappingFile;
        this.mappingGraph = null;

        /**
         * Computed lazily  (when requested for the first time).
         */
        this.obdaModel = null;
    }

    @AssistedInject
    private R2RMLMappingParser(@Assisted Model mappingGraph,
                               NativeQueryLanguageComponentFactory nativeQLFactory,
                               OBDAProperties configuration) {
        //TODO: complete
        this.nativeQLFactory = nativeQLFactory;
        this.configuration = configuration;
        this.mappingGraph = mappingGraph;
        this.mappingFile = null;
        this.obdaModel = null;

    }

    @AssistedInject
    private R2RMLMappingParser(@Assisted Reader reader,
                               NativeQueryLanguageComponentFactory nativeQLFactory,
                               Properties configuration) {
        // TODO: support this
        throw new IllegalArgumentException("The R2RMLMappingParser does not support" +
                "yet the Reader interface.");
    }

    @Override
    public OBDAModel getOBDAModel() throws InvalidMappingException, IOException {
        /**
         * The OBDA model is only computed once.
         */
        if (obdaModel != null) {
            return obdaModel;
        }

        R2RMLManager r2rmlManager;
        if (mappingFile != null)
            r2rmlManager = new R2RMLManager(mappingFile);
        else if (mappingGraph != null)
            r2rmlManager = new R2RMLManager(mappingGraph);
        else
            throw new RuntimeException("Internal inconsistency. A mappingFile or a mappingGraph should be defined.");

        //TODO: make the R2RMLManager simpler.
        ImmutableList<OBDAMappingAxiom> sourceMappings = r2rmlManager.getMappings(r2rmlManager.getModel());

        OBDADataSourceFromConfigExtractor dataSourceExtractor = new OBDADataSourceFromConfigExtractor(configuration);
        OBDADataSource dataSource = dataSourceExtractor.getDataSource() ;

        //TODO: try to extract prefixes from the R2RML mappings
        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());

        obdaModel = nativeQLFactory.create(ImmutableSet.of(dataSource), ImmutableMap.of(dataSource.getSourceID(), sourceMappings),
                prefixManager);

    return obdaModel;
    }
}
