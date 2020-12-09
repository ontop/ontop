package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;


public class TurtleOBDASQLParser extends AbstractTurtleOBDAParser implements TargetQueryParser {

    @AssistedInject
    private TurtleOBDASQLParser(@Assisted PrefixManager prefixManager, TermFactory termFactory,
                                TypeFactory typeFactory, TargetAtomFactory targetAtomFactory,
                                OntopMappingSettings settings) {
        super(targetAtomFactory, () -> new TurtleOBDASQLTermVisitor(termFactory, typeFactory, settings, prefixManager));
    }
}
