package it.unibz.inf.ontop.utils;

import org.junit.Test;

import java.util.Map;

import static it.unibz.inf.ontop.utils.R2RMLIRISafeEncoder.decode;
import static it.unibz.inf.ontop.utils.R2RMLIRISafeEncoder.encode;
import static org.junit.Assert.assertEquals;

public class R2RMLIRISafeEncoderTest {

    @Test
    public void testTable() {
        for (Map.Entry<String, Character> entry : R2RMLIRISafeEncoder.TABLE.entrySet()) {
            String key = entry.getKey();
            assertEquals(3, key.length());
            assertEquals(Integer.parseInt(key.substring(1, 3), 16),
                    entry.getValue().charValue());
        }
    }

    @Test
    public void testEncode() {
        assertEquals("42", encode("42"));
        assertEquals("Hello%20World%21", encode("Hello World!"));
        assertEquals("2011-08-23T22%3A17%3A00Z", encode("2011-08-23T22:17:00Z"));
        assertEquals("~A_17.1-2", encode("~A_17.1-2"));
        assertEquals("葉篤正", encode("葉篤正"));
    }

    @Test
    public void testDecode() {
        assertEquals("42", decode("42"));
        assertEquals("Hello World!", decode("Hello%20World%21"));
        assertEquals("2011-08-23T22:17:00Z", decode("2011-08-23T22%3A17%3A00Z"));
        assertEquals("~A_17.1-2", decode("~A_17.1-2"));
        assertEquals("葉篤正", decode("葉篤正"));
    }


}