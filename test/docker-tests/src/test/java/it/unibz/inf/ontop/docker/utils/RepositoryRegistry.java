package it.unibz.inf.ontop.docker.utils;

import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import org.eclipse.rdf4j.repository.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RepositoryRegistry {

    private final Map<RegistryKey, Repository> map;

    public RepositoryRegistry() {
        map = new ConcurrentHashMap<>();
    }

    public Optional<Repository> getRepository(@Nullable String owlFileURL,
                                              @Nonnull String obdaFileURL,
                                              @Nullable String parameterFileURL) {
        return Optional.ofNullable(map.get(new RegistryKey(owlFileURL, obdaFileURL, parameterFileURL)));
    }

    public void register(@Nonnull OntopRepository repository, @Nullable String owlFileURL,
                         @Nonnull String obdaFileURL, @Nullable String parameterFileURL) {
        map.put(new RegistryKey(owlFileURL, obdaFileURL, parameterFileURL), repository);
    }

    public void shutdown() {
        for (Repository repository: map.values())
            repository.shutDown();
        map.clear();
    }

    protected static class RegistryKey {
        @Nullable
        private final String owlFileURL;
        @Nonnull
        private final String obdaFileURL;
        @Nullable
        private final String parameterFileURL;

        protected RegistryKey(@Nullable String owlFileURL,
                              @Nonnull String obdaFileURL,
                              @Nullable String parameterFileURL) {
            this.owlFileURL = owlFileURL;
            this.obdaFileURL = obdaFileURL;
            this.parameterFileURL = parameterFileURL;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegistryKey registryKey = (RegistryKey) o;
            return Objects.equals(owlFileURL, registryKey.owlFileURL) &&
                    Objects.equals(obdaFileURL, registryKey.obdaFileURL) &&
                    Objects.equals(parameterFileURL, registryKey.parameterFileURL);
        }

        @Override
        public int hashCode() {
            return Objects.hash(owlFileURL, obdaFileURL, parameterFileURL);
        }
    }

}
