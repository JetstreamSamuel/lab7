package org.utils.adapters;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class XmlDateAdapter extends XmlAdapter<String, LocalDateTime> {
    private static final DateTimeFormatter formater = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

    @Override
    public LocalDateTime unmarshal(String v) throws Exception {
        return LocalDateTime.parse(v, formater);
    }

    @Override
    public String marshal(LocalDateTime v) throws Exception {
        return v.format(formater);
    }

}
