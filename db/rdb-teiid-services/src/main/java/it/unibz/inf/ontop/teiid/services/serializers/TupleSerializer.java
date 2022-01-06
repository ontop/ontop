package it.unibz.inf.ontop.teiid.services.serializers;

import com.google.common.net.MediaType;

import it.unibz.inf.ontop.teiid.services.model.Signature;

public interface TupleSerializer {

    Signature getSignature();

    MediaType getMediaType();

}
