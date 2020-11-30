package it.unibz.inf.ontop.model.template;

import com.google.common.collect.ImmutableList;

public class Template {
    public static ImmutableList<TemplateComponent> of(String prefix, int index) {
        if (index != 0)
            throw new IllegalArgumentException("Index should be 0");
        return ImmutableList.of(
                new TemplateComponent(prefix),
                new TemplateComponent(index, ""));
    }

    public static ImmutableList<TemplateComponent> of(String prefix, int index0, String separator, int index1) {
        if (index0 != 0)
            throw new IllegalArgumentException("Index should be 0");
        if (index1 != 1)
            throw new IllegalArgumentException("Index should be 1");
        return ImmutableList.of(
                new TemplateComponent(prefix),
                new TemplateComponent(index0, ""),
                new TemplateComponent(separator),
                new TemplateComponent(index1, ""));
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ImmutableList.Builder<TemplateComponent> builder = ImmutableList.builder();
        private int index = 0;

        public Builder addColumn() {
            int index1 = index++;
            builder.add(new TemplateComponent(index1, ""));
            return this;
        }
        public Builder addColumn(String column) {
            int index1 = index++;
            builder.add(new TemplateComponent(index1, column));
            return this;
        }
        public Builder addSeparator(String separator) {
            builder.add(new TemplateComponent(separator));
            return this;
        }
        public ImmutableList<TemplateComponent> build() {
            return builder.build();
        }
    }
}
