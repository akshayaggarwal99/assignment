package com.aka.assignment.model;

/**
 * Created by akshayaggarwal99 on 17-02-2017.
 */
public class Captures {
    private String name;
    private String key;

    public Captures() {
    }

    public Captures(String name, String key, String thumbnail) {
        this.name = name;
        this.key = key;
        this.thumbnail = thumbnail;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private String thumbnail;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
