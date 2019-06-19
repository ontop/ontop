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

import java.util.function.Function;

public class ImmutableLinearInclusionDependenciesTools {
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final ImmutableUnificationTools immutableUnificationTools;
    private final CoreUtilsFactory coreUtilsFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private ImmutableLinearInclusionDependenciesTools(AtomFactory atomFactory, TermFactory termFactory, ImmutableUnificationTools immutableUnificationTools, CoreUtilsFactory coreUtilsFactory, SubstitutionFactory substitutionFactory) {
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.immutableUnificationTools = immutableUnificationTools;
        this.coreUtilsFactory = coreUtilsFactory;
        this.substitutionFactory = substitutionFactory;
    }

    public LinearInclusionDependencies<AtomPredicate> getABoxDependencies(ClassifiedTBox reasoner) {

        final LinearInclusionDependencies.Builder<AtomPredicate> builder = LinearInclusionDependencies.builder(immutableUnificationTools, coreUtilsFactory, substitutionFactory);

        traverseDAG(reasoner.objectPropertiesDAG(),
                p -> !p.isInverse(),
                p -> translate(p, "x", "y"),
                p -> translate(p, "x", "y"),
                builder);

        traverseDAG(reasoner.dataPropertiesDAG(),
                p -> true,
                p -> translate(p, "x", "y"),
                p -> translate(p, "x", "y"),
                builder);

        traverseDAG(reasoner.classesDAG(),
                c -> true,
                sc -> translate(sc, "x", "y"),
                c -> translate(c, "x", "z"), // use a different variable name in case the body has an existential as well
                builder);

        return builder.build();
    }

    public LinearInclusionDependencies<AtomPredicate> getABoxFullDependencies(ClassifiedTBox reasoner) {

        final LinearInclusionDependencies.Builder<AtomPredicate> builder = LinearInclusionDependencies.builder(immutableUnificationTools, coreUtilsFactory, substitutionFactory);

        traverseDAG(reasoner.objectPropertiesDAG(),
                p -> !p.isInverse(),
                p -> translate(p, "x", "y"),
                p -> translate(p, "x", "y"),
                builder);

        traverseDAG(reasoner.dataPropertiesDAG(),
                p -> true,
                p -> translate(p, "x", "y"),
                p -> translate(p, "x", "y"),
                builder);

        traverseDAG(reasoner.classesDAG(),
                c -> (c instanceof OClass),
                sc -> translate(sc, "x", "y"),
                c -> translate(c, "x", "z"), // use a different variable name in case the body has an existential as well
                builder);

        return builder.build();
    }

    private static <T> void traverseDAG(EquivalencesDAG<T> dag,
                                        java.util.function.Predicate<T> filter,
                                        Function<T, DataAtom<AtomPredicate>> translateBody,
                                        Function<T, DataAtom<AtomPredicate>> translateHead,
                                        LinearInclusionDependencies.Builder<AtomPredicate> builder) {
        for (Equivalences<T> node : dag)
            for (Equivalences<T> subNode : dag.getSub(node))
                for (T sub : subNode)
                    for (T e : node)
                        if (e != sub && filter.test(e)) {
                            DataAtom<AtomPredicate> body = translateBody.apply(sub);
                            DataAtom<AtomPredicate> head = translateHead.apply(e);
                            builder.add(head, body);
                        }
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

    private DataAtom<AtomPredicate> translate(ClassExpression description, String x, String existentialVariableName) {
        if (description instanceof OClass) {
            final Variable varX = termFactory.getVariable(x);
            return atomFactory.getIntensionalTripleAtom(varX, ((OClass) description).getIRI());
        }
        else if (description instanceof ObjectSomeValuesFrom) {
            ObjectPropertyExpression property = ((ObjectSomeValuesFrom) description).getProperty();
            return translate(property, x, existentialVariableName);
        }
        else {
            DataPropertyExpression property = ((DataSomeValuesFrom) description).getProperty();
            return translate(property, x, existentialVariableName);
        }
    }
}
