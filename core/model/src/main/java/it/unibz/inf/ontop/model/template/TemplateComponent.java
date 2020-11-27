package it.unibz.inf.ontop.model.template;

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
