package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import it.unibz.inf.ontop.model.type.lexical.DefaultLexicalSpaces;

import javax.annotation.Nullable;
import java.util.Optional;

public class NumberDBTermType extends DBTermTypeImpl {

    private final String castName;
    @Nullable
    private final RDFDatatype rdfDatatype;
    private final Category category;

    protected NumberDBTermType(String name, String castName, TermTypeAncestry parentAncestry, RDFDatatype rdfDatatype,
                               Category category) {
        super(name, parentAncestry, false, category);
        this.castName = castName;
        this.rdfDatatype = rdfDatatype;
        this.category = category;
    }

    protected NumberDBTermType(String name, TermTypeAncestry parentAncestry, RDFDatatype rdfDatatype, Category category) {
        super(name, parentAncestry, false, category);
        this.rdfDatatype = rdfDatatype;
        castName = name;
        this.category = category;
    }

    @Override
    public Optional<RDFDatatype> getNaturalRDFDatatype() {
        return Optional.ofNullable(rdfDatatype);
    }

    /**
     * False by default as the vast majority of number formats are not sensible to IRI safe encoding.
     * In theory rationals would pose problems if they use the "/" character, but I have so far never seen them in a DB.
     */
    @Override
    public boolean isNeedingIRISafeEncoding() {
        return false;
    }

    /**
     * NB: here we ignore the presence of + and 0s on the left (for the sake of simplicity)
     * TODO: stop ignoring this presence
     */
    @Override
    public boolean areEqualitiesStrict() {
        return category == Category.INTEGER;
    }

    @Override
    public Optional<Boolean> areEqualitiesStrict(DBTermType otherType) {
        return Optional.of(otherType.getCategory() == Category.INTEGER);
    }

    @Override
    public boolean areEqualitiesBetweenTwoDBAttributesStrict() {
        return true;
    }

    @Override
    public String getCastName() {
        return castName;
    }
}
