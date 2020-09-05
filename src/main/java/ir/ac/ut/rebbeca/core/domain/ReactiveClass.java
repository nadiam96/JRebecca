/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.ac.ut.rebbeca.core.domain;

import ir.ac.ut.rebbeca.annotations.MsgSrv;
import ir.ac.ut.rebbeca.annotations.StateVar;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mohammad
 */
public class ReactiveClass {

    protected Long id;
    protected List<Message> queue;
    protected ReactiveStatus status;
    protected int maxQueueSize;
    protected Map<String, Long> knownRebecs;

    public ReactiveClass() {
    }

    public ReactiveClass(Long id) {
        this.id = id;
        status = ReactiveStatus.ReceivingMessage;
    }

    public State handleMethod(State primaryState) throws NoMessageException {
        status = ReactiveStatus.HandlingMessage;
        if (queue == null || queue.isEmpty()) {
            throw new NoMessageException();
        }
        Message toHandle = queue.get(0);

        Class cls = this.getClass();
        Class[] strClasses = new Class[toHandle.getArgs().length + 1];
        List<Class> strCls = new ArrayList<>();
        strCls.add(State.class);
        for (int i = 0; i < toHandle.getArgs().length; i++) {
            Object x = toHandle.getArgs()[i];
            Class c = x.getClass();
            if (c.getSuperclass() != null && c.getSuperclass().equals(ReactiveClass.class)) {
                strCls.add(ReactiveClass.class);
            } else {
                strCls.add(c);
            }
        }
        for (int i = 0; i < strCls.size(); i++) {
            strClasses[i] = strCls.get(i);
        }

        State successor = null;
        try {
            if (toHandle.getArgs().length > 0) {
                Method meth = cls.getDeclaredMethod(toHandle.getMethodName(), strClasses);
                if (meth.isAnnotationPresent(MsgSrv.class)) {
                    Object[] params = new Object[strClasses.length];
                    params[0] = primaryState;
                    for(int i=0;i<toHandle.getArgs().length ; i++){
                        params[i+1] = toHandle.getArgs()[i];
                    }
                    successor = (State) meth.invoke(this, params);
                }
            } else {
                Method meth = cls.getDeclaredMethod(toHandle.getMethodName(), strClasses);
                if (meth.isAnnotationPresent(MsgSrv.class)) {
                    successor = (State) meth.invoke(this, primaryState);
                }
            }
            if (queue.size() == 1) {
                queue = new ArrayList<>();
            } else {
                queue = queue.subList(1, queue.size() - 1);
            }
            return successor;
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(ReactiveClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(ReactiveClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ReactiveClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ReactiveClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(ReactiveClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        status = ReactiveStatus.ReceivingMessage;
        return null;
    }

    public void messagePushBack(Message msg) {
        if (queue == null) {
            queue = new ArrayList<>();
        }
        if (queue.size() < maxQueueSize) {
            queue.add(msg);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Message> getQueue() {
        return queue;
    }

    public void setQueue(List<Message> queue) {
        this.queue = queue;
    }

    public ReactiveClass copy(Class clazz) {
        try {
            Constructor ctor = clazz.getConstructor();
            ReactiveClass newReactiveClass = (ReactiveClass) ctor.newInstance();
            for (Field field : ReflectionUtils.getClassFields(clazz)) {
                try {
                    field.set(newReactiveClass, field.get(this));
                } catch (IllegalArgumentException ex) {
                } catch (IllegalAccessException ex) {
                }
            }
            return newReactiveClass;
        } catch (NoSuchMethodException ex) {
        } catch (SecurityException ex) {
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (IllegalArgumentException ex) {
        } catch (InvocationTargetException ex) {
        }

        return null;
    }

    public Map<String, Long> getKnownRebecs() {
        return knownRebecs;
    }

    public void setKnownRebecs(Map<String, Long> knownRebecs) {
        this.knownRebecs = knownRebecs;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    @Override
    public boolean equals(Object o) {
        Class<?> clazz = o.getClass().getSuperclass();
        if (clazz != null && clazz.equals(ReactiveClass.class)) {
            ReactiveClass temp = (ReactiveClass) o;
            return id != null && temp.getId() != null && id.equals(temp.getId());
        }
        return false;
    }

}
