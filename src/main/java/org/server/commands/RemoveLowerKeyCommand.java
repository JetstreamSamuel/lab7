package org.server.commands;

import org.server.service.Context;

public class RemoveLowerKeyCommand extends Command implements TypeOfArgument{
    private Context context = Context.getInstance();
    public RemoveLowerKeyCommand() {super("remove_lower_key");}

    @Override
    public String execute() {
        String bigKey = args.key;
        for (var key : context.getKeys()) {
            if (bigKey.compareTo(key) > 0) {context.removeKey(key);}
        }
        return "Removed lower";
    }

    @Override
    public String typeOfArgument() {
        return "String";
    }

    @Override
    public boolean needArg() {return true;}

    @Override
    public String description() {return "remove_lower_key null : " +
            "удалить из коллекции все элементы, ключ которых меньше, чем заданный";}
}
