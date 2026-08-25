package org.common;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;


@Setter
@Getter
public class Request implements Serializable {
    private final String type;
    private final String args;
    private final Object object;

    public Request(String type, String args, Object object) {
        this.type = type;
        this.args = args;
        this.object = object;
    }

    public String getCommandName() {
        return type;
    }

    public Object getObjectArg() {
        return object;
    }

    public String getStringArg() {
        return args;
    }
}
