package org.openspaces.spatial.internal;

import com.gigaspaces.internal.server.space.SpaceConfigReader;
import org.openspaces.spatial.shapes.Polygon;
import com.spatial4j.core.shape.Shape;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.openspaces.spatial.ShapeFactory.*;


/**
 * @author Yohana Khoury
 * @since 11.0
 */
public class LuceneGeospatialCustomRelationHandlerTest {

    private LuceneGeospatialCustomRelationHandler _handler;
    @Before
    public void setup() throws Exception {
        _handler = new LuceneGeospatialCustomRelationHandler();
        _handler.initialize("geospatial", "dummy", new SpaceConfigReader("dummy"));
    }

    @Test
    public void testClosedPolygon() throws Exception {

        Polygon polygonWithCloseRing = polygon(point(75.05722045898438, 41.14039880964587),
                point(73.30490112304686, 41.15797827873605),
                point(73.64822387695311, 40.447992135544304),
                point(74.87319946289062, 40.50544628405211),
                point(75.05722045898438, 41.14039880964587));

        Shape spatial4jPolygon = _handler.toSpatial4j(polygonWithCloseRing);
        Assert.assertNotNull(spatial4jPolygon);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalClosedPolygon() throws Exception {

        Polygon polygonWithCloseRing = polygon(point(75.05722045898438, 41.14039880964587),
                point(73.30490112304686, 41.15797827873605),
                point(75.05722045898438, 41.14039880964587));

        Shape spatial4jPolygon = _handler.toSpatial4j(polygonWithCloseRing);
        Assert.assertNotNull(spatial4jPolygon);
    }

    @Test
    public void testLegalClosedPolygon() throws Exception {

        Polygon polygonWithCloseRing = polygon(point(75.05722045898438, 41.14039880964587),
                point(73.30490112304686, 41.15797827873605),
                point(73.64822387695311, 40.447992135544304));

        Shape spatial4jPolygon = _handler.toSpatial4j(polygonWithCloseRing);
        Assert.assertNotNull(spatial4jPolygon);
    }
}