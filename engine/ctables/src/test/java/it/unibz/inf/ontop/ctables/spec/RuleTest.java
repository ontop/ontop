package it.unibz.inf.ontop.ctables.spec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableSet;

import org.junit.Assert;
import org.junit.Test;

public class RuleTest {

    @Test
    public void test() throws Throwable {

        final String yaml = "" //
                + "id:     index_accommodation_texts\n" //
                + "target: vt_semsearch_index\n" //
                + "source: |-\n" //
                + "  SELECT \"Id\" AS id, \"AccoDetail-en-Longdesc\" AS text\n" //
                + "  FROM   v_accommodationsopen\n" //
                + "  WHERE  \"AccoDetail-en-Longdesc\" IS NOT NULL\n" //
                + "uses:   [ v_some_other_table ]";

        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final Rule rule = mapper.readValue(yaml, Rule.class);

        Assert.assertEquals(ImmutableSet.of("v_accommodationsopen", "v_some_other_table"),
                rule.getDependencies());
    }

}
