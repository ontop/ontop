package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.mapping.parser.impl.TurtleOBDAParser.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class AbstractTurtleOBDAVisitor extends TurtleOBDABaseVisitor implements TurtleOBDAVisitor {

    // Column placeholder pattern
    private static final Pattern varPattern = Pattern.compile("\\{([^\\}]+)\\}");
    private static final Pattern constantBnodePattern = Pattern.compile("^_:(.*)");

    protected abstract boolean validateAttributeName(String value);

    /**
     * Map of directives
     */
    private HashMap<String, String> directives = new HashMap<>();

    /**
     * The current subject term
     */
    private ImmutableTerm currentSubject;

    protected String error = "";
    private final TermFactory termFactory;
    private final RDF rdfFactory;
    private final TargetAtomFactory targetAtomFactory;

    public AbstractTurtleOBDAVisitor(TermFactory termFactory, TargetAtomFactory targetAtomFactory, RDF rdfFactory) {
        this.targetAtomFactory = targetAtomFactory;
        this.rdfFactory = rdfFactory;
        this.termFactory = termFactory;
    }

    public String getError() {
        return error;
    }

    private String removeBrackets(String text) {
        return text.substring(1, text.length() - 1);
    }

    private ImmutableTerm typeTerm(String text, IRI datatype) {
        ValueConstant integerConstant = termFactory.getConstantLiteral(text, datatype);
        return termFactory.getImmutableTypedTerm(integerConstant, datatype);
    }

    protected ImmutableTerm constructIRI(String text) {
        ImmutableTerm toReturn = null;
        final String PLACEHOLDER = "{}";
        List<ImmutableTerm> terms = new LinkedList<>();
        List<FormatString> tokens = parseIRI(text);
        int size = tokens.size();
        if (size == 1) {
            FormatString token = tokens.get(0);
            if (token instanceof FixedString) {
                ValueConstant uriTemplate = termFactory.getConstantLiteral(token.toString()); // a single URI template
                toReturn = termFactory.getImmutableUriTemplate(uriTemplate);
            } else if (token instanceof ColumnString) {
                // a single URI template
                Variable column = termFactory.getVariable(token.toString());
                toReturn = termFactory.getImmutableUriTemplate(column);
            }
        } else {
            StringBuilder sb = new StringBuilder();
            for (FormatString token : tokens) {
                if (token instanceof FixedString) { // if part of URI template
                    sb.append(token.toString());
                } else if (token instanceof ColumnString) {
                    sb.append(PLACEHOLDER);
                    Variable column = termFactory.getVariable(token.toString());
                    terms.add(column);
                }
            }
            ValueConstant uriTemplate = termFactory.getConstantLiteral(sb.toString()); // complete URI template
            terms.add(0, uriTemplate);
            toReturn = termFactory.getImmutableUriTemplate(ImmutableList.copyOf(terms));
        }
        return toReturn;
    }


    private List<FormatString> parseIRI(String text) {
        List<FormatString> toReturn = new ArrayList<>();
        Matcher m = varPattern.matcher(text);
        int i = 0;
        while (i < text.length()) {
            if (m.find(i)) {
                if (m.start() != i) {
                    toReturn.add(new FixedString(text.substring(i, m.start())));
                }
                String value = m.group(1);
                if (validateAttributeName(value)) {
                    toReturn.add(new ColumnString(value));
                    i = m.end();
                }
            } else {
                toReturn.add(new FixedString(text.substring(i)));
                break;
            }
        }
        return toReturn;
    }

    private ImmutableTerm constructConstantBNode(String text) {
        Matcher m = constantBnodePattern.matcher(text);
        return termFactory.getConstantBNode(m.group(1));
    }

    private ImmutableTerm constructBnodeFunction(String text) {
        ImmutableList.Builder<ImmutableTerm> args = ImmutableList.builder();
        Matcher m = varPattern.matcher(text);
        while (m.find()) {
            args.add(termFactory.getVariable(m.group(1)));
        }
        return termFactory.getImmutableBNodeTemplate(args.build());
    }

    private interface FormatString {
        int index();

        String toString();
    }

    private class FixedString implements FormatString {
        private String s;

        FixedString(String s) {
            this.s = s;
        }

        @Override
        public int index() {
            return -1;
        }  // flag code for fixed string

        @Override
        public String toString() {
            return s;
        }
    }

    private class ColumnString implements FormatString {
        private String s;

        ColumnString(String s) {
            this.s = s;
        }

        @Override
        public int index() {
            return 0;
        }  // flag code for column string

        @Override
        public String toString() {
            return s;
        }
    }

    //this function distinguishes curly bracket with
    //back slash "\{" from curly bracket "{"
    private int getIndexOfCurlyB(String str) {
        int i;
        int j;
        i = str.indexOf("{");
        j = str.indexOf("\\{");
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
                terms.add(termFactory.getConstantLiteral(st));
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
            terms.add(termFactory.getConstantLiteral(str));
        }
        return terms;
    }

    //this function returns nested concats
    //in case of more than two terms need to be concatted
    private ImmutableTerm getNestedConcat(String str) {
        List<ImmutableTerm> terms;
        terms = addToTermsList(str);
        if (terms.size() == 1) {
            return terms.get(0);
        }

        ImmutableFunctionalTerm f = termFactory.getImmutableFunctionalTerm(ExpressionOperation.CONCAT, terms.get(0), terms.get(1));
        for (int j = 2; j < terms.size(); j++) {
            f = termFactory.getImmutableFunctionalTerm(ExpressionOperation.CONCAT, f, terms.get(j));
        }
        return f;
    }


    private String concatPrefix(String prefixedName) {
        String[] tokens = prefixedName.split(":", 2);
        String uri = directives.get(tokens[0]);  // the first token is the prefix
        return uri + tokens[1];  // the second token is the local name
    }

    @Override
    public ImmutableList<TargetAtom> visitParse(ParseContext ctx) {
        ctx.directiveStatement().forEach(this::visit);
        return ctx.triplesStatement().stream()
                .flatMap(this::visitTriplesStatement)
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public Void visitDirectiveStatement(DirectiveStatementContext ctx) {
        visit(ctx.directive());
        return null;
    }

    @Override
    public Void visitDirective(DirectiveContext ctx) {
        visit(ctx.prefixID());
        return null;
    }

    @Override
    public Stream<TargetAtom> visitTriplesStatement(TriplesStatementContext ctx) {
        return visitTriples(ctx.triples());
    }

    @Override
    public Void visitPrefixID(PrefixIDContext ctx) {
        String iriref = removeBrackets(ctx.IRIREF().getText());
        String ns = ctx.PNAME_NS().getText();
        directives.put(ns.substring(0, ns.length() - 1), iriref); // remove the end colon
        return null;
    }

    @Override
    public Object visitBase(BaseContext ctx) {
        String iriRef = removeBrackets(ctx.IRIREF().getText());
        directives.put("", iriRef);
        return null;
    }

    @Override
    public Stream<TargetAtom> visitTriples(TriplesContext ctx) {
        currentSubject = visitSubject(ctx.subject());
        return visitPredicateObjectList(ctx.predicateObjectList());
    }

    @Override
    public Stream<TargetAtom> visitPredicateObjectList(PredicateObjectListContext ctx) {
        return ctx.predicateObject().stream()
                .flatMap(this::visitPredicateObject);
    }

    @Override
    public Stream<TargetAtom> visitPredicateObject(PredicateObjectContext ctx) {
        return visitObjectList(ctx.objectList())
                .map(t -> targetAtomFactory.getTripleTargetAtom(currentSubject, visitVerb(ctx.verb()), t));
    }

    @Override
    public ImmutableTerm visitVerb(VerbContext ctx) {
        ResourceContext rc = ctx.resource();
        if (rc != null) {
            return visitResource(rc);
        }
        return termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(it.unibz.inf.ontop.model.vocabulary.RDF.TYPE.getIRIString()));
    }

    @Override
    public Stream<ImmutableTerm> visitObjectList(ObjectListContext ctx) {
        return ctx.object().stream()
                .map(this::visitObject);
    }

    @Override
    public ImmutableTerm visitSubject(SubjectContext ctx) {
        ResourceContext rc = ctx.resource();
        if (rc != null) {
            return visitResource(rc);
        }
        VariableContext vc = ctx.variable();
        if (vc != null) {
            return visitVariable(vc);
        }
        BlankContext bc = ctx.blank();
        if (bc != null) {
            return visitBlank(bc);
        }
        return null;
    }

    @Override
    public ImmutableTerm visitObject(ObjectContext ctx) {
        return (ImmutableTerm) visit(ctx.children.iterator().next());
    }

    @Override
    public ImmutableTerm visitResource(ResourceContext ctx) {
        if (ctx.iriExt() != null) {
            return visitIriExt(ctx.iriExt());
        }
        return constructIRI(this.visitIri(ctx.iri()).getIRIString());
    }

    public ImmutableTerm visitIriExt(IriExtContext ctx) {
        if (ctx.IRIREF_EXT() != null) {
            return constructIRI(removeBrackets(ctx.IRIREF_EXT().getText()));
        }
        return constructIRI(concatPrefix(ctx.PREFIXED_NAME_EXT().getText()));
    }

    @Override
    public ImmutableTerm visitVariableLiteral_1(VariableLiteral_1Context ctx) {
        return termFactory.getImmutableTypedTerm(visitVariable(ctx.variable()), visitLanguageTag(ctx.languageTag()));
    }

    @Override
    public ImmutableTerm visitVariableLiteral_2(VariableLiteral_2Context ctx) {
        Variable var = visitVariable(ctx.variable());
        IRI iri = visitIri(ctx.iri());
        return termFactory.getImmutableTypedTerm(var, iri);
    }

    @Override
    public IRI visitIri(IriContext ctx) {
        TerminalNode token = ctx.PREFIXED_NAME();
        return rdfFactory.createIRI(
                token != null
                        ? concatPrefix(token.getText())
                        : removeBrackets(ctx.IRIREF().getText()));
    }

    @Override
    public Variable visitVariable(VariableContext ctx) {
        String variableName = removeBrackets(ctx.STRING_WITH_CURLY_BRACKET().getText());
        validateAttributeName(variableName);
        return termFactory.getVariable(variableName);
    }

    @Override
    public ImmutableTerm visitBlank(BlankContext ctx) {
        if (ctx.BLANK_NODE_FUNCTION() != null){
            return constructBnodeFunction(ctx.BLANK_NODE_FUNCTION().getText());
        }
        if (ctx.BLANK_NODE_LABEL() != null) {
            return constructConstantBNode(ctx.BLANK_NODE_LABEL().getText());
        }
        throw new IllegalArgumentException("Anonymous blank nodes not supported yet in mapping targets");
    }

    @Override
    public String visitLanguageTag(LanguageTagContext ctx) {
        return ctx.LANGTAG().getText().substring(1).toLowerCase();
    }

    @Override
    public ImmutableTerm visitUntypedStringLiteral(UntypedStringLiteralContext ctx) {
        LitStringContext lsc = ctx.litString();
        ImmutableTerm literal = visitLitString(lsc);
        LanguageTagContext lc = ctx.languageTag();
        if (lc != null) {
            return termFactory.getImmutableTypedTerm(literal, visitLanguageTag(lc));
        }
        return termFactory.getImmutableTypedTerm(literal, XSD.STRING);
    }

    @Override
    public ImmutableTerm visitLitString(LitStringContext ctx) {
        String str = ctx.STRING_LITERAL_QUOTE().getText();
        if (str.contains("{")) {
            return getNestedConcat(str);
        }
        return termFactory.getConstantLiteral(str.substring(1, str.length() - 1), XSD.STRING); // without the double quotes
    }

    @Override
    public ImmutableTerm visitTypedLiteral(TypedLiteralContext ctx) {
        ImmutableTerm stringValue = visitLitString(ctx.litString());
        IRI iriRef = visitIri(ctx.iri());
        return termFactory.getImmutableTypedTerm(stringValue, iriRef);
    }

    @Override
    public ImmutableTerm visitUntypedNumericLiteral(UntypedNumericLiteralContext ctx) {
        return (ImmutableTerm) visitChildren(ctx);
    }

    @Override
    public ImmutableTerm visitUntypedBooleanLiteral(UntypedBooleanLiteralContext ctx) {
        return typeTerm(ctx.BOOLEAN_LITERAL().getText(), XSD.BOOLEAN);
    }

    @Override
    public ImmutableTerm visitNumericUnsigned(NumericUnsignedContext ctx) {

        TerminalNode token = ctx.INTEGER();
        if (token != null) {
            return typeTerm(token.getText(), XSD.INTEGER);
        }
        token = ctx.DOUBLE();
        if (token != null) {
            return typeTerm(token.getText(), XSD.DOUBLE);
        }
        return typeTerm(ctx.DECIMAL().getText(), XSD.DECIMAL);
    }

    @Override
    public ImmutableTerm visitNumericPositive(NumericPositiveContext ctx) {
        TerminalNode token = ctx.INTEGER_POSITIVE();
        if (token != null) {
            return typeTerm(token.getText(), XSD.INTEGER);
        }
        token = ctx.DOUBLE_POSITIVE();
        if (token != null) {
            return typeTerm(token.getText(), XSD.DOUBLE);
        }
        return typeTerm(ctx.DECIMAL_POSITIVE().getText(), XSD.DECIMAL);
    }

    @Override
    public ImmutableTerm visitNumericNegative(NumericNegativeContext ctx) {
        TerminalNode token = ctx.INTEGER_NEGATIVE();
        if (token != null) {
            return typeTerm(token.getText(), XSD.INTEGER);
        }
        token = ctx.DOUBLE_NEGATIVE();
        if (token != null) {
            return typeTerm(token.getText(), XSD.DOUBLE);
        }
        return typeTerm(ctx.DECIMAL_NEGATIVE().getText(), XSD.DECIMAL);
    }
}
