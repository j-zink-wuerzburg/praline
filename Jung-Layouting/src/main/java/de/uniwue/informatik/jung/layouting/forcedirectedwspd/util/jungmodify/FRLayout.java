/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 */
package de.uniwue.informatik.jung.layouting.forcedirectedwspd.util.jungmodify;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ConcurrentModificationException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.uniwue.informatik.jung.layouting.forcedirectedwspd.util.Constants;
import de.uniwue.informatik.jung.layouting.forcedirectedwspd.util.Randomness;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Implements the Fruchterman-Reingold force-directed algorithm for node layout.
 * 
 * <p>Behavior is determined by the following settable parameters:
 * <ul>
 * <li/>attraction multiplier: how much edges try to keep their vertices together
 * <li/>repulsion multiplier: how much vertices try to push each other apart
 * <li/>maximum iterations: how many iterations this algorithm will use before stopping
 * </ul>
 * Each of the first two defaults to 0.75; the maximum number of iterations defaults to 700.
 *
 * <p>
 * <br>
 * EDIT Johannes Zink on 2015/05/14:
 * The following changes were made to get access to private/protected class variables/...
 * <ul>
 * <li/> "private int currentIteration" to "protected int currentIteration" (instance variable)
 * <li/> "private double EPSILON" to "protected double EPSILON" (instance variable)
 * <li/> "private double repulsion_constant" to "protected double repulsion_constant" (instance variable)
 * <li/> "private void cool()" to "protected void cool()" (instance method)
 * <li/> in class "FRLayout.FRVertexData": "protected void offset(double x, double y)" to "public void offset(double x,
 * double y)" (instance method within FRVertexData)
 * <li/> "private Map<V, FRVertexData> frVertexData" to "protected Map<V, FRVertexData> frVertexData" (instance variable)
 * </ul>
 * <p>
 * EDIT Johannes Zink on 2015/06/24:
 * <ul>
 * <li/> "private double max_dimension" to "protected double max_dimension" (instance variable)
 * <li/> class "FRVertexData" from protected to public
 * </ul>
 * <br>
 * <p>
 * EDIT Johannes Zink on 2015/20/07:
 * <ul>
 * <li/> "private double temperature" to "protected double temperature" (instance variable)
 * <li/> "private double forceConstant" to "protected double forceConstant" (instance variable)
 * <li/> "private double attraction_constant" to "protected double attraction_constant" (instance variable)
 * <li/> "private double attraction_multiplier" to "protected double attraction_multiplier" (instance variable)
 * <li/> "private double repulsion_multiplier" to "protected double repulsion_multiplier" (instance variable)
 * <li/> "protected double FRVertexData.norm()" to "public double FRVertexData.norm()" (method of FRVertexData)
 * </ul>
 * <br>
 * <p>
 * EDIT Johannes Zink on 2015/07/26:
 * <ul>
 * <li/> "private int mMaxIterations" to "protected int mMaxIterations" (instance variable)
 * </ul>
 * <p>
 *
 * @see "Fruchterman and Reingold, 'Graph Drawing by Force-directed Placement'"
 * @see "http://i11www.ilkd.uni-karlsruhe.de/teaching/SS_04/visualisierung/papers/fruchterman91graph.pdf"
 * @author Scott White, Yan-Biao Boey, Danyel Fisher
 */
public class FRLayout<V, E> extends AbstractLayout<V, E> implements IterativeContext {

    protected double forceConstant;

    protected double temperature;

    protected int currentIteration;

    protected int mMaxIterations = 700;

    protected LoadingCache<V, FRVertexData> frVertexData =
            CacheBuilder.newBuilder().build(new CacheLoader<V, FRVertexData>() {
                    public FRVertexData load(V vertex) {
                        return new FRVertexData();
                    }
    });

    protected double attraction_multiplier = 0.50;

    protected double attraction_constant;

    protected double repulsion_multiplier = 0.99;

    protected double repulsion_constant;

    protected double max_dimension;

