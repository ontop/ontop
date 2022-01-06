package it.unibz.inf.ontop.teiid.services.model;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

public final class Table extends AbstractList<Tuple>
        implements RandomAccess, Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private final Signature signature;

    private List<Tuple> tuples;

    private Table(final Signature signature, final List<Tuple> tuples) {
        this.signature = signature;
        this.tuples = tuples;
    }

    public static Table create(final Signature signature) {
        return create(signature, null);
    }

    public static Table create(final Signature signature, @Nullable List<Tuple> tuples) {
        Objects.requireNonNull(signature);
        tuples = tuples != null ? tuples : new ArrayList<>();
        return new Table(signature, tuples);
    }

    public Signature getSignature() {
        return this.signature;
    }

    @Override
    public int size() {
        return this.tuples.size();
    }

    @Override
    public Tuple get(final int index) {
        return this.tuples.get(index);
    }

    @Override
    public Tuple set(final int index, final Tuple tuple) {
        return this.tuples.set(index, tuple);
    }

    @Override
    public void add(final int index, final Tuple tuple) {
        this.tuples.add(index, tuple);
    }

    @Override
    public Tuple remove(final int index) {
        return this.tuples.remove(index);
    }

    @Override
    public Table subList(final int fromIndex, final int toIndex) {
        return new Table(this.signature, this.tuples.subList(fromIndex, toIndex));
    }

    public Table subTable(final int fromIndex, final int toIndex) {
        return new Table(this.signature, this.tuples.subList(fromIndex, toIndex));
    }

    @Override
    public Table clone() {
        try {
            final Table clone = (Table) super.clone();
            if (!(this.tuples instanceof ImmutableList)) {
                clone.tuples = new ArrayList<>(this.tuples);
            }
            return clone;
        } catch (final CloneNotSupportedException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Table)) {
            return false;
        }
        final Table other = (Table) object;
        return this.signature.equals(other.signature) && this.tuples.equals(other.tuples);
    }

    @Override
    public int hashCode() {
        return this.signature.hashCode() * 37 + this.tuples.hashCode();
    }

    public String toString(final boolean includeNames) {
        if (!includeNames) {
            return this.tuples.toString();
        }
        final StringBuilder sb = new StringBuilder();
        if (this.tuples.isEmpty()) {
            sb.append("[] <signature: ");
            sb.append(this.signature);
            sb.append(">");
        } else {
            sb.append("[ ");
            String prefix = "";
            for (final Tuple tuple : this.tuples) {
                sb.append(prefix);
                sb.append(tuple.toString(true));
                prefix = ",\n  ";
            }
            sb.append(" ]");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(false);
    }

}
