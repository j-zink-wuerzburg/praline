package de.uniwue.informatik.praline.io.output.jsforcegraph;

import de.uniwue.informatik.praline.datastructure.graphs.Edge;
import de.uniwue.informatik.praline.datastructure.graphs.Graph;
import de.uniwue.informatik.praline.datastructure.graphs.Port;
import de.uniwue.informatik.praline.datastructure.graphs.Vertex;
import de.uniwue.informatik.praline.io.output.jsforcegraph.model.JsForceGraph;
import de.uniwue.informatik.praline.io.output.jsforcegraph.model.Link;
import de.uniwue.informatik.praline.io.output.jsforcegraph.model.Node;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsForceGraphConverter
{
    private final Map<Vertex, Node> vertexMap = new LinkedHashMap<>();
    private int jsIdCounter = 1;

    public JsForceGraphConverter()
    {
    }

    public JsForceGraph convertGraph(Graph graph)
    {
        JsForceGraph jsForceGraph = new JsForceGraph();

        for (Vertex vertex : graph.getVertices())
        {
            Node node = new Node();
            if (vertex.getReference() != null && !vertex.getReference().isBlank())
            {
                node.setId(vertex.getReference());
            }
            else
            {
                node.setId(Integer.toString(this.jsIdCounter));
                this.jsIdCounter++;
            }
            node.setName(vertex.getLabelManager().getMainLabel().toString());
            this.addVertexAttributes(vertex, node);
            jsForceGraph.getNodes().add(node);
            this.vertexMap.put(vertex, node);
        }

        for (Edge edge : graph.getEdges())
        {
            Link link = new Link();
            if (edge.getPorts().size() > 1)
            {
                link.setTarget(this.getVertexNodeId(edge.getPorts().get(0)));
                link.setSource(this.getVertexNodeId(edge.getPorts().get(1)));
            }
            link.setName(edge.getLabelManager().getMainLabel().toString());
            this.addEdgeAttributes(edge, link);
            jsForceGraph.getLinks().add(link);
        }

        return jsForceGraph;
    }

    private String getVertexNodeId(Port port)
    {
        Vertex vertex = port.getVertex();
        return this.vertexMap.get(vertex).getId();
    }

    // Possibility to extend exported data using inheritance
    @SuppressWarnings("unused")
    protected void addVertexAttributes( Vertex vertex, Node node)
    { }

    // Possibility to extend exported data using inheritance
    @SuppressWarnings("unused")
    protected void addEdgeAttributes(Edge edge, Link link)
    { }
}
