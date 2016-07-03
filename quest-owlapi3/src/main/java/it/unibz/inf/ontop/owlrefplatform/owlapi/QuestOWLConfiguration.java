package it.unibz.inf.ontop.owlrefplatform.owlapi;


import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.owlapi.reasoner.NullReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import javax.annotation.Nonnull;

/**
 * See QuestPreferences.builder() for an high-level configuration builder.
 */
@SuppressWarnings("serial")
public class QuestOWLConfiguration extends SimpleConfiguration {

    private final QuestPreferences preferences;

    @Nonnull
    public QuestPreferences getPreferences() {
        return preferences;
    }

    public QuestOWLConfiguration(@Nonnull QuestPreferences preferences) {
        super(new NullReasonerProgressMonitor());
        this.preferences = preferences;
    }

    /**
     * Constructor
     */
    public QuestOWLConfiguration(@Nonnull QuestPreferences preferences,
                                 @Nonnull ReasonerProgressMonitor progressMonitor) {
        super(progressMonitor);
        this.preferences = preferences;
    }
}
