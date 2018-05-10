package it.unibz.inf.ontop.protege.panels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TemporalRuleTextPane extends JTextPane {
    private static final SimpleAttributeSet variables = new SimpleAttributeSet();
    private static final SimpleAttributeSet operators = new SimpleAttributeSet();
    private static final SimpleAttributeSet intervals = new SimpleAttributeSet();
    private static final SimpleAttributeSet braces = new SimpleAttributeSet();
    private static final SimpleAttributeSet uris = new SimpleAttributeSet();
    private static final Map<Pattern, AttributeSet> regexAndStyleMap = new LinkedHashMap<>();

    static {
        variables.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(0xc79b29));
        variables.addAttribute(StyleConstants.CharacterConstants.Bold, true);

        operators.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(0xd0766c));
        operators.addAttribute(StyleConstants.CharacterConstants.Bold, true);

        uris.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(0x2977a7));
        uris.addAttribute(StyleConstants.CharacterConstants.Bold, true);

        intervals.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(0x29a779));
        intervals.addAttribute(StyleConstants.CharacterConstants.Bold, true);

        braces.addAttribute(StyleConstants.CharacterConstants.Bold, true);
        braces.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.BLACK);

        regexAndStyleMap.put(
                Pattern.compile("\\?[a-zA-Z0-9]+", Pattern.CASE_INSENSITIVE), variables);
        regexAndStyleMap.put(
                Pattern.compile("always in past|always in future|sometime in past|sometime in future|true|false", Pattern.CASE_INSENSITIVE), operators);
        regexAndStyleMap.put(
                Pattern.compile("[(\\[]\\s*[a-zA-Z0-9]+\\s*,\\s*[a-zA-Z0-9]+\\s*[)\\]]", Pattern.CASE_INSENSITIVE), intervals);
        regexAndStyleMap.put(
                Pattern.compile("\\([a-zA-Z0-9]+\\s*[<=>]+\\s*[\"a-zA-Z0-9]+\\s*\\)", Pattern.CASE_INSENSITIVE), intervals);
        regexAndStyleMap.put(
                Pattern.compile("http://[a-zA-Z0-9.#/-]*|[a-zA-Z0-9]*:", Pattern.CASE_INSENSITIVE), uris);
        regexAndStyleMap.put(Pattern.compile("[{}()\\[\\]]|:-", Pattern.CASE_INSENSITIVE), braces);
    }

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    TemporalRuleTextPane() {
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> stylize(false));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> stylize(false));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
    }

    TemporalRuleTextPane(String datalogMTLRule, boolean selected) {
        setRule(datalogMTLRule, selected);
    }

    private void stylize(boolean selected) {
        try {
            regexAndStyleMap.forEach((regexPattern, style) -> {
                try {
                    Matcher matcher = regexPattern.matcher(getStyledDocument().getText(0, getStyledDocument().getLength()));
                    while (matcher.find()) {
                        getStyledDocument().setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), style, true);
                    }
                } catch (BadLocationException e) {
                    LOGGER.error(e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (selected) {
            setBackground(Color.LIGHT_GRAY);
        }
    }

    void setRule(String datalogMTLRule, boolean selected) {
        setText(datalogMTLRule);
        setOpaque(true);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                new Color(0xc0c0c0),
                new Color(0xc0c0c0).darker())
        );
        stylize(selected);
    }

}
