/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.ac.ut.graph;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Mohammad TRB
 */
public class VisGraph implements Serializable{

    private List<VisEdge> edges;
    private List<VisNode> nodes;

    public VisGraph(List<VisEdge> edges, List<VisNode> nodes) {
        this.edges = edges;
        this.nodes = nodes;
    }

    public List<VisEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<VisEdge> edges) {
        this.edges = edges;
    }

    public List<VisNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<VisNode> nodes) {
        this.nodes = nodes;
    }

   
}
