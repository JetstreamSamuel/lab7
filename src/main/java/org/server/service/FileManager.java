package org.server.service;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.*;

public class FileManager {
    public FileManager() {}

    public static Context loadXML(String path) throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Context.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        try (InputStream is = new FileInputStream(path);
             InputStreamReader reader = new InputStreamReader(is, "UTF-8")) {
            return (Context) unmarshaller.unmarshal(reader);
        } catch (Exception e) {return new Context();}
    }

    public static void writeXML(Context context, String path) throws JAXBException, IOException{
        JAXBContext jaxbContext = JAXBContext.newInstance(Context.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        try (OutputStream os = new FileOutputStream(path);
             OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8")){
            marshaller.marshal(context, writer);
        }
    }
}
