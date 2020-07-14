package com.searise.sof.optimize;

import java.util.NoSuchElementException;

public class Iterator<E> {
    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

    private Node<E> lastReturned;
    private Node<E> next;
    private int nextIndex;

    private int size = 0;
    private Node<E> last;
    private Node<E> first;

    public Iterator() {
        nextIndex = 0;
    }

    public boolean hasNext() {
        return nextIndex < size;
    }

    public E next() {
        if (!hasNext())
            throw new NoSuchElementException();

        lastReturned = next;
        next = next.next;
        nextIndex++;
        return lastReturned.item;
    }

    public void remove() {
        if (lastReturned == null)
            throw new IllegalStateException();

        Node<E> lastNext = lastReturned.next;
        unlink(lastReturned);
        if (next == lastReturned)
            next = lastNext;
        else
            nextIndex--;
        lastReturned = null;
    }

    public void reset() {
        nextIndex = 0;
        next = first;
    }

    public void add(E e) {
        lastReturned = null;
        linkLast(e);
    }

    private void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null) {
            first = newNode;
            this.next = first;
        } else {
            l.next = newNode;
            if (this.next == null) {
                this.next = newNode;
            }
        }
        size++;
    }

    private E unlink(Node<E> x) {
        // assert x != null;
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.item = null;
        size--;
        return element;
    }
}
