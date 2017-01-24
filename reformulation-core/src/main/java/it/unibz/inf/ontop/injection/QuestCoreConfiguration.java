package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.injection.impl.QuestCoreConfigurationImpl;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * TODO: explain
 *
 */
public interface QuestCoreConfiguration extends OBDACoreConfiguration, OntopRuntimeConfiguration {

    Optional<TMappingExclusionConfig> getTmappingExclusions();

    @Override
    QuestCoreSettings getSettings();


    static Builder<? extends Builder> defaultBuilder() {
        return new QuestCoreConfigurationImpl.BuilderImpl<>();
    }

    /**
     * By default, it assumes that the A-box is virtual.
     *
     * If you want to enable the classic A-box, please use a Properties object/file.
     * Not the classic A-box mode is not intended to be used by end-users (but for test purposes).
     *
     */
    interface QuestCoreBuilderFragment<B extends Builder<B>> {
        B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config);
    }

    interface Builder<B extends Builder<B>> extends QuestCoreBuilderFragment<B>,
            OBDACoreConfiguration.Builder<B>,
            OntopRuntimeConfiguration.Builder<B> {

        @Override
        QuestCoreConfiguration build();
    }
}
