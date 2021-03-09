package de.uniwue.informatik.praline.datastructure.shapes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.uniwue.informatik.praline.datastructure.oldUnstyledObjects.OldUnstyledRectangle;
import de.uniwue.informatik.praline.datastructure.styles.ShapeStyle;

import java.awt.geom.Rectangle2D;

/**
 * Some geometric 2D object covering some area in a drawing for a {@link ShapedObject}.
 * Typically position and size of the {@link Shape} are determined by the drawing algorithm.
 * However they may be specified in the forehand by the input of the user -- in particular the size of the
 * {@link Shape}.
 *
 * Implementations are {@link Rectangle}, {@link Circle} and {@link ArrowHeadTriangle}.
 */
@JsonIgnoreProperties({ "bounds", "bounds2D", "boundingBox", "x", "y", "empty", "minX", "minY", "maxX", "maxY",
        "centerX", "centerY", "frame", "pathIterator", "cornerPoints" })
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OldUnstyledRectangle.class, name = "rectangle"),
        @JsonSubTypes.Type(value = Rectangle.class, name = "rect"),
        @JsonSubTypes.Type(value = Circle.class, name = "circle"),
        @JsonSubTypes.Type(value = ArrowHeadTriangle.class, name = "arrowHeadTriangle"),
})
public interface Shape extends Cloneable {

    /*==========
     * Default values
     *==========*/

    double UNDEFINED_LENGTH = java.lang.Double.NaN;
    double UNDEFINED_POSITION = java.lang.Double.NaN;


    /*==========
     * Methods to be implemented
     *==========*/

    double getXPosition();
    double getYPosition();
    ShapeStyle getShapeStyle();
    void setShapeStyle(ShapeStyle shapeStyle);
    Rectangle2D getBoundingBox();
    Shape clone();
    void translate(double xOffset, double yOffset);
}
