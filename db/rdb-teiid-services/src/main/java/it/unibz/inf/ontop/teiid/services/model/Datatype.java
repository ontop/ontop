package it.unibz.inf.ontop.teiid.services.model;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.teiid.core.types.DataTypeManager;
import org.teiid.core.types.DataTypeManager.DataTypeAliases;
import org.teiid.core.types.DataTypeManager.DefaultDataTypes;
import org.teiid.core.types.JsonType;
import org.teiid.core.types.Transform;
import org.teiid.core.types.TransformationException;

public abstract class Datatype implements Serializable, Comparable<Datatype> {

    private static final long serialVersionUID = 1L;

    // Integer datatypes

    public static final Datatype BYTE = new ScalarDatatype(ScalarDatatype.FLAG_INTEGER,
            DefaultDataTypes.BYTE, DataTypeAliases.TINYINT);

    public static final Datatype SHORT = new ScalarDatatype(ScalarDatatype.FLAG_INTEGER,
            DefaultDataTypes.SHORT, DataTypeAliases.SMALLINT);

    public static final Datatype INTEGER = new ScalarDatatype(ScalarDatatype.FLAG_INTEGER,
            DefaultDataTypes.INTEGER);

    public static final Datatype LONG = new ScalarDatatype(ScalarDatatype.FLAG_INTEGER,
            DefaultDataTypes.LONG, DataTypeAliases.BIGINT);

    public static final Datatype BIGINTEGER = new ScalarDatatype(ScalarDatatype.FLAG_INTEGER,
            DefaultDataTypes.BIG_INTEGER);

    // Float datatypes

    public static final Datatype FLOAT = new ScalarDatatype(ScalarDatatype.FLAG_FLOAT,
            DefaultDataTypes.FLOAT, DataTypeAliases.REAL);

    public static final Datatype DOUBLE = new ScalarDatatype(ScalarDatatype.FLAG_FLOAT,
            DefaultDataTypes.DOUBLE);

    public static final Datatype BIGDECIMAL = new ScalarDatatype(ScalarDatatype.FLAG_FLOAT,
            DefaultDataTypes.BIG_DECIMAL, DataTypeAliases.DECIMAL);

    // Date/time datatypes

    public static final Datatype DATE = new ScalarDatatype(0, DefaultDataTypes.DATE);

    public static final Datatype TIME = new ScalarDatatype(0, DefaultDataTypes.TIME);

    public static final Datatype TIMESTAMP = new ScalarDatatype(0, DefaultDataTypes.TIMESTAMP);

    // Boolean datatype

    public static final Datatype BOOLEAN = new ScalarDatatype(0, DefaultDataTypes.BOOLEAN);

    // Textual datatypes - non-lob

    public static final Datatype CHAR = new ScalarDatatype(0, DefaultDataTypes.CHAR);

    public static final Datatype STRING = new ScalarDatatype(0, DefaultDataTypes.STRING,
            DataTypeAliases.VARCHAR);

    // Textual datatypes - lob

    public static final Datatype CLOB = new ScalarDatatype(0, DefaultDataTypes.CLOB);

    public static final Datatype XML = new ScalarDatatype(0, DefaultDataTypes.XML);

    public static final Datatype JSON = new ScalarDatatype(0, DefaultDataTypes.JSON);

    // Binary datatypes - non-lob

    public static final Datatype VARBINARY = new ScalarDatatype(ScalarDatatype.FLAG_BINARY,
            DefaultDataTypes.VARBINARY);

    // Binary datatypes - lob

    public static final Datatype BLOB = new ScalarDatatype(ScalarDatatype.FLAG_BINARY,
            DefaultDataTypes.BLOB);

    public static final Datatype GEOMETRY = new ScalarDatatype(ScalarDatatype.FLAG_BINARY,
            DefaultDataTypes.GEOMETRY);

    public static final Datatype GEOGRAPHY = new ScalarDatatype(ScalarDatatype.FLAG_BINARY,
            DefaultDataTypes.GEOGRAPHY);

    public static final Datatype OBJECT = new ScalarDatatype(ScalarDatatype.FLAG_BINARY,
            DefaultDataTypes.OBJECT);

    // Indexes

    private static final List<Datatype> SCALAR_DATATYPES = ImmutableList.of(BYTE, SHORT, INTEGER,
            LONG, BIGINTEGER, FLOAT, DOUBLE, BIGDECIMAL, DATE, TIME, TIMESTAMP, BOOLEAN, CHAR,
            STRING, CLOB, XML, JSON, VARBINARY, BLOB, GEOMETRY, GEOGRAPHY, OBJECT);

    private static final Map<String, Datatype> SCALAR_DATATYPES_BY_NAME = //
            ImmutableMap.copyOf(SCALAR_DATATYPES.stream()
                    .collect(Collectors.toMap(dt -> dt.getName(), dt -> dt)));

    private static final Map<Class<?>, Datatype> SCALAR_DATATYPES_BY_CLASS = //
            ImmutableMap.copyOf(SCALAR_DATATYPES.stream()
                    .collect(Collectors.toMap(dt -> dt.getValueClass(), dt -> dt)));

