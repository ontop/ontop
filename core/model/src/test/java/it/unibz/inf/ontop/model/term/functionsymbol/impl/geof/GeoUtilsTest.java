package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import org.junit.Test;
import org.locationtech.proj4j.units.Units;

import static org.junit.Assert.*;

public class GeoUtilsTest {

    @Test
    public void testCRS() {
        String iri = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
        assertEquals("CRS:84", GeoUtils.toProj4jName(iri));
        assertEquals(DistanceUnit.DEGREE, GeoUtils.getUnitFromSRID(iri));
    }

    @Test
    public void testEPSG4326() {
        String iri = "http://www.opengis.net/def/crs/EPSG/0/4326";
        assertEquals("EPSG:4326", GeoUtils.toProj4jName(iri));
        assertEquals(DistanceUnit.DEGREE, GeoUtils.getUnitFromSRID(iri));
    }

    @Test
    public void testEPSG3044() {
        String iri = "http://www.opengis.net/def/crs/EPSG/0/3044";
        assertEquals("EPSG:3044", GeoUtils.toProj4jName(iri));
        assertEquals(DistanceUnit.METRE, GeoUtils.getUnitFromSRID(iri));
    }
}
