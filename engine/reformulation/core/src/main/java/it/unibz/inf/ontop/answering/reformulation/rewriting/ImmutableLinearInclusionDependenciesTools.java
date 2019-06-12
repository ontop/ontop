package it.unibz.inf.ontop.answering.reformulation.rewriting;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.ImmutableLinearInclusionDependency;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public class ImmutableLinearInclusionDependenciesTools {
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final ImmutableUnificationTools immutableUnificationTools;

    private static final String variableXname = "x";
    private static final String variableYname = "y";
    private static final String variableZname = "z";

    @Inject
    private ImmutableLinearInclusionDependenciesTools(AtomFactory atomFactory, TermFactory termFactory, ImmutableUnificationTools immutableUnificationTools) {
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.immutableUnificationTools = immutableUnificationTools;
    }

    public ImmutableList<ImmutableLinearInclusionDependency<AtomPredicate>> getABoxDependencies(ClassifiedTBox reasoner, boolean full) {

        ImmutableList.Builder<ImmutableLinearInclusionDependency<AtomPredicate>> builder = ImmutableList.builder();

        for (Equivalences<ObjectPropertyExpression> propNode : reasoner.objectPropertiesDAG()) {
            for (Equivalences<ObjectPropertyExpression> subpropNode : reasoner.objectPropertiesDAG().getSub(propNode)) {
                for (ObjectPropertyExpression subprop : subpropNode) {
                    if (subprop.isInverse())
                        continue;
                    DataAtom<AtomPredicate> body = translate(subprop, variableXname, variableYname);
                    for (ObjectPropertyExpression prop : propNode)  {
                        if (prop != subprop) {
                            DataAtom<AtomPredicate> head = translate(prop, variableXname, variableYname);
                            builder.add(new ImmutableLinearInclusionDependency<>(head, body));
                        }
                    }
                }
            }
        }
        for (Equivalences<DataPropertyExpression> propNode : reasoner.dataPropertiesDAG()) {
            for (Equivalences<DataPropertyExpression> subpropNode : reasoner.dataPropertiesDAG().getSub(propNode)) {
                for (DataPropertyExpression subprop : subpropNode) {
                    DataAtom<AtomPredicate> body = translate(subprop, variableXname, variableYname);
                    for (DataPropertyExpression prop : propNode)
                        if (prop != subprop) {
                            DataAtom<AtomPredicate> head = translate(prop, variableXname, variableYname);
                            builder.add(new ImmutableLinearInclusionDependency<>(head, body));
                        }
                }
            }
        }
        for (Equivalences<ClassExpression> classNode : reasoner.classesDAG()) {
            for (Equivalences<ClassExpression> subclassNode : reasoner.classesDAG().getSub(classNode)) {
                for (ClassExpression subclass : subclassNode) {
                    DataAtom<AtomPredicate> body = translate(subclass, variableYname);
                    for (ClassExpression cla : classNode)
                        if (cla != subclass) {
                            if (!(cla instanceof OClass) && !(!full && ((cla instanceof ObjectSomeValuesFrom) || (cla instanceof DataSomeValuesFrom))))
                                continue;

                            // use a different variable name in case the body has an existential as well
                            DataAtom<AtomPredicate> head = translate(cla, variableZname);
                            builder.add(new ImmutableLinearInclusionDependency<>(head, body));
                        }
                    }
            }
        }

        return builder.build();
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

    /**
     * This method is used to chase foreign key constraint rule in which the rule
     * has only one atom in the body.
     *
     * IMPORTANT: each rule is applied only ONCE to the atom
     *
     * @param atom
     * @return set of atoms
     */

    public ImmutableSet<DataAtom> chaseAtom(DataAtom atom, ImmutableCollection<ImmutableLinearInclusionDependency<AtomPredicate>> dependencies) {
        return dependencies.stream()
                .map(dependency -> immutableUnificationTools.computeAtomMGU(dependency.getBody(), atom)
                        .map(theta -> theta.applyToDataAtom(dependency.getHead())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toSet());
    }

    /**
     * Hotfix
     * FIXME(xiao)
     * @param atom
     * @param dependencies
     * @return
     */
    public ImmutableSet<DataAtom> chaseAtomUsingDLAxioms(DataAtom atom, ImmutableCollection<ImmutableLinearInclusionDependency<AtomPredicate>> dependencies) {
        if (atom.getArguments().stream().allMatch(x -> x instanceof Variable)){
            return ImmutableSet.of(atom);
        } else {
            return chaseAtom(atom, dependencies);
        }
    }
}
