package it.unibz.inf.ontop.dbschema;

import com.google.inject.Inject;
import it.unibz.inf.ontop.model.type.TypeFactory;

/**
 * For test purposes
 */
public class DummyRDBMetadata extends RDBMetadata {

    @Inject
    private DummyRDBMetadata(TypeFactory typeFactory, JdbcTypeMapper jdbcTypeMapper) {

        super("dummy class", null, null, "",
                new QuotedIDFactoryStandardSQL("\""), jdbcTypeMapper,
                typeFactory);
    }
}
