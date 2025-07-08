package it.unibz.inf.ontop.docker.lightweight.tdengine;

import it.unibz.inf.ontop.docker.lightweight.TDEngineLightWeightTest;

@TDEngineLightWeightTest
public class RestConstraintTDengineTest extends AbstractConstraintTDengineTest {

    @Override
    protected String getPropertiesFile() {
        return "/tdengine/constraints-rs.properties";
    }
}
