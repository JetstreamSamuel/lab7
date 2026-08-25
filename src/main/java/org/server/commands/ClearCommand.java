package org.server.commands;

import org.server.service.ArgsForCommands;
import org.server.service.Context;

public class ClearCommand extends Command{
    private Context context = Context.getInstance();
    public ClearCommand() {super("clear");}

    @Override
    public String execute() {
        context.clear();
        return ("Context has been cleared");
    }

    @Override
    public String description() {return "clear : очистить коллекцию";}
}
