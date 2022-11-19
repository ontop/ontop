package it.unibz.inf.ontop.protege.utils;


import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.function.Consumer;

public class ColorSettings {
    public enum Category {
        BACKGROUND("background", "background"),
        PLAIN("plain", "default text"),
        ERROR("error", "errors"),
        PUNCTUATION("punctuation", "punctuation"),
        CLASS("class", "classes"),
        OBJECT_PROPERTY("object-property", "object properties"),
        DATA_PROPERTY("data-property", "data properties"),
        ANNOTATION_PROPERTY("annotation-property", "annotation properties"),
        INDIVIDUAL("individual", "individuals"),
        TEMPLATE_ARGUMENT("template-argument", "IRI template arguments");

        private final String property, description;

        Category(String property, String description) {
            this.property = property;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    };

    private final Map<Category, Color> colors = new HashMap<>();
    private final Map<Category, Color> selectedColors = new HashMap<>();

    public ColorSettings() {
        revertToDefaults();
        load();
    }

    public final void revertToDefaults() {
        colors.clear();
        selectedColors.clear();

        colors.put(Category.ERROR, Color.RED);
        colors.put(Category.PUNCTUATION, Color.GRAY);
        colors.put(Category.ANNOTATION_PROPERTY, new Color(109, 159, 162));
        colors.put(Category.DATA_PROPERTY, new Color(41, 167, 121));
        colors.put(Category.OBJECT_PROPERTY, new Color(41, 119, 167));
        colors.put(Category.CLASS, new Color(199, 155, 41));
        colors.put(Category.INDIVIDUAL, new Color(83, 24, 82));
        selectedColors.putAll(colors);

        colors.put(Category.TEMPLATE_ARGUMENT, new Color(97, 66, 151));
        selectedColors.put(Category.TEMPLATE_ARGUMENT, Color.LIGHT_GRAY);

        colors.put(Category.PLAIN, UIManager.getDefaults().getColor("List.foreground"));
        selectedColors.put(Category.PLAIN, UIManager.getDefaults().getColor("List.selectionForeground"));

        colors.put(Category.BACKGROUND, new Color(240, 245, 240));
        selectedColors.put(Category.BACKGROUND, UIManager.getDefaults().getColor("List.selectionBackground"));
    }

    private static final String PREFIX = "MappingList.";
    private static final String SELECTED_SUFFIX = ".selected";
    private static final String APPLICATION = "OBDA Plugin";

    private void load() {
        PreferencesManager man = PreferencesManager.getInstance();
        Preferences pref = man.getApplicationPreferences(APPLICATION);
        for (Map.Entry<Category, Color> k : colors.entrySet())
            loadColor(pref, k.getKey().property, c -> colors.put(k.getKey(), c));
        for (Map.Entry<Category, Color> k : selectedColors.entrySet())
            loadColor(pref,  k.getKey().property + SELECTED_SUFFIX, c -> selectedColors.put(k.getKey(), c));
    }

    private static void loadColor(Preferences pref, String key, Consumer<Color> consumer) {
        int c = pref.getInt(PREFIX + key, -1);
        if (c != -1)
            consumer.accept(new Color(c));
    }

    public void store() {
        PreferencesManager man = PreferencesManager.getInstance();
        Preferences pref = man.getApplicationPreferences(APPLICATION);
        for (Map.Entry<Category, Color> e : colors.entrySet())
            storeColor(pref, e.getKey().property, e.getValue());
        for (Map.Entry<Category, Color> e : selectedColors.entrySet())
            storeColor(pref, e.getKey().property + SELECTED_SUFFIX, e.getValue());
    }

    private static void storeColor(Preferences pref, String key, Color color) {
        if (color != null)
            pref.putInt(PREFIX + key, color.getRGB() & 0x00ffffff);
    }

    public void updateFrom(ColorSettings colorSettings) {
        revertToDefaults();

        colors.putAll(colorSettings.colors);
        selectedColors.putAll(colorSettings.selectedColors);
    }

    public Color getBackground(boolean isSelected) {
        return (isSelected ? selectedColors : colors).get(Category.BACKGROUND);
    }

    public Color getForeground(boolean isSelected, Category category) {
        return (isSelected ? selectedColors : colors).get(category);
    }

    public void setBackground(boolean isSelected, Color color) {
        (isSelected ? selectedColors : colors).put(Category.BACKGROUND, color);

    }
    public void setForeground(boolean isSelected, Category category, Color color) {
        (isSelected ? selectedColors : colors).put(category, color);
    }
}
