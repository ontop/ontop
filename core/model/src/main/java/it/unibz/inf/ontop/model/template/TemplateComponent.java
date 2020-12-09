package it.unibz.inf.ontop.model.template;

import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.util.Objects;

public class TemplateComponent {
    private final String component;
    private final int index; // -1 if separator

    public TemplateComponent(String component) {
        Objects.requireNonNull(component);
        this.component = component;
        this.index = -1;
    }

    public TemplateComponent(int index, @Nullable String component) {
        if (index < 0)
            throw new IllegalArgumentException("Column template component index must be non-negative");

        this.component = component;
        this.index = index;
    }

    public boolean isColumnNameReference() {
        return index >= 0;
    }

    public String getComponent() {
        return component;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return index >=0
                ? "_" + index + (component == null ? "" : "/" + component) + "_"
                : component;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TemplateComponent) {
            TemplateComponent other = (TemplateComponent)o;
            return // separators
                    this.index == -1 && other.index == -1 && this.component.equals(other.component)
                  // columns
                 || this.index >= 0 && this.index == other.index;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (index == -1) ? component.hashCode() : Integer.hashCode(index);
    }
}
