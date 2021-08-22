package it.unibz.inf.ontop.generation.serializer.impl;

import it.unibz.inf.ontop.utils.ConstantSerializer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * NB: ConstantSerializer is a util for third-party applications, not for Ontop itself
 */
public class ConstantSerializerTest {

    @Test
    public void testPostgres() {
        ConstantSerializer constantSerializer = new ConstantSerializer("org.postgresql.Driver");

        assertEquals("'2'", constantSerializer.serializeConstantIntoSQL("2", "text"));
        assertEquals("2", constantSerializer.serializeConstantIntoSQL("2", "int4"));
    }
}
