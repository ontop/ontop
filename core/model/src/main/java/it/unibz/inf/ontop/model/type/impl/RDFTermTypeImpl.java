package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import java.util.function.Function;

public class RDFTermTypeImpl extends TermTypeImpl implements RDFTermType {

    private final Function<DBTypeFactory, DBTermType> closestDBTypeSupplier;

    /**
     * Concrete RDF term type
     */
    protected RDFTermTypeImpl(String name, TermTypeAncestry parentAncestry,
                              Function<DBTypeFactory, DBTermType> closestDBTypeFct) {
        super(name, parentAncestry, false);
        this.closestDBTypeSupplier = closestDBTypeFct;
    }

    protected RDFTermTypeImpl(String name, TermTypeAncestry parentAncestry) {
        super(name, parentAncestry, true);
        this.closestDBTypeSupplier = (dbTypeFactory) -> {
            throw new UnsupportedOperationException("This RDF term type is abstract");
        };
    }

    /**
     * For the root of all the RDF term types ONLY
     */
    private RDFTermTypeImpl(TermTypeAncestry parentAncestry) {
        this("RDFTerm", parentAncestry);
    }

    static RDFTermType createRDFTermRoot(TermTypeAncestry parentAncestry) {
        return new RDFTermTypeImpl(parentAncestry);
    }

    @Override
    public DBTermType getClosestDBType(DBTypeFactory dbTypeFactory) throws UnsupportedOperationException {
        return closestDBTypeSupplier.apply(dbTypeFactory);
    }
}
