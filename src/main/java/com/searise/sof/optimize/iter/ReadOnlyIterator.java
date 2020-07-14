package com.searise.sof.optimize.iter;

public class ReadOnlyIterator<E> extends Iterator<E> {
    ReadOnlyIterator(LinkList<E> linkList) {
        super(linkList);
    }

    public Iterator<E> add(E e) {
        throw new UnsupportedOperationException("can not add in ReadOnlyIterator");
    }

    public Iterator<E> remove() {
        throw new UnsupportedOperationException("can not remove in ReadOnlyIterator");
    }
}
