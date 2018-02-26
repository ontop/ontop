package it.unibz.inf.ontop.answering.reformulation.generation.calcite;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.iq.IntermediateQuery;

public interface CalciteBasedSQLGenerator extends NativeQueryGenerator{

    CalciteBasedSQLGenerator clone(DBMetadata dbMetadata);
}
