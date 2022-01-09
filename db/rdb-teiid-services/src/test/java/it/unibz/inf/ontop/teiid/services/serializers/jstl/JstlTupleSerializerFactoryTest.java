package it.unibz.inf.ontop.teiid.services.serializers.jstl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;

import org.junit.Assert;
import org.junit.Test;

import it.unibz.inf.ontop.teiid.services.model.Attribute;
import it.unibz.inf.ontop.teiid.services.model.Datatype;
import it.unibz.inf.ontop.teiid.services.model.Signature;
import it.unibz.inf.ontop.teiid.services.model.Tuple;
import it.unibz.inf.ontop.teiid.services.serializers.TupleReader;
import it.unibz.inf.ontop.teiid.services.serializers.TupleSerializerFactory;
import it.unibz.inf.ontop.teiid.services.serializers.TupleWriter;

public class JstlTupleSerializerFactoryTest {

    @Test
    public void testPlainText() {

        final Signature signature = Signature.forAttributes( //
                Attribute.create("name", Datatype.STRING), //
                Attribute.create("date", Datatype.DATE),
                Attribute.create("limit", Datatype.SHORT));

        final String jstlIn = "[capture(., \"http://localhost:8080/api/(?<name>[^/]+)/(?<date>[^?]+)[?]limit=(?<limit>[1-9][0-9]*)\")]";
        final String jstlOut = "\"http://localhost:8080/api/\" + .[0].name + \"/\" + .[0].date + \"?limit=\" + .[0].limit";

        final TupleSerializerFactory io = new JstlTupleSerializerFactory();
        final TupleReader reader = io.createReader(signature, MediaType.PLAIN_TEXT_UTF_8,
                ImmutableMap.of(JstlTupleSerializerFactory.KEY_JSTL_IN, jstlIn));
        final TupleWriter writer = io.createWriter(signature, MediaType.PLAIN_TEXT_UTF_8,
                ImmutableMap.of(JstlTupleSerializerFactory.KEY_JSTL_OUT, jstlOut));

        final Tuple inTuple = Tuple.create(signature, "xyz", "2021-07-15", 42L);
        final String string = writer.writeString(ImmutableList.of(inTuple));
        Assert.assertEquals("http://localhost:8080/api/xyz/2021-07-15?limit=42", string);

        final Tuple outTuple = reader.readString(string).get(0);
        Assert.assertEquals(inTuple, outTuple);
    }
    
    // TODO: test json

}
