package it.unibz.inf.ontop.teiid.services.serializers.template;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.net.MediaType;
import com.google.common.primitives.Ints;

import it.unibz.inf.ontop.teiid.services.model.Datatype;
import it.unibz.inf.ontop.teiid.services.model.Signature;
import it.unibz.inf.ontop.teiid.services.model.Tuple;
import it.unibz.inf.ontop.teiid.services.serializers.AbstractTupleSerializer;
import it.unibz.inf.ontop.teiid.services.serializers.TupleReader;
import it.unibz.inf.ontop.teiid.services.serializers.TupleWriter;

public final class TextTemplateTupleSerializer extends AbstractTupleSerializer
        implements TupleReader, TupleWriter {

    private final MessageFormat format;

    private final int[] indexes;

    public TextTemplateTupleSerializer(final String template, final Signature signature) {

        // Initialize parent
        super(signature, MediaType.PLAIN_TEXT_UTF_8);

        // Allocate variables for MessageFormat text, slot names, corresp. attribute indexes
        final StringBuilder fmt = new StringBuilder();
        final List<String> fmtNames = Lists.newArrayList();
        final List<Integer> fmtIndexes = Lists.newArrayList();

        // Rewrite placeholders in template string from {name,type} to {slot-index,type}
        boolean inQuote = false;
        int i = 0;
        while (i < template.length()) {
            char ch = template.charAt(i++);
            fmt.append(ch);
            if (ch == '\'') {
                inQuote = !inQuote;
            } else if (ch == '{' && !inQuote) {

                // Read placeholder name
                int start = i;
                for (ch = template.charAt(i); ch != '}' && ch != ',';) {
                    ch = template.charAt(++i);
                }
                final String name = template.substring(start, i);

                // Lookup attribute index and datatype, failing if undefined
                final int index = signature.nameToIndex(name);
                final Datatype datatype = signature.get(index).getDatatype();
                Preconditions.checkArgument(datatype != null);

                // Map the datatype to a MessageFormat type, if possible
                String fmtExpectedType = null;
                if (datatype.isNumeric()) {
                    fmtExpectedType = "number";
                } else if (datatype == Datatype.DATE) {
                    fmtExpectedType = "date";
                } else if (datatype == Datatype.TIME) {
                    fmtExpectedType = "time";
                }

                // Read placeholder MessageFormat type, and check consistency with signature
                String fmtType = fmtExpectedType;
                if (ch == ',') {
                    start = ++i;
                    for (ch = template.charAt(i); ch != '}' && ch != ',';) {
                        ch = template.charAt(++i);
                    }
                    fmtType = template.substring(start, i);
                    if (fmtExpectedType != null && !fmtExpectedType.equals(fmtType)) {
                        throw new IllegalArgumentException(
                                "Incompatible template type " + fmtType + " for argument " + name
                                        + ", expected none or " + fmtExpectedType);
                    }
                }

                // Reuse or allocate a "slot" in the MessageFormat values array
                int slot = fmtNames.indexOf(name);
                if (slot < 0) {
                    slot = fmtNames.size();
                    fmtNames.add(name);
                    fmtIndexes.add(index);
                }

                // Emit MessageFormat slot number and type
                fmt.append(slot);
                if (fmtType != null) {
                    fmt.append(',').append(fmtType);
                }
            }
        }

        // Store signature, initialize MessageFormat, slot -> attribute index mappings
        this.format = new MessageFormat(fmt.toString());
        this.indexes = Ints.toArray(fmtIndexes);
    }

    int[] getIndexes() {
        return this.indexes;
    }

    @Override
    public List<Tuple> read(final Reader stream) throws IOException {
        return readString(CharStreams.toString(stream));
    }

    @Override
    public List<Tuple> readString(final String string) {
        try {
            final Object[] values = this.format.parse(string);
            final Tuple tuple = Tuple.create(getSignature());
            for (int i = 0; i < this.indexes.length; ++i) {
                tuple.set(this.indexes[i], values[i]);
            }
            return ImmutableList.of(tuple);
        } catch (final ParseException ex) {
            throw new IllegalArgumentException("Cannot parse placeholders from string '" + string
                    + "' using template " + this.format, ex);
        }
    }

    @Override
    public void write(final Writer writer, final Iterable<Tuple> tuples) throws IOException {
        writer.write(writeString(tuples));
    }

    @Override
    public String writeString(final Iterable<Tuple> tuples) {
        final Tuple tuple = Iterables.isEmpty(tuples) //
                ? Tuple.create(getSignature())
                : Iterables.getFirst(tuples, null);
        final Object[] values = new Object[this.indexes.length];
        for (int i = 0; i < this.indexes.length; ++i) {
            values[i] = tuple.get(this.indexes[i]);
        }
        return this.format.format(values);
    }

}