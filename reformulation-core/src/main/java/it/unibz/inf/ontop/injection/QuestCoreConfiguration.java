package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.injection.impl.QuestCoreConfigurationImpl;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * TODO: explain
 *
 */
public interface QuestCoreConfiguration extends OBDACoreConfiguration, OntopOptimizationConfiguration {

    Optional<TMappingExclusionConfig> getTmappingExclusions();

    Optional<DBMetadata> getDatasourceMetadata();

    @Override
    QuestCoreSettings getSettings();


    static Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder>>>>>>>>>>>> defaultBuilder() {
        return new QuestCoreConfigurationImpl.BuilderImpl<>();
    }

    /**
     * By default, it assumes that the A-box is virtual.
     *
     * If you want to enable the classic A-box, please use a Properties object/file.
     * Not the classic A-box mode is not intended to be used by end-users (but for test purposes).
     *
     */
    interface QuestCoreBuilderFragment<B extends Builder> {
        B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config);

        B dbMetadata(@Nonnull DBMetadata dbMetadata);

        B enableOntologyAnnotationQuerying(boolean queryingAnnotationsInOntology);

        /**
         * In the case of SQL, inserts REPLACE functions in the generated query
         */
        B enableIRISafeEncoding(boolean enable);

        B sameAsMappings(boolean sameAsMappings);

        B enableEquivalenceOptimization(boolean enable);

        B enableExistentialReasoning(boolean enable);
    }

    interface Builder<B extends Builder> extends QuestCoreBuilderFragment<B>,
            OBDACoreConfiguration.Builder<B>,
            OntopOptimizationConfiguration.Builder<B> {

        @Override
        QuestCoreConfiguration build();
    }
}
