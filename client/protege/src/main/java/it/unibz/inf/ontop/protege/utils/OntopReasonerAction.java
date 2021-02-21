package it.unibz.inf.ontop.protege.utils;

import it.unibz.inf.ontop.protege.core.OntopProtegeReasoner;
import org.protege.editor.core.editorkit.EditorKit;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.function.Function;

public class OntopReasonerAction extends OntopAbstractAction {
    private final EditorKit editorKit;
    private final Function<OntopProtegeReasoner, OntopQuerySwingWorker<?, ?>> workerFactory;

    public OntopReasonerAction(String name, String icon, String tooltip, KeyStroke accelerator,
                               EditorKit editorKit, Function<OntopProtegeReasoner, OntopQuerySwingWorker<?, ?>> workerFactory) {
        super(name, icon, tooltip, accelerator);
        this.editorKit = editorKit;
        this.workerFactory = workerFactory;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Optional<OntopProtegeReasoner> ontop = DialogUtils.getOntopProtegeReasoner(editorKit);
        if (!ontop.isPresent())
            return;

        OntopQuerySwingWorker<?, ?> worker = workerFactory.apply(ontop.get());
        if (worker != null)
            worker.execute();
    }
}
