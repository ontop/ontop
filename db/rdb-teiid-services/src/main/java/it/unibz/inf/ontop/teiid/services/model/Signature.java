package it.unibz.inf.ontop.teiid.services.model;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.teiid.metadata.BaseColumn;
import org.teiid.metadata.Column;

public final class Signature extends AbstractList<Attribute> implements Serializable {

    public static final Signature EMPTY = forAttributes();

    private static final long serialVersionUID = 1L;

    private final Attribute[] attributes;

    private transient int hash;

    private Signature(final Attribute[] attributes, final int hash) {
        this.attributes = attributes;
        this.hash = hash;
    }

    public static Signature forNames(final String... names) {
        return forNames(Arrays.asList(names));
    }

    public static Signature forNames(final Iterable<String> names) {
        return Signature.forAttributes(Iterables.transform(names, n -> Attribute.create(n)));
    }

    public static Signature forAttributes(final Attribute... attributes) {
        return forAttributes(Arrays.asList(attributes));
    }

    public static Signature forAttributes(final Iterable<Attribute> attributes) {
        final Attribute[] attrs = new Attribute[Iterables.size(attributes)];
        final Set<String> names = Sets.newIdentityHashSet();
        int i = 0;
        for (final Attribute attribute : attributes) {
            Objects.requireNonNull(attribute);
            if (!names.add(attribute.getName())) {
                throw new IllegalArgumentException(
                        "Duplicate attribute " + attribute.getName() + " in " + attributes);
            }
            attrs[i++] = attribute;
        }
        return new Signature(attrs, 0);
    }

    public static Signature forColumns(final Column... columns) {
        return forColumns(Arrays.asList(columns));
    }

    public static Signature forColumns(final Iterable<? extends BaseColumn> columns) {
        return Signature.forAttributes(Iterables.transform(columns, c -> Attribute.create(c)));
    }

    @Override
    public int size() {
        return this.attributes.length;
    }

    @Override
    public Attribute get(final int index) {
        return this.attributes[index];
    }

    public Attribute get(final String name) {
        return this.attributes[nameToIndex(name)];
    }

    public boolean has(String name) {
        return nameToIndex(name, -1) >= 0;
    }

    public int nameToIndex(final String name) {

        // Delegate and return index if successful
        final int index = nameToIndex(name, -1);
        if (index >= 0) {
            return index;
        }

        // Fail if no match
        Objects.requireNonNull(name);
        throw new NoSuchElementException("No attribute named " + name + " in signature " + this);
    }

    public int nameToIndex(final String name, final int indexIfNameMissing) {

        // First use equality, as it's likely that supplied name is interned as attribute names
        for (int i = 0; i < this.attributes.length; ++i) {
            if (this.attributes[i].getName() == name) {
                return i;
            }
        }

        // Fallback to slower equality test if a match is not found
        for (int i = 0; i < this.attributes.length; ++i) {
            if (this.attributes[i].getName().equals(name)) {
                return i;
            }
        }

        // Return fallback value
        return indexIfNameMissing;
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        } else if (object instanceof Signature) {
            final Signature other = (Signature) object;
            if (this.hash != 0 && other.hash != 0 && this.hash != other.hash) {
                return false;
            }
            return Arrays.equals(this.attributes, other.attributes);
        } else if (object instanceof List<?>) {
            final List<?> other = (List<?>) object;
            if (this.attributes.length != other.size()) {
                return false;
            }
            int i = 0;
            for (final Object otherAttribute : other) {
                final Attribute thisAttribute = this.attributes[i++];
                if (!thisAttribute.equals(otherAttribute)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.hash == 0) {
            int hash = 1;
            for (int i = 0; i < this.attributes.length; ++i) {
                hash = 31 * hash + this.attributes[i].hashCode();
            }
            this.hash = hash;
        }
        return this.hash;
    }

    public static Signature join(final Iterable<Signature> signatures) {
        final List<Attribute> attributes = Lists.newArrayList();
        for (final Signature signature : signatures) {
            outer: for (final Attribute attribute : signature) {
                for (final Attribute oldAttribute : attributes) {
                    if (oldAttribute.getName() == attribute.getName()) {
                        if (oldAttribute.getDatatype() != attribute.getDatatype()) {
                            throw new IllegalArgumentException("Type mismatch for attribute "
                                    + attribute.getName() + ": " + oldAttribute.getDatatype()
                                    + ", " + attribute.getDatatype());
                        }
                        continue outer;
                    }
                }
                attributes.add(attribute);
            }
        }
        return new Signature(attributes.toArray(new Attribute[attributes.size()]), 0);
    }

}