    /**
     * Creates an instance for the specified graph.
     */
    public FRLayout(Graph<V, E> g) {
        super(g);
    }

    /**
     * Creates an instance of size {@code d} for the specified graph.
     */
    public FRLayout(Graph<V, E> g, Dimension d) {
        super(g, new RandomLocationTransformer<V>(d, Constants.random.nextLong()), d);
        initialize();
        max_dimension = Math.max(d.height, d.width);
    }

    public FRLayout(Graph<V, E> g, Dimension d, long seed) {
        super(g, new RandomLocationTransformer<V>(d, seed), d);
        initialize();
        max_dimension = Math.max(d.height, d.width);
    }

	@Override
	public void setSize(Dimension size) {
        setSize(size, Constants.random.nextLong());
	}

    public void setSize(Dimension size, long seed) {
        if(initialized == false) {
            setInitializer(new RandomLocationTransformer<V>(size, seed));
        }
        super.setSize(size);
        max_dimension = Math.max(size.height, size.width);
    }

	/**
	 * Sets the attraction multiplier.
	 */
	public void setAttractionMultiplier(double attraction) {
        this.attraction_multiplier = attraction;
    }

	/**
	 * Sets the repulsion multiplier.
	 */
    public void setRepulsionMultiplier(double repulsion) {
        this.repulsion_multiplier = repulsion;
    }

	public void reset() {
		doInit();
	}

    public void initialize() {
    	doInit();
    }

    private void doInit() {
    	Graph<V,E> graph = getGraph();
    	Dimension d = getSize();
    	if(graph != null && d != null) {
    		currentIteration = 0;
    		temperature = d.getWidth() / 10;

    		forceConstant =
    			Math
    			.sqrt(d.getHeight()
    					* d.getWidth()
    					/ graph.getVertexCount());

    		attraction_constant = attraction_multiplier * forceConstant;
    		repulsion_constant = repulsion_multiplier * forceConstant;
    	}
    }

    protected double EPSILON = 0.000001D;

    /**
     * Moves the iteration forward one notch, calculation attraction and
     * repulsion between vertices and edges and cooling the temperature.
     */
    public synchronized void step() {
        currentIteration++;

        /**
         * Calculate repulsion
         */
        while(true) {

            try {
                for(V v1 : getGraph().getVertices()) {
                    calcRepulsion(v1);
                }
                break;
            } catch(ConcurrentModificationException cme) {}
        }

        /**
         * Calculate attraction
         */
        while(true) {
            try {
                for(E e : getGraph().getEdges()) {

                    calcAttraction(e);
                }
                break;
            } catch(ConcurrentModificationException cme) {}
        }


        while(true) {
            try {
                for(V v : getGraph().getVertices()) {
                    if (isLocked(v)) continue;
                    calcPositions(v);
                }
                break;
            } catch(ConcurrentModificationException cme) {}
        }
        cool();
    }

    protected synchronized void calcPositions(V v) {
        FRVertexData fvd = getFRData(v);
        if(fvd == null) return;
        Point2D xyd = apply(v);
        double deltaLength = Math.max(EPSILON, fvd.norm());

        double newXDisp = fvd.getX() / deltaLength
                * Math.min(deltaLength, temperature);

        if (Double.isNaN(newXDisp)) {
        	throw new IllegalArgumentException(
                "Unexpected mathematical result in FRLayout:calcPositions [xdisp]"); }

        double newYDisp = fvd.getY() / deltaLength
                * Math.min(deltaLength, temperature);
        xyd.setLocation(xyd.getX()+newXDisp, xyd.getY()+newYDisp);

        double borderWidth = getSize().getWidth() / 50.0;
        double newXPos = xyd.getX();
        if (newXPos < borderWidth) {
            newXPos = borderWidth + Randomness.random.nextDouble() * borderWidth * 2.0;
        } else if (newXPos > (getSize().getWidth() - borderWidth)) {
            newXPos = getSize().getWidth() - borderWidth - Randomness.random.nextDouble()
                    * borderWidth * 2.0;
        }

        double newYPos = xyd.getY();
        if (newYPos < borderWidth) {
            newYPos = borderWidth + Randomness.random.nextDouble() * borderWidth * 2.0;
        } else if (newYPos > (getSize().getHeight() - borderWidth)) {
            newYPos = getSize().getHeight() - borderWidth
                    - Randomness.random.nextDouble() * borderWidth * 2.0;
        }

        xyd.setLocation(newXPos, newYPos);
    }

