package it.unibz.inf.ontop.teiid.services.model;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import org.teiid.core.types.DataTypeManager;
import org.teiid.core.types.TransformationException;
import org.teiid.metadata.BaseColumn;

public final class Attribute implements Comparable<Attribute>, Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;

    private final Datatype datatype;

    @Nullable
    private final Optional<?> defaultValue;

    @Nullable
    private transient Role role;

    @Nullable
    private transient String target;

    private Attribute(final String name, @Nullable final Datatype datatype,
            @Nullable final Optional<?> defaultValue) {
        this.name = name;
        this.datatype = datatype;
        this.defaultValue = defaultValue;
    }

    public static Attribute create(final String name) {
        return create(name, (Datatype) null, null);
    }

    public static Attribute create(final String name, @Nullable final String datatype) {
        return create(name, Datatype.forName(datatype), null);
    }

    public static Attribute create(final String name, @Nullable final Datatype datatype) {
        return create(name, datatype, null);
    }

    public static Attribute create(final String name, @Nullable final String datatype,
            @Nullable final Optional<?> defaultValue) {
        return create(name, Datatype.forName(datatype), defaultValue);
    }

    public static Attribute create(final String name, @Nullable final Datatype datatype,
            @Nullable final Optional<?> defaultValue) {
        Objects.requireNonNull(name);
        return new Attribute(name.intern(), datatype, defaultValue);
    }

    public static Attribute create(final BaseColumn column) {

        final String name = column.getName();
        final String typeName = column.getRuntimeType();

        Object v = column.getDefaultValue();
        Optional<?> defaultValue = null;
        if (v != null) {
            Preconditions.checkArgument(typeName != null, "No data type specified for column "
                    + name + " to interpret default value '" + v + "'");
            final Class<?> typeClass = DataTypeManager.getDataTypeClass(typeName);
            try {
                v = DataTypeManager.transformValue(v, String.class, typeClass);
                defaultValue = Optional.ofNullable(v);
            } catch (final TransformationException ex) {
                throw new IllegalArgumentException("Could not convert default value '" + v
                        + "' to " + typeName + " (" + typeClass + ")");
            }
        }

        return create(name, typeName, defaultValue);
    }

    public String getName() {
        return this.name;
    }

    public Datatype getDatatype() {
        return this.datatype;
    }

    @Nullable
    public Optional<?> getDefaultValue() {
        return this.defaultValue;
    }

    public Role getRole() {
        decodeNameIfNeeded();
        return this.role;
    }

    @Nullable
    public String getTarget() {
        decodeNameIfNeeded();
        return this.target;
    }

    private void decodeNameIfNeeded() {
        if (this.role == null) {
            final Entry<Role, String> e = Role.decodeAttributeName(this.name);
            this.role = e.getKey();
            this.target = e.getValue();
        }
    }

    @Override
    public int compareTo(final Attribute other) {
        int result = this.name.compareTo(other.name);
        if (result == 0) {
            result = this.datatype == null //
                    ? other.datatype == null ? 0 : -1
                    : other.datatype == null ? 1 : this.datatype.compareTo(other.datatype);
        }
        return result;
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Attribute)) {
            return false;
        }
        final Attribute other = (Attribute) object;
        return this.name == other.name && this.datatype == other.datatype;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() * 31 + (this.datatype == null ? 0 : this.datatype.hashCode());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        if (this.datatype != null) {
            sb.append(' ').append(this.datatype);
        }
        if (this.defaultValue != null) {
            sb.append(" = ").append(this.defaultValue.orElse(null));
        }
        if (this.role != null) {
            sb.append(" {").append(this.role.name().toLowerCase()).append("}");
        }
        return sb.toString();
    }

    public enum Role {

        SELECT("__select__", false),

        SORT_BY("__sort_by__", false),

        SORT_ASC("__sort_asc__", false),

        OFFSET("__offset__", false),

        LIMIT("__limit__", false),

        MIN_INCLUSIVE("__min_inclusive__", true),

        MAX_INCLUSIVE("__max_inclusive__", true),

        MIN_EXCLUSIVE("__min_exclusive__", true),

        MAX_EXCLUSIVE("__max_exclusive__", true),

        EQUAL("", true);

        private String prefix;

        private boolean targetUsed;

        private Role(final String prefix, final boolean targetUsed) {
            this.prefix = prefix;
            this.targetUsed = targetUsed;
        }

        public String getPrefix() {
            return this.prefix;
        }

        public boolean isTargetUsed() {
            return this.targetUsed;
        }

        public static String encodeAttributeName(final Role role, @Nullable final String target) {
            Preconditions.checkArgument(role.targetUsed == (target != null));
            return role.targetUsed ? role.prefix + target : role.prefix;
        }

        public static Entry<Role, String> decodeAttributeName(final String attributeName) {
            for (final Role role : Role.values()) {
                if (!attributeName.startsWith(role.prefix)) {
                    continue;
                }
                String target = null;
                if (role.targetUsed) {
                    Preconditions.checkArgument(role.prefix.length() < attributeName.length());
                    target = attributeName.substring(role.prefix.length());
                } else {
                    Preconditions.checkArgument(role.prefix.length() == attributeName.length());
                }
                return new AbstractMap.SimpleEntry<>(role, target);
            }
            throw new Error(); // should not happen, EQUAL should always match
        }

    }

}