package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.krdb.sql.ImplicitDBConstraintsReader;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import javax.annotation.Nonnull;

@SuppressWarnings("serial")
public class QuestOWLConfiguration extends SimpleConfiguration {

    private final TMappingExclusionConfig excludeFromTMappings;

    private final ImplicitDBConstraintsReader userConstraints;

    @Nonnull
    private final OBDAModel obdaModel;

    private final QuestPreferences preferences;

    @Nonnull
    public TMappingExclusionConfig getExcludeFromTMappings() {
        return excludeFromTMappings;
    }

    public ImplicitDBConstraintsReader getUserConstraints() {
        return userConstraints;
    }

    @Nonnull
    public OBDAModel getObdaModel() {
        return obdaModel;
    }

    @Nonnull
    public QuestPreferences getPreferences() {
        return preferences;
    }

    private QuestOWLConfiguration(Builder builder) {
        super();

        if (builder.excludeFromTMappings == null)
            excludeFromTMappings = TMappingExclusionConfig.empty();
        else
            excludeFromTMappings = builder.excludeFromTMappings;

        if (builder.preferences == null)
            preferences = new QuestPreferences();
        else
            preferences = builder.preferences;

        userConstraints = builder.userConstraints;
        obdaModel = builder.obdaModel;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static final class Builder {
        private TMappingExclusionConfig excludeFromTMappings;
        private ImplicitDBConstraintsReader userConstraints;
        private OBDAModel obdaModel;
        private QuestPreferences preferences;

        private Builder() {
        }

        public Builder tMappingExclusionConfig(TMappingExclusionConfig val) {
            excludeFromTMappings = val;
            return this;
        }

        public Builder dbConstraintsReader(ImplicitDBConstraintsReader val) {
            userConstraints = val;
            return this;
        }

        public Builder obdaModel(OBDAModel val) {
            obdaModel = val;
            return this;
        }

        public Builder preferences(QuestPreferences val) {
            preferences = val;
            return this;
        }

        public QuestOWLConfiguration build() {
            return new QuestOWLConfiguration(this);
        }
    }
}
