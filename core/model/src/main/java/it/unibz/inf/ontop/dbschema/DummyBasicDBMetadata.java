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
    private DummyBasicDBMetadata(TypeFactory typeFactory) {
        super("dummy", null, null, "",
                new DummyTypeMapper(typeFactory), new QuotedIDFactoryStandardSQL("\"")
        );
    }


    private static class DummyTypeMapper implements TypeMapper {

        private final RDFDatatype defaultType;

        private DummyTypeMapper(TypeFactory typeFactory) {
            this.defaultType = typeFactory.getXsdStringDatatype();
        }

        @Override
        public TermType getTermType(int typeCode, String typeName) {
            return defaultType;
        }
    }
}
