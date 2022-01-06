package it.unibz.inf.ontop.teiid.services.serializers.template;

import java.util.Date;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;

import org.junit.Assert;
import org.junit.Test;
import org.teiid.core.types.DataTypeManager.DefaultDataTypes;

import it.unibz.inf.ontop.teiid.services.model.Attribute;
import it.unibz.inf.ontop.teiid.services.model.Datatype;
import it.unibz.inf.ontop.teiid.services.model.Signature;
import it.unibz.inf.ontop.teiid.services.model.Tuple;
import it.unibz.inf.ontop.teiid.services.serializers.TupleReader;
import it.unibz.inf.ontop.teiid.services.serializers.TupleSerializerFactory;
import it.unibz.inf.ontop.teiid.services.serializers.TupleWriter;

public class TemplateTupleSerializerFactoryTest {

    @Test
    public void testPlainText() {

        final Signature signature = Signature.forAttributes( //
                Attribute.create("name", Datatype.STRING), //
                Attribute.create("date", Datatype.DATE),
                Attribute.create("limit", Datatype.SHORT));

        final String t = "http://localhost:8080/api/{name}/{date,date,yyyy-MM-dd}?limit={limit,number,integer}";

        final TupleSerializerFactory io = new TemplateTupleSerializerFactory();
        final TupleReader reader = io.createReader(signature, MediaType.PLAIN_TEXT_UTF_8,
                ImmutableMap.of(TemplateTupleSerializerFactory.KEY, t));
        final TupleWriter writer = io.createWriter(signature, MediaType.PLAIN_TEXT_UTF_8,
                ImmutableMap.of(TemplateTupleSerializerFactory.KEY, t));

        final Tuple inTuple = Tuple.create(signature, "xyz", new Date(1626300000000L), 42L);
        final String string = writer.writeString(ImmutableList.of(inTuple));
        Assert.assertEquals("http://localhost:8080/api/xyz/2021-07-15?limit=42", string);

        final Tuple outTuple = reader.readString(string).get(0);
        Assert.assertEquals(inTuple, outTuple);
    }

    @Test
    public void test() {

        final String template = "{\"flag\": \"{flag,json,boolean}\", \"n\": [ \"{n,json,long}\" ], "
                + "\"output\": [ {\"text\": \"{text}\", \"emb\": \"{emb,json,double}\"} ] }";

        final Signature signature = Signature.forAttributes( //
                Attribute.create("flag", DefaultDataTypes.BOOLEAN),
                Attribute.create("n", DefaultDataTypes.BIG_DECIMAL),
                Attribute.create("text", DefaultDataTypes.STRING),
                Attribute.create("emb", DefaultDataTypes.BIG_DECIMAL));

        final List<Tuple> tuples = ImmutableList.of( //
                Tuple.create(signature, true, 1L, "text1", 15.1), //
                Tuple.create(signature, true, 2L, "text2", 16.2));

        final String json = "{\"flag\":true,\"n\":[1,2],\"output\":[" //
                + "{\"text\":\"text1\",\"emb\":15.1}," //
                + "{\"text\":\"text2\",\"emb\":16.2}" //
                + "]}";

        final TupleSerializerFactory io = new TemplateTupleSerializerFactory();
        final TupleReader reader = io.createReader(signature, MediaType.JSON_UTF_8,
                ImmutableMap.of(TemplateTupleSerializerFactory.KEY, template));
        final TupleWriter writer = io.createWriter(signature, MediaType.JSON_UTF_8,
                ImmutableMap.of(TemplateTupleSerializerFactory.KEY, template));

        final String writtenJson = writer.writeString(tuples);
        // System.out.println(writtenJson);
        Assert.assertEquals(json, writtenJson);

        final List<Tuple> readTuples = reader.readString(writtenJson);
        // System.out.println(readTuples);
        Assert.assertEquals(tuples, readTuples);
    }

}
