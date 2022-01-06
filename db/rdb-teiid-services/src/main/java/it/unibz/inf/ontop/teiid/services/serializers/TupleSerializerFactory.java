package it.unibz.inf.ontop.teiid.services.serializers;

import java.util.Map;
import java.util.ServiceLoader;

import com.google.common.collect.Iterables;
import com.google.common.net.MediaType;

import it.unibz.inf.ontop.teiid.services.model.Signature;

public interface TupleSerializerFactory {

    TupleSerializerFactory DEFAULT_INSTANCE = compose(
            ServiceLoader.load(TupleSerializerFactory.class));

    boolean supportsReader(Signature signature, MediaType type, Map<String, ?> settings);

    boolean supportsWriter(Signature signature, MediaType type, Map<String, ?> settings);

    TupleReader createReader(Signature signature, MediaType type, Map<String, ?> settings);

    TupleWriter createWriter(Signature signature, MediaType type, Map<String, ?> settings);

    static TupleSerializerFactory compose(final Iterable<TupleSerializerFactory> instances) {
        final int size = Iterables.size(instances);
        if (size == 1) {
            return Iterables.getFirst(instances, null);
        } else {
            final TupleSerializerFactory[] delegates = Iterables.toArray(instances,
                    TupleSerializerFactory.class);
            return new TupleSerializerFactory() {

                @Override
                public boolean supportsReader(final Signature signature, final MediaType type,
                        final Map<String, ?> settings) {
                    for (final TupleSerializerFactory delegate : delegates) {
                        if (delegate.supportsReader(signature, type, settings)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public boolean supportsWriter(final Signature signature, final MediaType type,
                        final Map<String, ?> settings) {
                    for (final TupleSerializerFactory delegate : delegates) {
                        if (delegate.supportsWriter(signature, type, settings)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public TupleReader createReader(final Signature signature, final MediaType type,
                        final Map<String, ?> settings) {
                    for (final TupleSerializerFactory delegate : delegates) {
                        if (delegate.supportsReader(signature, type, settings)) {
                            return delegate.createReader(signature, type, settings);
                        }
                    }
                    throw new IllegalArgumentException("Cannot read tuples for signature "
                            + signature + ", media type " + type + ", and settings " + settings);
                }

                @Override
                public TupleWriter createWriter(final Signature signature, final MediaType type,
                        final Map<String, ?> settings) {
                    for (final TupleSerializerFactory delegate : delegates) {
                        if (delegate.supportsWriter(signature, type, settings)) {
                            return delegate.createWriter(signature, type, settings);
                        }
                    }
                    throw new IllegalArgumentException("Cannot write tuples for signature "
                            + signature + ", media type " + type + ", and settings " + settings);
                }

            };
        }
    }

}
