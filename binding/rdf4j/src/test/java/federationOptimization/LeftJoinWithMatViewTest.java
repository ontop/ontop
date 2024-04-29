package federationOptimization;

import org.junit.Test;

public class LeftJoinWithMatViewTest {

    @Test
    public void test() throws Exception {
        Tester tester = Tester.create(Tester.Federator.TEIID, Tester.Setting.HET, Tester.Optimization.OPTMATV);
        tester.reformulate(Tester.Query.Q7.getSparql());
    }

}
