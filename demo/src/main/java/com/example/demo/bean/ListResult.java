package com.example.demo.bean;

import java.util.List;

public class ListResult<T> {
    private List<T> items;

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
