package org.server.commands;

import org.models.Movie;
import org.server.service.Context;
import org.client.CreateMovie;

public class ReplaceGreaterCommand extends Command implements TypeOfArgument{
    private Context context = Context.getInstance();
    public ReplaceGreaterCommand() {super("replace_if_greater");}

    @Override
    public String execute() {
        String key = args.key;
        if (context.getKeys().contains(key)) {
            Movie target = args.movie;
            Movie prev = context.getElem(key);
            if (target.compareTo(prev) > 0) {context.add(key, target);}
        } else {return "Такого ключа нет!";}
        return "Successfully replaced";
    }

    @Override
    public String typeOfArgument() {
        return "Movie";
    }

    @Override
    public boolean needArg() {return true;}

    @Override
    public String description() {
        return "replace_if_greater null {element} : заменить значение по ключу, если новое значение больше старого";
    }
}
