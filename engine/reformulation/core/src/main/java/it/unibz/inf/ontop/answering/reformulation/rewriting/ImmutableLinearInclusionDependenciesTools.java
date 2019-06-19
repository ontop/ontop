package it.unibz.inf.ontop.answering.reformulation.rewriting;

import com.google.inject.Inject;
import it.unibz.inf.ontop.constraints.LinearInclusionDependencies;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;

import java.util.function.BiConsumer;

public class ImmutableLinearInclusionDependenciesTools {
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final ImmutableUnificationTools immutableUnificationTools;
    private final CoreUtilsFactory coreUtilsFactory;
    private final SubstitutionFactory substitutionFactory;

    private static final String variableXname = "x";
    private static final String variableYname = "y";
    private static final String variableZname = "z";

    @Inject
    private ImmutableLinearInclusionDependenciesTools(AtomFactory atomFactory, TermFactory termFactory, ImmutableUnificationTools immutableUnificationTools, CoreUtilsFactory coreUtilsFactory, SubstitutionFactory substitutionFactory) {
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.immutableUnificationTools = immutableUnificationTools;
        this.coreUtilsFactory = coreUtilsFactory;
        this.substitutionFactory = substitutionFactory;
    }

    public LinearInclusionDependencies<AtomPredicate> getABoxDependencies(ClassifiedTBox reasoner, boolean full) {

        final LinearInclusionDependencies.Builder<AtomPredicate> builder = LinearInclusionDependencies.builder(immutableUnificationTools, coreUtilsFactory, substitutionFactory);

        traverseDAG(reasoner.objectPropertiesDAG(), (subprop, prop) -> {
            if (!subprop.isInverse()) {
                DataAtom<AtomPredicate> body = translate(subprop, variableXname, variableYname);
                DataAtom<AtomPredicate> head = translate(prop, variableXname, variableYname);
                builder.add(head, body);
            }
        });
        traverseDAG(reasoner.dataPropertiesDAG(), (subprop, prop) -> {
            DataAtom<AtomPredicate> body = translate(subprop, variableXname, variableYname);
            DataAtom<AtomPredicate> head = translate(prop, variableXname, variableYname);
            builder.add(head, body);
        });
        traverseDAG(reasoner.classesDAG(), (subclass, cla) -> {
            if (!(cla instanceof OClass) && !(!full && ((cla instanceof ObjectSomeValuesFrom) || (cla instanceof DataSomeValuesFrom))))
                return;

            DataAtom<AtomPredicate> body = translate(subclass, variableYname);
            // use a different variable name in case the body has an existential as well
            DataAtom<AtomPredicate> head = translate(cla, variableZname);
            builder.add(head, body);
        });

        return builder.build();
    }

    private static <T> void traverseDAG(EquivalencesDAG<T> dag, BiConsumer<T, T> consumer) {
        for (Equivalences<T> node : dag)
            for (Equivalences<T> subNode : dag.getSub(node))
                for (T sub : subNode)
                    for (T e : node)
                        if (e != sub)
                            consumer.accept(sub, e);
    }

    private DataAtom<AtomPredicate> translate(ObjectPropertyExpression property, String x, String y) {
        Variable varX = termFactory.getVariable(x);
        Variable varY = termFactory.getVariable(y);

        if (property.isInverse())
            return atomFactory.getIntensionalTripleAtom(varY, property.getIRI(), varX);
        else
            return atomFactory.getIntensionalTripleAtom(varX, property.getIRI(), varY);
    }

    private DataAtom<AtomPredicate> translate(DataPropertyExpression property, String x, String y) {
        Variable varX = termFactory.getVariable(x);
        Variable varY = termFactory.getVariable(y);

        return atomFactory.getIntensionalTripleAtom(varX, property.getIRI(), varY);
    }

    private DataAtom<AtomPredicate> translate(ClassExpression description, String existentialVariableName) {
        if (description instanceof OClass) {
            final Variable varX = termFactory.getVariable(variableXname);
            OClass klass = (OClass) description;
            return atomFactory.getIntensionalTripleAtom(varX, klass.getIRI());
        }
        else if (description instanceof ObjectSomeValuesFrom) {
            ObjectPropertyExpression property = ((ObjectSomeValuesFrom) description).getProperty();
            return translate(property, variableXname, existentialVariableName);
        }
        else {
            DataPropertyExpression property = ((DataSomeValuesFrom) description).getProperty();
            return translate(property, variableXname, existentialVariableName);
        }
    }
}
