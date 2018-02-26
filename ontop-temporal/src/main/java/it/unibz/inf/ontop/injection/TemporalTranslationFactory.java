package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.answering.reformulation.generation.TemporalNativeQueryGenerator;
import it.unibz.inf.ontop.answering.reformulation.generation.calcite.TemporalCalciteBasedSQLGenerator;
import it.unibz.inf.ontop.dbschema.DBMetadata;

public interface TemporalTranslationFactory {

    TemporalNativeQueryGenerator create(DBMetadata metadata);
    TemporalCalciteBasedSQLGenerator createSQLGenerator(DBMetadata metadata);
}
