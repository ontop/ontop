package it.unibz.inf.ontop.dbschema;

public class DBMetadataTestingTools {

    public static BasicDBMetadata createDummyMetadata() {
        return new BasicDBMetadata("dummy", null, null, "", new QuotedIDFactoryStandardSQL("\""));
    }
}
