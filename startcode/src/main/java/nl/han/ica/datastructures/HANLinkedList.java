package nl.han.ica.datastructures;

import java.util.ArrayList;
import java.util.List;

public class HANLinkedList<T> implements IHANLinkedList<T> {

    private final List<T> nodes;

    public HANLinkedList() {
        this.nodes = new ArrayList<>();
    }

    public HANLinkedList(List<T> nodes) {
        this.nodes = new ArrayList<>(nodes);
    }

    @Override
    public void addFirst(T value) {
        nodes.add(0, value);
    }

    @Override
    public void clear() {
        nodes.clear();
    }

    @Override
    public void insert(int index, T value) {
        nodes.add(index, value);
    }

    @Override
    public void delete(int pos) {
        nodes.remove(pos); // idem: laat IndexOutOfBoundsException door
    }

    @Override
    public T get(int pos) {
        return nodes.get(pos);
    }

    @Override
    public void removeFirst() {
        nodes.remove(0);
    }

    @Override
    public T getFirst() {
        return nodes.get(0);
    }

    @Override
    public int getSize() {
        return nodes.size();
    }
}
