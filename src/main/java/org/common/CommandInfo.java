package org.common;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;


@Setter
@Getter
public class CommandInfo implements Serializable {
    private final String name;
    private final boolean needArg;
    private final boolean needObject;

    public CommandInfo(String name, boolean arg, boolean obj) {
        this.name = name;
        this.needArg = arg;
        this.needObject = obj;
    }

    public String getName() { return name; }
    public boolean needArg() { return needArg; }
    public boolean needObject() { return needObject; }
}