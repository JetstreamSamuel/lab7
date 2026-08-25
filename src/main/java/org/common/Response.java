package org.common;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;


@Setter
@Getter
public class Response implements Serializable {
    private final String message;
    private final Object data;

    public Response(String message, Object data) {
        this.message = message;
        this.data = data;
    }
}
