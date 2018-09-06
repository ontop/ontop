package it.unibz.inf.ontop.answering.reformulation.rewriting;

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.LinearInclusionDependencies;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.ontology.*;
import org.apache.commons.rdf.api.IRI;

public class LinearInclusionDependencyTools {

    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final DatalogFactory datalogFactory;

    private static final String variableXname = "x";
    private static final String variableYname = "y";
    private static final String variableZname = "z";

    @Inject
    private LinearInclusionDependencyTools(AtomFactory atomFactory, TermFactory termFactory,
                                           DatalogFactory datalogFactory) {
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.datalogFactory = datalogFactory;
    }

    public LinearInclusionDependencies getABoxDependencies(ClassifiedTBox reasoner, boolean full) {
        LinearInclusionDependencies dependencies = new LinearInclusionDependencies(datalogFactory);

        for (Equivalences<ObjectPropertyExpression> propNode : reasoner.objectPropertiesDAG()) {
            // super might be more efficient
            for (Equivalences<ObjectPropertyExpression> subpropNode : reasoner.objectPropertiesDAG().getSub(propNode)) {
                for (ObjectPropertyExpression subprop : subpropNode) {
                    if (subprop.isInverse())
                        continue;

                    Function body = translate(subprop, variableXname, variableYname);

                    for (ObjectPropertyExpression prop : propNode)  {
                        if (prop == subprop)
                            continue;

                        Function head = translate(prop, variableXname, variableYname);
                        dependencies.addRule(head, body);
                    }
                }
            }
        }
        for (Equivalences<DataPropertyExpression> propNode : reasoner.dataPropertiesDAG()) {
            // super might be more efficient
            for (Equivalences<DataPropertyExpression> subpropNode : reasoner.dataPropertiesDAG().getSub(propNode)) {
                for (DataPropertyExpression subprop : subpropNode) {

                    Function body = translate(subprop, variableXname, variableYname);

                    for (DataPropertyExpression prop : propNode)  {
                        if (prop == subprop)
                            continue;

                        Function head = translate(prop, variableXname, variableYname);
                        dependencies.addRule(head, body);
                    }
                }
            }
        }
        for (Equivalences<ClassExpression> classNode : reasoner.classesDAG()) {
            // super might be more efficient
            for (Equivalences<ClassExpression> subclassNode : reasoner.classesDAG().getSub(classNode)) {
                for (ClassExpression subclass : subclassNode) {

                    Function body = translate(subclass, variableYname);
                    //if (!(subclass instanceof OClass) && !(subclass instanceof PropertySomeRestriction))
                    if (body == null)
                        continue;

                    for (ClassExpression cla : classNode)  {
                        if (cla == subclass)
                            continue;

                        if (!(cla instanceof OClass) && !(!full && ((cla instanceof ObjectSomeValuesFrom) || (cla instanceof DataSomeValuesFrom))))
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

    private Function translate(ObjectPropertyExpression property, String x, String y) {
        Variable varX = termFactory.getVariable(x);
        Variable varY = termFactory.getVariable(y);

        if (property.isInverse())
            return atomFactory.getMutableTripleAtom(varY, wrapUpIRI(property.getIRI()), varX);
        else
            return atomFactory.getMutableTripleAtom(varX, wrapUpIRI(property.getIRI()), varY);
    }

    private Function translate(DataPropertyExpression property, String x, String y) {
        Variable varX = termFactory.getVariable(x);
        Variable varY = termFactory.getVariable(y);

        return atomFactory.getMutableTripleAtom(varX, wrapUpIRI(property.getIRI()), varY);
    }

    private Function translate(ClassExpression description, String existentialVariableName) {
        if (description instanceof OClass) {
            final Variable varX = termFactory.getVariable(variableXname);
            OClass klass = (OClass) description;
            return atomFactory.getMutableTripleAtom(varX, wrapUpIRI(RDF.TYPE), wrapUpIRI(klass.getIRI()));
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

    private Function wrapUpIRI(IRI iri) {
        return termFactory.getUriTemplate(termFactory.getConstantLiteral(iri.getIRIString()));
    }
}
