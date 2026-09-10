package org.server.commands;

import org.server.service.Context;
import org.server.service.DBManager;

public class RemoveKeyCommand extends Command implements TypeOfArgument{
    private Context context = Context.getInstance();
    private DBManager dbManager = DBManager.getInstance();


    public RemoveKeyCommand() {super("remove_key");}

    @Override
    public String execute() {
        String key = args.key;
        if (dbManager.removeMovie(args.key, args.id)) context.removeKey(key);
        return "Successfully removed";
    }

    @Override
    public boolean needArg(){
        return true;
    }

    @Override
    public String typeOfArgument() {
        return "String";
    }

    @Override
    public String description() {return "remove_key null : удалить элемент из коллекции по его ключу";}
}
