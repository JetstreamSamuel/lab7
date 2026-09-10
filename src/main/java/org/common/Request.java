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
    private boolean isRegistration;
    private final String login;
    private final String password;

    public Request(String type, String args, Object object, boolean isRegistration, String login, String password) {
        this.type = type;
        this.args = args;
        this.object = object;
        this.isRegistration = isRegistration;
        this.login = login;
        this.password = password;
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
