package it.unibz.inf.ontop.dbschema.impl.json;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class FunctionalDependencyConstruct {
    private final ImmutableSet<QuotedID> determinants;
    private final ImmutableSet<QuotedID> dependents;

    protected FunctionalDependencyConstruct(ImmutableSet<QuotedID> determinants, ImmutableSet<QuotedID> dependents) {
        this.determinants = determinants;
        this.dependents = dependents;
    }

    public ImmutableSet<QuotedID> getDeterminants() {
        return determinants;
    }

    public ImmutableSet<QuotedID> getDependents() {
        return dependents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionalDependencyConstruct that = (FunctionalDependencyConstruct) o;
        return getDeterminants().equals(that.getDeterminants()) && getDependents().equals(that.getDependents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeterminants(), getDependents());
    }

    @Override
    public String toString() {
        return "FunctionalDependencyConstruct{" +
                "determinants=" + determinants +
                ", dependents=" + dependents +
                '}';
    }

    /**
     * If FD cannot be merged with the provided FD return empty otherwise return the merged one
     */
    public Optional<FunctionalDependencyConstruct> merge(FunctionalDependencyConstruct fdConstruct) {
        if (this.determinants.equals(fdConstruct.getDeterminants())) {
            FunctionalDependencyConstruct construct = new FunctionalDependencyConstruct(
                    this.determinants,
                    Stream.of(this.dependents, fdConstruct.getDependents())
                            .flatMap(Collection::stream)
                            .collect(ImmutableCollectors.toSet())
            );
            return Optional.of(construct);
        } else {
            return Optional.empty();
        }
    }
}
