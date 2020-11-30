package it.unibz.inf.ontop.model.template;

public class TemplateComponent {
    private final boolean isColumnNameReference;
    private final String component;
    private final int index;

    public TemplateComponent(String component) {
        this.isColumnNameReference = false;
        this.component = component;
        this.index = -1;
    }

    public TemplateComponent(int index, String component) {
        this.isColumnNameReference = true;
        this.component = component;
        this.index = index;
    }

    public boolean isColumnNameReference() {
        return isColumnNameReference;
    }

    public String getComponent() {
        return component;
    }

    public int getIndex() {
        return index;
    }
    
    public static TemplateComponent ofColumn(int index, String s) {
        return new TemplateComponent(index, s);
    }
    public static TemplateComponent ofColumn(int index) {
        return new TemplateComponent(index, "");
    }
    public static TemplateComponent ofSeparator(String s) {
        return new TemplateComponent(s);
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
