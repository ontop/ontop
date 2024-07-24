package it.unibz.inf.ontop.model.template;

import com.google.common.collect.ImmutableList;

import java.util.Objects;

public class Template {

    public static Builder builder() { return new Builder(); }

    public static ImmutableList<Component> replaceFirst(ImmutableList<Component> components, String prefix) {
        if (prefix.isEmpty())
            return components.subList(1, components.size());

        ImmutableList.Builder<Component> builder = ImmutableList.builder();
        builder.add(new Component(Component.STRING_INDEX, prefix));
        builder.addAll(components.subList(1, components.size()));
        return builder.build();
    }

    public static ImmutableList<Component> replaceLast(ImmutableList<Component> components, String suffix) {
        if (suffix.isEmpty())
            return components.subList(0, components.size() - 1);

        ImmutableList.Builder<Component> builder = ImmutableList.builder();
        builder.addAll(components.subList(0, components.size() - 1));
        builder.add(new Component(Component.STRING_INDEX, suffix));
        return builder.build();
    }


    public static class Builder {
        private final ImmutableList.Builder<Component> builder = ImmutableList.builder();
        private int index = 0;

        public Builder placeholder() {
            return column("");
        }
        public Builder column(String column) {
            int index1 = index++;
            builder.add(new Component(index1, column));
            return this;
        }
        public Builder string(String str) {
            builder.add(new Component(Component.STRING_INDEX, str));
            return this;
        }
        public ImmutableList<Component> build() {
            return builder.build();
        }
    }

    /**
     * A component is either a placeholder (database column) or a string
     */

    public static class Component {
        private final String component; // placeholder or string
        private final int index; // STRING_INDEX if a string

        private static final int STRING_INDEX = -1;

        private Component(int index, String component) {
            this.index = index;
            this.component = Objects.requireNonNull(component);
        }

        public boolean isColumn() {
            return index != STRING_INDEX;
        }

        public String getComponent() {
            return component;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return index == STRING_INDEX
                    ? component
                    : "_" + index + "/" + component + "_";
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Component) {
                Component other = (Component)o;
                return this.index == other.index && (this.index != STRING_INDEX || this.component.equals(other.component));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return index == STRING_INDEX ? component.hashCode() : Integer.hashCode(index);
        }
    }
}
