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
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TurtleOBDASQLTermVisitor extends TurtleOBDABaseVisitor<ImmutableTerm> implements TurtleOBDAVisitor<ImmutableTerm> {
    // Column placeholder pattern
    private static final Pattern varPattern = Pattern.compile("\\{([^}]+)}");

    /**
     * Map of prefixes
     */
    private final PrefixManager prefixManager;

    private final TermFactory termFactory;
    private final RDF rdfFactory;
    private final TypeFactory typeFactory;
    private final OntopMappingSettings settings;

    private final MappingParserHelper factory;

    TurtleOBDASQLTermVisitor(TermFactory termFactory, RDF rdfFactory, TypeFactory typeFactory, OntopMappingSettings settings, PrefixManager prefixManager) {
        this.termFactory = termFactory;
        this.rdfFactory = rdfFactory;
        this.typeFactory = typeFactory;
        this.settings = settings;
        this.prefixManager = prefixManager;
        this.factory = new MappingParserHelper(termFactory, typeFactory);
    }

    private String removeBrackets(String text) {
        return text.substring(1, text.length() - 1);
    }

    private ImmutableTerm constructBnodeOrIRI(String text,
                                              Function<ImmutableTerm, ImmutableFunctionalTerm> columnFct,
                                              BiFunction<String, ImmutableList<ImmutableTerm>, ImmutableFunctionalTerm> templateFct) {
        final String PLACEHOLDER = "{}";
        List<FormatString> tokens = parseIRIOrBnode(text);
        int size = tokens.size();
        if (size == 1) {
            FormatString token = tokens.get(0);
            if (token instanceof FixedString) {
                return termFactory.getConstantIRI(rdfFactory.createIRI(token.str()));
            } else if (token instanceof ColumnString) {
                // the IRI string is coming from the DB (no escaping needed)
                return columnFct.apply(factory.getVariable(token.str()));
            }
            throw new MinorOntopInternalBugException("Unexpected token: " + token);
        } else {
            StringBuilder sb = new StringBuilder();
            List<ImmutableTerm> terms = new ArrayList<>();
            for (FormatString token : tokens) {
                if (token instanceof FixedString) { // if part of URI template
                    sb.append(token.str());
                } else if (token instanceof ColumnString) {
                    sb.append(PLACEHOLDER);
                    terms.add(factory.getVariable(token.str()));
                }
            }
            String iriTemplate = sb.toString(); // complete IRI template
            return templateFct.apply(iriTemplate, ImmutableList.copyOf(terms));
        }
    }


    private List<FormatString> parseIRIOrBnode(String text) {
        List<FormatString> toReturn = new ArrayList<>();
        Matcher m = varPattern.matcher(text);
        int i = 0;
        while (i < text.length()) {
            if (m.find(i)) {
                if (m.start() != i) {
                    String subString = text.substring(i, m.start());
                    toReturn.add(new FixedString(subString));
                }
                String value = m.group(1);
                toReturn.add(new ColumnString(value));
                i = m.end();
            }
            else {
                toReturn.add(new FixedString(text.substring(i)));
                break;
            }
        }
        return toReturn;
    }

    // Remove the prefix _:
    private static String extractBnodeId(String text) {
        return text.substring(2);
    }

    private interface FormatString {
        String str();
    }

    private static class FixedString implements FormatString {
        private final String s;

        FixedString(String s) {
            this.s = s;
        }

        @Override
        public String str() {
            return s;
        }
    }

    private static class ColumnString implements FormatString {
        private final String s;

        ColumnString(String s) {
            this.s = s;
        }

        @Override
        public String str() {
            return s;
        }
    }

    //this function distinguishes curly bracket with
    //back slash "\{" from curly bracket "{"
    private int getIndexOfCurlyB(String str) {
        int i = str.indexOf("{");
        int j = str.indexOf("\\{");
        while ((i - 1 == j) && (j != -1)) {
            i = str.indexOf("{", i + 1);
            j = str.indexOf("\\{", j + 1);
        }
        return i;
    }

    //in case of concat this function parses the literal
    //and adds parsed constant literals and template literal to terms list
    private List<ImmutableTerm> addToTermsList(String str) {
        ArrayList<ImmutableTerm> terms = new ArrayList<>();
        while (str.contains("{")) {
            int i = getIndexOfCurlyB(str);
            if (i > 0) {
                String st = str.substring(0, i);
                st = st.replace("\\\\", "");
                terms.add(termFactory.getDBStringConstant(st));
                str = str.substring(str.indexOf("{", i), str.length());
            } else if (i == 0) {
                int j = str.indexOf("}");
                terms.add(factory.getVariable(str.substring(1, j)));
                str = str.substring(j + 1, str.length());
            } else {
                break;
            }
        }
        if (!str.equals("")) {
            str = str.replace("\\\\", "");
            terms.add(termFactory.getDBStringConstant(str));
        }
        return terms;
    }

    @Override
    public ImmutableTerm visitPredicateRdfType(TurtleOBDAParser.PredicateRdfTypeContext ctx) {
        return termFactory.getConstantIRI(it.unibz.inf.ontop.model.vocabulary.RDF.TYPE);
    }

    @Override
    public ImmutableTerm visitResourceIri(TurtleOBDAParser.ResourceIriContext ctx) {
        return termFactory.getConstantIRI(rdfFactory.createIRI(removeBrackets(ctx.IRIREF().getText())));
    }

    @Override
    public ImmutableTerm visitResourceTemplate(TurtleOBDAParser.ResourceTemplateContext ctx) {
        return constructBnodeOrIRI(removeBrackets(ctx.IRIREF_WITH_PLACEHOLDERS().getText()),
                termFactory::getIRIFunctionalTerm,
                termFactory::getIRIFunctionalTerm);
    }

    @Override
    public ImmutableTerm visitResourcePrefixedIri(TurtleOBDAParser.ResourcePrefixedIriContext ctx) {
        return termFactory.getConstantIRI(rdfFactory.createIRI(prefixManager.getExpandForm(ctx.PREFIXED_NAME().getText())));
    }

    @Override
    public ImmutableTerm visitResourcePrefixedTemplate(TurtleOBDAParser.ResourcePrefixedTemplateContext ctx) {
        return constructBnodeOrIRI(prefixManager.getExpandForm(ctx.PREFIXED_NAME_WITH_PLACEHOLDERS().getText()),
                termFactory::getIRIFunctionalTerm,
                termFactory::getIRIFunctionalTerm);
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
    public ImmutableTerm visitBlankNodeTemplate(TurtleOBDAParser.BlankNodeTemplateContext ctx) {
        return constructBnodeOrIRI(extractBnodeId(ctx.BLANK_NODE_LABEL_WITH_PLACEHOLDERS().getText()),
                termFactory::getBnodeFunctionalTerm,
                termFactory::getBnodeFunctionalTerm);
    }

    @Override
    public ImmutableTerm visitBlankNode(TurtleOBDAParser.BlankNodeContext ctx) {
        return termFactory.getConstantBNode(extractBnodeId(ctx.BLANK_NODE_LABEL().getText()));
    }

    @Override
    public ImmutableTerm visitBlankNodeAnonymous(TurtleOBDAParser.BlankNodeAnonymousContext ctx) {
        throw new IllegalArgumentException("Anonymous blank nodes not supported yet in mapping targets");
    }

    @Override
    public ImmutableTerm visitRdfLiteral(TurtleOBDAParser.RdfLiteralContext ctx) {
        Optional<RDFDatatype> rdfDatatype = extractDatatype(ctx.LANGTAG(), ctx.IRIREF(), ctx.PREFIXED_NAME());
        return termFactory.getRDFLiteralFunctionalTerm(visitString(ctx.string()),
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
    public ImmutableTerm visitString(TurtleOBDAParser.StringContext ctx) {
        String str = removeBrackets(ctx.STRING_LITERAL_QUOTE().getText()); // without the double quotes
        List<ImmutableTerm> terms = addToTermsList(str);
        if (terms.size() == 1) {
            return terms.get(0);
        }
        return termFactory.getNullRejectingDBConcatFunctionalTerm(ImmutableList.copyOf(terms));
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
