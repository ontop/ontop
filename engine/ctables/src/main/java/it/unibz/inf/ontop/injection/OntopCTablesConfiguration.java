package it.unibz.inf.ontop.injection;

import java.io.File;
import java.io.Reader;
import java.util.Optional;

import javax.annotation.Nullable;

import it.unibz.inf.ontop.ctables.spec.Ruleset;
import it.unibz.inf.ontop.injection.impl.OntopCTablesConfigurationImpl;

public interface OntopCTablesConfiguration extends OntopSQLCredentialConfiguration {

    @Override
    OntopCTablesSettings getSettings();

    Ruleset loadRuleset();

    static Builder<? extends Builder<?>> defaultBuilder() {
        return new OntopCTablesConfigurationImpl.BuilderImpl<>();
    }

    interface OntopCTablesBuilderFragment<B extends Builder<B>> {

        B ctablesRuleset(@Nullable Ruleset ctablesRuleset);

        B ctablesRulesetFile(@Nullable File ctablesRulesetFile);

        B ctablesRulesetFile(@Nullable String ctablesRulesetFile);

        B ctablesRulesetReader(@Nullable Reader ctablesRulesetReader);

        B ctablesRefreshSchedule(@Nullable String ctablesRefreshSchedule);

    }

    interface Builder<B extends Builder<B>>
            extends OntopCTablesBuilderFragment<B>, OntopSQLCredentialConfiguration.Builder<B> {

        @Override
        OntopCTablesConfiguration build();
    }

}