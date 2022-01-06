package it.unibz.inf.ontop.teiid.services.serializers;

import java.util.Objects;

import com.google.common.net.MediaType;

import it.unibz.inf.ontop.teiid.services.model.Signature;

public abstract class AbstractTupleSerializer implements TupleSerializer {

    private final Signature signature;

    private final MediaType mediaType;

    protected AbstractTupleSerializer(final Signature signature, final MediaType mediaType) {
        this.signature = Objects.requireNonNull(signature);
        this.mediaType = Objects.requireNonNull(mediaType);
    }

    @Override
    public Signature getSignature() {
        return this.signature;
    }

    @Override
    public MediaType getMediaType() {
        return this.mediaType;
    }

}
