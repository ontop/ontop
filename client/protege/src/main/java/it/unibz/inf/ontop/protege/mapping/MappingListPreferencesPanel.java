package it.unibz.inf.ontop.protege.mapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.utils.ColorSettings;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MappingListPreferencesPanel extends OWLPreferencesPanel {

    private static final int GAP = 2;

    private ColorSettings colorSettings;

    @Override
    public void initialise() {
        setPreferredSize(new Dimension(620, 300));
        setLayout(new GridBagLayout());

        Map<Boolean, Map<ColorSettings.Category, JLabel>> samples = ImmutableMap.of(
                false, new HashMap<>(),
                true, new HashMap<>());

        colorSettings = new ColorSettings();
        colorSettings.updateFrom(OBDAEditorKitSynchronizerPlugin.getColorSettings(getOWLEditorKit()));

        int y = 0;
        for (ColorSettings.Category category : ColorSettings.Category.values()) {
            add(new JLabel(category.getDescription()),
                    new GridBagConstraints(0, y, 1, 1, 0, 0,
                            GridBagConstraints.WEST, GridBagConstraints.NONE,
                            new Insets(GAP, GAP, GAP, GAP), 0, 0));
            int x = 1;
            for (Boolean isSelected : ImmutableList.of(false, true)) {
                String s = "sample "  + (isSelected ? "selected " : "") + "text";
                String text = (category == ColorSettings.Category.PLAIN)
                       ? s
                       : "<html><b>" + s + "</b></html>";
                JLabel sample = new JLabel(text);
                samples.get(isSelected).put(category, sample);
                sample.setHorizontalAlignment(SwingConstants.CENTER);
                sample.setOpaque(true);
                Optional.ofNullable(colorSettings.getForeground(isSelected, category))
                        .ifPresent(sample::setForeground);
                Optional.ofNullable(colorSettings.getBackground(isSelected))
                        .ifPresent(sample::setBackground);
                add(sample,
                        new GridBagConstraints(x, y, 1, 1, 1, 0,
                                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                new Insets(GAP, GAP, GAP, GAP), 0, 0));
                x++;
                JButton button = new JButton("Color...");
                button.addActionListener(evt -> {
                    Color newColor = JColorChooser.showDialog(
                            this,
                            "Choose Color",
                            sample.getForeground());
                    if (newColor != null) {
                        sample.setForeground(newColor);
                        if (category == ColorSettings.Category.BACKGROUND) {
                            colorSettings.setBackground(isSelected, newColor);
                            for (JLabel component : samples.get(isSelected).values())
                                component.setBackground(newColor);
                        }
                        else
                            colorSettings.setForeground(isSelected, category, newColor);
                    }
                });
                add(button,
                        new GridBagConstraints(x, y, 1, 1, 0, 0,
                                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                new Insets(GAP, GAP, GAP, GAP), 0, 0));
                x++;
            }
            y++;
        }
        JButton defaultButton = new JButton("Revert to defaults");
        defaultButton.addActionListener(evt -> {
            colorSettings.revertToDefaults();
            for (Boolean isSelected : ImmutableList.of(false, true)) {
                for (ColorSettings.Category category : ColorSettings.Category.values()) {
                    samples.get(isSelected).get(category).setForeground(colorSettings.getForeground(isSelected, category));
                    samples.get(isSelected).get(category).setBackground(colorSettings.getBackground(isSelected));
                }
            }
        });
        add(defaultButton,
                new GridBagConstraints(0, y, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        add(new JLabel(),
                new GridBagConstraints(1, y + 1, 1, 1, 1, 1,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));
    }

    @Override
    public void dispose() {  /* NO-OP */ }

    @Override
    public void applyChanges() {
        colorSettings.store();
        OBDAEditorKitSynchronizerPlugin.getColorSettings(getOWLEditorKit()).updateFrom(colorSettings);
    }
}
