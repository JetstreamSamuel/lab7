package org.server.service;


import org.models.Movie;
import java.nio.channels.SocketChannel;

public class ArgsForCommands {
    public Movie movie;
    public String key;
    public Integer id;

    public FileName fileName;
    public SocketChannel socketChannel;

    public ArgsForCommands(String key, Movie movie) {
        this.key = key;
        this.movie = movie;
    }

    public ArgsForCommands(String key) {
        this.key = key;
    }

    public ArgsForCommands(Integer id) {
        this.id = id;
    }

    public ArgsForCommands(FileName fileName) {
        this.fileName = fileName;
    }
    public ArgsForCommands(Integer id, Movie movie) {
        this.id = id;
        this.movie = movie;
    }


    public ArgsForCommands(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }



}