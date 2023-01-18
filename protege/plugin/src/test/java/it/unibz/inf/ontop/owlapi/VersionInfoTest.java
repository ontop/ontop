package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.utils.VersionInfo;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author xiao
 */
public class VersionInfoTest {

    @Test
    public void testGetVersion() {
        String version = VersionInfo.getVersionInfo().getVersion();
        System.out.println(version);
        assertNotNull(version);
    }
}