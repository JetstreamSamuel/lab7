package org.server.commands;

import org.models.Movie;
import org.server.service.Context;
import org.client.CreateMovie;

public class UpdateCommand extends Command implements TypeOfArgument, TypeOfSecondArgument{
    private Context context = Context.getInstance();
    public UpdateCommand() {super("update");}

    @Override
    public String execute() {
        int id = args.id;
        boolean flag = false;
        for (var key : context.getKeys()) {
            Movie prev = context.getElem(key);

            if (prev.getId().equals(id)) {
                flag = true;
                Movie newMovie = args.movie;
                prev.setCoordinates(newMovie.getCoordinates());
                prev.setDirector(newMovie.getDirector());
                prev.setName(newMovie.getName());
                prev.setTagline(newMovie.getTagline());
                prev.setMpaaRating(newMovie.getMpaaRating());
                prev.setOscarsCount(newMovie.getOscarsCount());
                prev.setUsaBoxOffice(newMovie.getUsaBoxOffice());
            }
        }
        if (!flag) {return "No such Id";}
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
