package com.creasypita.generic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lujq on 9/7/2022.
 */
public class Pair<T> {
    private T first;
    private T second;

    public T getFirst() {
        return first;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public T getSecond() {
        return second;
    }

    public void setSecond(T second) {
        this.second = second;
    }

    public <E> List<E> getList(E e){
        ArrayList<E> es = new ArrayList<>();
        es.add(e);
        es.add(e);
        return es;
    }

    public <U> List<U> getList1(List list){
        return list;
    }
}
