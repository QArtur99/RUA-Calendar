package com.artf.ruacalendar.Event;

/**
 * Created by ART_F on 2017-03-01.
 */
import java.io.Serializable;

class Person implements Serializable{
    private String name;
    private String email;

    Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }

    @Override
    public String toString() { return name; }
}