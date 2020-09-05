/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.ac.ut.graph;

import java.io.Serializable;

/**
 *
 * @author Mohammad TRB
 */
public class VisEdge implements Serializable{
    private int from;
    private int to;
    private String fromStr;
    private String toStr;
    private String label;

    public VisEdge(String from, String to, String label) {
        this.fromStr = from;
        this.toStr = to;
        this.label = label;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFromStr() {
        return fromStr;
    }

    public void setFromStr(String fromStr) {
        this.fromStr = fromStr;
    }

    public String getToStr() {
        return toStr;
    }

    public void setToStr(String toStr) {
        this.toStr = toStr;
    }
    
    
}
