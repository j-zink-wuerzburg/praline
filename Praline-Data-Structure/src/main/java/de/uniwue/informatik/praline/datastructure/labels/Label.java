package de.uniwue.informatik.praline.datastructure.labels;

import com.fasterxml.jackson.annotation.*;
import de.uniwue.informatik.praline.datastructure.placements.HorizontalPlacement;
import de.uniwue.informatik.praline.datastructure.placements.Placement;
import de.uniwue.informatik.praline.datastructure.placements.VerticalPlacement;
import de.uniwue.informatik.praline.datastructure.shapes.Shape;
import de.uniwue.informatik.praline.datastructure.shapes.ShapedObject;

/**
 * Can be attached to a {@link LabeledObject} (e. g. a
 * {@link de.uniwue.informatik.praline.datastructure.graphs.Vertex}, a
 * {@link de.uniwue.informatik.praline.datastructure.graphs.Port}, an
 * {@link de.uniwue.informatik.praline.datastructure.graphs.Edge} or a
 * {@link de.uniwue.informatik.praline.datastructure.graphs.VertexGroup}) to provide additional information about
 * this object.
 *
 * A {@link LabeledObject} can have an arbitrary number of {@link Label}s, which are managed by the
 * {@link LabelManager} of the {@link LabeledObject}.
 *
 * A {@link Label} is supposed to be visualized by the drawing algorithm unless {@link Label#isShowLabel()} is set to
 * false. Therefore, a {@link Label} is a {@link ShapedObject}.
 * The position of the {@link Label} relative to its {@link LabeledObject} is specified by
 * {@link Label#getPlacement()}, {@link Label#getHorizontalPlacement()} and {@link Label#getVerticalPlacement()}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextLabel.class, name = "text"),
        @JsonSubTypes.Type(value = IconLabel.class, name = "icon"),
        @JsonSubTypes.Type(value = LeaderedLabel.class, name = "leadered"),
})
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public abstract class Label implements ShapedObject {

    /*==========
     * Default values
     *==========*/

    public static final boolean DEFAULT_SHOW_LABEL = true;


    /*==========
     * Instance variables
     *==========*/

    private LabelManager associatedLabelManager;
    private boolean showLabel;
    private Placement placement;
    private HorizontalPlacement horizontalPlacement;
    private VerticalPlacement verticalPlacement;
    private Shape shape;


    /*==========
     * Constructors
     *==========*/

    protected Label() {
        this(Placement.FREE, HorizontalPlacement.FREE, VerticalPlacement.FREE, Label.DEFAULT_SHOW_LABEL, null);
    }

    protected Label(Shape shape) {
        this(Placement.FREE, HorizontalPlacement.FREE, VerticalPlacement.FREE, Label.DEFAULT_SHOW_LABEL, shape);
    }

    protected Label(Placement placement, HorizontalPlacement horizontalPlacement, VerticalPlacement verticalPlacement) {
        this(placement, horizontalPlacement, verticalPlacement, Label.DEFAULT_SHOW_LABEL, null);
    }

    protected Label(Placement placement, HorizontalPlacement horizontalPlacement, VerticalPlacement verticalPlacement,
                 boolean showLabel, Shape shape) {
        this.shape = shape;
        this.showLabel = showLabel;
        this.placement = placement == null ? Placement.FREE : placement;
        this.horizontalPlacement = horizontalPlacement == null ? HorizontalPlacement.FREE : horizontalPlacement;
        this.verticalPlacement = verticalPlacement == null ? VerticalPlacement.FREE : verticalPlacement;
    }


    /*==========
     * Getters & Setters
     *==========*/

    /**
     * This value should be changed from an instance of {@link LabelManager}
     * whenever a {@link Label} is added to a {@link LabelManager} of a {@link LabeledObject}
     * via {@link LabelManager#addLabel(Label)}
     * or {@link LabelManager#removeLabel(Label)}.
     *
     * It can be used to find its associated {@link LabeledObject} via
     * {@link LabelManager#getManagedLabeledObject}.
     */
    @JsonIgnore
    public LabelManager getAssociatedLabelManager() {
        return associatedLabelManager;
    }

    /**
     * This value should be changed from an instance of {@link LabelManager}
     * whenever a {@link Label} is added to a {@link LabelManager} of a {@link LabeledObject}
     * via {@link LabelManager#addLabel(Label)}
     * or {@link LabelManager#removeLabel(Label)}.
     * This is the reason this method is "protected".
     *
     * @param associatedLabelManager
     */
    protected void setAssociatedLabelManager(LabelManager associatedLabelManager) {
        this.associatedLabelManager = associatedLabelManager;
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }

    public Placement getPlacement() {
        return placement;
    }

    public void setPlacement(Placement placement) {
        this.placement = placement;
    }

    public HorizontalPlacement getHorizontalPlacement() {
        return horizontalPlacement;
    }

    public void setHorizontalPlacement(HorizontalPlacement horizontalPlacement) {
        this.horizontalPlacement = horizontalPlacement;
    }

    public VerticalPlacement getVerticalPlacement() {
        return verticalPlacement;
    }

    public void setVerticalPlacement(VerticalPlacement verticalPlacement) {
        this.verticalPlacement = verticalPlacement;
    }

    @Override
    public Shape getShape() {
        return shape;
    }

    @Override
    public void setShape(Shape shape) {
        this.shape = shape;
    }
}
