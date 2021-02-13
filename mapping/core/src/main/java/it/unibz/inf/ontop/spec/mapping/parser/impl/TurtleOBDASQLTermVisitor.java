package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.template.impl.BnodeTemplateFactory;
import it.unibz.inf.ontop.model.template.impl.IRITemplateFactory;
import it.unibz.inf.ontop.model.template.impl.LiteralTemplateFactory;
import it.unibz.inf.ontop.model.template.impl.ObjectTemplateFactory;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.rdf4j.rio.turtle.TurtleUtil;

import java.util.Optional;

public class TurtleOBDASQLTermVisitor extends TurtleOBDABaseVisitor<ImmutableTerm> implements TurtleOBDAVisitor<ImmutableTerm> {

    private final PrefixManager prefixManager;

    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final OntopMappingSettings settings;

    private final IRITemplateFactory iriTemplateFactory;
    private final BnodeTemplateFactory bnodeTemplateFactory;
    private final LiteralTemplateFactory literalTemplateFactory;

    TurtleOBDASQLTermVisitor(TermFactory termFactory, TypeFactory typeFactory, OntopMappingSettings settings, PrefixManager prefixManager) {
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.settings = settings;
        this.prefixManager = prefixManager;
        this.iriTemplateFactory = new IRITemplateFactory(termFactory);
        this.bnodeTemplateFactory = new BnodeTemplateFactory(termFactory);
        this.literalTemplateFactory = new LiteralTemplateFactory(termFactory, typeFactory);
    }

    @Override
    public ImmutableTerm visitPredicateRdfType(TurtleOBDAParser.PredicateRdfTypeContext ctx) {
        return termFactory.getConstantIRI(it.unibz.inf.ontop.model.vocabulary.RDF.TYPE);
    }

    @Override
    public ImmutableTerm visitResourceIri(TurtleOBDAParser.ResourceIriContext ctx) {
        String text = ctx.IRIREF().getText();
        return getTermForObjectTemplate(
                text.substring(1, text.length() - 1), // remove " "
                iriTemplateFactory);
    }

    @Override
    public ImmutableTerm visitResourcePrefixedIri(TurtleOBDAParser.ResourcePrefixedIriContext ctx) {
        return getTermForObjectTemplate(
                prefixManager.getExpandForm(ctx.PNAME_LN().getText()),
                iriTemplateFactory);
    }

    @Override
    public ImmutableTerm visitBlankNode(TurtleOBDAParser.BlankNodeContext ctx) {
        return getTermForObjectTemplate(
                ctx.BLANK_NODE_LABEL().getText().substring(2), // remove the _: prefix
                bnodeTemplateFactory);
    }

    private static ImmutableTerm getTermForObjectTemplate(String template, ObjectTemplateFactory factory) {
        ImmutableList<Template.Component> components = factory.getComponents(template);

        if (components.size() == 1 && components.get(0).isColumnNameReference())
            return factory.getColumn(components.get(0).getComponent());

        return factory.getTemplateTerm(components);
    }

    @Override
    public ImmutableTerm visitBlankNodeAnonymous(TurtleOBDAParser.BlankNodeAnonymousContext ctx) {
        throw new IllegalArgumentException("Anonymous blank nodes not supported yet in mapping targets");
    }

    @Override
    public ImmutableTerm visitConstantRdfLiteral(TurtleOBDAParser.ConstantRdfLiteralContext ctx) {
        Optional<RDFDatatype> rdfDatatype = extractDatatype(ctx.LANGTAG(), ctx.iri());

        // https://www.w3.org/TR/turtle/#grammar-production-STRING_LITERAL_QUOTE
        // [22]	STRING_LITERAL_QUOTE ::= '"' ([^#x22#x5C#xA#xD] | ECHAR | UCHAR)* '"'
        //    (inserted a space below because the Java compiler complains of invalid Unicode)
        // [26] UCHAR ::= '\ u' HEX HEX HEX HEX | \U HEX HEX HEX HEX HEX HEX HEX HEX
        // [159s] ECHAR ::= '\' [tbnrf"'\]
        // TurtleUtil.decodeString deals with UCHAR and ECHAR, in particular, replace \\ by \, etc.

        String text = ctx.STRING_LITERAL_QUOTE().getText();
        String template = TurtleUtil.decodeString(text.substring(1, text.length() - 1)); // remove " "

        ImmutableList<Template.Component> components = literalTemplateFactory.getComponents(template);
        ImmutableTerm lexicalValue = literalTemplateFactory.getTemplateTerm(components);
        return termFactory.getRDFLiteralFunctionalTerm(lexicalValue,
                rdfDatatype.orElse(typeFactory.getXsdStringDatatype()));
    }

    @Override
    public ImmutableTerm visitVariableRdfLiteral(TurtleOBDAParser.VariableRdfLiteralContext ctx) {
        Optional<RDFDatatype> rdfDatatype = extractDatatype(ctx.LANGTAG(), ctx.iri());
        rdfDatatype.filter(dt -> !settings.areAbstractDatatypesToleratedInMapping())
                .filter(TermType::isAbstract)
                .ifPresent(dt -> {
                    // TODO: throw a better exception (invalid input)
                    throw new IllegalArgumentException("The datatype of a literal must not be abstract: "
                            + dt.getIRI() + "\nSet the property "
                            + OntopMappingSettings.TOLERATE_ABSTRACT_DATATYPE + " to true to tolerate them."); });

        String text = ctx.ENCLOSED_COLUMN_NAME().getText();
        String column = text.substring(1, text.length() - 1); // remove " "

        return termFactory.getRDFLiteralFunctionalTerm(literalTemplateFactory.getColumn(column),
                // We give the abstract datatype RDFS.LITERAL when it is not determined yet
                // --> The concrete datatype be inferred afterwards
                rdfDatatype.orElse(typeFactory.getAbstractRDFSLiteral()));
    }

    private Optional<RDFDatatype> extractDatatype(TerminalNode langNode, TurtleOBDAParser.IriContext iri) {
        return literalTemplateFactory.extractDatatype(
                Optional.ofNullable(langNode)
                        .map(l -> l.getText().substring(1).toLowerCase()),
                Optional.ofNullable(iri)
                        .map(i -> i.accept(this))
                        .filter(term -> term instanceof IRIConstant)
                        .map(term -> (IRIConstant)term)
                        .map(IRIConstant::getIRI));
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
