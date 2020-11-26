package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.rdf4j.rio.turtle.TurtleUtil;

import java.util.Optional;

public class TurtleOBDASQLTermVisitor extends TurtleOBDABaseVisitor<ImmutableTerm> implements TurtleOBDAVisitor<ImmutableTerm> {

    private final PrefixManager prefixManager;

    private final TermFactory termFactory;
    private final RDF rdfFactory;
    private final TypeFactory typeFactory;
    private final OntopMappingSettings settings;

    private final Templates factory;

    TurtleOBDASQLTermVisitor(TermFactory termFactory, RDF rdfFactory, TypeFactory typeFactory, OntopMappingSettings settings, PrefixManager prefixManager) {
        this.termFactory = termFactory;
        this.rdfFactory = rdfFactory;
        this.typeFactory = typeFactory;
        this.settings = settings;
        this.prefixManager = prefixManager;
        this.factory = new Templates(termFactory, typeFactory);
    }

    private String removeBrackets(String text) {
        return text.substring(1, text.length() - 1);
    }

    private static String extractBnodeId(String text) {
        return text.substring(2); // Remove the prefix _:
    }

    @Override
    public ImmutableTerm visitPredicateRdfType(TurtleOBDAParser.PredicateRdfTypeContext ctx) {
        return termFactory.getConstantIRI(it.unibz.inf.ontop.model.vocabulary.RDF.TYPE);
    }

    @Override
    public ImmutableTerm visitResourceIri(TurtleOBDAParser.ResourceIriContext ctx) {
        ImmutableList<TemplateComponent> components = TemplateComponent.getComponents(
                removeBrackets(ctx.IRIREF().getText()));
        if (components.size() == 1) {
            TemplateComponent c = components.get(0);
            return (c.isColumnNameReference())
                ? termFactory.getIRIFunctionalTerm(factory.getVariable(c.getComponent()))
                : termFactory.getConstantIRI(rdfFactory.createIRI(removeBrackets(ctx.IRIREF().getText())));
        }
        return termFactory.getIRIFunctionalTerm(Templates.getTemplateString(components), factory.getTemplateTerms(components));
    }


    @Override
    public ImmutableTerm visitResourcePrefixedIri(TurtleOBDAParser.ResourcePrefixedIriContext ctx) {
        ImmutableList<TemplateComponent> components = TemplateComponent.getComponents(
                prefixManager.getExpandForm(ctx.PREFIXED_NAME().getText()));
        if (components.size() == 1) {
            TemplateComponent c = components.get(0);
            return (c.isColumnNameReference())
                ? termFactory.getIRIFunctionalTerm(factory.getVariable(c.getComponent()))
                : termFactory.getConstantIRI(rdfFactory.createIRI(prefixManager.getExpandForm(ctx.PREFIXED_NAME().getText())));
        }
        return termFactory.getIRIFunctionalTerm(Templates.getTemplateString(components), factory.getTemplateTerms(components));
    }

    @Override
    public ImmutableTerm visitVariableLiteral(TurtleOBDAParser.VariableLiteralContext ctx) {
        Optional<RDFDatatype> rdfDatatype = extractDatatype(ctx.LANGTAG(), ctx.IRIREF(), ctx.PREFIXED_NAME());
        rdfDatatype.filter(dt -> !settings.areAbstractDatatypesToleratedInMapping())
                .filter(TermType::isAbstract)
                .ifPresent(dt -> {
                    // TODO: throw a better exception (invalid input)
                    throw new IllegalArgumentException("The datatype of a literal must not be abstract: "
                            + dt.getIRI() + "\nSet the property "
                            + OntopMappingSettings.TOLERATE_ABSTRACT_DATATYPE + " to true to tolerate them."); });

        ImmutableFunctionalTerm lexicalTerm = factory.getVariable(removeBrackets(ctx.PLACEHOLDER().getText()));
        return termFactory.getRDFLiteralFunctionalTerm(lexicalTerm,
                // We give the abstract datatype RDFS.LITERAL when it is not determined yet
                // --> The concrete datatype be inferred afterwards
                rdfDatatype.orElse(typeFactory.getAbstractRDFSLiteral()));
    }

