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

    public LinearInclusionDependencies<AtomPredicate> getABoxFullDependencies(ClassifiedTBox reasoner) {

        final LinearInclusionDependencies.Builder<AtomPredicate> builder = LinearInclusionDependencies.builder(immutableUnificationTools, coreUtilsFactory, substitutionFactory);

        traverseDAG(reasoner.objectPropertiesDAG(), p -> !p.isInverse(), this::translate, builder);

        traverseDAG(reasoner.dataPropertiesDAG(), p -> true, this::translate, builder);

        // the head will have no existential variables
        traverseDAG(reasoner.classesDAG(), c -> (c instanceof OClass), this::translate, builder);

        return builder.build();
    }

    private static <T> void traverseDAG(EquivalencesDAG<T> dag,
                                        java.util.function.Predicate<T> filter,
                                        Function<T, DataAtom<AtomPredicate>> translate,
                                        LinearInclusionDependencies.Builder<AtomPredicate> builder) {
        for (Equivalences<T> node : dag)
            for (Equivalences<T> subNode : dag.getSub(node))
                for (T sub : subNode)
                    for (T e : node)
                        if (e != sub && filter.test(e)) {
                            DataAtom<AtomPredicate> body = translate.apply(sub);
                            DataAtom<AtomPredicate> head = translate.apply(e);
                            builder.add(head, body);
                        }
    }

    private DataAtom<AtomPredicate> translate(ObjectPropertyExpression property) {
        return property.isInverse()
            ? atomFactory.getIntensionalTripleAtom(
                    termFactory.getVariable("y"),
                    property.getIRI(),
                    termFactory.getVariable("x"))
            : atomFactory.getIntensionalTripleAtom(
                    termFactory.getVariable("x"),
                    property.getIRI(),
                    termFactory.getVariable("y"));
    }

    private DataAtom<AtomPredicate> translate(DataPropertyExpression property) {
        return atomFactory.getIntensionalTripleAtom(
                termFactory.getVariable("x"),
                property.getIRI(),
                termFactory.getVariable("y"));
    }

    private DataAtom<AtomPredicate> translate(ClassExpression description) {
        if (description instanceof OClass) {
            return atomFactory.getIntensionalTripleAtom(
                    termFactory.getVariable("x"),
                    ((OClass) description).getIRI());
        }
        else if (description instanceof ObjectSomeValuesFrom) {
            return translate(((ObjectSomeValuesFrom) description).getProperty());
        }
        else {
            return translate(((DataSomeValuesFrom) description).getProperty());
        }
    }
}