    protected void calcAttraction(E e) {
    	Pair<V> endpoints = getGraph().getEndpoints(e);
        V v1 = endpoints.getFirst();
        V v2 = endpoints.getSecond();
        boolean v1_locked = isLocked(v1);
        boolean v2_locked = isLocked(v2);

        if(v1_locked && v2_locked) {
        	// both locked, do nothing
        	return;
        }
        Point2D p1 = apply(v1);
        Point2D p2 = apply(v2);
        if(p1 == null || p2 == null) return;
        double xDelta = p1.getX() - p2.getX();
        double yDelta = p1.getY() - p2.getY();

        double deltaLength = Math.max(EPSILON, Math.sqrt((xDelta * xDelta)
                + (yDelta * yDelta)));

        double force = (deltaLength * deltaLength) / attraction_constant;

        if (Double.isNaN(force)) { throw new IllegalArgumentException(
                "Unexpected mathematical result in FRLayout:calcPositions [force]"); }

        double dx = (xDelta / deltaLength) * force;
        double dy = (yDelta / deltaLength) * force;
        if(v1_locked == false) {
        	FRVertexData fvd1 = getFRData(v1);
        	fvd1.offset(-dx, -dy);
        }
        if(v2_locked == false) {
        	FRVertexData fvd2 = getFRData(v2);
        	fvd2.offset(dx, dy);
        }
    }

    protected void calcRepulsion(V v1) {
        FRVertexData fvd1 = getFRData(v1);
        if(fvd1 == null)
            return;
        fvd1.setLocation(0, 0);

        try {
            for(V v2 : getGraph().getVertices()) {

//                if (isLocked(v2)) continue;
                if (v1 != v2) {
                    Point2D p1 = apply(v1);
                    Point2D p2 = apply(v2);
                    if(p1 == null || p2 == null) continue;
                    double xDelta = p1.getX() - p2.getX();
                    double yDelta = p1.getY() - p2.getY();

                    double deltaLength = Math.max(EPSILON, Math
                            .sqrt((xDelta * xDelta) + (yDelta * yDelta)));

                    double force = (repulsion_constant * repulsion_constant) / deltaLength;

                    if (Double.isNaN(force)) { throw new RuntimeException(
                    "Unexpected mathematical result in FRLayout:calcPositions [repulsion]"); }

                    fvd1.offset((xDelta / deltaLength) * force,
                            (yDelta / deltaLength) * force);
                }
            }
        } catch(ConcurrentModificationException cme) {
            calcRepulsion(v1);
        }
    }

    protected void cool() {
        temperature *= (1.0 - currentIteration / (double) mMaxIterations);
    }

    /**
     * Sets the maximum number of iterations.
     */
    public void setMaxIterations(int maxIterations) {
        mMaxIterations = maxIterations;
    }

    protected FRVertexData getFRData(V v) {
        return frVertexData.getUnchecked(v);
    }

    /**
     * This one is an incremental visualization.
     */
    public boolean isIncremental() {
        return true;
    }

    /**
     * Returns true once the current iteration has passed the maximum count,
     * <tt>MAX_ITERATIONS</tt>.
     */
    public boolean done() {
        if (currentIteration > mMaxIterations || temperature < 1.0/max_dimension)
        {
            return true;
        }
        return false;
    }

    public static class FRVertexData extends Point2D.Double
    {
        public void offset(double x, double y)
        {
            this.x += x;
            this.y += y;
        }

        public double norm()
        {
            return Math.sqrt(x*x + y*y);
        }
     }
}