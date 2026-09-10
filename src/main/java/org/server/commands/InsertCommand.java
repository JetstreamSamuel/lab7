package org.server.commands;

import org.models.Movie;
import org.server.service.ArgsForCommands;
import org.server.service.Context;
import org.client.CreateMovie;
import org.server.service.DBManager;

import java.time.ZonedDateTime;
import java.util.UUID;

public class InsertCommand extends Command implements TypeOfArgument, TypeOfSecondArgument{
    private Context context = Context.getInstance();
    private DBManager dbManager = DBManager.getInstance();

    public InsertCommand() {super("insert");}


    @Override
    public String execute() {
        if(context.getKeys().contains(args.key)){
            return ("Movie with this key already created");
        }else {
            int userId = dbManager.getUserId(args.login);
            dbManager.saveToBd(args.movie, args.key,userId);
            return ("Complete");
        }
    }
    @Override
    public String typeOfArgument() {
        return "String";
    }

    @Override
    public boolean needArg(){
        return true;
    }

    @Override
    public boolean needMoreThanOneArg(){
        return true;
    }
    @Override
    public String typeOfSecondArgument() {
        return "Movie";
    }

    @Override
    public String description() {return "insert null {elemnt} : добавить новый элемент с заданным ключом";}
}
