package org.server.commands;

import org.models.Movie;
import org.server.service.Context;

import java.util.*;
import java.util.stream.Collectors;

public class PrintUniqueTaglineCommand extends Command{
    private Context context = Context.getInstance();
    public PrintUniqueTaglineCommand() {super("print_unique_tagline");}

    @Override
    public String execute() {
        return context.getKeys().stream()
                .map(context::getElem)
                .map(Movie::getTagline)
                .collect(Collectors.toSet()).toString();
    }

    @Override
    public String description() {
        return "print_unique_tagline : вывести уникальные значения поля tagline всех элементов в коллекции";
    }
}
