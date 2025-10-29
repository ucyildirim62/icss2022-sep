package nl.han.ica.datastructures;

import java.util.LinkedList;
import java.util.Queue;

public class HANQueue<T> implements IHANQueue<T> {

    private final Queue<T> queue = new LinkedList<>();

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public void enqueue(T value) {
        queue.add(value);
    }

    @Override
    public T dequeue() {
        return queue.poll();
    }

    @Override
    public T peek() {
        return queue.peek();
    }

    @Override
    public int getSize() {
        return queue.size();
    }
}
