package it.unibz.inf.ontop.model.type.impl;
import it.unibz.inf.ontop.model.type.COL_TYPE;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class TermTypeImpl implements TermType {

    @Nullable
    private final COL_TYPE colType;
    private final TermTypeAncestry ancestry;
    private final boolean isAbstract;
    private final String name;

    protected TermTypeImpl(@Nonnull COL_TYPE colType, TermTypeAncestry parentAncestry, boolean isAbstract) {
        this.colType = colType;
        this.name = colType.toString();
        this.isAbstract = isAbstract;
        this.ancestry = parentAncestry.newAncestry(this);
    }

    protected TermTypeImpl(String name, TermTypeAncestry parentAncestry, boolean isAbstract) {
        this.colType = null;
        this.name = name;
        this.isAbstract = isAbstract;
        this.ancestry = parentAncestry.newAncestry(this);
    }

    /**
     * For the origin only
     */
    private TermTypeImpl() {
        this.colType = null;
        this.name = "Origin";
        this.isAbstract = true;
        this.ancestry = new TermTypeAncestryImpl(this);
    }

    /**
     * To be called ONLY ONCE!
     */
    static TermType createOriginTermType() {
        return new TermTypeImpl();
    }

    /**
     * TODO: get rid of it
     */
    @Override
    public COL_TYPE getColType() {
        if (colType == null)
            throw new UnsupportedOperationException(name + " does not have a COL_TYPE");
        return colType;
    }

    /**
     * TODO: get rid of it
     */
    @Override
    public Optional<COL_TYPE> getOptionalColType() {
        return Optional.ofNullable(colType);
    }

    @Override
    public final boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean isA(TermType moreGeneralType) {
        TermType commonDenominator = getCommonDenominator(moreGeneralType);
        return moreGeneralType.equals(commonDenominator);
    }

    /**
     * Can be overloaded
     */
    @Override
    public TermType getCommonDenominator(TermType otherTermType){
        if (equals(otherTermType))
            return this;
        if (ancestry.contains(otherTermType))
            return otherTermType;

        TermTypeAncestry otherAncestry = otherTermType.getAncestry();
        if (otherAncestry.contains(this))
            return this;
        return ancestry.getClosestCommonAncestor(otherAncestry);
    }

    @Override
    public TermTypeAncestry getAncestry() {
        return ancestry;
    }

    /**
     * TODO: refactor
     */
    @Override
    public boolean equals(Object other) {
        return Optional.ofNullable(other)
                .filter(o -> (o instanceof TermType))
                .map(o -> (TermType) o)
                .filter(o -> o.toString().equals(toString()))
                .isPresent();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
