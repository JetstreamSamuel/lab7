package org.models;


import jakarta.xml.bind.annotation.*;

import java.io.Serializable;

@XmlRootElement(name="location")
@XmlAccessorType(XmlAccessType.FIELD)
public class Location implements Serializable {
    @XmlElement(name="X")
    private Long x;

    @XmlElement(name="Y")
    private float y;

    @XmlElement(name="Z")
    private Float z;

    @XmlElement(name="name")
    private String name;

    public Location() {}

    public Location(Long x, float y, Float z, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }

    public Long getX() {return x;}
    public float getY() {return y;}
    public Float getZ() {return  z;}
    public String getName() {return name;}

    public void setX(Long x) {this.x = x;}
    public void setY(float y) {this.y = y;}
    public void setZ(Float z) {this.z = z;}
    public void setName(String name) {this.name = name;}

    @Override
    public String toString() {
        return "{Название: " + name + ", " +
                "Координаты: ("+x+", "+y+", "+z+")}";
    }
}
