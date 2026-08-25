package org.server.commands;

import org.server.service.ArgsForCommands;
import org.server.service.CommandExecuter;
import org.server.service.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class ExecuteScriptCommand extends Command implements TypeOfArgument{
    private ArgsForCommands argsForScript;

    public ExecuteScriptCommand() {
        super("execute_script");
    }

    @Override
    public String execute(){
        try(BufferedReader sc = new BufferedReader(new InputStreamReader(new FileInputStream(argsForScript.fileName.name())))){
            while(sc.readLine() != null){
                String line = sc.readLine().trim();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return "Скрипт исполнился";
    }

    @Override
    public boolean needArg() {
        return true;
    }

    @Override
    public String typeOfArgument() {
        return "String";
    }

    @Override
    public String description() {
        return "execute_script file_name : считать и исполнить скрипт из указанного файла";
    }
}
