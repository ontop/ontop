package it.unibz.inf.ontop.dbschema;

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;

/**
 * For test purposes
 */
public class DummyRDBMetadata extends RDBMetadata {

    @Inject
    private DummyRDBMetadata(AtomFactory atomFactory, Relation2Predicate relation2Predicate, TermFactory termFactory,
                             DatalogFactory datalogFactory, JdbcTypeMapper jdbcTypeMapper) {

        super("dummy class", null, null, "",
                new QuotedIDFactoryStandardSQL("\""), jdbcTypeMapper, atomFactory,
                relation2Predicate, termFactory, datalogFactory);
    }
}
