package it.unibz.inf.ontop.model.template;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Objects;

public class Template {
    public static ImmutableList<Component> of(String prefix, int index) {
        if (index != 0)
            throw new IllegalArgumentException("Index should be 0");
        return ImmutableList.of(
                new Component(prefix),
                new Component(index, ""));
    }

    public static ImmutableList<Component> of(String prefix, int index0, String separator, int index1) {
        if (index0 != 0)
            throw new IllegalArgumentException("Index should be 0");
        if (index1 != 1)
            throw new IllegalArgumentException("Index should be 1");
        return ImmutableList.of(
                new Component(prefix),
                new Component(index0, ""),
                new Component(separator),
                new Component(index1, ""));
    }

    public static Builder builder() { return new Builder(); }

    public static ImmutableList<Component> replaceFirst(ImmutableList<Component> components, String prefix) {
        if (prefix.isEmpty())
            return components.subList(1, components.size());

        ImmutableList.Builder<Component> builder = ImmutableList.builder();
        builder.add(new Component(prefix));
        builder.addAll(components.subList(1, components.size()));
        return builder.build();
    }

    public static ImmutableList<Component> replaceLast(ImmutableList<Component> components, String suffix) {
        if (suffix.isEmpty())
            return components.subList(0, components.size() - 1);

        ImmutableList.Builder<Component> builder = ImmutableList.builder();
        builder.addAll(components.subList(0, components.size() - 1));
        builder.add(new Component(suffix));
        return builder.build();
    }


    public static class Builder {
        private final ImmutableList.Builder<Component> builder = ImmutableList.builder();
        private int index = 0;

        public Builder addColumn() {
            int index1 = index++;
            builder.add(new Component(index1, ""));
            return this;
        }
        public Builder addColumn(String column) {
            int index1 = index++;
            builder.add(new Component(index1, column));
            return this;
        }
        public Builder addSeparator(String separator) {
            builder.add(new Component(separator));
            return this;
        }
        public ImmutableList<Component> build() {
            return builder.build();
        }
    }

    public static class Component {
        private final String component;
        private final int index; // -1 if separator

        // TODO: used in tests only - make private (by using the builder)
        Component(String component) {
            Objects.requireNonNull(component);
            this.component = component;
            this.index = -1;
        }

        // TODO: used in tests only - make private (by using the builder)
        Component(int index, @Nullable String component) {
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
            if (o instanceof Component) {
                Component other = (Component)o;
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
}
