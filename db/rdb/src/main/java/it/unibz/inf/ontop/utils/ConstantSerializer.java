package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.generation.serializer.impl.SQLTermSerializer;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

/**
 * Util for THIRD-PARTY applications
 *
 * Useful for instance for converting from high-level mapping languages to R2RML
 *
 * Designed to be extensible
 *
 */
public class ConstantSerializer {

    protected final SQLTermSerializer sqlTermSerializer;
    protected final TermFactory termFactory;
    protected final DBTypeFactory dbTypeFactory;
    protected final Injector injector;

    public ConstantSerializer(String jdbcDriver) {
        OntopSQLCoreConfiguration configuration = OntopSQLCoreConfiguration.defaultBuilder()
                .jdbcDriver(jdbcDriver)
                .jdbcUrl("jdbc:fake://do.not.use/")
                .build();

        this.injector = configuration.getInjector();
        SelectFromWhereSerializer selectFromWhereSerializer = injector.getInstance(SelectFromWhereSerializer.class);
        this.sqlTermSerializer = selectFromWhereSerializer.getTermSerializer();

        this.termFactory = configuration.getTermFactory();
        this.dbTypeFactory = configuration.getTypeFactory().getDBTypeFactory();

    }

    /**
     * Returns the SQL string for a constant
     */
    public String serializeConstantIntoSQL(String lexicalValue, String dbTypeString) {
        DBTermType dbTermType = dbTypeFactory.getDBTermType(dbTypeString);

        DBConstant constant = termFactory.getDBConstant(lexicalValue, dbTermType);

        return sqlTermSerializer.serialize(constant, ImmutableMap.of());
    }


}
