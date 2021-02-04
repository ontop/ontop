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

    /*
     * Ovverride equals method to ensure we can check for object equality based on determinants
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FunctionalDependencyConstruct other = (FunctionalDependencyConstruct) obj;
        return Objects.equals(determinants, other.getDeterminants());
    }

    /*
     * Ovverride hashCode method to ensure we can check for object equality based on determinants
     */
    @Override
    public int hashCode() {
        return Objects.hash(determinants);
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
