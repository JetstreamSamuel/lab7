package org.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.common.CommandInfo;
import org.common.Request;
import org.common.Response;
import org.models.Movie;
import org.server.commands.Command;
import org.server.commands.TypeOfArgument;
import org.server.commands.TypeOfSecondArgument;
import org.server.service.*;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class Server {
    private final Logger logger = LogManager.getLogger(Server.class);
    private static final String fileName = "data.xml";
    private Context context;
    private CommandExecuter commandExecuter;
    private final InetAddress address;
    private final int PORT;



    public Server(InetAddress address,int PORT){
        logger.debug("address:{} PORT:{}", address, PORT);
        this.PORT = PORT;
        this.address = address;
    }

    public static void main(String[] args){
        try {
            InetAddress address = InetAddress.getByName("0.0.0.0");
            Server server = new Server(address,35555);

            server.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void startServer(){
        logger.info("Попытка загрузки коллекции из файла: {}", fileName);
        try {
            context = FileManager.loadXML(fileName);
        } catch (Exception e) {
            logger.error("Ошибка загрузки коллекции из файла: {}", fileName);
            context = new Context();
        }
        commandExecuter = new CommandExecuter();
        startConsoleThread();
        try{
            try (var serverSocketChannel = ServerSocketChannel.open()) {
                serverSocketChannel.configureBlocking(false);
                var serverSocket = serverSocketChannel.socket();
                serverSocket.bind(new InetSocketAddress(address, PORT));
                logger.info("Сервер запущен и ожидает подключений на порту {}", PORT);
                try (var selector = Selector.open()) {
                    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                    while (true) {
                        selector.select();
                        Set<SelectionKey> selectedKeys = selector.selectedKeys();
                        Iterator<SelectionKey> iter = selectedKeys.iterator();
                        while (iter.hasNext()) {
                            SelectionKey key = iter.next();
                            iter.remove();
                            if (!key.isValid()) continue;
                            if (key.isAcceptable()) {
                                ServerSocketChannel server = (ServerSocketChannel) key.channel();
                                connectionAccept(server,selector);
                            }
                            if (key.isReadable()) {
                                SocketChannel client = (SocketChannel) key.channel();
                                readFromClient(client);
                            }
                        }
                    }
                }

            }

        } catch (Exception e){
            logger.error(e);
        }
    }
    private void connectionAccept(ServerSocketChannel server, Selector selector){
        try {
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            logger.info("Установлено новое соединение с клиентом: {}", client.getRemoteAddress());
        } catch (IOException e) {
            logger.error("Ошибка при принятии соединения: ", e);
        }
    }

    private void readFromClient(SocketChannel client){
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        try{
            int readByte = client.read(lengthBuffer);
            if(readByte == -1){
                logger.info("Client disconnected:{}",client.getRemoteAddress());
                client.close();
                return;
            }
            if (readByte < 4){
                while(lengthBuffer.position() < 4) {
                    client.read(lengthBuffer);
                }
            }
            lengthBuffer.flip();
            int dataLength = lengthBuffer.getInt();
            logger.debug("Получен заголовок: ожидаемый размер данных: {} байт", dataLength);
            ByteBuffer bodyBuf = ByteBuffer.allocate(dataLength);
            while (bodyBuf.hasRemaining()) {
                client.read(bodyBuf);
            }

            bodyBuf.flip();
            byte[] resBytes = new byte[bodyBuf.remaining()];
            bodyBuf.get(resBytes);
            Request request = deserializeRequest(resBytes);
            logger.debug("Запрос успешно десериализован: {}", request.getCommandName());
            processing(request,client);


        } catch (Exception e) {
            logger.error(e);
        }
    }

    private Request deserializeRequest(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        return (Request) ois.readObject();
    }

    private byte[] serializeResponse(Response response) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(response);
        oos.flush();
        return baos.toByteArray();
    }

    public void processing(Request request, SocketChannel client) {
        try {
            if (request == null){
                logger.warn("Получен пустой запрос от клиента");
                return;
            }
            String commandName = request.getCommandName();
            logger.info("Command accepted: {}", commandName);
            Response response;

            if (commandName.equalsIgnoreCase("get_commands")) {
                Map<String, CommandInfo> commandData = commandExecuter.getCommandsData();
                response = new Response("Команды синхронизированны", commandData);
            } else {
                Command cmd = commandExecuter.getCommand(commandName);
                if (cmd == null) {
                    response = new  Response("Нет такой команды", null);
                    sendResponse(client, response);
                    return;
                }
                if(cmd.needArg()) {
                    TypeOfArgument tp = (TypeOfArgument) cmd;
                    if (tp.typeOfArgument().equals("String")) {
                        try {
                            String key = request.getStringArg();
                            if (cmd.needMoreThanOneArg()) {
                                TypeOfSecondArgument tp2 = (TypeOfSecondArgument) cmd;
                                if (tp2.typeOfSecondArgument().equals("Movie")) {
                                    Movie movie = (Movie) request.getObjectArg();
                                    ArgsForCommands args = new ArgsForCommands(key, movie);
                                    cmd.setArgs(args);
                                }
                            } else {
                                ArgsForCommands args = new ArgsForCommands(key);
                                cmd.setArgs(args);
                            }
                        } catch (Exception e) {
                            logger.warn("Ошибка в аргументах команды {}: {}", commandName, e.getMessage());
                            response = new Response("Mistake in argue " + e.getMessage(), null);
                            sendResponse(client, response);
                            return;
                        }
                    } else if (tp.typeOfArgument().equals("fileName")) {
                        FileName filenameFromPerson = new FileName(request.getStringArg());
                        if (commandExecuter.fileNames.contains(filenameFromPerson.name())) {
                            System.out.println("Cyclic dependency in the file");
                            return;
                        }
                        commandExecuter.fileNames.add(filenameFromPerson.name());
                        ArgsForCommands args = new ArgsForCommands(filenameFromPerson);
                        cmd.setArgs(args);
                    } else if (tp.typeOfArgument().equals("Integer")) {
                        try {
                            Integer id = Integer.valueOf(request.getArgs());
                            if (cmd.needMoreThanOneArg()) {
                                TypeOfSecondArgument tp2 = (TypeOfSecondArgument) cmd;
                                if (tp2.typeOfSecondArgument().equals("Movie")) {
                                    Movie movie = (Movie) request.getObjectArg();
                                    ArgsForCommands args = new ArgsForCommands(id, movie);
                                    cmd.setArgs(args);
                                }
                            } else {
                                ArgsForCommands args = new ArgsForCommands(id);
                                cmd.setArgs(args);
                            }
                        } catch (Exception e) {
                            logger.warn("Ошибка в аргументах команды {}: {}", commandName, e.getMessage());
                            response = new Response("Mistake in argue " + e.getMessage(), null);
                            sendResponse(client, response);
                            return;
                        }
                    }
                }
                String result = cmd.execute();
                response = new Response(result, null);
            }
            sendResponse(client, response);
        } catch (Exception e) {
            try {
                logger.info("Сессия с клиентом {} завершена", client.getRemoteAddress());
            } catch (IOException w) {
                logger.info("Сессия с клиентом завершена (адрес недоступен)");
            }
        }
    }

    private void sendResponse(SocketChannel client, Response response) throws IOException {
        byte[] responseBytes = serializeResponse(response);
        logger.debug("Отправка ответа клиенту. Размер: {} байт", responseBytes.length);
        int length = responseBytes.length;
        ByteBuffer responseBuffer = ByteBuffer.allocate(4+length);
        responseBuffer.putInt(length);
        responseBuffer.put(responseBytes);
        responseBuffer.flip();
        while(responseBuffer.hasRemaining()) {
            client.write(responseBuffer);
        }
    }

    private void startConsoleThread() {
        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.equalsIgnoreCase("save")) {
                    logger.info("Сохранение коллекции в файл по команде из консоли");
                    try {FileManager.writeXML(context, fileName);} catch (Exception e) {
                        logger.error("Ошибка сохранения коллекции в файл");
                    }
                } else if (line.equalsIgnoreCase("exit")) {
                    logger.info("Завершение работы сервера");
                    try {FileManager.writeXML(context, fileName);} catch (Exception e) {
                        logger.error("Ошибка сохранения коллекции в файл");
                    }
                    System.exit(0);
                }
            }
        });
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

}