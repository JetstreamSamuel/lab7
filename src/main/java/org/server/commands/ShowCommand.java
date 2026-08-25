package org.server.commands;

import org.models.Movie;
import org.server.service.Context;

public class ShowCommand extends Command{
    private Context context = Context.getInstance();
    public ShowCommand() {super("show");}

    @Override
    public String execute() {
        return context.show();
    }

    @Override
    public String description() {return "show : вывести в стандартный поток вывода все элементы коллекции";}
}
