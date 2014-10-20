package org.semanticweb.ontop.injection;

import com.google.common.collect.ImmutableList;
import org.openrdf.model.Model;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;

import java.io.File;
import java.io.Reader;
import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Factory following the Guice AssistedInject pattern.
 *
 * See https://github.com/google/guice/wiki/AssistedInject.
 *
 * Builds core components that we want to be modular.
 *
 */
public interface NativeQueryLanguageComponentFactory {

    public MappingParser create(Reader reader);
    public MappingParser create(File file);

    public MappingParser create(Model mappingGraph);

    public OBDAModel create(Set<OBDADataSource> dataSources,
                            Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings,
                            PrefixManager prefixManager);

    public PrefixManager create(Map<String, String> prefixToURIMap);
}
