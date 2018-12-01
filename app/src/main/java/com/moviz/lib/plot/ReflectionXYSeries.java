package com.moviz.lib.plot;

import com.androidplot.xy.XYSeries;

import java.lang.reflect.Field;
import java.util.List;
import java.util.NoSuchElementException;

public class ReflectionXYSeries implements XYSeries {
    private List<? extends Object> values = null;
    private String title;
    private Field fieldX;
    private Field fieldY;
    private double scaleX = 1.0;
    private double scaleY = 1.0;

    private Field getField(List<? extends Object> lst, String fld) throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException {
        Object first = null;
        Field field = null;
        if (lst == null || (!lst.isEmpty() && (field = (first = lst.get(0)).getClass().getField(fld)) == null)) {
            throw new IllegalArgumentException("Please pass a list with a number field");
        } else if (field != null) {
            Class<?> tp = field.getType();
            if ((tp.isPrimitive() && !tp.equals(Character.TYPE) && !tp.equals(Boolean.TYPE)) || (!tp.isPrimitive() && Number.class.isAssignableFrom(tp))) {
                Number n = (Number) field.get(first);
            } else {
                throw new IllegalArgumentException("Please make sure that " + fld + " is a number field");
            }
            //if (tp.equals(Long.TYPE) || tp.equals(Integer.TYPE) || tp.equals(Short.TYPE) ||
            //		tp.equals(Byte.TYPE) || tp.equals(Double.TYPE) || tp.equals(Float.TYPE) || tp.is)
        }
        return field;
    }

    public ReflectionXYSeries(String tit, List<? extends Object> lst, String fldx, String fldy, double sfx, double sfy) throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException {
        fieldX = getField(lst, fldx);
        fieldY = getField(lst, fldy);
        title = tit;
        values = lst;
        scaleX = sfx;
        scaleY = sfy;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Number getX(int arg0) {
        try {
            Number n = (Number) fieldX.get(values.get(arg0));
            return scaleX == 1.0 ? n : n.doubleValue() * scaleX;
        } catch (NullPointerException npe) {
            throw new NoSuchElementException("the given list is empty");
        } catch (NoSuchElementException npe) {
            throw npe;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Number getY(int arg0) {
        try {
            Number n = (Number) fieldY.get(values.get(arg0));
            return scaleY == 1.0 ? n : n.doubleValue() * scaleY;
        } catch (NullPointerException npe) {
            throw new NoSuchElementException("the given list is empty");
        } catch (NoSuchElementException npe) {
            throw npe;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int size() {
        return values.size();
    }

}
