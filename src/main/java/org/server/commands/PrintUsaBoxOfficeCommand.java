package org.server.commands;

import org.models.Movie;
import org.server.service.Context;
import java.util.stream.Collectors;

public class PrintUsaBoxOfficeCommand extends Command{
    private Context context = Context.getInstance();
    public PrintUsaBoxOfficeCommand() {super("print_field_ascending_usa_box_office");}

    @Override
    public String execute() {
        return context.getKeys().stream()
                .map(context::getElem)
                .mapToLong(Movie::getUsaBoxOffice)
                .sorted()
                .boxed()
                .collect(Collectors.toList()).toString();
    }

    @Override
    public String description() {
        return "print_field_ascending_usa_box_office : " +
                "вывести значения поля usaBoxOffice всех элементов в порядке возрастания";
    }
}
