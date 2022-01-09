package it.unibz.inf.ontop.teiid.services.serializers.jstl;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.common.net.MediaType;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.Parser;

import it.unibz.inf.ontop.teiid.services.model.Signature;
import it.unibz.inf.ontop.teiid.services.serializers.TupleReader;
import it.unibz.inf.ontop.teiid.services.serializers.TupleSerializerFactory;
import it.unibz.inf.ontop.teiid.services.serializers.TupleWriter;

public final class JstlTupleSerializerFactory implements TupleSerializerFactory {

    public static final String KEY_JSTL_IN = "jstlIn";

    public static final String KEY_JSTL_OUT = "jstlOut";

    public static final String KEY_JSTL_OUT_TYPE = "jstlOutType";

    @Override
    public boolean supportsReader(final Signature signature, final MediaType mediaType,
            final Map<String, ?> settings) {
        return settings.get(KEY_JSTL_IN) instanceof String && supportsMediaType(mediaType);
    }

    @Override
    public boolean supportsWriter(final Signature signature, final MediaType mediaType,
            final Map<String, ?> settings) {
        return settings.get(KEY_JSTL_OUT) instanceof String && supportsMediaType(mediaType);
    }

    @Override
    public TupleReader createReader(final Signature signature, final MediaType mediaType,
            final Map<String, ?> settings) {

        // Compile JSTL expression for mapping custom tuple JSON to normalized tuple JSON
        final Expression jstl = compile((String) settings.get(KEY_JSTL_IN));

        // Build a JSTL tuple reader for the parameters supplied
        return new JstlTupleReader(jstl, signature, mediaType);
    }

    @Override
    public TupleWriter createWriter(final Signature signature, final MediaType mediaType,
            final Map<String, ?> settings) {

        // Compile JSTL expression for mapping normalized tuple JSON to custom tuple JSON
        final Expression jstl = compile((String) settings.get(KEY_JSTL_OUT));

        // Extract JSON node type to use in normalized tuple JSON (if specified, default object)
        Class<? extends JsonNode> nodeType = null;
        String nodeTypeStr = (String) settings.get(KEY_JSTL_OUT_TYPE);
        if (nodeTypeStr != null) {
            nodeTypeStr = nodeTypeStr.trim().toLowerCase();
            if (nodeTypeStr.equals("object")) {
                nodeType = ObjectNode.class;
            } else if (nodeTypeStr.equals("array")) {
                nodeType = ArrayNode.class;
            } else if (nodeTypeStr.equals("value")) {
                nodeType = ValueNode.class;
            } else {
                throw new IllegalArgumentException("Unsupported node type " + nodeTypeStr
                        + " (expected: 'object', 'array', or 'value')");
            }
        }

        // Build a JSTL tuple writer for the parameters supplied
        return new JstlTupleWriter(jstl, signature, mediaType, nodeType);
    }

    public static Expression compile(final String jstl) {
        return Parser.compileString(jstl); // TODO consider injecting functions
    }

    private boolean supportsMediaType(final MediaType mediaType) {
        return mediaType.is(MediaType.ANY_TEXT_TYPE)
                || mediaType.is(MediaType.JSON_UTF_8.withoutParameters());
    }

}
