package it.unibz.inf.ontop.teiid.services.model;

import java.io.Serializable;
import java.sql.Clob;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.teiid.core.types.BaseClobType;

import it.unibz.inf.ontop.teiid.services.util.Json;

public final class Tuple extends AbstractList<Object> implements Serializable, Cloneable {

    public static final Tuple EMPTY = create(Signature.EMPTY);

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

    public static Tuple create(final Signature signature, final JsonNode json) {
        final Tuple tuple = new Tuple(signature, new Object[signature.size()]);
        tuple.setAll(json);
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
        if (value != null) {
            final Datatype datatype = this.signature.get(index).getDatatype();
            if (value instanceof ValueNode) {
                value = Json.map(value, Object.class); // Json -> Java primitive values
            }
            if (value instanceof JsonNode) {
                value = datatype != null //
                        ? Json.map(value, datatype.getValueClass())
                        : Datatype.normalize(Json.map(value, Object.class));
            } else {
                value = datatype != null ? datatype.cast(value) : Datatype.normalize(value);
            }
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

    public void setAll(final JsonNode json) {
        if (json instanceof ObjectNode) {
            for (final Iterator<Entry<String, JsonNode>> i = json.fields(); i.hasNext();) {
                final Entry<String, JsonNode> e = i.next();
                final int index = this.signature.nameToIndex(e.getKey(), -1);
                set(index, e.getValue());
            }
        } else if (json instanceof ArrayNode) {
            final int arraySize = json.size();
            final int signatureSize = this.values.length;
            final int minSize = Math.min(arraySize, signatureSize);
            for (int index = 0; index < minSize; ++index) {
                set(index, json.get(index));
            }
        } else {
            Preconditions.checkArgument(this.signature.size() == 1,
                    "Json object or array required for non-unary tuple signature");
            set(0, json);
        }
    }

    public Tuple project(final Signature signature) {
        if (signature.equals(this.signature)) {
            return clone();
        } else {
            final Tuple tuple = Tuple.create(signature);
            for (int i = 0; i < signature.size(); ++i) {
                final int index = this.signature.nameToIndex(signature.get(i).getName(), -1);
                if (index >= 0) {
                    tuple.set(i, get(index));
                }
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
            final Datatype outputDatatype = outputAttr.getDatatype();
            for (int j = 0; j < inputSignatures.length; ++j) {
                final int k = inputSignatures[j].nameToIndex(outputAttr.getName(), -1);
                if (k >= 0) {
                    final Attribute inputAttr = inputSignatures[j].get(k);
                    final Datatype inputDatatype = inputAttr.getDatatype();
                    if (outputDatatype != null && inputDatatype != null
                            && !outputDatatype.canCast(inputDatatype.getValueClass())) {
                        throw new IllegalArgumentException("Cannot convert " + outputAttr.getName()
                                + " from " + inputAttr.getDatatype() + " to "
                                + outputAttr.getDatatype());
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
                result.set(i, tuples[inputTupleIndexes[i]].get(inputArgumentIndexes[i]));
            }
            return result;
        };
    }

    public static Map<Tuple, List<Tuple>> groupBy(final Signature signature,
            final Iterable<Tuple> tuples) {

        if (signature.isEmpty()) {
            return ImmutableMap.of(Tuple.EMPTY, tuples instanceof List<?> //
                    ? (List<Tuple>) tuples
                    : ImmutableList.copyOf(tuples));
        } else {
            Map<Tuple, List<Tuple>> groups = Maps.newLinkedHashMap();
            for (final Tuple tuple : tuples) {
                final Tuple key = tuple.project(signature);
                groups.computeIfAbsent(key, k -> Lists.newArrayList()).add(tuple);
            }
            return groups;
        }
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

    public JsonNode toJson() {
        return toJson(JsonNode.class);
    }

    public <T extends JsonNode> T toJson(final Class<T> nodeType) {

        if (this.values.length == 1 && !ContainerNode.class.isAssignableFrom(nodeType)) {
            final JsonNode node = Json.map(this.values[0], JsonNode.class);
            return nodeType.cast(node); // may fail if incompatible nodeType was requested

        } else if (nodeType.isAssignableFrom(ObjectNode.class)) {
            final ObjectNode node = Json.getNodeFactory().objectNode();
            for (int i = 0; i < this.values.length; ++i) {
                final Object value = this.values[i];
                if (value != null) {
                    node.set(this.signature.get(i).getName(), Json.map(value, JsonNode.class));
                }
            }
            return nodeType.cast(node);

        } else if (nodeType.isAssignableFrom(ArrayNode.class)) {
            final ArrayNode node = Json.getNodeFactory().arrayNode();
            for (int i = 0; i < this.values.length; ++i) {
                final Object value = this.values[i];
                node.add(value == null ? NullNode.getInstance() : Json.map(value, JsonNode.class));
            }
            return nodeType.cast(node);

        } else {
            throw new ClassCastException("Cannot convert to " + nodeType);
        }
    }

    public String toString(final boolean includeNames) {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < this.values.length; ++i) {
            if (i > 0) {
                sb.append(',').append(' ');
            }
            Object v = this.values[i];

            if (includeNames) {
                sb.append(this.signature.get(i).getName()).append('=');
            }

            if (v instanceof Clob) {
                try {
                    v = BaseClobType.getString((Clob) v);
                } catch (final Throwable ex) {
                    v = "<error>";
                }
            }
            if (v == null) {
                v = "<null>";
            }
            String s = v.toString();
            s = s.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
            if (s.length() > 64) {
                s = s.substring(0, 61) + "...";
            }
            sb.append(s);
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.toString(false);
    }

}
