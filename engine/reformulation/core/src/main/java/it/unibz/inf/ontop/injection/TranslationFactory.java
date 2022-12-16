package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;

/**
 * Following the Guice AssistedInject pattern
 */
public interface TranslationFactory {

    NativeQueryGenerator create(DBParameters dbParameters);
}
