package it.unibz.inf.ontop.answering.reformulation.generation.calcite.rel2sql;

import it.unibz.inf.ontop.answering.reformulation.generation.calcite.algebra.*;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.sql.SqlDialect;

public class TemporalRelToSqlConverter extends RelToSqlConverter{
    /**
     * Creates a RelToSqlConverter.
     *
     * @param dialect
     */
    public TemporalRelToSqlConverter(SqlDialect dialect) {
        super(dialect);
    }

    public Result visit(BoxMinusRelNode e) {
        return null;
    }

    public Result visit(BoxPlusRelNode e) {
        return null;
    }

    public Result visit(DiamondMinusRelNode e) {
        return null;
    }

    public Result visit(DiamondPlusRelNode e) {
        return null;
    }

    public Result visit(TemporalJoinRelNode e) {
        return null;
    }

    public Result visit(TemporalCoalesceRelNode e) {
        return null;
    }
}
