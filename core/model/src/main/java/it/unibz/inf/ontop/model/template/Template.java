package it.unibz.inf.ontop.model.template;

import com.google.common.collect.ImmutableList;

public class Template {
    public static ImmutableList<TemplateComponent> of(int index, String suffix) {
        if (index != 0)
            throw new IllegalArgumentException("Index should be 0");
        return ImmutableList.of(
                TemplateComponent.ofColumn(index),
                TemplateComponent.ofSeparator(suffix));
    }

    public static ImmutableList<TemplateComponent> of(String prefix, int index) {
        if (index != 0)
            throw new IllegalArgumentException("Index should be 0");
        return ImmutableList.of(
                TemplateComponent.ofSeparator(prefix),
                TemplateComponent.ofColumn(index));
    }

    public static ImmutableList<TemplateComponent> of(String prefix, int index0, String separator, int index1) {
        if (index0 != 0)
            throw new IllegalArgumentException("Index should be 0");
        if (index1 != 1)
            throw new IllegalArgumentException("Index should be 1");
        return ImmutableList.of(
                TemplateComponent.ofSeparator(prefix),
                TemplateComponent.ofColumn(index0),
                TemplateComponent.ofSeparator(separator),
                TemplateComponent.ofColumn(index1));
    }

    public static ImmutableList<TemplateComponent> of(String prefix, int index0, int index1) {
        if (index0 != 0)
            throw new IllegalArgumentException("Index should be 0");
        if (index1 != 1)
            throw new IllegalArgumentException("Index should be 1");
        return ImmutableList.of(
                TemplateComponent.ofSeparator(prefix),
                TemplateComponent.ofColumn(index0),
                TemplateComponent.ofColumn(index1));
    }

    public static ImmutableList<TemplateComponent> of(String prefix, int index0, String suffix) {
        if (index0 != 0)
            throw new IllegalArgumentException("Index should be 0");
        return ImmutableList.of(
                TemplateComponent.ofSeparator(prefix),
                TemplateComponent.ofColumn(index0),
                TemplateComponent.ofSeparator(suffix));
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ImmutableList.Builder<TemplateComponent> builder = ImmutableList.builder();
        private int index = 0;

        public Builder addColumn() {
            builder.add(TemplateComponent.ofColumn(index++));
            return this;
        }
        public Builder addColumn(String column) {
            builder.add(TemplateComponent.ofColumn(index++, column));
            return this;
        }
        public Builder addSeparator(String separator) {
            builder.add(TemplateComponent.ofSeparator(separator));
            return this;
        }
        public ImmutableList<TemplateComponent> build() {
            return builder.build();
        }
    }
}
