package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.OntopTemporalSQLOWLAPIConfigurationImpl;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;

public interface OntopTemporalSQLOWLAPIConfiguration extends OntopSQLOWLAPIConfiguration, OntopTemporalMappingSQLAllConfiguration {

    static Builder<? extends Builder> defaultBuilder() {
        return new OntopTemporalSQLOWLAPIConfigurationImpl.BuilderImpl<>();
    }

    interface OntopTemporalSQLOWLAPIBuilderFragment<B extends Builder<B>> {
        B nativeOntopTemporalRuleFile(@Nonnull File ruleFile);
        B nativeOntopTemporalRuleFile(@Nonnull String ruleFilename);
        B nativeOntopTemporalRuleReader(@Nonnull Reader ruleReader);

        B nativeOntopTemporalRuleProgram(@Nonnull DatalogMTLProgram program);
    }

    interface Builder<B extends Builder<B>> extends OntopSQLOWLAPIConfiguration.Builder<B>,
            OntopTemporalMappingSQLAllConfiguration.Builder<B>, OntopTemporalSQLOWLAPIBuilderFragment<B> {

        @Override
        OntopTemporalSQLOWLAPIConfiguration build();

    }

}
