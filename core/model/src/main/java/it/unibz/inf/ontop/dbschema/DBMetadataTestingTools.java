package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.model.atom.AtomFactory;

public class DBMetadataTestingTools {

    public static BasicDBMetadata createDummyMetadata(AtomFactory atomFactory, Relation2Predicate relation2Predicate) {
        return new BasicDBMetadata("dummy", null, null, "", new QuotedIDFactoryStandardSQL("\""),
                atomFactory, relation2Predicate);
    }
}
