package it.unibz.inf.ontop.protege.utils;

import com.google.common.collect.ImmutableList;
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
import org.apache.commons.rdf.api.IRI;
import org.semanticweb.owlapi.rdf.rdfxml.parser.RDFConstants;

import javax.swing.text.*;
import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;


public class TargetQueryStyledDocument extends DefaultStyledDocument {

    public static final Font TARGET_QUERY_FONT = new Font("Lucida Grande", Font.PLAIN, 14);

    private final SimpleAttributeSet plainStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet punctuationStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet classStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet objectPropertyStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet dataPropertyStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet annotationPropertyStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet individualStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet templateArgumentStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet errorStyle = new SimpleAttributeSet();

    private final OBDAModelManager obdaModelManager;
    private final Consumer<TargetQueryStyledDocument> validationCallback;

    private ImmutableList<String> invalidPlaceholders = ImmutableList.of();

    public TargetQueryStyledDocument(OBDAModelManager obdaModelManager, Consumer<TargetQueryStyledDocument> validationCallback) {
        this.obdaModelManager = obdaModelManager;
        this.validationCallback = validationCallback;

        StyleConstants.setFontFamily(plainStyle, TARGET_QUERY_FONT.getFamily());
        StyleConstants.setFontSize(plainStyle, TARGET_QUERY_FONT.getSize());

        StyleConstants.setBold(punctuationStyle, true);
        StyleConstants.setForeground(punctuationStyle, Color.GRAY);

        StyleConstants.setForeground(annotationPropertyStyle, new Color(109, 159, 162));
        StyleConstants.setBold(annotationPropertyStyle, true);

        StyleConstants.setForeground(dataPropertyStyle, new Color(41, 167, 121));
        StyleConstants.setBold(dataPropertyStyle, true);

        StyleConstants.setForeground(objectPropertyStyle, new Color(41, 119, 167));
        StyleConstants.setBold(objectPropertyStyle, true);

        StyleConstants.setForeground(classStyle, new Color(199, 155, 41));
        StyleConstants.setBold(classStyle, true);

        StyleConstants.setForeground(individualStyle, new Color(83, 24, 82));
        StyleConstants.setBold(individualStyle, true);

        StyleConstants.setBold(templateArgumentStyle, true);
        StyleConstants.setForeground(templateArgumentStyle, new Color(97, 66, 151));

        StyleConstants.setForeground(errorStyle, Color.RED);
        StyleConstants.setBold(errorStyle, true);
        StyleConstants.setUnderline(errorStyle, true);
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

    public ImmutableSet<IRI> validate() throws TargetQueryParserException {
        ImmutableSet.Builder<IRI> unrecognisedIRIsBuilder = ImmutableSet.builder();
        try {
            setCharacterAttributes(0, getLength(), plainStyle, true);

            highlight("(", punctuationStyle);
            highlight(")", punctuationStyle);
            highlight("{", punctuationStyle);
            highlight("}", punctuationStyle);
            highlight(".", punctuationStyle);
            highlight(",", punctuationStyle);
            highlight(";", punctuationStyle);
            highlight("a", punctuationStyle);
            highlight("GRAPH", punctuationStyle);

            for (TargetAtom atom : getTargetAtoms())
                unrecognisedIRIsBuilder.addAll(validateTargetAtom(atom));
        }
        catch (BadLocationException ignore) {
        }
        return unrecognisedIRIsBuilder.build();
    }

    private ImmutableSet<IRI> validateTargetAtom(TargetAtom atom) throws BadLocationException {
        ImmutableSet.Builder<IRI> unrecognisedIRIsBuilder = ImmutableSet.builder();
        OntologySignature vocabulary = obdaModelManager.getCurrentVocabulary();

        ImmutableList<ImmutableTerm> substitutedTerms = atom.getSubstitutedTerms();
        RDFAtomPredicate atomPredicate = (RDFAtomPredicate) atom.getProjectionAtom().getPredicate();

        ImmutableTerm term1 = atomPredicate.getSubject(substitutedTerms);
        highlightTemplateArguments(term1);
        if (term1 instanceof IRIConstant)
            highlight(((IRIConstant) term1).getIRI(), individualStyle);

        ImmutableTerm term2 = atomPredicate.getProperty(substitutedTerms);
        highlightTemplateArguments(term2);
        if (term2 instanceof IRIConstant) {
            IRI predicateIri = ((IRIConstant) term2).getIRI();
            if (predicateIri.getIRIString().equals(RDFConstants.RDF_TYPE)) {
                ImmutableTerm term3 = atomPredicate.getObject(substitutedTerms);
                if (term3 instanceof IRIConstant) {
                    IRI classIri = ((IRIConstant) term3).getIRI();
                    if (vocabulary.containsClass(classIri))
                        highlight(classIri, classStyle);
                    else {
                        highlight(classIri, errorStyle);
                        unrecognisedIRIsBuilder.add(classIri);
                    }
                }
            }
            else {
                if (vocabulary.containsObjectProperty(predicateIri))
                    highlight(predicateIri, objectPropertyStyle);
                else if (vocabulary.containsDataProperty(predicateIri))
                    highlight(predicateIri, dataPropertyStyle);
                else if (vocabulary.containsAnnotationProperty(predicateIri))
                    highlight(predicateIri, annotationPropertyStyle);
                else if (vocabulary.isBuiltinProperty(predicateIri))
                    highlight(predicateIri, punctuationStyle);
                else {
                    highlight(predicateIri, errorStyle);
                    unrecognisedIRIsBuilder.add(predicateIri);
                }

                ImmutableTerm term3 = atomPredicate.getObject(substitutedTerms);
                highlightTemplateArguments(term3);
                if (term3 instanceof IRIConstant)
                    highlight(((IRIConstant) term3).getIRI(), individualStyle);
            }
        }
        else {
            ImmutableTerm term3 = atomPredicate.getObject(substitutedTerms);
            highlightTemplateArguments(term3);
            if (term3 instanceof IRIConstant)
                highlight(((IRIConstant) term3).getIRI(), individualStyle);
        }
        Optional<ImmutableTerm> term4 = atomPredicate.getGraph(substitutedTerms);
        if (term4.isPresent())
            highlightTemplateArguments(term4.get());

        return unrecognisedIRIsBuilder.build();
    }

    private ImmutableList<TargetAtom> getTargetAtoms() throws TargetQueryParserException, BadLocationException {
        try {
            String input = getText(0, getLength());
            if (!input.isEmpty())
                return obdaModelManager.getTriplesMapCollection().parseTargetQuery(input);
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
                setCharacterAttributes(pos >= input.length() ? pos - 1 : pos, 1, errorStyle, false);
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

    private void highlightTemplateArguments(ImmutableTerm term) throws BadLocationException {
        Optional<ImmutableFunctionalTerm> template = getTemplateImmutableFunctionalTerm(term);

        if (template.isPresent()) {
            String input = getText(0, getLength());
            for (Variable v : template.get().getVariables()) {
                String arg = "{" + v.getName() + "}";
                SimpleAttributeSet style = invalidPlaceholders.contains(v.getName()) ? errorStyle : templateArgumentStyle;
                int p, po = 0;
                while ((p = input.indexOf(arg, po)) != -1) {
                    setCharacterAttributes(p + 1, arg.length() - 2, style, false);
                    po = p + arg.length();
                }
            }
        }
    }

    private void highlight(IRI iri, SimpleAttributeSet attributeSet) throws BadLocationException {
        PrefixManager prefixManager = obdaModelManager.getTriplesMapCollection().getMutablePrefixManager();
        String rendered = prefixManager.getShortForm(iri.getIRIString());
        highlight(rendered, attributeSet);
    }


    private void highlight(String s, SimpleAttributeSet attributeSet) throws BadLocationException {
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