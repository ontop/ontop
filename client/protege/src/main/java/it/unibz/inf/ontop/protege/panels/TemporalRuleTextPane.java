package it.unibz.inf.ontop.protege.panels;

import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;
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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TemporalRuleTextPane extends JTextPane {
    private static final SimpleAttributeSet variables = new SimpleAttributeSet();
    private static final SimpleAttributeSet intervals = new SimpleAttributeSet();
    private static final SimpleAttributeSet braces = new SimpleAttributeSet();
    private static final SimpleAttributeSet uris = new SimpleAttributeSet();
    private static final SimpleAttributeSet defaultFont = new SimpleAttributeSet();
    private static final Map<Pattern, AttributeSet> regexAndStyleMap = new HashMap<>();

    static {
        defaultFont.addAttribute(StyleConstants.CharacterConstants.Family, "Lucida Grande");
        defaultFont.addAttribute(StyleConstants.CharacterConstants.Size, 12);
        defaultFont.addAttribute(StyleConstants.CharacterConstants.Bold, false);

        variables.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(199, 155, 41));
        variables.addAttribute(StyleConstants.CharacterConstants.Bold, true);

        uris.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(41, 119, 167));
        uris.addAttribute(StyleConstants.CharacterConstants.Italic, true);

        intervals.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(41, 167, 121));
        intervals.addAttribute(StyleConstants.CharacterConstants.Bold, true);

        braces.addAttribute(StyleConstants.CharacterConstants.Bold, true);

        regexAndStyleMap.put(
                Pattern.compile("\\?[a-zA-Z0-9]+|always in past|always in future|sometime in past|sometime in future|true|false", Pattern.CASE_INSENSITIVE), variables);
        regexAndStyleMap.put(
                Pattern.compile("[(\\[]\\s*[a-zA-Z0-9]+\\s*,\\s*[a-zA-Z0-9]+\\s*[)\\]]", Pattern.CASE_INSENSITIVE), intervals);
        regexAndStyleMap.put(
                Pattern.compile("\\([a-zA-Z0-9]+\\s*[<=>]+\\s*[\"a-zA-Z0-9]+\\s*\\)", Pattern.CASE_INSENSITIVE), intervals);
        regexAndStyleMap.put(
                Pattern.compile("http://[a-zA-Z0-9.#/-]*|[a-zA-Z0-9]+:[a-zA-Z0-9]+", Pattern.CASE_INSENSITIVE), uris);
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

    TemporalRuleTextPane(DatalogMTLRule datalogMTLRule, boolean selected) {
        setRule(datalogMTLRule, selected);
    }

    private void stylize(boolean selected) {
        try {
            regexAndStyleMap.forEach((regexPattern, style) -> {
                try {
                    Matcher matcher = regexPattern.matcher(getStyledDocument().getText(0, getStyledDocument().getLength()));
                    while (matcher.find()) {
                        getStyledDocument().setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), style, false);
                    }
                } catch (BadLocationException e) {
                    LOGGER.error(e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        // set background color
        if (selected) {
            setBackground(Color.LIGHT_GRAY);
        } else {
            setBackground(new Color(240, 245, 240));
        }
        // set font style
        getStyledDocument().setParagraphAttributes(0, getStyledDocument().getLength(), defaultFont, false);
    }

    void setRule(DatalogMTLRule datalogMTLRule, boolean selected) {
        setText(datalogMTLRule.toString());
        setOpaque(true);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, new Color(192, 192, 192), new Color(192, 192, 192).darker()));
        stylize(selected);
    }

}
