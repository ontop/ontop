package it.unibz.inf.ontop.teiid.services.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.teiid.core.types.DataTypeManager.DefaultDataTypes;

public final class JsonTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    // Matches {<name>,json,<type>} with type optional
    private static final Pattern CUSTOM_PLACEHOLDER_PATTERN = Pattern.compile("[{]([^,]+)[,]json"
            + "(?:[,](boolean|double|float|real|long|integer|short|byte|tinyint|smallint|biginteger))?[}]");

    private final JsonNode template;

    @Nullable
    private transient List<Path> paths;

    private transient boolean scalar;

    @Nullable
    private transient Signature signature;

    @Nullable
    private transient Signature signatureFixed;

    @Nullable
    private transient Signature signatureVariable;

    public JsonTemplate(final String template) {
        this(Json.read(template, JsonNode.class));
    }

    public JsonTemplate(final JsonNode template) {
        this.template = Objects.requireNonNull(template);
        processTemplateIfNeeded();
    }

    /**
     * Returns true if this template formats/parses exactly one tuple.
     * 
     * @return true, if this template deals with a scalar (1 tuple) input/output
     */
    public boolean isScalar() {
        processTemplateIfNeeded();
        return this.scalar;
    }

    /**
     * Returns the signature of tuples formatted/parsed by this template.
     * 
     * @return the tuples' signature
     */
    public Signature getSignature() {
        processTemplateIfNeeded();
        return this.signature;
    }

    /**
     * Returns the fixed part of the signature, i.e., the attributes that must have fixed values
     * in all the tuples formatted/parsed by this template. For scalar (see {@link #isScalar()})
     * templates, the fixed signature corresponds to the whole signature.
     * 
     * @return the fixed part of the signature
     */
    public Signature getSignatureFixed() {
        processTemplateIfNeeded();
        return this.signatureFixed;
    }

    /**
     * Returns the variable part of the signature, i.e., the attributes whose values may change
     * along tuples formatted/parsed by this template. For scalar (see {@link #isScalar()})
     * templates, the variable signature is empty.
     * 
     * @return the variable part of the signature
     */
    public Signature getSignatureVariable() {
        processTemplateIfNeeded();
        return this.signatureVariable;
    }

    public JsonNode format(final List<Tuple> tuples) {

        processTemplateIfNeeded();

        final JsonNode result = this.template.deepCopy();

        for (final Path path : this.paths) {
            path.set(result, tuples);
        }

        return result;
    }

    public List<Tuple> parse(final JsonNode json) {

        processTemplateIfNeeded();

        final List<List<Tuple>> pathsTuples = Lists.newArrayList();

        int size = 0;
        for (final Path path : this.paths) {
            final List<Tuple> tuples = Lists.newArrayList();
            path.get(json, tuples);
            pathsTuples.add(tuples);
            if (size > 1 && tuples.size() > 1 && tuples.size() != size) {
                throw new IllegalArgumentException(
                        "Incompatible number of extracted tuples: expected 0, 1, or " + size
                                + ", got " + tuples.size());
            }
            size = Math.max(size, tuples.size());
        }

        final List<Tuple> result = Lists.newArrayListWithCapacity(size);
        for (int i = 0; i < size; ++i) {
            final Tuple tuple = Tuple.create(this.signature);
            for (final List<Tuple> pathTuples : pathsTuples) {
                final int index = pathTuples.size() > 1 ? i : pathTuples.size() - 1;
                if (index >= 0) {
                    final Tuple pathTuple = pathTuples.get(index);
                    final Signature pathTupleSignature = pathTuple.signature();
                    for (int j = 0; j < pathTuple.size(); ++j) {
                        tuple.set(pathTupleSignature.get(j).getName(), pathTuple.get(j));
                    }
                }
            }
            result.add(tuple);
        }

        return result;
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof JsonTemplate)) {
            return false;
        }
        final JsonTemplate other = (JsonTemplate) object;
        return this.template.equals(other.template);
    }

    @Override
    public int hashCode() {
        return this.template.hashCode();
    }

    @Override
    public String toString() {
        return this.template.toPrettyString();
    }

    private void processTemplateIfNeeded() {
        if (this.paths == null) {
            this.paths = processTemplateRecursive(this.template, true);
            this.signature = Signature
                    .join(Iterables.transform(this.paths, p -> p.getSignature()));
            this.signatureFixed = Signature.join(Iterables.transform(
                    Iterables.filter(this.paths, p -> p.scalar), p -> p.getSignature()));
            this.signatureVariable = Signature.join(Iterables.transform(
                    Iterables.filter(this.paths, p -> !p.scalar), p -> p.getSignature()));
            this.scalar = this.signatureVariable.isEmpty();
        }
    }

    private static List<Path> processTemplateRecursive(final JsonNode node,
            final boolean mayEmitIteration) {

        if (node instanceof ValueNode) {
            final String text = node.asText();
            if (text.indexOf('{') >= 0) {
                final Matcher m = CUSTOM_PLACEHOLDER_PATTERN.matcher(text.trim().toLowerCase());
                if (m.matches()) {
                    final String name = m.group(1);
                    final String type = m.group(2);
                    if (type == null) {
                        return ImmutableList.of(Path.createJsonPlaceholder(name));
                    } else if (type.equals("boolean")) {
                        return ImmutableList.of(Path.createBooleanPlaceholder(name));
                    } else if (type.equals("double") || type.equals("float")
                            || type.equals("real")) {
                        return ImmutableList.of(Path.createDecimalPlaceholder(name));
                    } else {
                        return ImmutableList.of(Path.createIntegerPlaceholder(name));
                    }
                }
                final StringTemplate template = new StringTemplate(text);
                if (!template.signature().isEmpty()) {
                    return ImmutableList.of(Path.createStringPlaceholder(template));
                }
            }
            return ImmutableList.of();

        } else if (node instanceof ObjectNode) {
            List<Path> result = ImmutableList.of();
            for (final Iterator<Entry<String, JsonNode>> i = node.fields(); i.hasNext();) {
                final Entry<String, JsonNode> e = i.next();
                final String field = e.getKey();
                final JsonNode childNode = e.getValue();
                final List<Path> childPaths = processTemplateRecursive(childNode,
                        mayEmitIteration);
                for (final Path path : childPaths) {
                    result = !result.isEmpty() ? result : Lists.newArrayList();
                    result.add(Path.createObjectField(field, path));
                }
            }
            return result;

        } else if (node instanceof ArrayNode) {
            List<Path> result = ImmutableList.of();
            for (int i = 0; i < node.size(); ++i) {
                final JsonNode childNode = node.get(i);
                final List<Path> childPaths = processTemplateRecursive(childNode,
                        mayEmitIteration && node.size() != 1);
                for (final Path path : childPaths) {
                    result = !result.isEmpty() ? result : Lists.newArrayList();
                    result.add(mayEmitIteration && node.size() == 1
                            ? Path.createArrayIteration(node, path)
                            : Path.createArrayElement(i, path));
                }
            }
            return result;
        }

        return ImmutableList.of();
    }

    private static abstract class Path {

        private final Signature signature;

        private final boolean scalar;

        private Path(final Signature signature, final boolean scalar) {
            this.signature = Objects.requireNonNull(signature);
            this.scalar = scalar;
        }

        final Signature getSignature() {
            return this.signature;
        }

        final boolean isScalar() {
            return this.scalar;
        }

        abstract void get(final JsonNode node, List<Tuple> tuples);

        abstract JsonNode set(final JsonNode node, List<Tuple> tuples);

        static Path createJsonPlaceholder(final String name) {
            return new Path(Signature.create(Attribute.create(name, DefaultDataTypes.JSON)),
                    true) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    tuples.add(Tuple.create(getSignature(), node));
                }

                @Override
                JsonNode set(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final String name = getSignature().get(0).getName();
                    return !tuples.isEmpty()
                            ? DataTypes.convert(tuples.get(0).get(name), JsonNode.class)
                            : null;
                }

            };
        }

        static Path createIntegerPlaceholder(final String name) {
            return new Path(Signature.create(Attribute.create(name, DefaultDataTypes.BIG_INTEGER)),
                    true) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final BigInteger value;
                    if (!(node instanceof ValueNode)) {
                        value = null;
                    } else if (node.isNumber()) {
                        value = node.bigIntegerValue();
                    } else {
                        final String text = node.asText(null);
                        value = text != null ? new BigInteger(text) : null;
                    }
                    tuples.add(Tuple.create(getSignature(), value));
                }

                @Override
                JsonNode set(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final String name = getSignature().get(0).getName();
                    final BigInteger value = !tuples.isEmpty() //
                            ? DataTypes.convert(tuples.get(0).get(name), BigInteger.class)
                            : null;
                    return value != null //
                            ? new BigIntegerNode(value)
                            : NullNode.getInstance();
                }

            };
        }

        static Path createDecimalPlaceholder(final String name) {
            return new Path(Signature.create(Attribute.create(name, DefaultDataTypes.BIG_DECIMAL)),
                    true) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final BigDecimal value;
                    if (!(node instanceof ValueNode)) {
                        value = null;
                    } else if (node.isNumber()) {
                        value = node.decimalValue();
                    } else {
                        final String text = node.asText(null);
                        value = text != null ? new BigDecimal(text) : null;
                    }
                    tuples.add(Tuple.create(getSignature(), value));
                }

                @Override
                JsonNode set(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final String name = getSignature().get(0).getName();
                    final BigDecimal value = !tuples.isEmpty() //
                            ? DataTypes.convert(tuples.get(0).get(name), BigDecimal.class)
                            : null;
                    return value != null //
                            ? new DecimalNode(value)
                            : NullNode.getInstance();
                }

            };
        }

        static Path createBooleanPlaceholder(final String name) {
            return new Path(Signature.create(Attribute.create(name, DefaultDataTypes.BOOLEAN)),
                    true) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final Boolean value = node instanceof ValueNode && !node.isNull()
                            && !node.isMissingNode() //
                                    ? ((ValueNode) node).asBoolean()
                                    : null;
                    tuples.add(Tuple.create(getSignature(), value));
                }

                @Override
                JsonNode set(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final String name = getSignature().get(0).getName();
                    final Boolean value = !tuples.isEmpty()
                            ? DataTypes.convert(tuples.get(0).get(name), Boolean.class)
                            : null;
                    return value != null //
                            ? BooleanNode.valueOf(value) //
                            : NullNode.getInstance();
                }

            };
        }

        static Path createStringPlaceholder(final StringTemplate template) {
            return new Path(template.signature(), true) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final String text = node instanceof ValueNode ? node.asText(null) : null;
                    tuples.add(text != null //
                            ? template.parse(text)
                            : Tuple.create(template.signature()));
                }

                @Override
                JsonNode set(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final String text = !tuples.isEmpty() //
                            ? template.format(tuples.get(0)) //
                            : null;
                    return text != null //
                            ? new TextNode(text) //
                            : NullNode.getInstance();
                }

            };
        }

        static Path createObjectField(final String field, final Path next) {
            Objects.requireNonNull(field);
            return new Path(next.getSignature(), next.isScalar()) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final JsonNode childNode = node != null ? node.get(field) : null;
                    next.get(childNode, tuples);
                }

                @Override
                JsonNode set(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final ObjectNode n = node instanceof ObjectNode ? (ObjectNode) node
                            : Json.getNodeFactory().objectNode();
                    final JsonNode oldChildNode = n.get(field);
                    final JsonNode newChildNode = next.set(oldChildNode, tuples);
                    if (newChildNode != oldChildNode) {
                        n.set(field, newChildNode);
                    }
                    return n;
                }

            };
        }

        static Path createArrayElement(final int index, final Path next) {
            return new Path(next.getSignature(), next.isScalar()) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final JsonNode childNode = node != null ? node.get(index) : null;
                    next.get(childNode, tuples);
                }

                @Override
                JsonNode set(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final ArrayNode n = node instanceof ArrayNode ? (ArrayNode) node
                            : Json.getNodeFactory().arrayNode();
                    final JsonNode oldChildNode = index < n.size() ? n.get(index) : null;
                    final JsonNode newChildNode = next.set(oldChildNode, tuples);
                    if (newChildNode != oldChildNode) {
                        while (n.size() <= index) {
                            n.add(NullNode.getInstance());
                        }
                        n.set(index, oldChildNode);
                    }
                    return n;
                }

            };
        }

        static Path createArrayIteration(final JsonNode childTemplate, final Path next) {
            Objects.requireNonNull(childTemplate);
            return new Path(next.getSignature(), false) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    for (final JsonNode childNode : node != null ? node
                            : MissingNode.getInstance()) {
                        next.get(childNode, tuples);
                    }
                }

                @Override
                JsonNode set(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final ArrayNode n = node instanceof ArrayNode ? (ArrayNode) node
                            : Json.getNodeFactory().arrayNode();
                    while (n.size() > tuples.size()) {
                        n.remove(n.size() - 1);
                    }
                    for (int i = 0; i < tuples.size(); ++i) {
                        final JsonNode oldChildNode = i < n.size() ? n.get(i) : null;
                        final JsonNode newChildNode = next.set(
                                oldChildNode != null ? oldChildNode : childTemplate.deepCopy(),
                                ImmutableList.of(tuples.get(i)));
                        if (newChildNode != oldChildNode) {
                            if (i < n.size()) {
                                n.set(i, newChildNode);
                            } else {
                                n.add(newChildNode);
                            }
                        }
                    }
                    return n;
                }

            };
        }
    }

}
