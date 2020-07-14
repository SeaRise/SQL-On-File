package com.searise.sof.optimize.iter;

import java.util.NoSuchElementException;

public class Iterator<E> {
    private Node<E> lastReturned;
    private Node<E> next;
    private int nextIndex;
    private LinkList<E> linkList;

    public Iterator() {
        nextIndex = 0;
        linkList = new LinkList<>();
    }

    Iterator(LinkList<E> linkList) {
        nextIndex = 0;
        this.linkList = linkList;
        this.next = linkList.first;
    }

    public boolean hasNext() {
        return nextIndex < linkList.size;
    }

    public E next() {
        if (!hasNext())
            throw new NoSuchElementException();

        lastReturned = next;
        next = next.next;
        nextIndex++;
        return lastReturned.item;
    }

    public Iterator<E> remove() {
        if (lastReturned == null)
            throw new IllegalStateException();

        Node<E> lastNext = lastReturned.next;
        linkList.unlink(lastReturned);
        if (next == lastReturned) {
            next = lastNext;
        } else {
            nextIndex--;
        }

        lastReturned = null;
        return this;
    }

    public Iterator<E> reset() {
        nextIndex = 0;
        next = linkList.first;
        return this;
    }

    public Iterator<E> add(E e) {
        lastReturned = null;
        linkList.linkLast(e);
        if (next == null) {
            next = linkList.last;
        }
        return this;
    }

    public Iterator<E> newReadOnlyIter() {
        return new ReadOnlyIterator<>(this.linkList);
    }
}
