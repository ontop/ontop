package it.unibz.inf.ontop.protege.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.OntologySignature;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.semanticweb.owlapi.rdf.rdfxml.parser.RDFConstants;

import javax.swing.text.*;
import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;


public class TargetQueryStyledDocument extends DefaultStyledDocument {

    public static final Font TARGET_QUERY_FONT = new Font("Lucida Grande", Font.PLAIN, 14);

    private final SimpleAttributeSet plainStyle = new SimpleAttributeSet();

    private final OBDAModelManager obdaModelManager;
    private final ColorSettings colorSettings;
    private final Consumer<TargetQueryStyledDocument> validationCallback;

    private ImmutableList<String> invalidPlaceholders = ImmutableList.of();
    private boolean isSelected = false;

    public TargetQueryStyledDocument(OBDAModelManager obdaModelManager, ColorSettings colorSettings, Consumer<TargetQueryStyledDocument> validationCallback) {
        this.obdaModelManager = obdaModelManager;
        this.colorSettings = colorSettings;
        this.validationCallback = validationCallback;

        StyleConstants.setFontFamily(plainStyle, TARGET_QUERY_FONT.getFamily());
        StyleConstants.setFontSize(plainStyle, TARGET_QUERY_FONT.getSize());
    }

    @Override
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offset, str, a);
        validationCallback.accept(this);
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        super.remove(offs, len);
        validationCallback.accept(this);
    }

    public void setInvalidPlaceholders(ImmutableList<String> invalidPlaceholders) {
        this.invalidPlaceholders = invalidPlaceholders;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public ImmutableSet<IRI> validate() throws TargetQueryParserException {
        ImmutableSet.Builder<IRI> unrecognisedIRIsBuilder = ImmutableSet.builder();
        ImmutableMap<ColorSettings.Category, AttributeSet> styles =
                Stream.of(ColorSettings.Category.values())
                .filter(c -> c != ColorSettings.Category.BACKGROUND && c != ColorSettings.Category.PLAIN)
                .collect(ImmutableCollectors.toMap(c -> c, c -> {
                    SimpleAttributeSet a = new SimpleAttributeSet();
                    StyleConstants.setBold(a, true);
                    StyleConstants.setForeground(a, colorSettings.getForeground(isSelected, c));
                    if (c == ColorSettings.Category.ERROR)
                        StyleConstants.setUnderline(a, true);
                    return a;
                }));

        try {
            setCharacterAttributes(0, getLength(), plainStyle, true);
            AttributeSet punctuationStyle = styles.get(ColorSettings.Category.PUNCTUATION);
            highlight("(", punctuationStyle);
            highlight(")", punctuationStyle);
            highlight("{", punctuationStyle);
            highlight("}", punctuationStyle);
            highlight(".", punctuationStyle);
            highlight(",", punctuationStyle);
            highlight(";", punctuationStyle);
            highlight("a", punctuationStyle);
            highlight("GRAPH", punctuationStyle);

            for (TargetAtom atom : getTargetAtoms(styles))
                unrecognisedIRIsBuilder.addAll(validateTargetAtom(atom, styles));
        }
        catch (BadLocationException ignore) {
        }
        return unrecognisedIRIsBuilder.build();
    }

    private ImmutableSet<IRI> validateTargetAtom(TargetAtom atom, ImmutableMap<ColorSettings.Category, AttributeSet> styles) throws BadLocationException {
        ImmutableSet.Builder<IRI> unrecognisedIRIsBuilder = ImmutableSet.builder();
        OntologySignature vocabulary = obdaModelManager.getCurrentOBDAModel().getOntologySignature();

        ImmutableList<ImmutableTerm> substitutedTerms = atom.getSubstitutedTerms();
        RDFAtomPredicate atomPredicate = (RDFAtomPredicate) atom.getProjectionAtom().getPredicate();

        ImmutableTerm term1 = atomPredicate.getSubject(substitutedTerms);
        highlightTemplateArguments(term1, styles);
        if (term1 instanceof IRIConstant)
            highlight(((IRIConstant) term1).getIRI(), styles.get(ColorSettings.Category.INDIVIDUAL));

        ImmutableTerm term2 = atomPredicate.getProperty(substitutedTerms);
        highlightTemplateArguments(term2, styles);
        if (term2 instanceof IRIConstant) {
            IRI predicateIri = ((IRIConstant) term2).getIRI();
            if (predicateIri.getIRIString().equals(RDFConstants.RDF_TYPE)) {
                ImmutableTerm term3 = atomPredicate.getObject(substitutedTerms);
                if (term3 instanceof IRIConstant) {
                    IRI classIri = ((IRIConstant) term3).getIRI();
                    if (vocabulary.containsClass(classIri))
                        highlight(classIri, styles.get(ColorSettings.Category.CLASS));
                    else {
                        highlight(classIri, styles.get(ColorSettings.Category.ERROR));
                        unrecognisedIRIsBuilder.add(classIri);
                    }
                }
            }
            else {
                if (vocabulary.containsObjectProperty(predicateIri))
                    highlight(predicateIri, styles.get(ColorSettings.Category.OBJECT_PROPERTY));
                else if (vocabulary.containsDataProperty(predicateIri))
                    highlight(predicateIri, styles.get(ColorSettings.Category.DATA_PROPERTY));
                else if (vocabulary.containsAnnotationProperty(predicateIri))
                    highlight(predicateIri, styles.get(ColorSettings.Category.ANNOTATION_PROPERTY));
                else if (vocabulary.isBuiltinProperty(predicateIri))
                    highlight(predicateIri, styles.get(ColorSettings.Category.PUNCTUATION));
                else {
                    highlight(predicateIri, styles.get(ColorSettings.Category.ERROR));
                    unrecognisedIRIsBuilder.add(predicateIri);
                }

                ImmutableTerm term3 = atomPredicate.getObject(substitutedTerms);
                highlightTemplateArguments(term3, styles);
                if (term3 instanceof IRIConstant)
                    highlight(((IRIConstant) term3).getIRI(), styles.get(ColorSettings.Category.INDIVIDUAL));
            }
        }
        else {
            ImmutableTerm term3 = atomPredicate.getObject(substitutedTerms);
            highlightTemplateArguments(term3, styles);
            if (term3 instanceof IRIConstant)
                highlight(((IRIConstant) term3).getIRI(), styles.get(ColorSettings.Category.INDIVIDUAL));
        }
        Optional<ImmutableTerm> term4 = atomPredicate.getGraph(substitutedTerms);
        if (term4.isPresent())
            highlightTemplateArguments(term4.get(), styles);

        return unrecognisedIRIsBuilder.build();
    }

    private ImmutableList<TargetAtom> getTargetAtoms(ImmutableMap<ColorSettings.Category, AttributeSet> styles) throws TargetQueryParserException, BadLocationException {
        try {
            String input = getText(0, getLength());
            if (!input.isEmpty())
                return obdaModelManager.getCurrentOBDAModel().getTriplesMapFactory().getTargetQuery(input);
        }
        catch (TargetQueryParserException e) {
            if (e.getLine() > 0) {
                int column = e.getColumn();
                int line = e.getLine();
                String input = getText(0, getLength());
                int lineStart = 0;
                for (int i = 1; i < line; i++) {
                    lineStart = input.indexOf('\n', lineStart) + 1;
                }
                int pos = lineStart + column;
                setCharacterAttributes(pos >= input.length() ? pos - 1 : pos, 1, styles.get(ColorSettings.Category.ERROR), false);
            }
            throw e;
        }
        return ImmutableList.of();
    }

    private Optional<ImmutableFunctionalTerm> getTemplateImmutableFunctionalTerm(ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm) {
            return Optional.of((ImmutableFunctionalTerm)term);
        }
        return Optional.empty();
    }

    private void highlightTemplateArguments(ImmutableTerm term, ImmutableMap<ColorSettings.Category, AttributeSet> styles) throws BadLocationException {
        Optional<ImmutableFunctionalTerm> template = getTemplateImmutableFunctionalTerm(term);

        if (template.isPresent()) {
            String input = getText(0, getLength());
            for (Variable v : template.get().getVariables()) {
                String arg = "{" + v.getName() + "}";
                AttributeSet style = invalidPlaceholders.contains(v.getName())
                        ? styles.get(ColorSettings.Category.ERROR)
                        : styles.get(ColorSettings.Category.TEMPLATE_ARGUMENT);
                int p, po = 0;
                while ((p = input.indexOf(arg, po)) != -1) {
                    setCharacterAttributes(p + 1, arg.length() - 2, style, false);
                    po = p + arg.length();
                }
            }
        }
    }

    private void highlight(IRI iri, AttributeSet attributeSet) throws BadLocationException {
        PrefixManager prefixManager = obdaModelManager.getCurrentOBDAModel().getMutablePrefixManager();
        String rendered = prefixManager.getShortForm(iri.getIRIString());
        highlight(rendered, attributeSet);
    }


    private void highlight(String s, AttributeSet attributeSet) throws BadLocationException {
        String input = getText(0, getLength());
        int total = input.length();
        int len = s.length();
        int pos = input.indexOf(s, 0);
        while (pos != -1) {
            if ((pos == 0 || isDelimiter(input.charAt(pos - 1))) &&
                    (pos + len == total || isDelimiter(input.charAt(pos + len))))
                setCharacterAttributes(pos, len, attributeSet, false);
            pos = input.indexOf(s, pos + len);
        }
    }


    private static boolean isDelimiter(char c) {
        return Character.isWhitespace(c)
                || c == '.' || c == ';' || c == ',' || c == '<' || c == '>' || c == '"'
                || c == '{' || c == '}';
    }
}