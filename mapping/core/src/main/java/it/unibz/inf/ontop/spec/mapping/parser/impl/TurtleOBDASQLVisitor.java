package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Stateful!
 */
public class TurtleOBDASQLVisitor extends TurtleOBDABaseVisitor implements TurtleOBDAVisitor {

    // Column placeholder pattern
    private static final Pattern varPattern = Pattern.compile("\\{([^}]+)}");

    @Override
    public Object visitLiteral(TurtleOBDAParser.LiteralContext ctx) {
        return super.visitLiteral(ctx);
    }

    /**
     * Map of prefixes
     */
    private final ImmutableMap<String, String> prefixes;

    /**
     * The current graph
     */
    private ImmutableTerm currentGraph;
    /**
     * The current subject term
     */
    private ImmutableTerm currentSubject;


    protected String error = "";
    private final TermFactory termFactory;
    private final RDF rdfFactory;
    private final TypeFactory typeFactory;
    private final TargetAtomFactory targetAtomFactory;
    private final OntopMappingSettings settings;

    protected TurtleOBDASQLVisitor(TermFactory termFactory, TypeFactory typeFactory,
                                        TargetAtomFactory targetAtomFactory, RDF rdfFactory,
                                        OntopMappingSettings settings,
                                   ImmutableMap<String, String> prefixes) {
        this.typeFactory = typeFactory;
        this.targetAtomFactory = targetAtomFactory;
        this.rdfFactory = rdfFactory;
        this.termFactory = termFactory;
        this.settings = settings;
        this.prefixes = prefixes;
    }

    private static void validateAttributeName(String value) {
        if (value.contains(".")) {
            throw new IllegalArgumentException("Fully qualified columns as "+value+" are not accepted.\nPlease, use an alias instead.");
        }
    }

    public String getError() {
        return error;
    }

    private String removeBrackets(String text) {
        return text.substring(1, text.length() - 1);
    }

    private ImmutableTerm constructIRI(String text) {
        return constructBnodeOrIRI(text,
                col -> termFactory.getIRIFunctionalTerm(col, true),
                termFactory::getIRIFunctionalTerm);
    }

