package it.unibz.inf.ontop.dbschema;

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;

/**
 * A dummy DBMetadata
 */
public class DummyBasicDBMetadata extends BasicDBMetadata {

    @Inject
    private DummyBasicDBMetadata(AtomFactory atomFactory, TermFactory termFactory,
                                 DatalogFactory datalogFactory) {
        super("dummy", null, null, "", atomFactory, termFactory, datalogFactory,
                new QuotedIDFactoryStandardSQL("\"")
        );
    }
}
