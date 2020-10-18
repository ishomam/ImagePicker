package com.nguyenhoanglam.imagepicker.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObservableList<T> {

    private List<T> list;
    private List<ListChangeListener> listeners = new ArrayList<>();

    public ObservableList(List<T> list){
        this.list = list;
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public T get(int index) {
        return list.get(index);
    }

    public boolean add(T element) {
        boolean result = list.add(element);
        notifyListenersForListChanged();
        notifyListenersForItemAdded();
        return result;
    }

    public void add(int index, T element) {
        list.add(index, element);
        notifyListenersForListChanged();
        notifyListenersForItemAdded();
    }

    public T remove(int index) {
        T result = list.remove(index);
        notifyListenersForListChanged();
        notifyListenersForItemRemoved(index);
        return result;
    }

    public boolean addAll(@NonNull Collection<? extends T> c) {
        boolean result = list.addAll(c);
        notifyListenersForListChanged();
        notifyListenersForBulkItemsChange();
        return result;
    }

    public boolean addAll(int index, @NonNull Collection<? extends T> c) {
        boolean result = list.addAll(index, c);
        notifyListenersForListChanged();
        notifyListenersForBulkItemsChange();
        return result;
    }

    public boolean removeAll(@NonNull Collection<?> c) {
        boolean result = list.removeAll(c);
        notifyListenersForListChanged();
        notifyListenersForBulkItemsChange();
        return result;
    }

    public void clear(){
        list.clear();
        notifyListenersForListChanged();
        notifyListenersForBulkItemsChange();
    }

    private void notifyListenersForItemAdded() {
        for (ListChangeListener listener : listeners) {
            listener.onOneItemAdded();
        }
    }

    private void notifyListenersForItemRemoved(int index) {
        for (ListChangeListener listener : listeners) {
            listener.onOneItemRemoved(index);
        }
    }

    private void notifyListenersForListChanged() {
        for (ListChangeListener listener : listeners) {
            listener.onListChanged(list);
        }
    }

    private void notifyListenersForBulkItemsChange() {
        for (ListChangeListener listener : listeners) {
            listener.onBulkItemsChange();
        }
    }

    public List<T> getList() {
        return list;
    }

    public void addListener(ListChangeListener listChangeListener) {
        listeners.add(listChangeListener);
    }

    public interface ListChangeListener {
        void onListChanged(List<?> list);
        void onBulkItemsChange();
        void onOneItemAdded();
        void onOneItemRemoved(int index);
    }
}
