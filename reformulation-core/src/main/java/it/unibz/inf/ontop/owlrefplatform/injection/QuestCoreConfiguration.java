package it.unibz.inf.ontop.owlrefplatform.injection;

import it.unibz.inf.ontop.injection.OBDACoreConfiguration;
import it.unibz.inf.ontop.model.DataSourceMetadata;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.owlrefplatform.injection.impl.QuestCoreConfigurationImpl;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * TODO: explain
 *
 */
public interface QuestCoreConfiguration extends OBDACoreConfiguration {

    Optional<TMappingExclusionConfig> getTmappingExclusions();

    Optional<DataSourceMetadata> getDatasourceMetadata();

    QuestCorePreferences getPreferences();


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
    interface Builder<B extends Builder> extends OBDACoreConfiguration.Builder<B> {

        B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config);

        B dbMetadata(@Nonnull DataSourceMetadata dbMetadata);

        B enableOntologyAnnotationQuerying(boolean queryingAnnotationsInOntology);

        /**
         * In the case of SQL, inserts REPLACE functions in the generated query
         */
        B enableIRISafeEncoding(boolean enable);

        B sameAsMappings(boolean sameAsMappings);

        B enableEquivalenceOptimization(boolean enable);

        B enableExistentialReasoning(boolean enable);

        QuestCoreConfiguration build();
    }

}
