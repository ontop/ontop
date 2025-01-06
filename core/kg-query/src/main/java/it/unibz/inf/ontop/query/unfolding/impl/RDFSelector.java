package it.unibz.inf.ontop.query.unfolding.impl;

import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class RDFSelector {

    @Nullable
    private final ObjectStringTemplateFunctionSymbol objectTemplate;

    @Nullable
    private final ObjectConstant objectConstant;

    private final RDFSelectorType type;


    protected RDFSelector(@Nonnull ObjectStringTemplateFunctionSymbol objectTemplate) {
        this.objectTemplate = objectTemplate;
        this.objectConstant = null;
        this.type = RDFSelectorType.OBJECT_TEMPLATE;
    }

    protected RDFSelector(ObjectConstant objectConstant) {
        this.objectConstant = objectConstant;
        this.objectTemplate = null;
        this.type = RDFSelectorType.OBJECT_CONSTANT;
    }

    protected RDFSelector() {
        this.objectConstant = null;
        this.objectTemplate = null;
        this.type = RDFSelectorType.LITERAL;
    }

    public boolean isObjectTemplate() {
        return type == RDFSelectorType.OBJECT_TEMPLATE;
    }

    public RDFSelectorType getType() {
        return type;
    }

    public Optional<ObjectStringTemplateFunctionSymbol> getObjectTemplate() {
        return Optional.ofNullable(objectTemplate);
    }

    public Optional<ObjectConstant> getObjectConstant() {
        return Optional.ofNullable(objectConstant);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RDFSelector)) return false;
        RDFSelector that = (RDFSelector) o;
        return Objects.equals(objectTemplate, that.objectTemplate)
                && Objects.equals(objectConstant, that.objectConstant) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectConstant, objectTemplate, type);
    }

    @Override
    public String toString() {
        switch (type) {
            case OBJECT_TEMPLATE:
                return "RDFSelector{" + objectTemplate + "}";
            case OBJECT_CONSTANT:
                return "RDFSelector{" + objectConstant + "}";
            default:
                return "RDFSelector{LITERAL}";
        }
    }

    public enum RDFSelectorType {
        OBJECT_TEMPLATE,
        OBJECT_CONSTANT,
        LITERAL
    }
}
