package org.server.commands;

import org.server.service.Context;
import org.models.Movie;

import java.util.Comparator;

public class MaxByNameCommand extends Command{
    private Context context = Context.getInstance();
    public MaxByNameCommand() {super("max_by_name");}

    @Override
    public String execute() {
        Movie target = context.getKeys().stream()
                .map(context::getElem)
                .max(Comparator.comparing(Movie::getName))
                .orElse(null);
        return target != null ? target.toString() : "Коллекция пуста";
    }

    @Override
    public String description() {return "max_by_name : вывести любой объект из коллекции, " +
            "значение поля name которого является максимальным";}
}
