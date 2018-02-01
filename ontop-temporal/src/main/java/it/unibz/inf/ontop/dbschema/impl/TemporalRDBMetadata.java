package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.List;
import java.util.Map;

public class TemporalRDBMetadata extends RDBMetadata {

    private int parserViewCounter;
    //private final JdbcTypeMapper jdbcTypeMapper;
    private final TypeFactory typeFactory;

    public TemporalRDBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion,
                               QuotedIDFactory idfac, Map<RelationID, DatabaseRelationDefinition> tables,
                               Map<RelationID, RelationDefinition> relations, List<DatabaseRelationDefinition> listOfTables,
                               int parserViewCounter,
                               JdbcTypeMapper jdbcTypeMapper, AtomFactory atomFactory,
                               TermFactory termFactory, TypeFactory typeFactory, DatalogFactory datalogFactory) {
        super(driverName,  driverVersion,  databaseProductName,  databaseVersion, idfac, tables, relations,  listOfTables, parserViewCounter,  jdbcTypeMapper,  atomFactory, termFactory,  typeFactory,  datalogFactory);
        //super(driverName, driverVersion, databaseProductName, databaseVersion, jdbcTypeMapper, tables, relations, listOfTables, atomFactory, termFactory, datalogFactory, idfac);
        //this.parserViewCounter = 0;
        //this.jdbcTypeMapper = jdbcTypeMapper;
        this.typeFactory = typeFactory;
    }

    /**
     * creates a view for SQLQueryParser
     * (NOTE: these views are simply names for complex non-parsable subqueries, not database views)
     *
     * TODO: make the second argument a callback (which is called only when needed)
     * TODO: make it re-use parser views for the same SQL
     *
     * @param sql
     * @return
     */

    public ParserViewDefinition createParserView(String sql, ImmutableList<QuotedID> attributes) {
        if (!isStillMutable()) {
            throw new IllegalStateException("Too late! Parser views must be created before freezing the DBMetadata");
        }
        RelationID id = getQuotedIDFactory().createRelationID(null, String.format("temporalView_%s", parserViewCounter++));

        ParserViewDefinition view = new ParserViewDefinition(id, attributes, sql, typeFactory.getXsdStringDatatype());
        // UGLY!!
        add(view, relations);
        return view;
    }
}
