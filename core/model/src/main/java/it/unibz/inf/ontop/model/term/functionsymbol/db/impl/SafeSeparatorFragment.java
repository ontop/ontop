package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.template.impl.TemplateParser;

import javax.annotation.Nullable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SafeSeparatorFragment {
    private final String fragment;
    private final ImmutableList<Template.Component> components;
    private final char separator;
    private final int firstPlaceholderIndex;

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

    private SafeSeparatorFragment(String fragment, char separator, int firstPlaceholderIndex) {
        this.fragment = fragment;
        this.separator = separator;
        this.firstPlaceholderIndex = firstPlaceholderIndex;
        this.components = TemplateParser.getComponents(fragment, true);
    }

    private SafeSeparatorFragment(String fragment, char separator) {
        this(fragment, separator, fragment.indexOf('{'));
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
        return fragment + (separator != 0 ? separator : "") + (firstPlaceholderIndex != -1 ? "P" : "");
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

    @Nullable
    private Pattern pattern;

    private Pattern getPattern() {
        if (pattern == null) {
            StringBuilder patternString = new StringBuilder();
            int start = 0, end;
            while ((end = fragment.indexOf('{', start)) != -1) {
                patternString.append(makeRegexSafe(fragment.substring(start, end)))
                        .append(NOT_A_SAFE_SEPARATOR_REGEX);
                start = end + 2;
            }
            if (start < fragment.length())
                patternString.append(makeRegexSafe(fragment.substring(start)));

            pattern = Pattern.compile("^" + patternString + "$");
        }
        return pattern;
    }

    private int getPrefixLength() {
        return firstPlaceholderIndex != -1 ? firstPlaceholderIndex : fragment.length();
    }

    private static boolean matchFragments(SafeSeparatorFragment subTemplate1, SafeSeparatorFragment subTemplate2) {
        boolean equal = subTemplate1.fragment.equals(subTemplate2.fragment);
        if (equal)
            return true;

        if (subTemplate1.firstPlaceholderIndex == -1 && subTemplate2.firstPlaceholderIndex == -1)
            return equal;

        int prefix = Math.min(subTemplate1.getPrefixLength(), subTemplate2.getPrefixLength());
        if (!subTemplate1.fragment.substring(0, prefix).equals(subTemplate2.fragment.substring(0, prefix)))
            return false;

        return subTemplate1.firstPlaceholderIndex != -1 && subTemplate1.getPattern().matcher(subTemplate2.fragment).find()
            || subTemplate2.firstPlaceholderIndex != -1 && subTemplate2.getPattern().matcher(subTemplate1.fragment).find();
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
