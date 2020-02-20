package it.unibz.inf.ontop.dbschema;

import com.google.inject.Inject;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

/**
 * For test purposes
 */
public class DummyRDBMetadata extends RDBMetadata {

    @Inject
    private DummyRDBMetadata(TypeFactory typeFactory) {

        super("dummy class", null, null, "",
                new QuotedIDFactoryStandardSQL("\""), typeFactory);
    }

    public DummyRDBMetadata emptyCopyOf() {
        return new DummyRDBMetadata(typeFactory);
    }

}
