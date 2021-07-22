package it.unibz.inf.ontop.teiid.services.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.teiid.core.types.DataTypeManager.DefaultDataTypes;

// TODO
// - boolean property indicating whether multiple tuples can be formatted/parsed
// - signatureFixed indicating which fields must have a unique value
// - support for JSON values

public final class JsonTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Pattern CUSTOM_PLACEHOLDER_PATTERN = Pattern.compile("[{]([^,]+)[,]json"
            + "(?:[,](boolean|double|float|real|long|integer|short|byte|tinyint|smallint|biginteger))?[}]");

    private final JsonNode template;

    @Nullable
    private transient List<Path> paths;

    @Nullable
    private transient Signature signature;

    public JsonTemplate(final String template) {
        this(Json.read(template, JsonNode.class));
    }

    public JsonTemplate(final JsonNode template) {
        this.template = Objects.requireNonNull(template);
        processTemplateIfNeeded();
    }

    public Signature signature() {
        processTemplateIfNeeded();
        return this.signature;
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
        if (this.paths == null || this.signature == null) {
            this.paths = processTemplateRecursive(this.template, true);
            this.signature = Signature.join(Iterables.transform(this.paths, p -> p.signature()));
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
                        return ImmutableList.of(Path.createDoublePlaceholder(name));
                    } else {
                        return ImmutableList.of(Path.createLongPlaceholder(name));
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

        private Path(final Signature signature) {
            this.signature = Objects.requireNonNull(signature);
        }

        final Signature signature() {
            return this.signature;
        }

        abstract void get(final JsonNode node, List<Tuple> tuples);

        abstract JsonNode set(final JsonNode node, List<Tuple> tuples);

        static Path createJsonPlaceholder(final String name) {
            return new Path(Signature.create(Attribute.create(name, DefaultDataTypes.JSON))) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    tuples.add(Tuple.create(signature(), node));
                }

                @Override
                JsonNode set(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final String name = signature().get(0).getName();
                    return !tuples.isEmpty()
                            ? DataTypes.convert(tuples.get(0).get(name), JsonNode.class)
                            : null;
                }

            };
        }

        static Path createLongPlaceholder(final String name) {
            return new Path(
                    Signature.create(Attribute.create(name, DefaultDataTypes.BIG_DECIMAL))) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    tuples.add(Tuple.create(signature(),
                            node instanceof ValueNode ? ((ValueNode) node).asLong() : null));
                }

                @Override
                JsonNode set(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final String name = signature().get(0).getName();
                    final BigDecimal value = !tuples.isEmpty() //
                            ? DataTypes.convert(tuples.get(0).get(name), BigDecimal.class)
                            : null;
                    return value != null //
                            ? new LongNode(value.longValue())
                            : NullNode.getInstance();
                }

            };
        }

        static Path createDoublePlaceholder(final String name) {
            return new Path(
                    Signature.create(Attribute.create(name, DefaultDataTypes.BIG_DECIMAL))) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    tuples.add(Tuple.create(signature(), node instanceof ValueNode //
                            ? new BigDecimal(((ValueNode) node).asText())
                            : null));
                }

                @Override
                JsonNode set(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final String name = signature().get(0).getName();
                    final BigDecimal value = !tuples.isEmpty() //
                            ? DataTypes.convert(tuples.get(0).get(name), BigDecimal.class)
                            : null;
                    return value != null //
                            ? new DoubleNode(value.doubleValue())
                            : NullNode.getInstance();
                }

            };
        }

        static Path createBooleanPlaceholder(final String name) {
            return new Path(Signature.create(Attribute.create(name, DefaultDataTypes.BOOLEAN))) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    tuples.add(Tuple.create(signature(),
                            node instanceof ValueNode ? ((ValueNode) node).asBoolean() : null));
                }

                @Override
                JsonNode set(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final String name = signature().get(0).getName();
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
            Objects.requireNonNull(template);
            return new Path(template.signature()) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    tuples.add(node instanceof ValueNode //
                            ? template.parse(node.asText())
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
            Objects.requireNonNull(next);
            return new Path(next.signature()) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final JsonNode childNode = node != null ? node.get(field) : null;
                    next.get(childNode, tuples);
                }

                @Override
                JsonNode set(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final ObjectNode n = node instanceof ObjectNode ? (ObjectNode) node
                            : JsonNodeFactory.instance.objectNode();
                    JsonNode childNode = n.get(field);
                    childNode = next.set(childNode, tuples);
                    n.set(field, childNode);
                    return n;
                }

            };
        }

        static Path createArrayElement(final int index, final Path next) {
            Objects.requireNonNull(next);
            return new Path(next.signature()) {

                @Override
                void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final JsonNode childNode = node != null ? node.get(index) : null;
                    next.get(childNode, tuples);
                }

                @Override
                JsonNode set(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final ArrayNode n = node instanceof ArrayNode ? (ArrayNode) node
                            : JsonNodeFactory.instance.arrayNode();
                    JsonNode childNode = index < n.size() ? n.get(index) : null;
                    childNode = next.set(childNode, tuples);
                    while (n.size() <= index) {
                        n.add(NullNode.getInstance());
                    }
                    n.set(index, childNode);
                    return n;
                }

            };
        }

        static Path createArrayIteration(final JsonNode childTemplate, final Path next) {
            Objects.requireNonNull(childTemplate);
            Objects.requireNonNull(next);
            return new Path(next.signature()) {

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
                            : JsonNodeFactory.instance.arrayNode();
                    while (n.size() > tuples.size()) {
                        n.remove(n.size() - 1);
                    }
                    for (int i = 0; i < tuples.size(); ++i) {
                        JsonNode childNode = i < n.size() ? n.get(i) : childTemplate.deepCopy();
                        childNode = next.set(childNode, ImmutableList.of(tuples.get(i)));
                        if (i < n.size()) {
                            n.set(i, childNode);
                        } else {
                            n.add(childNode);
                        }
                    }
                    return n;
                }

            };
        }
    }

}
