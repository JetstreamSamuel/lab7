package org.server.commands;

import org.models.Movie;
import org.server.service.Context;
import org.client.CreateMovie;
import org.server.service.DBManager;

public class UpdateCommand extends Command implements TypeOfArgument, TypeOfSecondArgument{
    private Context context = Context.getInstance();
    public UpdateCommand() {super("update");}

    @Override
    public String execute() {
        int id = args.id;
        if (DBManager.getInstance().updateMovie(args.key, args.movie, id)) DBManager.getInstance().loadMovies(context);
        return "Updated";
    }
    @Override
    public String typeOfArgument() {return "Integer";}

    @Override
    public String typeOfSecondArgument() {return "Movie";}

    @Override
    public boolean needArg() {return true;}

    @Override
    public boolean needMoreThanOneArg() {return true;}

    @Override
    public String description() {return "update id {element} : " +
            "обновить значение элемента коллекции, id которого равен заданному";}
}
