package it.unibz.inf.ontop.dbschema;

import com.google.inject.Inject;


/**
 * A dummy DBMetadata
 */
public class DummyBasicDBMetadata extends BasicDBMetadata {

    @Inject
    private DummyBasicDBMetadata() {
        super("dummy", null, null, "",
                new QuotedIDFactoryStandardSQL("\"")
        );
    }

    public DummyBasicDBMetadata emptyCopyOf() {
        return new DummyBasicDBMetadata();
    }
}
