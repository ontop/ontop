package it.unibz.inf.ontop.dbschema;

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

/**
 * For test purposes
 */
public class DummyRDBMetadata extends RDBMetadata {

    @Inject
    private DummyRDBMetadata(AtomFactory atomFactory, TermFactory termFactory, TypeFactory typeFactory,
                             JdbcTypeMapper jdbcTypeMapper) {

        super("dummy class", null, null, "",
                new QuotedIDFactoryStandardSQL("\""), jdbcTypeMapper, atomFactory,
                termFactory, typeFactory);
    }
}
