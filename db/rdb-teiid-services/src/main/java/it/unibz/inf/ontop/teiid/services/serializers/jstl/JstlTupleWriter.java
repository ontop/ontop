package it.unibz.inf.ontop.teiid.services.serializers.jstl;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.net.MediaType;
import com.schibsted.spt.data.jslt.Expression;

import it.unibz.inf.ontop.teiid.services.model.Signature;
import it.unibz.inf.ontop.teiid.services.model.Tuple;
import it.unibz.inf.ontop.teiid.services.serializers.AbstractTupleSerializer;
import it.unibz.inf.ontop.teiid.services.serializers.TupleWriter;
import it.unibz.inf.ontop.teiid.services.util.Json;

public final class JstlTupleWriter extends AbstractTupleSerializer implements TupleWriter {

    private final Expression jstl;

    private final Class<? extends JsonNode> normalizedNodeType;

    public JstlTupleWriter(final Expression jstl, final Signature signature) {
        this(jstl, signature, null, null);
    }

    public JstlTupleWriter(final Expression jstl, final Signature signature,
            final MediaType mediaType,
            @Nullable final Class<? extends JsonNode> normalizedNodeClass) {

        // Initialize parent class
        super(signature, MoreObjects.firstNonNull(mediaType, MediaType.JSON_UTF_8));

        // Store JSTL expression and normalized node type (default = JSON object)
        this.jstl = jstl;
        this.normalizedNodeType = MoreObjects.firstNonNull(normalizedNodeClass, ObjectNode.class);

        // Check media type is plain text or JSON
        Preconditions.checkArgument(mediaType == null || mediaType.is(MediaType.ANY_TEXT_TYPE)
                || mediaType.is(MediaType.JSON_UTF_8));
    }

    @Override
    public void write(final Writer writer, final Iterable<Tuple> tuples) throws IOException {

        // Delegate generation of JSON tree
        final JsonNode json = writeJson(tuples);

        // Write to output
        if (getMediaType().is(MediaType.ANY_TEXT_TYPE)) {
            Preconditions.checkArgument(json instanceof ValueNode);
            writer.write(json.asText());
        } else {
            Json.write(json, writer);
        }
    }

    @Override
    public String writeString(final Iterable<Tuple> tuples) {

        // Delegate generation of JSON tree
        final JsonNode json = writeJson(tuples);

        // Write to output
        if (getMediaType().is(MediaType.ANY_TEXT_TYPE)) {
            Preconditions.checkArgument(json instanceof ValueNode);
            return json.asText();
        } else {
            return Json.write(json);
        }
    }

    @Override
    public JsonNode writeJson(final Iterable<Tuple> tuples) {

        // Build a normalized JSON representation of tuples, using configured JSON node type
        final ArrayNode normalizedJson = Json.getNodeFactory().arrayNode();
        for (final Tuple tuple : tuples) {
            normalizedJson.add(tuple.toJson(this.normalizedNodeType));
        }

        // Apply JSLT transformation and return generated JsonNode tree
        return this.jstl.apply(normalizedJson);
    }

}