    private static final Map<List<?>, Datatype> ARRAY_DATATYPES = Maps.newConcurrentMap();

    // Fields

    private final String name;

    private final List<String> aliases;

    private final Class<?> valueClass;

    private final Map<Class<?>, Transform> transforms;

    private Datatype(final Class<?> valueClass, final String... aliases) {
        this.name = aliases[0];
        this.aliases = ImmutableList.copyOf(aliases);
        this.valueClass = valueClass;
        this.transforms = Maps.newConcurrentMap();
    }

    public static List<Datatype> getScalarDatatypes() {
        return SCALAR_DATATYPES;
    }

    public static Datatype getArrayDatatype(final Datatype componentType, final int dimensions) {
        Preconditions.checkArgument(dimensions > 0);
        Preconditions.checkArgument(componentType instanceof ScalarDatatype);
        final List<?> key = ImmutableList.of(componentType, dimensions);
        return ARRAY_DATATYPES.computeIfAbsent(key,
                k -> new ArrayDatatype((ScalarDatatype) k.get(0), (Integer) k.get(1)));
    }

    public static Datatype forName(String name) {
        name = name.trim().toLowerCase();
        int dimensions = 0;
        while (name.endsWith("[]")) {
            name = name.substring(0, name.length() - 2);
            ++dimensions;
        }
        final Datatype dt = SCALAR_DATATYPES_BY_NAME.get(name);
        Preconditions.checkArgument(dt != null, "No datatype for " + name);
        return dimensions == 0 ? dt : getArrayDatatype(dt, dimensions);
    }

    public static Datatype forValueClass(Class<?> valueClass) {
        int dimensions = 0;
        while (valueClass.isArray()) {
            valueClass = valueClass.getComponentType();
            ++dimensions;
        }
        final Datatype dt = SCALAR_DATATYPES_BY_CLASS.get(valueClass);
        Preconditions.checkArgument(dt != null, "No datatype for " + valueClass);
        return dimensions == 0 ? dt : getArrayDatatype(dt, dimensions);
    }

