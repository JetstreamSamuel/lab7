package org.server.commands;

import org.server.service.ArgsForCommands;

public abstract class Command {
    protected final String name;
    protected ArgsForCommands args;

    protected Command(String name) {this.name = name;}

    public abstract String execute();
    public abstract String description()
            ;
    public String getName() {return name;}

    public void setArgs(Object args) {this.args = (ArgsForCommands) args;}

    public boolean needArg() {
        return false;
    }

    public boolean needMoreThanOneArg() {
        return false;
    }
}
