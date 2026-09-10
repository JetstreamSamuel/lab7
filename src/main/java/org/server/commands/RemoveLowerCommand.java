package org.server.commands;

import org.server.service.Context;
import org.client.CreateMovie;
import org.models.Movie;
import org.server.service.DBManager;

public class RemoveLowerCommand extends Command implements  TypeOfArgument{
    private Context context = Context.getInstance();

    public RemoveLowerCommand() {super("remove_lower");}

    @Override
    public String execute() {
        Movie target = args.movie;
        for (var key : context.getKeys()) {
            if (target.compareTo(context.getElem(key)) > 0) {DBManager.getInstance().removeMovie(key, args.id);}
        }
        DBManager.getInstance().loadMovies(context);
        return "Successfully removed";
    }

    @Override
    public String typeOfArgument() {return "Movie";}

    @Override
    public boolean needArg(){
        return true;
    }

    @Override
    public String description() {return "remove_lower {element} : " +
            "удалить из коллекции все элементы, меньшие, чем заданный";}
}
