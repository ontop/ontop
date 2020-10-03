package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import org.junit.Test;
import org.locationtech.proj4j.units.Units;

import static org.junit.Assert.*;

public class GeoUtilsTest {

    @Test
    public void testCRS() {
        String crs84_iri = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
        assertEquals("CRS:84", GeoUtils.toProj4jName(crs84_iri));
        assertEquals(Units.DEGREES, GeoUtils.getUnit(crs84_iri));
    }

    @Test
    public void testEPSG4326() {
        String crs84_iri = "http://www.opengis.net/def/crs/EPSG/0/4326";
        assertEquals("EPSG:4326", GeoUtils.toProj4jName(crs84_iri));
        assertEquals(Units.DEGREES, GeoUtils.getUnit(crs84_iri));
    }

    @Test
    public void testEPSG3044() {
        String crs84_iri = "http://www.opengis.net/def/crs/EPSG/0/3044";
        assertEquals("EPSG:3044", GeoUtils.toProj4jName(crs84_iri));
        assertEquals(Units.METRES, GeoUtils.getUnit(crs84_iri));
    }
}
