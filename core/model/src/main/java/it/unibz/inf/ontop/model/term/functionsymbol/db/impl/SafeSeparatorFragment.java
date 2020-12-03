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

    private SafeSeparatorFragment(String fragment, char separator) {
        this.fragment = fragment;
        this.separator = separator;
    }

    public String getFragment() {
        return fragment;
    }

    public char getSeparator() {
        return separator;
    }

    public static ImmutableList<SafeSeparatorFragment> split(String s) {
        ImmutableList.Builder<SafeSeparatorFragment> builder = ImmutableList.builder();
        int start = 0, end;
        while ((end = firstIndexOfSafeSeparator(s, start)) != -1) {
            builder.add(new SafeSeparatorFragment(s.substring(start, end), s.charAt(end)));
            start = end + 1;
        }
        builder.add(new SafeSeparatorFragment(s.substring(start), (char) 0));
        System.out.println("SPLIT: " + s + " INTO " + builder.build());
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

    private boolean matchDifferent(String other) {
        return fragment.indexOf('{') >= 0
                && getPattern().matcher(other).find();
    }

    private static boolean matchFragments(SafeSeparatorFragment subTemplate1, SafeSeparatorFragment subTemplate2) {
        return subTemplate1.getFragment().equals(subTemplate2.getFragment())
                || subTemplate1.matchDifferent(subTemplate2.getFragment())
                || subTemplate2.matchDifferent(subTemplate1.getFragment());
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
