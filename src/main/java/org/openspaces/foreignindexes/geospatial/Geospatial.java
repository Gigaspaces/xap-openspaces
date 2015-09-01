package org.openspaces.foreignindexes.geospatial;

import com.j_spaces.core.cache.foreignIndexes.CustomRelation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Created by Barak Bar Orion
 *
 * @since 11.0
 */

@CustomRelation(namespace = "geospatial", handler = LuceneGeospatialCustomRelationHandler.class)
@Target({METHOD, FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Geospatial {
    boolean indexed() default true;
}
