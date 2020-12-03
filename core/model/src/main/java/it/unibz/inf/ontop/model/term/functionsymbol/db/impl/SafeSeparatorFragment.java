package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SafeSeparatorFragment {
    private final String fragment;
    private final char separator;
    private final boolean containsPlaceholder;

    /**
     * TODO: enrich this list (incomplete)
     */
    protected static final ImmutableSet<Character> SOME_SAFE_SEPARATORS = ImmutableSet.of(
            '/', '!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=', '#');

    public static final String NOT_A_SAFE_SEPARATOR_REGEX = "[^"
            + SOME_SAFE_SEPARATORS.stream()
            .map(Object::toString)
            .map(SafeSeparatorFragment::makeRegexSafe)
            .collect(Collectors.joining())
            + "]*";

    private SafeSeparatorFragment(String fragment, char separator, boolean containsPlaceholder) {
        this.fragment = fragment;
        this.separator = separator;
        this.containsPlaceholder = containsPlaceholder;
    }

    private SafeSeparatorFragment(String fragment, char separator) {
        this(fragment, separator, fragment.indexOf('{') > 0);
    }

    public String getFragment() {
        return fragment;
    }

    public char getSeparator() {
        return separator;
    }

    @Override
    public String toString() {
        return fragment + separator + (containsPlaceholder ? "P" : "");
    }

    public static ImmutableList<SafeSeparatorFragment> split(String s) {
        ImmutableList.Builder<SafeSeparatorFragment> builder = ImmutableList.builder();
        int start = 0, current_start = 0, end;
        while ((end = firstIndexOfSafeSeparator(s, current_start)) != -1) {
            String fragment = s.substring(current_start, end);
            if (fragment.indexOf('{') >= 0) {
                if (current_start > start)
                    builder.add(new SafeSeparatorFragment(s.substring(start, current_start - 1), s.charAt(current_start - 1), false));
                builder.add(new SafeSeparatorFragment(s.substring(current_start, end), s.charAt(end), true));
                start = end + 1;
            }
            current_start = end + 1;
        }
        if (current_start > start)
            builder.add(new SafeSeparatorFragment(s.substring(start, current_start - 1), s.charAt(current_start - 1), false));
        if (current_start < s.length())
            builder.add(new SafeSeparatorFragment(s.substring(current_start), (char) 0));

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

    private static boolean matchFragments(SafeSeparatorFragment subTemplate1, SafeSeparatorFragment subTemplate2) {
        return subTemplate1.getFragment().equals(subTemplate2.getFragment())
                || subTemplate1.containsPlaceholder && subTemplate1.getPattern().matcher(subTemplate2.getFragment()).find()
                || subTemplate2.containsPlaceholder && subTemplate2.getPattern().matcher(subTemplate1.getFragment()).find();
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
        return s.replaceAll("[<(\\[{\\\\^=$!|\\]})?*+.>]", "\\\\$0");
    }


}
