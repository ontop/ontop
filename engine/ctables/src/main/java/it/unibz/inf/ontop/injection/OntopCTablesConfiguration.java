package it.unibz.inf.ontop.injection;

import java.io.File;
import java.io.Reader;
import java.util.Optional;

import javax.annotation.Nonnull;

import it.unibz.inf.ontop.ctables.Ruleset;
import it.unibz.inf.ontop.injection.impl.OntopCTablesConfigurationImpl;

public interface OntopCTablesConfiguration extends OntopSQLCredentialConfiguration {

    @Override
    OntopCTablesSettings getSettings();

    Optional<Ruleset> loadRuleset();

    static Builder<? extends Builder<?>> defaultBuilder() {
        return new OntopCTablesConfigurationImpl.BuilderImpl<>();
    }

    interface OntopCTablesBuilderFragment<B extends Builder<B>> {

        B ctablesRuleset(@Nonnull Ruleset ruleset);

        B ctablesRulesetFile(@Nonnull File mappingFilename);

        B ctablesRulesetFile(@Nonnull String mappingFilename);

        B ctablesRulesetReader(@Nonnull Reader mappingReader);

    }

    interface Builder<B extends Builder<B>>
            extends OntopCTablesBuilderFragment<B>, OntopSQLCredentialConfiguration.Builder<B> {

        @Override
        OntopCTablesConfiguration build();
    }

}