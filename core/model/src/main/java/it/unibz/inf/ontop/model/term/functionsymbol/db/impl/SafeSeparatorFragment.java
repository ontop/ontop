package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.template.impl.TemplateParser;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SafeSeparatorFragment {
    // the whole fragment as a string
    private final String fragment;
    // individual components: either strings or placeholders
    // (each two strings are separated by a placeholder); the list can be empty
    private final ImmutableList<Template.Component> components;
    // the trailing safe separator
    private final char separator;
    // the part before the first placehorder (can be empty)
    private final String prefix;
    // the part after the last placeholder
    // (can be empty or can coincide with prefix, if there are no placeholders)
    private final String suffix;
    private final boolean hasPlaceholders;

    private static final Pattern TO_BE_ESCAPED = Pattern.compile("[<(\\[{\\\\^=$!|\\]})?*+.>]");

    /**
     * TODO: enrich this list (incomplete)
     */
    protected static final ImmutableSet<Character> SOME_SAFE_SEPARATORS = ImmutableSet.of(
            '!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=',  // sub-delims
            '#', '/');

    public static final String NOT_A_SAFE_SEPARATOR_REGEX = "[^"
            + SOME_SAFE_SEPARATORS.stream()
            .map(Object::toString)
            .map(SafeSeparatorFragment::makeRegexSafe)
            .collect(Collectors.joining())
            + "]*";

    private SafeSeparatorFragment(String fragment, char separator) {
        this.fragment = fragment;
        this.separator = separator;
        this.components = TemplateParser.getComponents(fragment, true);
        if (components.isEmpty()) {
            prefix = "";
            suffix = "";
            hasPlaceholders = false;
        }
        else {
            Template.Component first = components.get(0);
            prefix = first.isColumn() ? "" : first.getComponent();
            Template.Component last = components.get(components.size() - 1);
            suffix = last.isColumn() ? "" : last.getComponent();
            hasPlaceholders = components.size() > 1 || first.isColumn();
        }
    }

    public String getFragment() {
        return fragment;
    }

    public char getSeparator() {
        return separator;
    }

    public ImmutableList<Template.Component> getComponents() { return components; }

    @Override
    public String toString() {
        return fragment + (separator != 0 ? separator : "") + "(" + prefix + ":" + suffix + ")";
    }

    public static ImmutableList<SafeSeparatorFragment> split(String s) {
        ImmutableList.Builder<SafeSeparatorFragment> builder = ImmutableList.builder();
        int start = 0, end = firstIndexOfSafeSeparator(s, start);
        while (end != -1) {
            int next_end = end;
            while ((next_end = firstIndexOfSafeSeparator(s, next_end + 1)) == end + 1)
                end = next_end;

            builder.add(new SafeSeparatorFragment(s.substring(start, end), s.charAt(end)));
            start = end + 1;
            end = next_end;
        }
        if (start < s.length())
            builder.add(new SafeSeparatorFragment(s.substring(start), (char) 0));

        return builder.build();
    }

    private static int firstIndexOfSafeSeparator(String s, int start) {
        for (int i = start; i < s.length(); i++)
            if (SOME_SAFE_SEPARATORS.contains(s.charAt(i)))
                return i;
        return -1;
    }

    private static boolean matchFragments(SafeSeparatorFragment subTemplate1, SafeSeparatorFragment subTemplate2) {
        if (subTemplate1.fragment.equals(subTemplate2.fragment))
            return true; // this also handles the case when there are no placeholders in both fragments

        if (subTemplate1.hasPlaceholders && !subTemplate2.hasPlaceholders) {
            if (!subTemplate2.fragment.startsWith(subTemplate1.prefix))
                return false;
            String remainder = subTemplate2.fragment.substring(subTemplate1.prefix.length());
            return remainder.endsWith(subTemplate1.suffix);
        }

        if (subTemplate2.hasPlaceholders && !subTemplate1.hasPlaceholders) {
            if (!subTemplate1.fragment.startsWith(subTemplate2.prefix))
                return false;
            String remainder = subTemplate1.fragment.substring(subTemplate2.prefix.length());
            return remainder.endsWith(subTemplate2.suffix);
        }

        // both contain placeholders
        return (subTemplate1.prefix.startsWith(subTemplate2.prefix) || subTemplate2.prefix.startsWith(subTemplate1.prefix))
                && (subTemplate1.suffix.startsWith(subTemplate2.suffix) || subTemplate2.suffix.startsWith(subTemplate1.suffix));
    }

    /**
     * Is guaranteed not to return false negative.
     */
    public static boolean areCompatible(ImmutableList<SafeSeparatorFragment> fragments1, ImmutableList<SafeSeparatorFragment> fragments2) {

        if (fragments1 == fragments2)
            return true;

        return fragments1.size() == fragments2.size()

                && IntStream.range(0, fragments1.size())
                .allMatch(i -> fragments1.get(i).getSeparator() == fragments2.get(i).getSeparator())

                && IntStream.range(0, fragments1.size())
                .allMatch(i -> SafeSeparatorFragment.matchFragments(fragments1.get(i), fragments2.get(i)));
    }

    public static String makeRegexSafe(String s) {
        return TO_BE_ESCAPED.matcher(s).replaceAll("\\\\$0");
    }


}
