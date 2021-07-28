package it.unibz.inf.ontop.injection.impl;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import it.unibz.inf.ontop.ctables.spec.Ruleset;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopCTablesConfiguration;
import it.unibz.inf.ontop.injection.OntopCTablesSettings;

public class OntopCTablesConfigurationImpl extends OntopSQLCredentialConfigurationImpl
        implements OntopCTablesConfiguration {

    private final OntopCTablesSettings settings;

    private final OntopCTablesOptions options;

    OntopCTablesConfigurationImpl(final OntopCTablesSettings settings,
            final OntopCTablesOptions options) {
        super(settings, options.sqlCredentialOptions);
        this.settings = settings;
        this.options = options;
    }

    @Override
    public OntopCTablesSettings getSettings() {
        return this.settings;
    }

    @Override
    public Ruleset loadRuleset() {

        if (this.options.ctablesRuleset.isPresent()) {
            return this.options.ctablesRuleset.get();
        }

        try {
            if (this.options.ctablesRulesetFile.isPresent()) {
                final URL url = this.options.ctablesRulesetFile.get().toURI().toURL();
                return Ruleset.create(url);
            }
            if (this.options.ctablesRulesetReader.isPresent()) {
                final Reader reader = this.options.ctablesRulesetReader.get();
                return Ruleset.create(reader);
            }
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return Ruleset.create(ImmutableList.of()); // empty
    }

    static class OntopCTablesOptions {

        private final Optional<Ruleset> ctablesRuleset;

        private final Optional<File> ctablesRulesetFile;

        private final Optional<Reader> ctablesRulesetReader;

        private final OntopSQLCredentialOptions sqlCredentialOptions;

        OntopCTablesOptions(final Optional<Ruleset> ctablesRuleset,
                final Optional<File> ctablesRulesetFile,
                final Optional<Reader> ctablesRulesetReader,
                final OntopSQLCredentialOptions sqlCredentialOptions) {
            this.ctablesRuleset = ctablesRuleset;
            this.ctablesRulesetFile = ctablesRulesetFile;
            this.ctablesRulesetReader = ctablesRulesetReader;
            this.sqlCredentialOptions = sqlCredentialOptions;
        }

    }

    static class StandardCTablesBuilderFragment<B extends OntopCTablesConfiguration.Builder<B>>
            implements OntopCTablesBuilderFragment<B> {

        private final B builder;

        private Optional<Ruleset> ctablesRuleset = Optional.empty();

        private Optional<File> ctablesRulesetFile = Optional.empty();

        private Optional<Reader> ctablesRulesetReader = Optional.empty();

        private Optional<String> ctablesRefreshSchedule = Optional.empty();

        protected StandardCTablesBuilderFragment(final B builder) {
            this.builder = builder;
        }

        protected Properties generateProperties() {
            final Properties p = new Properties();
            if (this.ctablesRulesetFile.isPresent()) {
                p.setProperty(OntopCTablesSettings.CTABLES_RULESET_FILE,
                        this.ctablesRulesetFile.get().toString());
            }
            if (this.ctablesRefreshSchedule.isPresent()) {
                p.setProperty(OntopCTablesSettings.CTABLES_REFRESH_SCHEDULE,
                        this.ctablesRefreshSchedule.get());
            }
            return p;
        }

        final OntopCTablesOptions generateCTablesOptions(
                final OntopSQLCredentialOptions sqlCredentialOptions) {
            return new OntopCTablesOptions(this.ctablesRuleset, this.ctablesRulesetFile,
                    this.ctablesRulesetReader, sqlCredentialOptions);
        }

        @Override
        public B ctablesRuleset(@Nullable final Ruleset ctablesRuleset) {
            this.ctablesRuleset = Optional.ofNullable(ctablesRuleset);
            return null;
        }

        @Override
        public B ctablesRulesetFile(@Nullable final File ctablesRulesetFile) {
            this.ctablesRulesetFile = Optional.ofNullable(ctablesRulesetFile);
            return this.builder;
        }

        @Override
        public B ctablesRulesetFile(@Nullable final String ctablesRulesetFilename) {
            if (ctablesRulesetFilename == null) {
                this.ctablesRulesetFile = Optional.empty();
            } else {
                try {
                    final URI fileURI = new URI(ctablesRulesetFilename);
                    final String scheme = fileURI.getScheme();
                    if (scheme == null) {
                        this.ctablesRulesetFile = Optional.of(new File(fileURI.getPath()));
                    } else if (scheme.equals("file")) {
                        this.ctablesRulesetFile = Optional.of(new File(fileURI));
                    } else {
                        throw new InvalidOntopConfigurationException(
                                "Currently only local files are supported"
                                        + "as ctables ruleset files");
                    }
                } catch (final URISyntaxException ex) {
                    throw new InvalidOntopConfigurationException(
                            "Invalid ctables ruleset file path: " + ex.getMessage());
                }
            }
            return this.builder;
        }

        @Override
        public B ctablesRulesetReader(@Nullable final Reader ctablesRulesetReader) {
            this.ctablesRulesetReader = Optional.ofNullable(ctablesRulesetReader);
            return this.builder;
        }

        @Override
        public B ctablesRefreshSchedule(@Nullable final String ctablesRefreshSchedule) {
            this.ctablesRefreshSchedule = Optional.ofNullable(ctablesRefreshSchedule);
            return this.builder;
        }

    }

    protected abstract static class OntopCTablesBuilderMixin<B extends OntopCTablesConfiguration.Builder<B>>
            extends OntopSQLCredentialBuilderMixin<B>
            implements OntopCTablesConfiguration.Builder<B> {

        private final StandardCTablesBuilderFragment<B> localFragmentBuilder;

        OntopCTablesBuilderMixin() {
            @SuppressWarnings("unchecked")
            final B builder = (B) this;
            this.localFragmentBuilder = new StandardCTablesBuilderFragment<>(builder);
        }

        @Override
        public B ctablesRuleset(final Ruleset ctablesRuleset) {
            return this.localFragmentBuilder.ctablesRuleset(ctablesRuleset);
        }

        @Override
        public B ctablesRulesetFile(final File ctablesRulesetFile) {
            return this.localFragmentBuilder.ctablesRulesetFile(ctablesRulesetFile);
        }

        @Override
        public B ctablesRulesetFile(final String ctablesRulesetFile) {
            return this.localFragmentBuilder.ctablesRulesetFile(ctablesRulesetFile);
        }

        @Override
        public B ctablesRulesetReader(final Reader ctablesRulesetReader) {
            return this.localFragmentBuilder.ctablesRulesetReader(ctablesRulesetReader);
        }

        @Override
        public B ctablesRefreshSchedule(final String ctablesRefreshSchedule) {
            return this.localFragmentBuilder.ctablesRefreshSchedule(ctablesRefreshSchedule);
        }

        @Override
        protected Properties generateProperties() {
            final Properties p = super.generateProperties();
            p.putAll(this.localFragmentBuilder.generateProperties());
            return p;
        }

        protected OntopCTablesOptions generateOntopCTablesOptions() {
            final OntopSQLCredentialOptions sqlCredentialOptions = generateSQLCredentialOptions();
            return this.localFragmentBuilder.generateCTablesOptions(sqlCredentialOptions);
        }

    }

    public static class BuilderImpl<B extends OntopCTablesConfiguration.Builder<B>>
            extends OntopCTablesBuilderMixin<B> {

        @Override
        public OntopCTablesConfiguration build() {
            final OntopCTablesSettings settings = new OntopCTablesSettingsImpl(
                    generateProperties());
            final OntopCTablesOptions options = generateOntopCTablesOptions();
            return new OntopCTablesConfigurationImpl(settings, options);
        }

    }

}
