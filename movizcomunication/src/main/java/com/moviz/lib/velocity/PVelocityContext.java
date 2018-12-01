package com.moviz.lib.velocity;

import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PVelocityContext extends AbstractContext implements Cloneable {
    protected Map<String, Object> context = new HashMap<String, Object>();

    /**
     * Creates a new instance (with no inner context).
     */
    public PVelocityContext() {
        this(null, null);
    }

    /**
     * Creates a new instance with the provided storage (and no inner
     * context).
     *
     * @param context
     */
    public PVelocityContext(Map<String, Object> context) {
        this(context, null);
    }

    /**
     * Chaining constructor, used when you want to
     * wrap a context in another.  The inner context
     * will be 'read only' - put() calls to the
     * wrapping context will only effect the outermost
     * context
     *
     * @param innerContext The <code>Context</code> implementation to
     *                     wrap.
     */
    public PVelocityContext(Context innerContext) {
        this(null, innerContext);
    }

    /**
     * Initializes internal storage (never to <code>null</code>), and
     * inner context.
     *
     * @param context      Internal storage, or <code>null</code> to
     *                     create default storage.
     * @param innerContext Inner context.
     */
    public PVelocityContext(Map<String, Object> context, Context innerContext) {
        super(innerContext);
        this.context = (context == null ? new HashMap<String, Object>() : context);
    }

    public boolean internalContainsKey(Object key) {
        return context.containsKey(key);
    }

    public Object internalGet(String key) {
        return context.get(key);
    }

    public Object[] internalGetKeys() {
        return context.keySet().toArray();
    }

    public Object internalPut(String key, Object value) {
        context.put(key, value);
        return value;
    }

    public Object internalRemove(Object key) {
        if (context.containsKey(key)) {
            context.remove(key);
        }
        return null;
    }

    public Object clone() {
        PVelocityContext clone = null;
        try {
            clone = (PVelocityContext) super.clone();
            clone.context = new HashMap<String, Object>(context);
        } catch (CloneNotSupportedException ignored) {
        }
        return clone;
    }

    public void clear() {
        context.clear();
    }

    @Override
    public String toString() {
        return ""+context;
    }
}
