package org.server.service;

import org.models.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.utils.adapters.XmlDateAdapter;
@XmlRootElement(name="context")
@XmlAccessorType(XmlAccessType.FIELD)
public class Context {
    @XmlElement(name="creationDate")
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    private final LocalDateTime creationDate;

    @XmlElementWrapper(name = "movies")
    @XmlElement(name = "movie")
    private HashMap<String, Movie> collection;

    private static Context instance;

    public Context() {
        collection = new HashMap<>();
        creationDate = LocalDateTime.now();
        instance = this;
    }

    public static Context getInstance() {
        if (instance == null) {
            instance = new Context();
        }
        return instance;
    }

    public void clear() {collection.clear();}

    public void add(String key, Movie movie) {collection.put(key, movie);}

    public String show() {
        return collection.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue().toString())
                .collect(Collectors.joining("\n"));
    }

    public void addMovie(String key, Movie movie) {
        collection.put(key, movie);
    }

    public Set<String> getKeys() {return collection.keySet();}

    public Movie getElem(String key) {return collection.get(key);}

    public void removeKey(String key) {collection.remove(key);}


    public String getInfo() {
        return "Информация о коллекции:\n" +
                "Тип: Movie\n" +
                "Дата создания: " + creationDate.format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")) + "\n" +
                "Количество элементов: " + collection.values().toArray().length;
    }
}
