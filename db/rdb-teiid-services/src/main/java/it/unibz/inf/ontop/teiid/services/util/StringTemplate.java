package it.unibz.inf.ontop.teiid.services.util;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import org.teiid.core.types.DataTypeManager.DefaultDataTypes;

public final class StringTemplate implements Serializable, Comparable<StringTemplate> {

    private static final long serialVersionUID = 1L;

    private final String template;

    private transient MessageFormat format;

    private transient Signature signature;

    public StringTemplate(final String template) {
        this.template = Objects.requireNonNull(template);
        processTemplateIfNeeded();
    }

    public Signature signature() {
        return this.signature;
    }

    public String format(final Tuple arguments) {

        processTemplateIfNeeded();

        final Object[] values = new Object[this.signature.size()];
        for (int i = 0; i < values.length; ++i) {
            final Attribute attr = this.signature.get(i);
            final Object value = arguments.get(attr.getName());
            values[i] = DataTypes.convert(value, attr.getTypeClass());
        }
        return this.format.format(values);
    }

    public Tuple parse(final String string) {

        processTemplateIfNeeded();

        try {
            final Object[] values = this.format.parse(string);
            return Tuple.create(this.signature, values);
        } catch (final ParseException ex) {
            throw new IllegalArgumentException("Cannot parse placeholders from string '" + string
                    + "' using template " + this.format, ex);
        }
    }

    @Override
    public int compareTo(final StringTemplate other) {
        return this.template.compareTo(other.template);
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof StringTemplate)) {
            return false;
        }
        final StringTemplate other = (StringTemplate) object;
        return this.template.equals(other.template);
    }

    @Override
    public int hashCode() {
        return this.template.hashCode();
    }

    @Override
    public String toString() {
        return this.template;
    }

    private void processTemplateIfNeeded() {

        if (this.format != null && this.signature != null) {
            return;
        }

        final List<String> names = Lists.newArrayList();
        final List<String> types = Lists.newArrayList();
        final StringBuilder sb = new StringBuilder();
        boolean inQuote = false;
        int i = 0;
        while (i < this.template.length()) {
            char ch = this.template.charAt(i++);
            sb.append(ch);
            if (ch == '\'') {
                inQuote = !inQuote;
            } else if (ch == '{' && !inQuote) {

                int start = i;
                for (ch = this.template.charAt(i); ch != '}' && ch != ',';) {
                    ch = this.template.charAt(++i);
                }
                final String name = this.template.substring(start, i);
                int index = names.indexOf(name);
                if (index < 0) {
                    index = names.size();
                    names.add(name);
                    types.add(null);
                }
                sb.append(index);

                if (ch == ',') {
                    start = ++i;
                    for (ch = this.template.charAt(i); ch != '}' && ch != ',';) {
                        ch = this.template.charAt(++i);
                    }
                    final String type = this.template.substring(start, i);
                    sb.append(',').append(type);
                    String typeName = null;
                    if (type.equals("number")) {
                        typeName = DefaultDataTypes.BIG_DECIMAL;
                    } else if (type.equals("date")) {
                        typeName = DefaultDataTypes.DATE;
                    } else if (type.equals("time")) {
                        typeName = DefaultDataTypes.TIME;
                    }
                    if (typeName != null) {
                        final String oldTypeName = types.get(index);
                        if (oldTypeName != null && !oldTypeName.equals(typeName)) {
                            throw new IllegalArgumentException("Incompatible types for argument "
                                    + name + ": " + oldTypeName + ", " + typeName);
                        }
                        types.set(index, typeName);
                    }
                }
            }
        }

        final Attribute[] attrs = new Attribute[names.size()];
        for (i = 0; i < attrs.length; ++i) {
            attrs[i] = Attribute.create(names.get(i),
                    MoreObjects.firstNonNull(types.get(i), DefaultDataTypes.STRING));
        }

        this.format = new MessageFormat(sb.toString());
        this.signature = Signature.create(attrs);
    }

}
