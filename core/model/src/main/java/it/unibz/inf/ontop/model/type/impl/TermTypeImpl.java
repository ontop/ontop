package it.unibz.inf.ontop.model.type.impl;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import java.util.Optional;

public class TermTypeImpl implements TermType {

    private final TermTypeAncestry ancestry;
    private final boolean isAbstract;
    private final String name;

    protected TermTypeImpl(String name, TermTypeAncestry parentAncestry, boolean isAbstract) {
        this.name = name;
        this.isAbstract = isAbstract;
        this.ancestry = parentAncestry.newAncestry(this);
    }

    /**
     * For the origin only
     */
    private TermTypeImpl() {
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
