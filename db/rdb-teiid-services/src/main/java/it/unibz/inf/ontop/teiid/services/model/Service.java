package it.unibz.inf.ontop.teiid.services.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

import org.teiid.metadata.AbstractMetadataRecord;
import org.teiid.metadata.FunctionMethod;
import org.teiid.metadata.FunctionParameter;
import org.teiid.metadata.Procedure;

public final class Service implements Serializable, Comparable<Service> {

    private static final long serialVersionUID = 1L;

    private final String name;

    private final Signature inputSignature;

    private final Signature outputSignature;

    private final Map<String, String> properties;

    private Service(final String name, final Signature inputSignature,
            final Signature outputSignature, @Nullable final Map<String, String> properties) {
        this.name = name;
        this.inputSignature = inputSignature;
        this.outputSignature = outputSignature;
        this.properties = properties;
    }

    public static Service create(final String name, @Nullable Signature inputSignature,
            @Nullable Signature outputSignature, @Nullable Map<String, String> properties) {

        // Check mandatory name parameter
        Objects.requireNonNull(name);

        // Use empty signatures and properties, if omitted
        inputSignature = inputSignature != null ? inputSignature : Signature.EMPTY;
        outputSignature = outputSignature != null ? outputSignature : Signature.EMPTY;
        properties = properties != null ? ImmutableMap.copyOf(properties) : ImmutableMap.of();

        // Create the service
        return new Service(name, inputSignature, outputSignature, properties);
    }

    public static Service create(final AbstractMetadataRecord metadata) {

        // Read the service name from the metadata object's name property
        final String name = metadata.getName();

        // Read the service properties from the metadata object's options
        final Map<String, String> properties = ImmutableMap.copyOf(metadata.getProperties());

        // Read the service input/output signature from the metadata object's input/output columns
        final Signature inputSignature, outputSignature;
        if (metadata instanceof Procedure) {
            final Procedure p = (Procedure) metadata;
            inputSignature = Signature.forColumns(p.getParameters());
            outputSignature = Signature.forColumns(p.getResultSet().getColumns());
        } else if (metadata instanceof FunctionMethod) {
            final FunctionMethod f = (FunctionMethod) metadata;
            final FunctionParameter p = f.getOutputParameter();
            inputSignature = Signature.forColumns(f.getInputParameters());
            outputSignature = Signature.forAttributes(Attribute
                    .create(MoreObjects.firstNonNull(p.getName(), "result"), p.getRuntimeType()));
        } else {
            throw new IllegalArgumentException("Unsupported metadata object " + metadata);
        }

        // Delegate
        return create(name, inputSignature, outputSignature, properties);
    }

    public String getName() {
        return this.name;
    }

    public Signature getInputSignature() {
        return this.inputSignature;
    }

    public Signature getOutputSignature() {
        return this.outputSignature;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    @Override
    public int compareTo(final Service other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Service)) {
            return false;
        }
        final Service other = (Service) object;
        return this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    public String toString(final boolean verbose) {
        if (!verbose) {
            return this.name;
        } else {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.name).append(": ");
            sb.append(this.inputSignature).append(" -> ").append(this.outputSignature);
            sb.append(" ").append(this.properties);
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        return toString(false);
    }

}
