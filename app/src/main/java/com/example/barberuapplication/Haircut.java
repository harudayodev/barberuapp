package com.example.barberuapplication;

public class Haircut {
    private final String id;
    private final String name;

    public Haircut(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}
