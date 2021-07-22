package it.unibz.inf.ontop.teiid.services.util;

import java.io.Serializable;
import java.sql.Clob;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

import org.teiid.core.types.BaseClobType;

public final class Tuple extends AbstractList<Object> implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private final Signature signature;

    private Object[] values;

    private Tuple(final Signature signature, final Object[] values) {
        this.signature = signature;
        this.values = values;
    }

    public static Tuple create(final Signature signature) {
        return new Tuple(signature, new Object[signature.size()]);
    }

    public static Tuple create(final Signature signature, final Object... values) {
        final Tuple tuple = new Tuple(signature, new Object[signature.size()]);
        tuple.setAll(values);
        return tuple;
    }

    public static Tuple create(final Signature signature, final Iterable<?> values) {
        final Tuple tuple = new Tuple(signature, new Object[signature.size()]);
        tuple.setAll(values);
        return tuple;
    }

    public static Tuple create(final Signature signature, final Map<String, ?> values) {
        final Tuple tuple = new Tuple(signature, new Object[signature.size()]);
        tuple.setAll(values);
        return tuple;
    }

    public Signature signature() {
        return this.signature;
    }

    @Override
    public int size() {
        return this.values.length;
    }

    @Override
    @Nullable
    public Object get(final int index) {
        return this.values[index];
    }

    @Nullable
    public Object get(@Nullable final String name) {
        return get(this.signature.nameToIndex(name));
    }

    @Override
    @Nullable
    public Object set(final int index, @Nullable Object value) {
        final Object oldValue = this.values[index];
        final Class<?> typeClass = this.signature.get(index).getTypeClass();
        if (value != null && !typeClass.isInstance(value)) {
            value = DataTypes.convert(value, typeClass);
        }
        this.values[index] = value;
        return oldValue;
    }

    @Nullable
    public Object set(@Nullable final String name, @Nullable final Object value) {
        return set(this.signature.nameToIndex(name), value);
    }

    public void setAll(final Object... values) {
        setAll(Arrays.asList(values));
    }

    public void setAll(final Iterable<Object> values) {
        final int size = Iterables.size(values);
        if (this.values.length != size) {
            throw new IllegalArgumentException(
                    "Wrong number of values: expected " + this.values.length + ", got " + size);
        }
        int i = 0;
        for (final Object value : values) {
            set(i++, value);
        }
    }

    public void setAll(final Map<String, ?> values) {
        for (final Entry<String, ?> e : values.entrySet()) {
            set(e.getKey(), e.getValue());
        }
    }

    public Tuple project(final Signature signature) {
        if (signature.equals(this.signature)) {
            return clone();
        } else {
            final Tuple tuple = Tuple.create(signature);
            for (int i = 0; i < signature.size(); ++i) {
                final String name = signature.get(i).getName();
                tuple.set(i, get(name));
            }
            return tuple;
        }
    }

    public static Tuple project(final Signature outputSignature, final Tuple... inputTuples) {

        final Signature[] inputSignatures = new Signature[inputTuples.length];
        for (int i = 0; i < inputTuples.length; ++i) {
            inputSignatures[i] = inputTuples[i].signature();
        }

        return projectFunction(outputSignature, inputSignatures).apply(inputTuples);
    }

    public static Function<Tuple[], Tuple> projectFunction(final Signature outputSignature,
            final Signature... inputSignatures) {

        final int[] inputTupleIndexes = new int[outputSignature.size()];
        final int[] inputArgumentIndexes = new int[outputSignature.size()];
        for (int i = 0; i < outputSignature.size(); ++i) {
            final Attribute outputAttr = outputSignature.get(i);
            for (int j = 0; j < inputSignatures.length; ++j) {
                final int k = inputSignatures[j].nameToIndex(outputAttr.getName(), -1);
                if (k >= 0) {
                    final Attribute inputAttr = inputSignatures[j].get(k);
                    if (!DataTypes.canConvert(inputAttr.getTypeClass(),
                            outputAttr.getTypeClass())) {
                        throw new IllegalArgumentException("Cannot convert " + outputAttr.getName()
                                + " from " + inputAttr.getTypeName() + " to "
                                + outputAttr.getTypeName());
                    }
                    inputTupleIndexes[i] = j;
                    inputArgumentIndexes[i] = k;
                    break;
                }
            }
        }

        return (final Tuple[] tuples) -> {
            final Tuple result = Tuple.create(outputSignature);
            for (int i = 0; i < result.size(); ++i) {
                result.set(i, tuples[inputTupleIndexes[i]].get(inputTupleIndexes[i]));
            }
            return result;
        };
    }

    @Override
    public Tuple clone() {
        try {
            final Tuple tuple = (Tuple) super.clone();
            tuple.values = tuple.values.clone();
            return tuple;
        } catch (final CloneNotSupportedException ex) {
            throw new Error(ex); // not expected
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < this.values.length; ++i) {
            if (i > 0) {
                sb.append(',').append(' ');
            }
            final Object v = this.values[i];

            if (v == null) {
                sb.append("null");
            } else if (v instanceof Clob) {
                try {
                    final String s = BaseClobType.getString((Clob) v);
                    sb.append(s);
                } catch (final Throwable ex) {
                    sb.append("<error>");
                }
            } else {
                sb.append(v.toString());
            }
        }
        sb.append(']');
        return sb.toString();
    }

}
