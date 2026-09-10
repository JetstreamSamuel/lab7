package org.server.service;

import org.common.CommandInfo;
import org.server.commands.*;
import java.util.Collections;

import java.util.*;
import java.util.stream.Collectors;

public class CommandExecuter {
    private static CommandExecuter instance;
    public List<String> fileNames = new ArrayList<>();
    private Map<String, Command> commands;


    public CommandExecuter(){
        commands = Collections.synchronizedMap(new LinkedHashMap<>());
        addCommand(new HelpCommand(this));
        addCommand(new InfoCommand());
        addCommand(new ClearCommand());
        addCommand(new ExitCommand());
        //addCommand(new ExecuteScriptCommand());
        addCommand(new MaxByNameCommand());
        addCommand(new InsertCommand());
        addCommand(new PrintUniqueTaglineCommand());
        addCommand(new PrintUsaBoxOfficeCommand());
        addCommand(new RemoveKeyCommand());
        addCommand(new RemoveLowerCommand());
        addCommand(new ReplaceGreaterCommand());
        //addCommand(new ShowCommand());
        addCommand(new UpdateCommand());
        addCommand(new ShowCommand());

    }

    public static CommandExecuter getInstance() {
        if (instance == null) {
            instance = new CommandExecuter();
        }
        return instance;
    }

    public Command getCommand(String name) {return commands.get(name);}

    public Set<String> getKeys() {return commands.keySet();}

    public void addCommand(Command command){commands.put(command.getName(), command);}

    public Map<String, CommandInfo> getCommandsData() {
        return commands.values().stream()
                .filter(cmd -> !cmd.getName().equalsIgnoreCase("save"))
                .collect(Collectors.toMap(
                        Command::getName,
                        cmd -> new CommandInfo(
                                cmd.getName(),
                                cmd.needArg(),
                                cmd.needMoreThanOneArg())
                ));

    }

}
