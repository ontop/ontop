package it.unibz.inf.ontop.docker.lightweight.tdengine;

import it.unibz.inf.ontop.docker.lightweight.TDEngineLightWeightTest;

@TDEngineLightWeightTest
public class WebSocketConstraintTDengineTest extends AbstractConstraintTDengineTest {

    @Override
    protected String getPropertiesFile() {
        return "/tdengine/constraints-ws.properties";
    }
}
