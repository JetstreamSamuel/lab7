package org.server.commands;

import org.server.service.CommandExecuter;
import org.server.service.Context;

import java.util.Set;

public class HelpCommand extends Command{
    private final CommandExecuter commandExecuter;

    public HelpCommand(CommandExecuter commandExecuter) {
        super("help");
        this.commandExecuter = commandExecuter;
    }

    @Override
    public String execute(){
        Set<String> allCommands = commandExecuter.getKeys();
        StringBuilder res = new StringBuilder();
        for (String name : allCommands){
            Command cmd = commandExecuter.getCommand(name);
            res.append(cmd.description()).append("\n");
        }
        return res.toString();
    }
    @Override
    public String description(){
        return "help: выведет все команды";
    }
}
