package it.unibz.inf.ontop.protege.mapping;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class MappingListPreferencesPanel extends OWLPreferencesPanel {

    public static final String BACKGROUND = ".background";
    public static final String FOREGOUND = ".foreground";

    public static final String DEFAULT_COLOR_PROPERTY = "MappingList";
    public static final String SELECTION_COLOR_PROPERTY = "MappingList.selection";

    private static final ImmutableMap<String,String> COLOR_PROPERTY_DESCRIPTION = ImmutableMap.of(
            DEFAULT_COLOR_PROPERTY, "Default",
            SELECTION_COLOR_PROPERTY, "Selection"
    );

    private static final int GAP = 2;

    @Override
    public void initialise() {
        setPreferredSize(new Dimension(620, 300));
        setLayout(new GridBagLayout());

        int y = 0;
        for (Map.Entry<String, String> e : COLOR_PROPERTY_DESCRIPTION.entrySet()) {
            add(new JLabel(e.getValue()),
                    new GridBagConstraints(0, y, 1, 1, 0, 0,
                            GridBagConstraints.WEST, GridBagConstraints.NONE,
                            new Insets(GAP, GAP, GAP, GAP), 0, 0));

            JLabel sample = new JLabel("sample text");
            sample.setHorizontalAlignment(SwingConstants.CENTER);
            sample.setOpaque(true);
            OBDAEditorKitSynchronizerPlugin.getColor(getOWLEditorKit(), e.getKey() + FOREGOUND)
                    .ifPresent(sample::setForeground);
            OBDAEditorKitSynchronizerPlugin.getColor(getOWLEditorKit(), e.getKey() + BACKGROUND)
                    .ifPresent(sample::setBackground);
            add(sample,
                    new GridBagConstraints(1, y, 1, 1, 1, 0,
                            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                            new Insets(GAP, GAP, GAP, GAP), 0, 0));

            JButton fgButton = new JButton("Foreground");
            fgButton.addActionListener(evt -> {
                Color newColor = JColorChooser.showDialog(
                        this,
                        "Choose Foreground Color",
                        sample.getForeground());
                if (newColor != null)
                    sample.setForeground(newColor);
            });
            add(fgButton,
                    new GridBagConstraints(2, y, 1, 1, 0, 0,
                            GridBagConstraints.CENTER, GridBagConstraints.NONE,
                            new Insets(GAP, GAP, GAP, GAP), 0, 0));

            JButton bgButton = new JButton("Background");
            bgButton.addActionListener(evt -> {
                Color newColor = JColorChooser.showDialog(
                        this,
                        "Choose Background Color",
                        sample.getBackground());
                if (newColor != null)
                    sample.setBackground(newColor);
            });
            add(bgButton,
                    new GridBagConstraints(3, y, 1, 1, 0, 0,
                            GridBagConstraints.CENTER, GridBagConstraints.NONE,
                            new Insets(GAP, GAP, GAP, GAP), 0, 0));
            y++;
        }
        add(new JLabel(),
                new GridBagConstraints(1, y, 1, 1, 1, 1,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));
    }

    @Override
    public void dispose() {  /* NO-OP */ }

    @Override
    public void applyChanges() {

    }
}
