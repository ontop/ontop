package it.unibz.inf.ontop.teiid.services.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import org.teiid.core.types.ClobImpl;
import org.teiid.core.types.DataTypeManager;
import org.teiid.core.types.JsonType;
import org.teiid.core.types.Transform;
import org.teiid.core.types.TransformationException;

public final class DataTypes {

    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("" //
            + "(\\d{4})-(\\d{2})-(\\d{2})" // date
            + "[T|t](\\d{2}):(\\d{2}):(\\d{2})" // time
            + "(\\.\\d{1})?" // optional fractions
            + "([Z|z]|([+|-](\\d{2}):(\\d{2})))"); // timezone

    public static boolean canConvert(final Class<?> sourceClass, final Class<?> targetClass) {

        return targetClass.isAssignableFrom(sourceClass)
                || (targetClass.isAssignableFrom(Date.class)
                        || targetClass.isAssignableFrom(Time.class)
                        || targetClass.isAssignableFrom(Timestamp.class))
                        && (Number.class.isAssignableFrom(sourceClass)
                                || String.class.isAssignableFrom(sourceClass))
                || DataTypeManager.getTransform(sourceClass, targetClass) != null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(@Nullable final Object value, final Class<T> targetClass) {

        if (value == null) {
            return null;
        }

        if (targetClass.isAssignableFrom(value.getClass())) {
            return (T) value;
        }

        if (targetClass.isAssignableFrom(Date.class)) {
            if (value instanceof Number) {
                return (T) new Date(((Number) value).longValue());
            } else if (value instanceof String) {
                try {
                    final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    return (T) new Date(formatter.parse((String) value).getTime());
                } catch (final ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        if (targetClass.isAssignableFrom(Time.class)) {
            if (value instanceof Number) {
                return (T) new Time(((Number) value).longValue());
            } else if (value instanceof String) {
                try {
                    final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    return (T) new Time(formatter.parse((String) value).getTime());
                } catch (final ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        if (targetClass.isAssignableFrom(Timestamp.class)) {
            if (value instanceof Number) {
                return (T) new Timestamp(((Number) value).longValue());
            } else if (value instanceof String) {
                final Matcher m = TIMESTAMP_PATTERN.matcher((String) value);
                if (m.matches()) {
                    Calendar cal = null;
                    final String timeZone = m.group(8);
                    if (timeZone.equalsIgnoreCase("Z")) {
                        cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    } else {
                        cal = Calendar.getInstance(TimeZone.getTimeZone("GMT" + m.group(9)));
                    }
                    cal.set(Integer.valueOf(m.group(1)), Integer.valueOf(m.group(2)) - 1,
                            Integer.valueOf(m.group(3)), Integer.valueOf(m.group(4)),
                            Integer.valueOf(m.group(5)), Integer.valueOf(m.group(6)));
                    final Timestamp ts = new Timestamp(cal.getTime().getTime());
                    if (m.group(7) != null) {
                        final String fraction = m.group(7).substring(1);
                        ts.setNanos(Integer.parseInt(fraction));
                    } else {
                        ts.setNanos(0);
                    }
                    return (T) ts;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }

        if (targetClass.isAssignableFrom(JsonType.class) && value instanceof Clob) {
            return (T) new JsonType((Clob) value); // TODO
        }

        if (targetClass.isAssignableFrom(JsonNode.class)) {
            if (value instanceof JsonType) {
                try {
                    return (T) Json.read(((JsonType) value).getCharacterStream(), JsonNode.class);
                } catch (final SQLException ex) {
                    throw new IllegalArgumentException(ex);
                }
            } else if (value instanceof Boolean) {
                return (T) BooleanNode.valueOf(((Boolean) value).booleanValue());
            } else if (value instanceof Long || value instanceof Integer || value instanceof Short
                    || value instanceof Byte) {
                return (T) new LongNode(((Number) value).longValue());
            } else if (value instanceof Double || value instanceof Float) {
                return (T) new DoubleNode(((Number) value).doubleValue());
            } else if (value instanceof BigInteger) {
                return (T) new BigIntegerNode((BigInteger) value);
            } else if (value instanceof BigDecimal) {
                return (T) new DecimalNode((BigDecimal) value);
            } else {
                final String asText = convert(value, String.class);
                return (T) new TextNode(asText);
                // TODO: there are other cases to consider here
            }
        }

        if (value instanceof JsonNode) {
            if (JsonType.class.isAssignableFrom(targetClass)) {
                return (T) new JsonType(new ClobImpl(Json.write(value)));
            } else if (value instanceof ValueNode) {
                final ValueNode n = (ValueNode) value;
                if (n.isIntegralNumber()) {
                    return convert(n.bigIntegerValue(), targetClass);
                } else if (n.isNumber()) {
                    return convert(n.decimalValue(), targetClass);
                } else if (n.isBoolean()) {
                    return convert(n.booleanValue(), targetClass);
                } else {
                    return convert(n.asText(), targetClass);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        final Transform transform = DataTypeManager.getTransform(value.getClass(), targetClass);
        if (transform != null) {
            try {
                return (T) transform.transform(value, targetClass);
            } catch (final TransformationException e) {
                throw new IllegalArgumentException(e);
            }
        }

        throw new IllegalArgumentException("Cannot convert " + value + " to " + targetClass);
    }

}
