package com.barq.interview.model;

import java.io.Serializable;

public class Todo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String description;

    // Constructors, Getters, and Setters
    public Todo() {}

    public Todo(Long id, String description) {
        this.id = id;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", description='" + description + '\'' +
                '}';
    }
}
