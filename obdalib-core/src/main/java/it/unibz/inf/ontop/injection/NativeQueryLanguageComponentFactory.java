package it.unibz.inf.ontop.injection;

import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.model.*;
import org.eclipse.rdf4j.model.Model;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.nativeql.DBMetadataExtractor;
import it.unibz.inf.ontop.utils.IMapping2DatalogConverter;

import java.io.File;
import java.io.Reader;
import java.util.List;
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

    public PrefixManager create(Map<String, String> prefixToURIMap);

    public DBMetadataExtractor create();

    public OBDAMappingAxiom create(String id, @Assisted("sourceQuery") SourceQuery sourceQuery,
                                   @Assisted("targetQuery") List<Function> targetQuery);

    public OBDAMappingAxiom create(@Assisted("sourceQuery") SourceQuery sourceQuery,
                                   @Assisted("targetQuery") List<Function> targetQuery);
}
