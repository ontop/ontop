package it.unibz.inf.ontop.injection;

import com.google.inject.assistedinject.Assisted;
import org.openrdf.model.Model;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.model.CQIE;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAQuery;
import it.unibz.inf.ontop.nativeql.DBMetadataExtractor;
import it.unibz.inf.ontop.model.DataSourceMetadata;
import it.unibz.inf.ontop.utils.IMapping2DatalogConverter;

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
                                   @Assisted("targetQuery") CQIE targetQuery);

    public OBDAMappingAxiom create(@Assisted("sourceQuery") OBDAQuery sourceQuery,
                                   @Assisted("targetQuery") CQIE targetQuery);

    public IMapping2DatalogConverter create(DataSourceMetadata metadata);
}
