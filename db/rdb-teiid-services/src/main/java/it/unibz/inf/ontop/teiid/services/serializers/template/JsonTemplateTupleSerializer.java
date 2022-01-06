package it.unibz.inf.ontop.teiid.services.serializers.template;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.net.MediaType;

import it.unibz.inf.ontop.teiid.services.model.Datatype;
import it.unibz.inf.ontop.teiid.services.model.Signature;
import it.unibz.inf.ontop.teiid.services.model.Tuple;
import it.unibz.inf.ontop.teiid.services.serializers.AbstractTupleSerializer;
import it.unibz.inf.ontop.teiid.services.serializers.TupleReader;
import it.unibz.inf.ontop.teiid.services.serializers.TupleWriter;
import it.unibz.inf.ontop.teiid.services.util.Json;

public final class JsonTemplateTupleSerializer extends AbstractTupleSerializer
        implements TupleReader, TupleWriter {

    // Matches {<name>,json,<type>} with type optional
    private static final Pattern CUSTOM_PLACEHOLDER_PATTERN = Pattern.compile("[{]([^,]+)[,]json"
            + "(?:[,](boolean|double|float|real|long|integer|short|byte|tinyint|smallint|biginteger))?[}]");

    private final JsonNode template;

    @Nullable
    private final List<JsonTemplateTupleSerializer.Path> paths;

    public JsonTemplateTupleSerializer(final JsonNode template, final Signature signature) {
        super(signature, MediaType.JSON_UTF_8);
        this.template = Objects.requireNonNull(template);
        this.paths = processTemplate(this.template, true);
    }

    private List<JsonTemplateTupleSerializer.Path> processTemplate(final JsonNode node,
            final boolean mayEmitIteration) {

        if (node instanceof ValueNode) {
            final String text = node.asText();
            if (text.indexOf('{') >= 0) {
                final Matcher m = CUSTOM_PLACEHOLDER_PATTERN.matcher(text.trim().toLowerCase());
                if (!m.matches()) {
                    final TextTemplateTupleSerializer template = new TextTemplateTupleSerializer(
                            text, getSignature());
                    return ImmutableList.of(Path.createTextTemplatePlaceholder(template));
                } else {
                    final String name = m.group(1);
                    final String type = m.group(2);
                    final int index = getSignature().nameToIndex(name);
                    final Datatype datatype = type != null ? Datatype.forName(type) : null;
                    return ImmutableList
                            .of(Path.createJsonPlaceholder(getSignature(), index, datatype));
                }
            }
            return ImmutableList.of();

        } else if (node instanceof ObjectNode) {
            List<JsonTemplateTupleSerializer.Path> result = ImmutableList.of();
            for (final Iterator<Entry<String, JsonNode>> i = node.fields(); i.hasNext();) {
                final Entry<String, JsonNode> e = i.next();
                final String field = e.getKey();
                final JsonNode childNode = e.getValue();
                final List<JsonTemplateTupleSerializer.Path> childPaths = processTemplate(
                        childNode, mayEmitIteration);
                for (final JsonTemplateTupleSerializer.Path path : childPaths) {
                    result = !result.isEmpty() ? result : Lists.newArrayList();
                    result.add(Path.createObjectField(field, path));
                }
            }
            return result;

        } else if (node instanceof ArrayNode) {
            List<JsonTemplateTupleSerializer.Path> result = ImmutableList.of();
            for (int i = 0; i < node.size(); ++i) {
                final JsonNode childNode = node.get(i);
                final List<JsonTemplateTupleSerializer.Path> childPaths = processTemplate(
                        childNode, mayEmitIteration && node.size() != 1);
                for (final JsonTemplateTupleSerializer.Path path : childPaths) {
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

    @Override
    public List<Tuple> read(final Reader stream) throws IOException {
        return readJson(Json.read(stream, JsonNode.class));
    }

    @Override
    public List<Tuple> readString(final String string) {
        return readJson(Json.read(string, JsonNode.class));
    }

    @Override
    public List<Tuple> readJson(final JsonNode json) {
        final List<Tuple> tuples = Lists.newArrayList();
        for (final JsonTemplateTupleSerializer.Path path : this.paths) {
            path.get(json, tuples);
        }
        return tuples;
    }

    @Override
    public void write(final Writer writer, final Iterable<Tuple> tuples) throws IOException {
        Json.write(writeJson(tuples), writer);
    }

    @Override
    public String writeString(final Iterable<Tuple> tuples) {
        return Json.write(writeJson(tuples));
    }

    @Override
    public JsonNode writeJson(final Iterable<Tuple> tuples) {
        final JsonNode result = this.template.deepCopy();
        for (final JsonTemplateTupleSerializer.Path path : this.paths) {
            path.set(result, tuples);
        }
        return result;
    }

    private static abstract class Path {

        private final Signature signature;

        private Path(final Signature signature) {
            this.signature = signature;
        }

        void assign(final List<Tuple> tuples, final int index, final Object value) {
            if (tuples.isEmpty()) {
                final Tuple tuple = Tuple.create(this.signature);
                tuple.set(index, value);
                tuples.add(tuple);
            } else {
                for (final Tuple tuple : tuples) {
                    tuple.set(index, value);
                }
            }
        }

        void resize(final List<Tuple> tuples, final int size) {
            if (size > 1 && tuples.size() > 1 && tuples.size() != size) {
                throw new IllegalArgumentException(
                        "Incompatible number of extracted tuples: expected 0, 1, or " + size
                                + ", got " + tuples.size());
            } else if (!tuples.isEmpty()) {
                final Tuple template = tuples.get(0);
                while (tuples.size() < size) {
                    tuples.add(template.clone());
                }
            } else {
                while (tuples.size() < size) {
                    tuples.add(Tuple.create(this.signature));
                }
            }
        }

        public abstract void get(final JsonNode node, List<Tuple> tuples);

        public abstract JsonNode set(final JsonNode node, Iterable<Tuple> tuples);

        public static JsonTemplateTupleSerializer.Path createTextTemplatePlaceholder(
                final TextTemplateTupleSerializer template) {
            return new Path(template.getSignature()) {

                @Override
                public void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final String text = node instanceof ValueNode ? node.asText(null) : null;
                    final Tuple tuple = text != null ? template.readString(text).get(0) : null;
                    for (final int index : template.getIndexes()) {
                        assign(tuples, index, tuple != null ? tuple.get(index) : null);
                    }
                }

                @Override
                public JsonNode set(@Nullable final JsonNode node, final Iterable<Tuple> tuples) {
                    final String value = template.writeString(tuples);
                    return value != null //
                            ? new TextNode(value) //
                            : NullNode.getInstance();
                }

            };
        }

        public static JsonTemplateTupleSerializer.Path createJsonPlaceholder(
                final Signature signature, final int index, @Nullable final Datatype asDatatype) {

            final Datatype dtTuple = signature.get(index).getDatatype();
            final Datatype dtJson = asDatatype != null ? asDatatype : dtTuple;

            return new Path(signature) {

                @Override
                public void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    Object value = Json.map(node, dtJson.getValueClass());
                    value = dtJson == dtTuple ? value : dtTuple.cast(value);
                    assign(tuples, index, value);
                }

                @Override
                public JsonNode set(@Nullable final JsonNode node, final Iterable<Tuple> tuples) {
                    Object value = !Iterables.isEmpty(tuples)
                            ? Iterables.getFirst(tuples, null).get(index)
                            : null;
                    value = dtJson == dtTuple ? value : dtJson.cast(value);
                    return value != null //
                            ? Json.map(value, JsonNode.class)
                            : NullNode.getInstance();
                }

            };
        }

        public static JsonTemplateTupleSerializer.Path createObjectField(final String field,
                final JsonTemplateTupleSerializer.Path next) {
            return new Path(next.signature) {

                @Override
                public void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final JsonNode childNode = node != null ? node.get(field) : null;
                    next.get(childNode, tuples);
                }

                @Override
                public JsonNode set(@Nullable final JsonNode node, final Iterable<Tuple> tuples) {
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

        public static JsonTemplateTupleSerializer.Path createArrayElement(final int index,
                final JsonTemplateTupleSerializer.Path next) {
            return new Path(next.signature) {

                @Override
                public void get(@Nullable final JsonNode node, final List<Tuple> tuples) {
                    final JsonNode childNode = node != null ? node.get(index) : null;
                    next.get(childNode, tuples);
                }

                @Override
                public JsonNode set(@Nullable final JsonNode node, final Iterable<Tuple> tuples) {
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

        public static JsonTemplateTupleSerializer.Path createArrayIteration(
                final JsonNode childTemplate, final JsonTemplateTupleSerializer.Path next) {
            return new Path(next.signature) {

                @Override
                public void get(@Nullable JsonNode node, final List<Tuple> tuples) {
                    node = node != null ? node : MissingNode.getInstance();
                    resize(tuples, node.size());
                    int i = 0;
                    for (final JsonNode childNode : node) {
                        next.get(childNode, tuples.subList(i, i + 1));
                        ++i;
                    }
                }

                @Override
                public JsonNode set(@Nullable final JsonNode node, final Iterable<Tuple> tuples) {
                    final ArrayNode n = node instanceof ArrayNode ? (ArrayNode) node
                            : Json.getNodeFactory().arrayNode();
                    final int size = Iterables.size(tuples);
                    while (n.size() > size) {
                        n.remove(n.size() - 1);
                    }
                    int i = 0;
                    for (final Tuple tuple : tuples) {
                        final JsonNode oldChildNode = i < n.size() ? n.get(i) : null;
                        final JsonNode newChildNode = next.set(
                                oldChildNode != null ? oldChildNode : childTemplate.deepCopy(),
                                ImmutableList.of(tuple));
                        if (newChildNode != oldChildNode) {
                            if (i < n.size()) {
                                n.set(i, newChildNode);
                            } else {
                                n.add(newChildNode);
                            }
                        }
                        ++i;
                    }
                    return n;
                }

            };
        }

    }

}