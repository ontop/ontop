package it.unibz.inf.ontop.dbschema;

import com.google.inject.Inject;


/**
 * A dummy DBMetadata
 */
public class DummyBasicDBMetadata extends BasicDBMetadata {

    @Inject
    private DummyBasicDBMetadata() {
        super(null, null, null, null, new QuotedIDFactoryStandardSQL("\""), null);
    }
}
