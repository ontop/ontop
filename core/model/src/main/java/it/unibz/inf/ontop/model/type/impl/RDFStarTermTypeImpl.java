package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.*;

import java.util.function.Function;

public class RDFStarTermTypeImpl extends TermTypeImpl implements RDFTermType {

    private final Function<DBTypeFactory, DBTermType> closestDBTypeSupplier;

    /**
     * Concrete RDF term type
     */
    protected RDFStarTermTypeImpl(String name, TermTypeAncestry parentAncestry,
                                  Function<DBTypeFactory, DBTermType> closestDBTypeFct) {
        super(name, parentAncestry, false);
        this.closestDBTypeSupplier = closestDBTypeFct;
    }

    protected RDFStarTermTypeImpl(String name, TermTypeAncestry parentAncestry) {
        super(name, parentAncestry, true);
        this.closestDBTypeSupplier = (dbTypeFactory) -> {
            throw new UnsupportedOperationException("This RDF term type is abstract");
        };
    }

    /**
     * For the root of all the RDF term types ONLY
     */
    private RDFStarTermTypeImpl(TermTypeAncestry parentAncestry) {
        this("RDFStarTerm", parentAncestry);
    }

    static RDFStarTermType createRDFStarTermRoot(TermTypeAncestry parentAncestry) {
        return new RDFStarTermTypeImpl(parentAncestry);
    }

    @Override
    public DBTermType getClosestDBType(DBTypeFactory dbTypeFactory) throws UnsupportedOperationException {
        return closestDBTypeSupplier.apply(dbTypeFactory);
    }
}
