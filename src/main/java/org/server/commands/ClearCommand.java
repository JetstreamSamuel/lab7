package org.server.commands;

import org.server.service.ArgsForCommands;
import org.server.service.Context;
import org.server.service.DBManager;
import org.server.service.Login;

import java.util.List;

public class ClearCommand extends Command{
    private Context context = Context.getInstance();
    DBManager dbManager = DBManager.getInstance();

    public ClearCommand() {super("clear");}


    @Override
    public String execute(){
        int userID = dbManager.getUserId(args.login);
        List<Integer> userIds = dbManager.getUserMoviesIds(userID);
        if (userIds.isEmpty()){
            return "No users tickets found";
        }
        int deletedInDb = dbManager.clearMovies(userID);
        if (deletedInDb >= 0) {
            for (Object idFromDb : userIds) {
                String key = idFromDb.toString();
                context.removeKey(key);
            }
            return "Collection was cleared successfully";
        }
        return ("Smt wrong");
    }

    @Override
    public String description() {return "clear : очистить коллекцию";}
}
