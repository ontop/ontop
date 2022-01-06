package it.unibz.inf.ontop.teiid.services.serializers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;

import it.unibz.inf.ontop.teiid.services.model.Tuple;

public interface TupleReader extends TupleSerializer {

    List<Tuple> read(Reader stream) throws IOException;

    default List<Tuple> read(final InputStream stream) throws IOException {
        final Charset charset = getMediaType().charset().or(Charsets.UTF_8);
        return read(new InputStreamReader(stream, charset));
    }

    default List<Tuple> readString(final String string) {
        try {
            return read(new StringReader(string));
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    default List<Tuple> readBytes(final byte[] bytes) {
        try {
            return read(new ByteArrayInputStream(bytes));
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    default List<Tuple> readJson(JsonNode json) {
        throw new UnsupportedOperationException();
    }

}
