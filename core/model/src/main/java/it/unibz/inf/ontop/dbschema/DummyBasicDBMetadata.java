package it.unibz.inf.ontop.dbschema;

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;

/**
 * A dummy DBMetadata
 */
public class DummyBasicDBMetadata extends BasicDBMetadata {

    @Inject
    private DummyBasicDBMetadata(AtomFactory atomFactory, Relation2Predicate relation2Predicate,
                                 TermFactory termFactory, DatalogFactory datalogFactory) {
        super("dummy", null, null, "",
                new QuotedIDFactoryStandardSQL("\""),
                atomFactory, relation2Predicate, termFactory, datalogFactory);
    }
}
