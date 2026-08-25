package org.models;

import jakarta.xml.bind.annotation.*;

import java.io.Serializable;

@XmlRootElement(name="director")
@XmlAccessorType(XmlAccessType.FIELD)
public class Person implements Serializable {
    @XmlElement(name="name")
    private String name;

    @XmlElement(name="passportID")
    private String passportID;

    @XmlElement(name="eyeColor")
    private Color eyeColor;

    @XmlElement(name="hairColor")
    private Color hairColor;

    @XmlElement(name="nationality")
    private Country nationality;

    @XmlElement(name="location")
    private Location location;

    public Person() {}

    public Person(String name, String passportID, Color eyeColor,
                  Color hairColor, Country nationality, Location location) {
        this.name = name;
        this.passportID = passportID;
        this.eyeColor = eyeColor;
        this.hairColor = hairColor;
        this.nationality = nationality;
        this.location = location;
    }

    public String getName() {return name;}
    public String getPassportID() {return passportID;}
    public Color getEyeColor() {return eyeColor;}
    public Color getHairColor() {return hairColor;}
    public Country getNationality() {return nationality;}
    public Location getLocation() {return location;}

    public void setName(String name) {this.name = name;}
    public void setPassportID(String passportID) {this.passportID = passportID;}
    public void setEyeColor(Color eyeColor) {this.eyeColor = eyeColor;}
    public void setHairColor(Color hairColor) {this.hairColor = hairColor;}
    public void setNationality(Country nationality) {this.nationality = nationality;}
    public void setLocation(Location location) {this.location = location;}

    @Override
    public String toString() {
        return "{Имя: " + name + ", " +
                "Паспорт: " + passportID + ", " +
                "Цвет глаз: " + (eyeColor == null ? "Не указан" : eyeColor) + ", " +
                "Цвет волос: " + hairColor + ", " +
                "Национальность: " + nationality + ", " +
                "Локация: " + (location == null ? "" : location) + "}";
    }
}