    @Override
    public ImmutableTerm visitVariable(TurtleOBDAParser.VariableContext ctx) {
        String variableName = removeBrackets(ctx.PLACEHOLDER().getText());
        return termFactory.getIRIFunctionalTerm(factory.getVariable(variableName));
    }

    @Override
    public ImmutableTerm visitBlankNode(TurtleOBDAParser.BlankNodeContext ctx) {
        ImmutableList<TemplateComponent> components = TemplateComponent.getComponents(
                extractBnodeId(ctx.BLANK_NODE_LABEL().getText()));
        if (components.size() == 1) {
            TemplateComponent c = components.get(0);
            return c.isColumnNameReference()
                    ? termFactory.getBnodeFunctionalTerm(factory.getVariable(c.getComponent()))
                    : termFactory.getConstantBNode(c.getComponent());
        }
        return termFactory.getBnodeFunctionalTerm(Templates.getTemplateString(components), factory.getTemplateTerms(components));
    }

    @Override
    public ImmutableTerm visitBlankNodeAnonymous(TurtleOBDAParser.BlankNodeAnonymousContext ctx) {
        throw new IllegalArgumentException("Anonymous blank nodes not supported yet in mapping targets");
    }

    @Override
    public ImmutableTerm visitRdfLiteral(TurtleOBDAParser.RdfLiteralContext ctx) {
        Optional<RDFDatatype> rdfDatatype = extractDatatype(ctx.LANGTAG(), ctx.IRIREF(), ctx.PREFIXED_NAME());

        // https://www.w3.org/TR/turtle/#grammar-production-STRING_LITERAL_QUOTE
        // [22]	STRING_LITERAL_QUOTE ::= '"' ([^#x22#x5C#xA#xD] | ECHAR | UCHAR)* '"'
        //    (inserted a space below because the Java compiler complains of invalid Unicode)
        // [26] UCHAR ::= '\ u' HEX HEX HEX HEX | \U HEX HEX HEX HEX HEX HEX HEX HEX
        // [159s] ECHAR ::= '\' [tbnrf"'\]
        // TurtleUtil.decodeString deals with UCHAR and ECHAR, in particular, replace \\ by \, etc.

        String template = TurtleUtil.decodeString(removeBrackets(ctx.STRING_LITERAL_QUOTE().getText()));
        ImmutableTerm lexicalValue = factory.getLiteralTemplateTerm(template);

        return termFactory.getRDFLiteralFunctionalTerm(lexicalValue,
                rdfDatatype.orElse(typeFactory.getXsdStringDatatype()));
    }

    private Optional<RDFDatatype> extractDatatype(TerminalNode langNode, TerminalNode iriNode, TerminalNode prefixedNameNode) {
        return factory.extractDatatype(
                Optional.ofNullable(langNode)
                        .map(l -> l.getText().substring(1).toLowerCase()),
                extractIRI(iriNode, prefixedNameNode)
                        .map(rdfFactory::createIRI));
    }

    private Optional<String> extractIRI(TerminalNode iriNode, TerminalNode prefixedNameNode) {
        if (iriNode != null)
            return Optional.of(removeBrackets(iriNode.getText()));

        if (prefixedNameNode != null)
            return Optional.of(prefixManager.getExpandForm(prefixedNameNode.getText()));

        return Optional.empty();
    }

    @Override
    public ImmutableTerm visitBooleanLiteral(TurtleOBDAParser.BooleanLiteralContext ctx) {
        return termFactory.getRDFLiteralConstant(ctx.getText(), XSD.BOOLEAN);
    }

    @Override
    public ImmutableTerm visitIntegerLiteral(TurtleOBDAParser.IntegerLiteralContext ctx) {
        return termFactory.getRDFLiteralConstant(ctx.INTEGER().getText(), XSD.INTEGER);
    }

    @Override
    public ImmutableTerm visitDoubleLiteral(TurtleOBDAParser.DoubleLiteralContext ctx) {
        return termFactory.getRDFLiteralConstant(ctx.DOUBLE().getText(), XSD.DOUBLE);
    }

    @Override
    public ImmutableTerm visitDecimalLiteral(TurtleOBDAParser.DecimalLiteralContext ctx) {
        return termFactory.getRDFLiteralConstant(ctx.DECIMAL().getText(), XSD.DECIMAL);
    }

}
