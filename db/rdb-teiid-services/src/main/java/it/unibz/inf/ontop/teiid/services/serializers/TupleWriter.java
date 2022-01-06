package it.unibz.inf.ontop.teiid.services.serializers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;

import it.unibz.inf.ontop.teiid.services.model.Tuple;

public interface TupleWriter extends TupleSerializer {

    void write(Writer stream, Iterable<Tuple> tuples) throws IOException;

    default void write(final OutputStream stream, final Iterable<Tuple> tuples)
            throws IOException {
        final Charset charset = getMediaType().charset().or(Charsets.UTF_8);
        write(new OutputStreamWriter(stream, charset), tuples);
    }

    default String writeString(final Iterable<Tuple> tuples) {
        try {
            final StringWriter writer = new StringWriter();
            write(writer, tuples);
            return writer.toString();
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    default byte[] writeBytes(final Iterable<Tuple> tuples) {
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            write(stream, tuples);
            return stream.toByteArray();
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    default JsonNode writeJson(Iterable<Tuple> tuples) {
        throw new UnsupportedOperationException();
    }

}
