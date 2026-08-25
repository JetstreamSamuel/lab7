package org.models;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.utils.adapters.XmlDateAdapter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@XmlRootElement(name="movie")
@XmlAccessorType(XmlAccessType.FIELD)
public class Movie implements Comparable<Movie>, Serializable {
    @XmlElement(name="id")
    private int id;

    @XmlElement(name="name")
    private String name;

    @XmlElement(name="coordinates")
    private Coordinates coordinates;

    @XmlElement(name="creationDate")
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    private LocalDateTime creationDate;

    @XmlElement(name="oscarsCount")
    private long oscarsCount;

    @XmlElement(name="usaBoxOffice")
    private long usaBoxOffice;

    @XmlElement(name="tagline")
    private String tagline;

    @XmlElement(name="mpaaRating")
    private MpaaRating mpaaRating;

    @XmlElement(name="director")
    private Person director;

    public Movie() {}

    public Movie(String name, Coordinates coordinates, long oscarsCount, long usaBoxOffice,
                 String tagline, MpaaRating mpaaRating, Person director) {
        this.name = name;
        this.coordinates = coordinates;
        this.oscarsCount = oscarsCount;
        this.usaBoxOffice = usaBoxOffice;
        this.tagline = tagline;
        this.mpaaRating = mpaaRating;
        this.director = director;

        id = Math.abs(UUID.randomUUID().hashCode());
        creationDate = LocalDateTime.now();

    }

    public Integer getId() {return id;}
    public String getName() {return name;}
    public Coordinates getCoordinates() {return coordinates;}
    public LocalDateTime getCreationDate() {return creationDate;}
    public long getOscarsCount() {return oscarsCount;}
    public long getUsaBoxOffice() {return usaBoxOffice;}
    public String getTagline() {return tagline;}
    public MpaaRating getMpaaRating() {return mpaaRating;}
    public Person getDirector() {return director;}

    public void setId(int id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setCoordinates(Coordinates coordinates) {this.coordinates = coordinates;}
    public void setCreationDate(LocalDateTime date) {this.creationDate = date;}
    public void setOscarsCount(long oscarsCount) {this.oscarsCount = oscarsCount;}
    public void setTagline(String tagline) {this.tagline = tagline;}
    public void setMpaaRating(MpaaRating mpaaRating) {this.mpaaRating = mpaaRating;}
    public void setUsaBoxOffice(long usaBoxOffice) {this.usaBoxOffice = usaBoxOffice;}
    public void setDirector(Person director) {this.director = director;}

    @Override
    public int compareTo(Movie other) {
        if (other == null) return 1;
        return Long.compare(oscarsCount, other.getOscarsCount());
    }

    @Override
    public String toString() {
        return "{id: " + id + "\n" +
                "Название: " + name + "\n" +
                "Координаты: " + coordinates + "\n" +
                "Дата создания: " + creationDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + "\n" +
                "Количество оскаров: " + oscarsCount + "\n" +
                "USA box office: " + usaBoxOffice + "\n" +
                "Ключевая фраза: " + tagline + "\n" +
                "Возрастной рейтинг: " + mpaaRating + "\n" +
                "Директор: " + (director == null ? "Не указан" : director) + "}\n";
    }
}
