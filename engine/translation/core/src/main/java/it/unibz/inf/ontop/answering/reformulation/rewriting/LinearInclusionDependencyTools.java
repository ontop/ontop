package it.unibz.inf.ontop.answering.reformulation.rewriting;

import it.unibz.inf.ontop.datalog.LinearInclusionDependencies;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.Equivalences;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;

public class LinearInclusionDependencyTools {

    public static LinearInclusionDependencies getABoxDependencies(TBoxReasoner reasoner, boolean full) {
        LinearInclusionDependencies dependencies = new LinearInclusionDependencies();

        for (Equivalences<ObjectPropertyExpression> propNode : reasoner.getObjectPropertyDAG()) {
            // super might be more efficient
            for (Equivalences<ObjectPropertyExpression> subpropNode : reasoner.getObjectPropertyDAG().getSub(propNode)) {
                for (ObjectPropertyExpression subprop : subpropNode) {
                    if (subprop.isInverse())
                        continue;

                    Function body = translate(subprop);

                    for (ObjectPropertyExpression prop : propNode)  {
                        if (prop == subprop)
                            continue;

                        Function head = translate(prop);
                        dependencies.addRule(head, body);
                    }
                }
            }
        }
        for (Equivalences<DataPropertyExpression> propNode : reasoner.getDataPropertyDAG()) {
            // super might be more efficient
            for (Equivalences<DataPropertyExpression> subpropNode : reasoner.getDataPropertyDAG().getSub(propNode)) {
                for (DataPropertyExpression subprop : subpropNode) {

                    Function body = translate(subprop);

                    for (DataPropertyExpression prop : propNode)  {
                        if (prop == subprop)
                            continue;

                        Function head = translate(prop);
                        dependencies.addRule(head, body);
                    }
                }
            }
        }
        for (Equivalences<ClassExpression> classNode : reasoner.getClassDAG()) {
            // super might be more efficient
            for (Equivalences<ClassExpression> subclassNode : reasoner.getClassDAG().getSub(classNode)) {
                for (ClassExpression subclass : subclassNode) {

                    Function body = translate(subclass, variableYname);
                    //if (!(subclass instanceof OClass) && !(subclass instanceof PropertySomeRestriction))
                    if (body == null)
                        continue;

                    for (ClassExpression cla : classNode)  {
                        if (!(cla instanceof OClass) && !(!full && ((cla instanceof ObjectSomeValuesFrom) || (cla instanceof DataSomeValuesFrom))))
                            continue;

                        if (cla == subclass)
                            continue;

                        // use a different variable name in case the body has an existential as well
                        Function head = translate(cla, variableZname);
                        dependencies.addRule(head, body);
                    }
                }
            }
        }

        return dependencies;
    }

    private static final String variableXname = "x";
    private static final String variableYname = "y";
    private static final String variableZname = "z";

    private static Function translate(ObjectPropertyExpression property) {
        final Variable varX = TERM_FACTORY.getVariable(variableXname);
        final Variable varY = TERM_FACTORY.getVariable(variableYname);

        if (property.isInverse())
            return TERM_FACTORY.getFunction(property.getPredicate(), varY, varX);
        else
            return TERM_FACTORY.getFunction(property.getPredicate(), varX, varY);
    }

    private static Function translate(DataPropertyExpression property) {
        final Variable varX = TERM_FACTORY.getVariable(variableXname);
        final Variable varY = TERM_FACTORY.getVariable(variableYname);

        return TERM_FACTORY.getFunction(property.getPredicate(), varX, varY);
    }

    private static Function translate(ClassExpression description, String existentialVariableName) {
        final Variable varX = TERM_FACTORY.getVariable(variableXname);
        if (description instanceof OClass) {
            OClass klass = (OClass) description;
            return TERM_FACTORY.getFunction(klass.getPredicate(), varX);
        }
        else if (description instanceof ObjectSomeValuesFrom) {
            final Variable varY = TERM_FACTORY.getVariable(existentialVariableName);
            ObjectPropertyExpression property = ((ObjectSomeValuesFrom) description).getProperty();
            if (property.isInverse())
                return TERM_FACTORY.getFunction(property.getPredicate(), varY, varX);
            else
                return TERM_FACTORY.getFunction(property.getPredicate(), varX, varY);
        }
        else {
            assert (description instanceof DataSomeValuesFrom);
            final Variable varY = TERM_FACTORY.getVariable(existentialVariableName);
            DataPropertyExpression property = ((DataSomeValuesFrom) description).getProperty();
            return TERM_FACTORY.getFunction(property.getPredicate(), varX, varY);
        }
    }
}
