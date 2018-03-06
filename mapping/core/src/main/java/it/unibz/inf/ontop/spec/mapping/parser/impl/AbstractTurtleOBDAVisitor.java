package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.mapping.parser.impl.TurtleOBDAParser.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractTurtleOBDAVisitor extends TurtleOBDABaseVisitor implements TurtleOBDAVisitor {

    protected abstract boolean validateAttributeName(String value);

    /**
     * Map of directives
     */
    private HashMap<String, String> directives = new HashMap<>();

    /**
     * The current subject term
     */
    private Term currentSubject;

    protected String error = "";
    private final TermFactory termFactory;
    private final AtomFactory atomFactory;
    private final RDF rdfFactory;

    public AbstractTurtleOBDAVisitor(TermFactory termFactory, AtomFactory atomFactory) {
        this.rdfFactory = new SimpleRDF();
        this.termFactory = termFactory;
        this.atomFactory = atomFactory;
    }

    public String getError() {
        return error;
    }

    private String removeBrackets(String text) {
        return text.substring(1, text.length() - 1);
    }

    private Term typeTerm(String text, IRI datatype) {
        ValueConstant integerConstant = termFactory.getConstantLiteral(text, datatype);
        return termFactory.getTypedTerm(integerConstant, datatype);
    }

    protected Term construct(String text) {
        Term toReturn = null;
        final String PLACEHOLDER = "{}";
        List<Term> terms = new LinkedList<>();
        List<FormatString> tokens = parse(text);
        int size = tokens.size();
        if (size == 1) {
            FormatString token = tokens.get(0);
            if (token instanceof FixedString) {
                ValueConstant uriTemplate = termFactory.getConstantLiteral(token.toString()); // a single URI template
                toReturn = termFactory.getUriTemplate(uriTemplate);
            } else if (token instanceof ColumnString) {
                // a single URI template
                Variable column = termFactory.getVariable(token.toString());
                toReturn = termFactory.getUriTemplate(column);
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
            toReturn = termFactory.getUriTemplate(terms);
        }
        return toReturn;
    }

    // Column placeholder pattern
    private static final String formatSpecifier = "\\{([^\\}]+)?\\}";
    private static Pattern chPattern = Pattern.compile(formatSpecifier);

    private List<FormatString> parse(String text) {
        List<FormatString> toReturn = new ArrayList<>();
        Matcher m = chPattern.matcher(text);
        int i = 0;
        while (i < text.length()) {
            if (m.find(i)) {
                if (m.start() != i) {
                    toReturn.add(new FixedString(text.substring(i, m.start())));
                }
                String value = m.group(1);
                if(validateAttributeName(value)) {
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
    private ArrayList<Term> addToTermsList(String str) {
        ArrayList<Term> terms = new ArrayList<>();
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
    private Term getNestedConcat(String str) {
        ArrayList<Term> terms;
        terms = addToTermsList(str);
        if (terms.size() == 1) {
            return terms.get(0);
        }

        Function f = termFactory.getFunction(ExpressionOperation.CONCAT, terms.get(0), terms.get(1));
        for (int j = 2; j < terms.size(); j++) {
            f = termFactory.getFunction(ExpressionOperation.CONCAT, f, terms.get(j));
        }
        return f;
    }

    /**
     * This methods construct an atom from a triple
     * <p>
     * For the input (subject, pred, object), the result is
     * <ul>
     * <li> object(subject), if pred == rdf:type and subject is grounded ; </li>
     * <li> predicate(subject, object), if pred != rdf:type and predicate is grounded ; </li>
     * <li> triple(subject, pred, object), otherwise (it is a higher order atom). </li>
     * </ul>
     */
    private Function makeAtom(Term subject, Term pred, Term object) {

        return  atomFactory.getTripleAtom(subject, pred, object);
    }


    private String concatPrefix(String prefixedName) {
        String[] tokens = prefixedName.split(":", 2);
        String uri = directives.get(tokens[0]);  // the first token is the prefix
        return uri + tokens[1];  // the second token is the local name
    }

    @Override
    public List<Function> visitParse(ParseContext ctx) {
        ctx.directiveStatement().forEach(this::visit);
        return ctx.triplesStatement().stream()
                .flatMap(c -> visitTriplesStatement(c).stream())
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
    public List<Function> visitTriplesStatement(TriplesStatementContext ctx) {
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
    public List<Function> visitTriples(TriplesContext ctx) {
        currentSubject = visitSubject(ctx.subject());
        return visitPredicateObjectList(ctx.predicateObjectList());
    }

    @Override
    public List<Function> visitPredicateObjectList(PredicateObjectListContext ctx) {
        return ctx.predicateObject().stream()
                .flatMap(c -> visitPredicateObject(c).stream())
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public List<Function> visitPredicateObject(PredicateObjectContext ctx) {
        return visitObjectList(ctx.objectList()).stream()
                .map(t -> makeAtom(currentSubject, visitVerb(ctx.verb()), t))
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public Term visitVerb(VerbContext ctx) {
        ResourceContext rc = ctx.resource();
        if (rc != null) {
            return visitResource(rc);
        }
        return termFactory.getUriTemplate(termFactory.getConstantLiteral(it.unibz.inf.ontop.model.vocabulary.RDF.TYPE.getIRIString()));
    }

    @Override
    public List<Term> visitObjectList(ObjectListContext ctx) {
        return ctx.object().stream()
                .map(this::visitObject)
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public Term visitSubject(SubjectContext ctx) {
        ResourceContext rc = ctx.resource();
        if (rc != null) {
            return visitResource(rc);
        }
        VariableContext vc = ctx.variable();
        if (vc != null) {
            return visitVariable(vc);
        }
        return null;
    }

    @Override
    public Term visitObject(ObjectContext ctx) {
        return (Term) visit(ctx.children.iterator().next());
    }

    @Override
    public Term visitResource(ResourceContext ctx) {
        if (ctx.iriExt() != null) {
            return visitIriExt(ctx.iriExt());
        }
        return construct(this.visitIri(ctx.iri()).getIRIString());
    }

    public Term visitIriExt(IriExtContext ctx) {
        if (ctx.IRIREF_EXT() != null) {
            return construct(removeBrackets(ctx.IRIREF_EXT().getText()));
        }
        return construct(concatPrefix(ctx.PREFIXED_NAME_EXT().getText()));
    }

    @Override
    public Function visitVariableLiteral_1(VariableLiteral_1Context ctx) {
        return termFactory.getTypedTerm(visitVariable(ctx.variable()), visitLanguageTag(ctx.languageTag()));
    }

    @Override
    public Function visitVariableLiteral_2(VariableLiteral_2Context ctx) {
        Variable var = visitVariable(ctx.variable());
        IRI iri = visitIri(ctx.iri());
        return termFactory.getTypedTerm(var, iri);
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
    public String visitLanguageTag(LanguageTagContext ctx) {
        return ctx.LANGTAG().getText().substring(1).toLowerCase();
    }

    @Override
    public Term visitLiteral(LiteralContext ctx) {
        StringLiteralContext slc = ctx.stringLiteral();
        if (slc != null) {
            Term literal = visitStringLiteral(slc);
            LanguageTagContext lc = ctx.languageTag();
            //if variable we cannot assign a datatype yet
            if (literal instanceof Variable) {
                return termFactory.getTypedTerm(literal, XSD.STRING);
            }
            if (lc != null) {
                return termFactory.getTypedTerm(literal, visitLanguageTag(lc));
            }
            return termFactory.getTypedTerm(literal, XSD.STRING);
        }
        return (Term) visitChildren(ctx);
    }

    @Override
    public Term visitStringLiteral(StringLiteralContext ctx) {
        String str = ctx.STRING_LITERAL_QUOTE().getText();
        if (str.contains("{")) {
            return getNestedConcat(str);
        }
        return termFactory.getConstantLiteral(str.substring(1, str.length() - 1), XSD.STRING); // without the double quotes
    }

    @Override
    public Term visitTypedLiteral(TypedLiteralContext ctx) {
        Term stringValue = visitStringLiteral(ctx.stringLiteral());
        IRI iriRef = visitIri(ctx.iri());
        return termFactory.getTypedTerm(stringValue, iriRef);
    }

    @Override
    public Term visitNumericLiteral(NumericLiteralContext ctx) {
        return (Term) visitChildren(ctx);
    }

    @Override
    public Term visitBooleanLiteral(BooleanLiteralContext ctx) {
        return typeTerm(ctx.BOOLEAN_LITERAL().getText(), XSD.BOOLEAN);
    }

    @Override
    public Term visitNumericUnsigned(NumericUnsignedContext ctx) {

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
    public Term visitNumericPositive(NumericPositiveContext ctx) {
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
    public Term visitNumericNegative(NumericNegativeContext ctx) {
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
