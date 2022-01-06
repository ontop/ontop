package it.unibz.inf.ontop.teiid.services.model;

import java.io.Serializable;
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
    private final Role role;

    private Attribute(final String name, @Nullable final Datatype datatype,
            @Nullable final Optional<?> defaultValue, @Nullable final Role role) {
        this.name = name;
        this.datatype = datatype;
        this.defaultValue = defaultValue;
        this.role = role;
    }

    public static Attribute create(final String name) {
        return create(name, (Datatype) null, null, null);
    }

    public static Attribute create(final String name, @Nullable final String datatype) {
        return create(name, Datatype.forName(datatype), null, null);
    }

    public static Attribute create(final String name, @Nullable final Datatype datatype) {
        return create(name, datatype, null, null);
    }

    public static Attribute create(final String name, @Nullable final String datatype,
            @Nullable final Optional<?> defaultValue, @Nullable final Role role) {
        return create(name, Datatype.forName(datatype), defaultValue, role);
    }

    public static Attribute create(final String name, @Nullable final Datatype datatype,
            @Nullable final Optional<?> defaultValue, @Nullable final Role role) {
        Objects.requireNonNull(name);
        return new Attribute(name.intern(), datatype, defaultValue, role);
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

        final String r = column.getProperty("role");
        final Role role = r == null ? null : Role.valueOf(r.trim().toUpperCase());

        return create(name, typeName, defaultValue, role);
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

    @Nullable
    public Role getRole() {
        return this.role;
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

        FILTER,

        OFFSET,

        LIMIT,

        PROJECT,

        ORDER

    }

}