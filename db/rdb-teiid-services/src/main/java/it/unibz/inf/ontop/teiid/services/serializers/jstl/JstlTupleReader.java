package it.unibz.inf.ontop.teiid.services.serializers.jstl;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.net.MediaType;
import com.schibsted.spt.data.jslt.Expression;

import it.unibz.inf.ontop.teiid.services.model.Signature;
import it.unibz.inf.ontop.teiid.services.model.Tuple;
import it.unibz.inf.ontop.teiid.services.serializers.AbstractTupleSerializer;
import it.unibz.inf.ontop.teiid.services.serializers.TupleReader;
import it.unibz.inf.ontop.teiid.services.util.Json;

public final class JstlTupleReader extends AbstractTupleSerializer implements TupleReader {

    private final Expression jstl;

    public JstlTupleReader(final Expression jstl, final Signature signature) {
        this(jstl, signature, null);
    }

    public JstlTupleReader(final Expression jstl, final Signature signature,
            @Nullable final MediaType mediaType) {

        // Initialize parent class
        super(signature, MoreObjects.firstNonNull(mediaType, MediaType.JSON_UTF_8));

        // Store JSTL expression
        this.jstl = Objects.requireNonNull(jstl);

        // Check media type is plain text or JSON
        Preconditions.checkArgument(mediaType == null || mediaType.is(MediaType.ANY_TEXT_TYPE)
                || mediaType.is(MediaType.JSON_UTF_8));

    }

    @Override
    public List<Tuple> read(final Reader stream) throws IOException {

        // Parse / wrap input into a JsonNode tree
        final JsonNode json = getMediaType().is(MediaType.ANY_TEXT_TYPE)
                ? new TextNode(CharStreams.toString(stream))
                : Json.read(stream, JsonNode.class);

        // Delegate conversion of JsonNode tree into tuples
        return readJson(json);
    }

    @Override
    public List<Tuple> readString(final String string) {

        // Parse / wrap input into a JsonNode tree
        final JsonNode json = getMediaType().is(MediaType.ANY_TEXT_TYPE) ? new TextNode(string)
                : Json.read(string, JsonNode.class);

        // Delegate conversion of JsonNode tree into tuples
        return readJson(json);
    }

    @Override
    public List<Tuple> readJson(final JsonNode json) {

        // Apply JSTL transformation to normalize tree into a supported tuples JSON encoding
        final JsonNode normalizedJson = this.jstl.apply(json);

        // Parse tuples from normalized JSON
        final List<Tuple> tuples = Lists.newArrayList();
        for (final JsonNode tupleJson : normalizedJson) {
            tuples.add(Tuple.create(getSignature(), tupleJson));
        }
        return tuples;
    }

}