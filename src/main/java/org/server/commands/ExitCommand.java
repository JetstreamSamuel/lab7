package org.server.commands;

import org.server.service.ArgsForCommands;
import org.server.service.Context;

public class ExitCommand extends Command implements TypeOfArgument{

    public ExitCommand() {super("exit");}

    @Override
    public String execute() {
        return "Работа завершена";
    }

    @Override
    public String typeOfArgument() {
        return "SocketChannel";
    }

    @Override
    public String description() {return "exit : завершить программу (без сохранения в файл)";}
}
