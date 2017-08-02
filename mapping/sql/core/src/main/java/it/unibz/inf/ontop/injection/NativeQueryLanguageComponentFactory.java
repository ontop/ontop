package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.spec.dbschema.RDBMetadataExtractor;

/**
 * Factory following the Guice AssistedInject pattern.
 *
 * See https://github.com/google/guice/wiki/AssistedInject.
 *
 * Builds core components that we want to be modular.
 *
 * Please note that the NativeQueryGenerator is NOT PART
 * of this factory because it belongs to another module.
 *
 */
public interface NativeQueryLanguageComponentFactory {

    RDBMetadataExtractor create();
}
