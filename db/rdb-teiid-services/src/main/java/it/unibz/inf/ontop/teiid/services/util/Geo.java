package it.unibz.inf.ontop.teiid.services.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.InputStreamInStream;
import org.locationtech.jts.io.OutputStreamOutStream;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.Assert;

public final class Geo {

    public static int dimension(final Geometry geometry) {
        final Coordinate c = geometry.getCoordinate();
        return c == null ? 0 : c.getM() != 0.0 ? 4 : c.getZ() != 0.0 ? 3 : 2;
    }

    public static Geometry fromWKB(final byte[] wkb) {
        try {
            return getWKBReader().read(wkb);
        } catch (final ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static Geometry fromWKB(final InputStream wkb) {
        try {
            return getWKBReader().read(new InputStreamInStream(wkb));
        } catch (final ParseException ex) {
            throw new IllegalArgumentException(ex);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static Geometry fromWKT(final String wkt) {
        try {
            return getWKTReader().read(wkt);
        } catch (final ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static Geometry fromWKT(final Reader wkt) {
        try {
            return getWKTReader().read(wkt);
        } catch (final ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static byte[] toWKB(final Geometry geometry) {
        return getWKBWriter(geometry).write(geometry);
    }

    public static void toWKB(final Geometry geometry, final OutputStream stream) {
        try {
            getWKBWriter(geometry).write(geometry, new OutputStreamOutStream(stream));
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String toWKT(final Geometry geometry) {
        return getWKTWriter(geometry).write(geometry);
    }

    public static void toWKT(final Geometry geometry, final Writer writer) {
        try {
            getWKTWriter(geometry).write(geometry, writer);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static WKBReader getWKBReader() {
        return new WKBReader();
    }

    private static WKTReader getWKTReader() {
        return new EWKTReader();
    }

    private static WKBWriter getWKBWriter(final Geometry geometry) {
        return new WKBWriter(dimension(geometry) >= 3 ? 3 : 2, geometry.getSRID() != 0);
    }

    private static WKTWriter getWKTWriter(final Geometry geometry) {
        final WKTWriter writer = new EWKTWriter(Math.max(2, dimension(geometry)));
        writer.setFormatted(false);
        writer.setPrecisionModel(geometry.getPrecisionModel());
        return writer;
    }

    private Geo() {
        throw new Error();
    }

    private static final class EWKTReader extends WKTReader {

        @Override
        public Geometry read(final Reader reader) throws ParseException {

            // Need a buffered reader to "peek" first 5 characters
            final BufferedReader in = reader instanceof BufferedReader //
                    ? (BufferedReader) reader
                    : new BufferedReader(reader);

            // Try to read SRID from optional EWKT prefix "SRID=<SRID>;"
            int srid = 0;
            try {
                final char[] buffer = new char[6];
                in.mark(5);
                in.read(buffer, 0, 5);
                if ("SRID=".equals(new String(buffer, 0, 5))) {
                    int len = 0;
                    for (; len == 0 || buffer[len - 1] != ';'; ++len) {
                        buffer[len] = (char) in.read();
                    }
                    srid = Integer.parseInt(new String(buffer, 0, len - 1));
                } else {
                    in.reset();
                }
            } catch (final IOException ex) {
                throw new ParseException(ex);
            }

            // Delegate reading of geometry
            final Geometry geometry = super.read(reader);

            // Apply SRID, if any, and return geometry
            if (srid != 0) {
                geometry.setSRID(srid);
            }
            return geometry;
        }

    };

    private static final class EWKTWriter extends WKTWriter {

        public EWKTWriter(final int dimension) {
            super(dimension);
        }

        @Override
        public String write(final Geometry geometry) {
            final StringWriter writer = new StringWriter();
            try {
                write(geometry, writer);
            } catch (final IOException ex) {
                Assert.shouldNeverReachHere();
            }
            return writer.toString();
        }

        @Override
        public void write(final Geometry geometry, final Writer writer) throws IOException {
            writeSrid(geometry, writer);
            super.write(geometry, writer);
        }

        @Override
        public String writeFormatted(final Geometry geometry) {
            final StringWriter writer = new StringWriter();
            try {
                writeFormatted(geometry, writer);
            } catch (final IOException ex) {
                Assert.shouldNeverReachHere();
            }
            return writer.toString();
        }

        @Override
        public void writeFormatted(final Geometry geometry, final Writer writer)
                throws IOException {
            writeSrid(geometry, writer);
            super.writeFormatted(geometry, writer);
        }

        private void writeSrid(final Geometry geometry, final Writer writer) throws IOException {
            if (geometry.getSRID() != 0) {
                writer.write("SRID=");
                writer.write(Integer.toString(geometry.getSRID()));
                writer.write(';');
            }
        }

    }

}
