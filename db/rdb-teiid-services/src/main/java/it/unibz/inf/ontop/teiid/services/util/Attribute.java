package it.unibz.inf.ontop.teiid.services.util;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import org.teiid.core.types.DataTypeManager;

public final class Attribute implements Comparable<Attribute>, Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;

    @Nullable
    private final String typeName;

    @Nullable
    private transient Class<?> typeClass;

    private Attribute(final String name, @Nullable final String typeName) {
        this.name = name;
        this.typeName = typeName;
    }

    public static Attribute create(final String name) {
        return create(name, null);
    }

    public static Attribute create(final String name, @Nullable final String typeName) {
        Objects.requireNonNull(name);
        Preconditions.checkArgument(
                typeName == null || DataTypeManager.getAllDataTypeNames().contains(typeName));
        return new Attribute(name.intern(), typeName == null ? null : typeName.intern());
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public String getTypeName() {
        return this.typeName;
    }

    @Nullable
    public Class<?> getTypeClass() {
        if (this.typeClass == null && this.typeName != null) {
            this.typeClass = DataTypeManager.getDataTypeClass(this.typeName);
        }
        return this.typeClass;
    }

    @Override
    public int compareTo(final Attribute other) {
        int result = this.name.compareTo(other.name);
        if (result == 0) {
            result = this.typeName == null //
                    ? other.typeName == null ? 0 : -1
                    : other.typeName == null ? 1 : this.typeName.compareTo(other.typeName);
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
        return this.name == other.name && this.typeName == other.typeName;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() * 31 + (this.typeName == null ? 0 : this.typeName.hashCode());
    }

    @Override
    public String toString() {
        return this.typeName == null ? this.name : this.name + " " + this.typeName;
    }

}