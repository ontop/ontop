package it.unibz.inf.ontop.teiid.util;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Text {

    // Matches \x, ${name}, ${name:-value}
    private static final Pattern PATTERN_PLACEHOLDER = Pattern
            .compile("(?:\\\\.)|(?:\\$\\{([a-zA-Z_][a-zA-Z0-9_]*)(?:\\:\\-([^}]*))?\\})");

    public static String substitute(final String string, final Map<?, ?>... maps) {

        // Delegate resolving placeholders by looking into supplied maps, in order
        return substitute(string, key -> {
            for (final Map<?, ?> map : maps) {
                final Object value = map.get(key);
                if (value != null) {
                    return value.toString();
                }
            }
            return null;
        });
    }

    public static String substitute(final String string, final Function<String, String> resolver) {

        // Iterate over all placeholder matches in input string, building output a piece at a time
        final Matcher m = PATTERN_PLACEHOLDER.matcher(string);
        final StringBuilder sb = new StringBuilder();
        int index = 0; // position of last match
        while (m.find()) {

            // Append input text before the match
            final int start = m.start();
            sb.append(string, index, start);
            index = m.end();

            if (string.charAt(start) == '\\') {
                // Handle \n, \r, \t and \x (which emits 'x')
                final char c = string.charAt(start + 1);
                if (c == 'n') {
                    sb.append('\n');
                } else if (c == 'r') {
                    sb.append('\r');
                } else if (c == 't') {
                    sb.append('\t');
                } else {
                    sb.append(c);
                }

            } else {
                // Handle ${name} or ${name:-value}
                final String key = m.group(1);
                final String defaultValue = m.group(2);
                String value = resolver.apply(key);
                value = value != null ? value : defaultValue;
                if (value == null) {
                    throw new IllegalArgumentException(
                            "Undefined value for placeholder ${" + key + "}");
                }
                sb.append(value);
            }
        }

        // Append remaining unmatched text and return result
        sb.append(string, index, string.length());
        return sb.toString();
    }

    private Text() {
        throw new Error(); // prevent instantiation via reflection
    }

}
