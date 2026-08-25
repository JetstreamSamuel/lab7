package org.server.commands;

import org.server.service.Context;
import org.server.service.FileManager;

public class SaveCommand extends Command{
    private Context context = Context.getInstance();
    public SaveCommand() {super("save");}

    @Override
    public String execute() {
        String path = args.key;
        try {
            FileManager.writeXML(context, path);
            System.out.println("Коллекция успешно сохранена в файл " + path);
        } catch (Exception e) {System.out.println(e.getMessage());}

        return "Successfully saved";
    }

    @Override
    public String description() {return "save : сохранить коллекцию в файл";}
}
