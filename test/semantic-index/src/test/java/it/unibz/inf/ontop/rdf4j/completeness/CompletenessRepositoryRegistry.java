package it.unibz.inf.ontop.rdf4j.completeness;

import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import org.eclipse.rdf4j.repository.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CompletenessRepositoryRegistry {

    private final Map<RegistryKey, Repository> map;

    public CompletenessRepositoryRegistry() {
        map = new ConcurrentHashMap<>();
    }

    public Optional<Repository> getRepository(@Nullable String owlFileURL,
                                              @Nullable String parameterFileURL) {
        return Optional.ofNullable(map.get(new RegistryKey(owlFileURL, parameterFileURL)));
    }

    public void register(@Nonnull OntopRepository repository, @Nullable String owlFileURL, @Nullable String parameterFileURL) {
        map.put(new RegistryKey(owlFileURL, parameterFileURL), repository);
    }

    public void shutdown() {
        for (Repository repository: map.values())
            repository.shutDown();
        map.clear();
    }

    protected static class RegistryKey {
        @Nullable
        private final String owlFileURL;
        @Nullable
        private final String parameterFileURL;

        protected RegistryKey(@Nullable String owlFileURL,
                              @Nullable String parameterFileURL) {
            this.owlFileURL = owlFileURL;
            this.parameterFileURL = parameterFileURL;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegistryKey registryKey = (RegistryKey) o;
            return Objects.equals(owlFileURL, registryKey.owlFileURL) &&
                    Objects.equals(parameterFileURL, registryKey.parameterFileURL);
        }

        @Override
        public int hashCode() {
            return Objects.hash(owlFileURL, parameterFileURL);
        }
    }

}
