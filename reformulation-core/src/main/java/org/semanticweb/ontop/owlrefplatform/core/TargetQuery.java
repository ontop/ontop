package org.semanticweb.ontop.owlrefplatform.core;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.owlrefplatform.core.translator.SesameConstructTemplate;

import javax.annotation.Nullable;

/**
 * Query string and signature.
 */
public class TargetQuery {

    private final String nativeQuery;
    private final ImmutableList<String> signature;
    private final SesameConstructTemplate constructTemplate;

    public TargetQuery(String nativeQuery, ImmutableList<String> signature,
                       @Nullable SesameConstructTemplate constructTemplate) {
        this.nativeQuery = nativeQuery;
        this.signature = signature;
        this.constructTemplate = constructTemplate;
    }

    public TargetQuery(String nativeQuery, ImmutableList<String> signature) {
        this(nativeQuery, signature, null);
    }

    public ImmutableList<String> getSignature() {
        return signature;
    }

    public String getNativeQueryString() {
        return nativeQuery;
    }

    public SesameConstructTemplate getConstructTemplate() {
        return constructTemplate;
    }
}
