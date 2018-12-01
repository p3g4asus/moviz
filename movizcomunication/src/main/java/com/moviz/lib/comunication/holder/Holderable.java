package com.moviz.lib.comunication.holder;

public interface Holderable {
    public com.moviz.lib.comunication.holder.HolderSetter toHolder(Class<? extends Holder> cl, Class<? extends com.moviz.lib.comunication.holder.HolderSetter> cllist, String pref);

    public void fromHolder(com.moviz.lib.comunication.holder.HolderSetter hs, String pref);
}
