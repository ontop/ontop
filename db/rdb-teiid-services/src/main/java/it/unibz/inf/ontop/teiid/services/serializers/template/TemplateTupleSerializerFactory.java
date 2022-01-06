package it.unibz.inf.ontop.teiid.services.serializers.template;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.net.MediaType;

import it.unibz.inf.ontop.teiid.services.model.Signature;
import it.unibz.inf.ontop.teiid.services.serializers.TupleReader;
import it.unibz.inf.ontop.teiid.services.serializers.TupleSerializerFactory;
import it.unibz.inf.ontop.teiid.services.serializers.TupleWriter;
import it.unibz.inf.ontop.teiid.services.util.Json;

public class TemplateTupleSerializerFactory implements TupleSerializerFactory {

    public static final String KEY = "template";

    @Override
    public boolean supportsReader(final Signature signature, final MediaType type,
            final Map<String, ?> settings) {
        return supports(signature, type, settings);
    }

    @Override
    public boolean supportsWriter(final Signature signature, final MediaType type,
            final Map<String, ?> settings) {
        return supports(signature, type, settings);
    }

    @Override
    public TupleReader createReader(final Signature signature, final MediaType type,
            final Map<String, ?> settings) {
        return create(signature, type, settings);
    }

    @Override
    public TupleWriter createWriter(final Signature signature, final MediaType type,
            final Map<String, ?> settings) {
        return create(signature, type, settings);
    }

    private boolean supports(final Signature signature, final MediaType type,
            final Map<String, ?> settings) {
        return settings.get(KEY) instanceof String && (type.is(MediaType.ANY_TEXT_TYPE)
                || type.is(MediaType.JSON_UTF_8.withoutParameters()));
    }

    @SuppressWarnings("unchecked")
    private <T> T create(final Signature signature, final MediaType type,
            final Map<String, ?> settings) {
        return type.is(MediaType.ANY_TEXT_TYPE)
                ? (T) new TextTemplateTupleSerializer((String) settings.get(KEY), signature)
                : (T) new JsonTemplateTupleSerializer(
                        Json.read((String) settings.get(KEY), JsonNode.class), signature);
    }

}
