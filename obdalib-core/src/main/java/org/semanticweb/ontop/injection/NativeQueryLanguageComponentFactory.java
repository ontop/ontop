package org.semanticweb.ontop.injection;

import com.google.inject.assistedinject.Assisted;
import org.openrdf.model.Model;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.nativeql.DBMetadataExtractor;

import java.io.File;
import java.io.Reader;
import java.util.Map;

/**
 * Factory following the Guice AssistedInject pattern.
 *
 * See https://github.com/google/guice/wiki/AssistedInject.
 *
 * Builds core components that we want to be modular.
 *
 * Please note that the NativeQueryGenerator is NOT PART
 * of this factory because it belongs to another module
 * (see the QuestComponentFactory).
 *
 */
public interface NativeQueryLanguageComponentFactory {

    public MappingParser create(Reader reader);
    public MappingParser create(File file);
    public MappingParser create(Model mappingGraph);

    /**
     * Useful for Protege (when the mapping file does not provide
     * this information, e.g. R2RML mappings).
     *
     * If possible, avoid this constructor and use the OBDAProperties instead.
     */
    public MappingParser create(File file, OBDADataSource dataSource);

    public PrefixManager create(Map<String, String> prefixToURIMap);

    public DBMetadataExtractor create();

    public OBDAMappingAxiom create(String id, @Assisted("sourceQuery") OBDAQuery sourceQuery,
                                   @Assisted("targetQuery") OBDAQuery targetQuery);

    public OBDAMappingAxiom create(@Assisted("sourceQuery") OBDAQuery sourceQuery,
                                   @Assisted("targetQuery") OBDAQuery targetQuery);
}
