package org.server.commands;

import org.models.Movie;
import org.server.service.Context;
import org.client.CreateMovie;

import java.time.LocalDateTime;
import java.util.UUID;

public class InsertCommand extends Command implements TypeOfArgument, TypeOfSecondArgument{
    private Context context = Context.getInstance();
    public InsertCommand() {super("insert");}

    @Override
    public String execute() {
        if(context.getKeys().contains(args.key)){
            return ("Movie with this key already created");
        }else {
            args.movie.setId(Math.abs(UUID.randomUUID().hashCode()));
            args.movie.setCreationDate(LocalDateTime.now());
            context.addMovie(args.key, args.movie);
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
