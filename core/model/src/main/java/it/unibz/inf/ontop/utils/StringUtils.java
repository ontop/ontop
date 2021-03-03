package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableMap;

import java.util.function.Consumer;

public class StringUtils {

    public static String encode(String s, ImmutableMap<Character, String> map) {
        int length = s.length();
        StringBuilder sb = new StringBuilder(length * 5 / 4);
        int start = 0;
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            String rep = map.get(c);
            if (rep != null) {
                sb.append(s, start, i);
                start = i + 1;
                sb.append(rep);
            }
        }
        if (start == 0)
            return s;
        sb.append(s, start, length);
        return sb.toString();
    }

    public static String decode(String encoded, char escape, int escapeLength, ImmutableMap<String, Character> map, Consumer<String> unrecognisedCodeConsumer) {
        int escapeIndex = encoded.indexOf(escape);
        if (escapeIndex == -1)
            return encoded;

        StringBuilder sb = new StringBuilder(encoded.length());
        int start = 0;
        do {
            sb.append(encoded, start, escapeIndex);

            String code = encoded.substring(escapeIndex, escapeIndex + escapeLength);
            Character rep = map.get(code);
            if (rep == null) {
                unrecognisedCodeConsumer.accept(code);
                sb.append(code);
            }
            else
                sb.append(rep);

            start = escapeIndex + escapeLength;

        } while ((escapeIndex = encoded.indexOf(escape, start)) != -1);

        sb.append(encoded, start, encoded.length());
        return sb.toString();
    }

}