    public String getName() {
        return this.name;
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    public Class<?> getValueClass() {
        return this.valueClass;
    }

    @Nullable
    public abstract Datatype getComponent();

    public abstract int getDimensions();

    public abstract boolean isArray();

    public boolean isNumeric() {
        return isInteger() || isFloat();
    }

    public abstract boolean isInteger();

    public abstract boolean isFloat();

    public abstract boolean isLob();

    public abstract boolean isBinary();

    public abstract boolean isComparable();

    @Nullable
    public Transform getTransform(final Datatype sourceDatatype) {
        return getTransform(sourceDatatype.valueClass);
    }

    @Nullable
    public Transform getTransform(final Class<?> sourceValueClass) {
        return this.transforms.computeIfAbsent(sourceValueClass, k -> {
            return DataTypeManager.getTransform(sourceValueClass, this.valueClass);
        });
    }

    public boolean canCast(final Class<?> sourceValueClass) {
        return this.valueClass.isAssignableFrom(sourceValueClass)
                || getTransform(sourceValueClass) != null;
    }

    public Object cast(@Nullable final Object sourceValue) {

        if (sourceValue == null) {
            return null;
        }

        if (this.valueClass.isInstance(sourceValue)) {
            return sourceValue;
        }

        final Transform transform = getTransform(sourceValue.getClass());
        if (transform == null) {
            throw new IllegalArgumentException(
                    "Cannot convert " + sourceValue.getClass() + " to " + this.valueClass);
        }

        try {
            return transform.transform(sourceValue, this.valueClass);
        } catch (final TransformationException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public int compareTo(final Datatype other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Nullable
    public static Object normalize(@Nullable final Object object) {
        return object == null ? null : DataTypeManager.convertToRuntimeType(object, true);
    }

    private static final void registerTransform(final Transform transform) {
        try {
            final Method method;
            method = DataTypeManager.class.getDeclaredMethod("addTransform", Transform.class);
            method.setAccessible(true);
            method.invoke(null, transform);
        } catch (final Throwable ex) {
            throw new Error(ex);
        }
    }

    static {
        registerTransform(new CustomTransform(Clob.class, JsonType.class) {

            @Override
            protected Object transformDirect(final Object value) throws TransformationException {
                return new JsonType((Clob) value);
            }

        });

        registerTransform(new CustomTransform(String.class, Date.class) {

            @Override
            protected Object transformDirect(final Object value) throws TransformationException {
                try {
                    // TODO
                    final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    return new Date(formatter.parse((String) value).getTime());
                } catch (final ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }

        });

        registerTransform(new CustomTransform(String.class, Time.class) {

            @Override
            protected Object transformDirect(final Object value) throws TransformationException {
                try {
                    // TODO
                    final DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
                    return new Time(formatter.parse((String) value).getTime());
                } catch (final ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }

        });

        registerTransform(new CustomTransform(String.class, Timestamp.class) {

            private final Pattern pattern = Pattern.compile("" //
                    + "(\\d{4})-(\\d{2})-(\\d{2})" // date
                    + "[T|t](\\d{2}):(\\d{2}):(\\d{2})" // time
                    + "(\\.\\d{1})?" // optional fractions
                    + "([Z|z]|([+|-](\\d{2}):(\\d{2})))"); // timezone

            @Override
            protected Object transformDirect(final Object value) throws TransformationException {
                final Matcher m = this.pattern.matcher((String) value);
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
                    return ts;
                } else {
                    throw new IllegalArgumentException();
                }
            }

        });

        for (final Datatype datatype : new Datatype[] { BYTE, SHORT, INTEGER, LONG, BIGINTEGER,
                FLOAT, DOUBLE, BIGDECIMAL }) {

            registerTransform(new CustomTransform(datatype.getValueClass(), Date.class) {

                @Override
                protected Object transformDirect(final Object value)
                        throws TransformationException {
                    return new Date(((Number) value).longValue());
                }

            });

            registerTransform(new CustomTransform(datatype.getValueClass(), Date.class) {

                @Override
                protected Object transformDirect(final Object value)
                        throws TransformationException {
                    return new Time(((Number) value).longValue());
                }

            });

            registerTransform(new CustomTransform(datatype.getValueClass(), Date.class) {

                @Override
                protected Object transformDirect(final Object value)
                        throws TransformationException {
                    return new Timestamp(((Number) value).longValue());
                }

            });
        }
    }

    private static final class ScalarDatatype extends Datatype {

        private static final long serialVersionUID = 1L;

        // Flags entered manually

        private static final int FLAG_INTEGER = 1 << 0;

        private static final int FLAG_FLOAT = 1 << 1;

        private static final int FLAG_BINARY = 1 << 2;

        // Flags derived from DataTypeManager

        private static final int FLAG_LOB = 1 << 3;

        private static final int FLAG_COMPARABLE = 1 << 3;

        private final int flags;

        public ScalarDatatype(final int flags, final String... aliases) {
            super(DataTypeManager.getDataTypeClass(aliases[0]), aliases);
            this.flags = flags | (DataTypeManager.isLOB(aliases[0]) ? FLAG_LOB : 0)
                    | (DataTypeManager.isNonComparable(aliases[0]) ? 0 : FLAG_COMPARABLE);
        }

        @Override
        public Datatype getComponent() {
            return null;
        }

        @Override
        public int getDimensions() {
            return 0;
        }

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public boolean isInteger() {
            return (this.flags & FLAG_INTEGER) != 0;
        }

        @Override
        public boolean isFloat() {
            return (this.flags & FLAG_FLOAT) != 0;
        }

        @Override
        public boolean isLob() {
            return (this.flags & FLAG_LOB) != 0;
        }

        @Override
        public boolean isBinary() {
            return (this.flags & FLAG_BINARY) != 0;
        }

        @Override
        public boolean isComparable() {
            return (this.flags & FLAG_COMPARABLE) != 0;
        }

        private Object writeReplace() throws ObjectStreamException {
            return new SerializedForm(getName());
        }

    }

    private static final class ArrayDatatype extends Datatype {

        private static final long serialVersionUID = 1L;

        private final ScalarDatatype component;

        private final int dimensions;

        public ArrayDatatype(final ScalarDatatype component, final int dimensions) {
            super(computeClass(component.getValueClass(), dimensions),
                    computeAliases(component.getAliases(), dimensions));
            this.component = component;
            this.dimensions = dimensions;
        }

        @Override
        public Datatype getComponent() {
            return this.component;
        }

        @Override
        public int getDimensions() {
            return this.dimensions;
        }

        @Override
        public boolean isArray() {
            return true;
        }

        @Override
        public boolean isInteger() {
            return false;
        }

        @Override
        public boolean isFloat() {
            return false;
        }

        @Override
        public boolean isLob() {
            return false;
        }

        @Override
        public boolean isBinary() {
            return false;
        }

        @Override
        public boolean isComparable() {
            return false;
        }

        private Object writeReplace() throws ObjectStreamException {
            return new SerializedForm(getName());
        }

        private static Class<?> computeClass(final Class<?> componentClass, final int dimensions) {
            Class<?> c = componentClass;
            for (int i = 0; i < dimensions; ++i) {
                c = Array.newInstance(c, 0).getClass();
            }
            return c;
        }

        private static String[] computeAliases(final List<String> componentAliases,
                final int dimensions) {
            final String[] aliases = new String[componentAliases.size()];
            for (int i = 0; i < aliases.length; ++i) {
                aliases[i] = componentAliases.get(i) + Strings.repeat("[]", dimensions);
            }
            return aliases;
        }

    }

    private static final class SerializedForm {

        private final String name;

        public SerializedForm(final String name) {
            this.name = name;
        }

        private Object readResolve() throws ObjectStreamException {
            return Datatype.forName(this.name);
        }

    }

    private static abstract class CustomTransform extends Transform {

        private final Class<?> sourceType;

        private final Class<?> targetType;

        public CustomTransform(final Class<?> sourceType, final Class<?> targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        @Override
        public Class<?> getSourceType() {
            return this.sourceType;
        }

        @Override
        public Class<?> getTargetType() {
            return this.targetType;
        }

    }

}
