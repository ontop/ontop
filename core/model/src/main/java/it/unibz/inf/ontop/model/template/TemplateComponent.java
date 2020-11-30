package it.unibz.inf.ontop.model.template;

import com.google.common.collect.ImmutableList;

public class TemplateComponent {
    private final boolean isColumnNameReference;
    private final String component;

    public TemplateComponent(boolean isColumnNameReference, String component) {
        this.isColumnNameReference = isColumnNameReference;
        this.component = component;
    }

    public boolean isColumnNameReference() {
        return isColumnNameReference;
    }

    public String getComponent() {
        return component;
    }

    public static TemplateComponent ofColumn(String s) {
        return new TemplateComponent(true, s);
    }
    public static TemplateComponent ofColumn() {
        return new TemplateComponent(true, "");
    }
    public static TemplateComponent ofSeparator(String s) {
        return new TemplateComponent(false, s);
    }

    public static ImmutableList<TemplateComponent> unaryTemplate(String prefix) {
        return ImmutableList.of(
                TemplateComponent.ofSeparator(prefix),
                TemplateComponent.ofColumn());
    }

    public static ImmutableList<TemplateComponent> binaryTemplate(String prefix) {
       return ImmutableList.of(
                TemplateComponent.ofSeparator(prefix),
                TemplateComponent.ofColumn(),
                TemplateComponent.ofSeparator("/"),
                TemplateComponent.ofColumn());
    }

    public static ImmutableList<TemplateComponent> binaryTemplateNoSeparator(String prefix) {
        return ImmutableList.of(
                TemplateComponent.ofSeparator(prefix),
                TemplateComponent.ofColumn(),
                TemplateComponent.ofColumn());
    }

    @Override
    public String toString() { return isColumnNameReference ? "_" + component + "_" : component; }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TemplateComponent) {
            TemplateComponent other = (TemplateComponent)o;
            return this.component.equals(other.component)
                    && this.isColumnNameReference == other.isColumnNameReference;
        }
        return false;
    }

}