    private ImmutableTerm constructBnodeOrIRI(String text,
                                                Function<Variable, ImmutableFunctionalTerm> columnFct,
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
                validateAttributeName(token.str());
                Variable column = termFactory.getVariable(token.str());
                return columnFct.apply(column);
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
                    validateAttributeName(token.str());
                    Variable column = termFactory.getVariable(token.str());
                    terms.add(termFactory.getPartiallyDefinedToStringCast(column));
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
        int i, j;
        String st;
        str = str.substring(1, str.length() - 1);
        while (str.contains("{")) {
            i = getIndexOfCurlyB(str);
            if (i > 0) {
                st = str.substring(0, i);
                st = st.replace("\\\\", "");
                terms.add(termFactory.getDBStringConstant(st));
                str = str.substring(str.indexOf("{", i), str.length());
            } else if (i == 0) {
                j = str.indexOf("}");
                terms.add(termFactory.getVariable(str.substring(1, j)));
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

    //this function returns nested concats
    private ImmutableTerm getNestedConcat(String str) {
        List<ImmutableTerm> terms = addToTermsList(str);
        if (terms.size() == 1) {
            return terms.get(0);
        }

        return termFactory.getNullRejectingDBConcatFunctionalTerm(ImmutableList.copyOf(terms));
    }


    private String concatPrefix(String prefixedName) {
        String[] tokens = prefixedName.split(":", 2);
        String uri = prefixes.get(tokens[0]);  // the first token is the prefix
        return uri + tokens[1];  // the second token is the local name
    }

    @Override
    public ImmutableList<TargetAtom> visitParse(TurtleOBDAParser.ParseContext ctx) {
        ImmutableList<TargetAtom> tripleAtoms = ctx.triplesStatement().stream()
                .flatMap(this::visitTriplesStatement)
                .collect(ImmutableCollectors.toList());

        ImmutableList<TargetAtom> quadAtoms = ctx.quadsStatement().stream()
                .flatMap(this::visitQuadsStatement)
                .collect(ImmutableCollectors.toList());

        return ImmutableList.copyOf(Iterables.concat(tripleAtoms, quadAtoms));
    }

    @Override
    public Stream<TargetAtom> visitTriplesStatement(TurtleOBDAParser.TriplesStatementContext ctx) {
        TurtleOBDAParser.TriplesContext triples = ctx.triples();
        if (triples != null) {
            return visitTriples(triples);
        } else {
            return Stream.empty();
        }
    }

    @Override
    public Stream<TargetAtom> visitQuadsStatement(TurtleOBDAParser.QuadsStatementContext ctx) {
        this.currentGraph = visitGraph(ctx.graph());
        return ctx.triplesStatement().stream().flatMap(this::visitTriplesStatement);
    }

    @Override
    public Stream<TargetAtom> visitTriples(TurtleOBDAParser.TriplesContext ctx) {
        currentSubject = visitSubject(ctx.subject());
        return visitPredicateObjectList(ctx.predicateObjectList());
    }

    @Override
    public Stream<TargetAtom> visitPredicateObjectList(TurtleOBDAParser.PredicateObjectListContext ctx) {
        return ctx.predicateObject().stream()
                .flatMap(this::visitPredicateObject);
    }

    @Override
    public Stream<TargetAtom> visitPredicateObject(TurtleOBDAParser.PredicateObjectContext ctx) {
        return visitObjectList(ctx.objectList()).map(object ->
                currentGraph == null
                        ? targetAtomFactory.getTripleTargetAtom(currentSubject, visitVerb(ctx.verb()), object)
                        : targetAtomFactory.getQuadTargetAtom(currentSubject, visitVerb(ctx.verb()), object, currentGraph));
    }


    @Override
    public ImmutableTerm visitVerb(TurtleOBDAParser.VerbContext ctx) {
        TurtleOBDAParser.ResourceContext rc = ctx.resource();
        if (rc != null) {
            return visitResource(rc);
        }
        return termFactory.getConstantIRI(it.unibz.inf.ontop.model.vocabulary.RDF.TYPE);
    }

    @Override
    public Stream<ImmutableTerm> visitObjectList(TurtleOBDAParser.ObjectListContext ctx) {
        return ctx.object().stream()
                .map(this::visitObject);
    }

    @Override
    public ImmutableTerm visitSubject(TurtleOBDAParser.SubjectContext ctx) {
        TurtleOBDAParser.ResourceContext rc = ctx.resource();
        if (rc != null) {
            return visitResource(rc);
        }
        TurtleOBDAParser.VariableContext vc = ctx.variable();
        if (vc != null) {
            return termFactory.getIRIFunctionalTerm(visitVariable(vc), true);
        }
        TurtleOBDAParser.BlankContext bc = ctx.blank();
        if (bc != null) {
            return visitBlank(bc);
        }
        return null;
    }

    @Override
    public ImmutableTerm visitGraph(TurtleOBDAParser.GraphContext ctx) {
        if (ctx == null) return null;
        TurtleOBDAParser.ResourceContext rc = ctx.resource();
        if (rc != null) {
            return visitResource(rc);
        }
        TurtleOBDAParser.VariableContext vc = ctx.variable();
        if (vc != null) {
            return termFactory.getIRIFunctionalTerm(visitVariable(vc), true);
        }
        TurtleOBDAParser.BlankContext bc = ctx.blank();
        if (bc != null) {
            return visitBlank(bc);
        }
        return null;
    }

    @Override
    public ImmutableTerm visitObject(TurtleOBDAParser.ObjectContext ctx) {
        ImmutableTerm term = (ImmutableTerm) visit(ctx.children.iterator().next());
        return (term instanceof Variable)
                ? termFactory.getRDFLiteralFunctionalTerm(
                termFactory.getPartiallyDefinedToStringCast((Variable) term),
                // We give the abstract datatype RDFS.LITERAL when it is not determined yet
                // --> The concrete datatype be inferred afterwards
                RDFS.LITERAL)
                : term;
    }

    @Override
    public ImmutableTerm visitResource(TurtleOBDAParser.ResourceContext ctx) {
        TerminalNode node = ctx.IRIREF_WITH_PLACEHOLDERS();
        if (node != null) {
            return constructIRI(removeBrackets(node.getText()));
        }
        node = ctx.PREFIXED_NAME_WITH_PLACEHOLDERS();
        if (node != null) {
            return constructIRI(concatPrefix(node.getText()));
        }
        return constructIRI(visitIri(ctx.iri()).getIRIString());
    }

    @Override
    public ImmutableTerm visitVariableLiteral(TurtleOBDAParser.VariableLiteralContext ctx) {
        String variableName = removeBrackets(ctx.PLACEHOLDER().getText());
        validateAttributeName(variableName);
        Variable variable = termFactory.getVariable(variableName);

        TerminalNode langTag = ctx.LANGTAG();
        if (langTag != null) {
            ImmutableFunctionalTerm lexicalTerm = termFactory.getPartiallyDefinedToStringCast(variable);
            return termFactory.getRDFLiteralFunctionalTerm(lexicalTerm, langTag.getText().substring(1).toLowerCase());
        }

        TurtleOBDAParser.IriContext iriCtx = ctx.iri();
        if (iriCtx != null) {
            IRI iri = visitIri(iriCtx);
            if ((!settings.areAbstractDatatypesToleratedInMapping())
                    && typeFactory.getDatatype(iri).isAbstract())
                // TODO: throw a better exception (invalid input)
                throw new IllegalArgumentException("The datatype of a literal must not be abstract: "
                        + iri + "\nSet the property "
                        + OntopMappingSettings.TOLERATE_ABSTRACT_DATATYPE + " to true to tolerate them.");

            ImmutableFunctionalTerm lexicalTerm = termFactory.getPartiallyDefinedToStringCast(variable);
            return termFactory.getRDFLiteralFunctionalTerm(lexicalTerm, iri);
        }

        return variable;
    }

    @Override
    public IRI visitIri(TurtleOBDAParser.IriContext ctx) {
        TerminalNode node = ctx.IRIREF();
        if (node != null)
            return rdfFactory.createIRI(removeBrackets(node.getText()));

        return rdfFactory.createIRI(concatPrefix(ctx.PREFIXED_NAME().getText()));
    }

    @Override
    public Variable visitVariable(TurtleOBDAParser.VariableContext ctx) {
        String variableName = removeBrackets(ctx.PLACEHOLDER().getText());
        validateAttributeName(variableName);
        return termFactory.getVariable(variableName);
    }

    @Override
    public ImmutableTerm visitBlank(TurtleOBDAParser.BlankContext ctx) {
        TerminalNode node = ctx.BLANK_NODE_LABEL_WITH_PLACEHOLDERS();
        if (node != null) {
            return constructBnodeOrIRI(extractBnodeId(node.getText()),
                    col -> termFactory.getBnodeFunctionalTerm(col, true),
                    termFactory::getBnodeFunctionalTerm);
        }
        node = ctx.BLANK_NODE_LABEL();
        if (node != null) {
            return termFactory.getConstantBNode(extractBnodeId(node.getText()));
        }
        throw new IllegalArgumentException("Anonymous blank nodes not supported yet in mapping targets");
    }

    @Override
    public ImmutableTerm visitRdfLiteral(TurtleOBDAParser.RdfLiteralContext ctx) {
        ImmutableTerm stringValue = visitLitString(ctx.litString());
        TerminalNode langtag = ctx.LANGTAG();
        if (langtag != null) {
            return termFactory.getRDFLiteralFunctionalTerm(stringValue, langtag.getText().substring(1).toLowerCase());
        }
        TurtleOBDAParser.IriContext iriCtx = ctx.iri();
        if (iriCtx != null) {
            IRI iri = visitIri(iriCtx);
            return termFactory.getRDFLiteralFunctionalTerm(stringValue, iri)
                    .simplify(); // why?
        }
        return termFactory.getRDFLiteralFunctionalTerm(stringValue, XSD.STRING)
                .simplify(); // why?
    }

    @Override
    public ImmutableTerm visitLitString(TurtleOBDAParser.LitStringContext ctx) {
        String str = ctx.STRING_LITERAL_QUOTE().getText();
        if (str.contains("{")) {
            return getNestedConcat(str);
        }
        return termFactory.getDBStringConstant(str.substring(1, str.length() - 1)); // without the double quotes
    }


    @Override
    public ImmutableTerm visitBooleanLiteral(TurtleOBDAParser.BooleanLiteralContext ctx) {
        return termFactory.getRDFLiteralConstant(ctx.getText(), XSD.BOOLEAN);
    }

    @Override
    public ImmutableTerm visitNumericLiteral(TurtleOBDAParser.NumericLiteralContext ctx) {
        TerminalNode node = ctx.INTEGER();
        if (node != null) {
            return termFactory.getRDFLiteralConstant(node.getText(), XSD.INTEGER);
        }
        node = ctx.DOUBLE();
        if (node != null) {
            return termFactory.getRDFLiteralConstant(node.getText(), XSD.DOUBLE);
        }
        node = ctx.DECIMAL();
        return termFactory.getRDFLiteralConstant(node.getText(), XSD.DECIMAL);
    }

}
