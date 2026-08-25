package org.server.commands;

import org.server.service.Context;

public class InfoCommand extends Command{
    private Context context = Context.getInstance();

    public InfoCommand() {super("info");}

    @Override
    public String execute() {
        return context.getInfo();
    }

    @Override
    public String description() {return "info : вывести в стандартный поток вывода информацию о коллекции";}
}
