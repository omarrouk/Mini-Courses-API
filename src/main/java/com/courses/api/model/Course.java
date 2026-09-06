package com.courses.api.model;

public class Course {
    private Long id;
    private String title;
    private String description;
    private Integer capacity;

    public Course() {
    }

    public Course(Long id, String title, String description, Integer capacity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
