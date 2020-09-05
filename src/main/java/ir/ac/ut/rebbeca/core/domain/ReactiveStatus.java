/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.ac.ut.rebbeca.core.domain;

/**
 *
 * @author mohammad
 */
public enum ReactiveStatus {
    ReceivingMessage("Receive Message"), HandlingMessage("Handle Message");
    
    String desc;
    private ReactiveStatus(String desc) {
        this.desc=desc;
    }

    @Override
    public String toString() {
        return desc;
    }
}
