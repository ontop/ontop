package it.unibz.inf.ontop.owlrefplatform.owlapi;


import it.unibz.inf.ontop.injection.QuestConfiguration;
import org.semanticweb.owlapi.reasoner.NullReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import javax.annotation.Nonnull;

/**
 * See QuestPreferences.builder() for an high-level configuration builder.
 */
@SuppressWarnings("serial")
public class QuestOWLConfiguration extends SimpleConfiguration {

    private final QuestConfiguration questConfiguration;

    @Nonnull
    public QuestConfiguration getQuestConfiguration() {
        return questConfiguration;
    }

    public QuestOWLConfiguration(@Nonnull QuestConfiguration questConfiguration) {
        super(new NullReasonerProgressMonitor());
        this.questConfiguration = questConfiguration;
    }

    /**
     * Constructor
     */
    public QuestOWLConfiguration(@Nonnull QuestConfiguration questConfiguration,
                                 @Nonnull ReasonerProgressMonitor progressMonitor) {
        super(progressMonitor);
        this.questConfiguration = questConfiguration;
    }
}
