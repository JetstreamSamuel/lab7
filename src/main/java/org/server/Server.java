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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private final Logger logger = LogManager.getLogger(Server.class);
    private Context context = Context.getInstance();
    private CommandExecuter commandExecuter = CommandExecuter.getInstance();
    private final DBManager dbManager = DBManager.getInstance();
    private final InetAddress address;
    private final int PORT;
    private final ExecutorService ioPool = Executors.newCachedThreadPool();
    private final ExecutorService fixedPool = Executors.newFixedThreadPool(3);



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
        dbManager.createTables();
        dbManager.loadMovies(context);
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
                                readFromClient(client, key);
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

    private void readFromClient(SocketChannel client, SelectionKey key){
        key.interestOps(0);
        ioPool.submit(() -> {
            try{
                ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
                if (client.read(lengthBuffer) <= 0) return;
                lengthBuffer.flip();
                int length = lengthBuffer.getInt();
                ByteBuffer bodyBuffer = ByteBuffer.allocate(length);
                while (bodyBuffer.hasRemaining()) {
                    client.read(bodyBuffer);
                }
                bodyBuffer.flip();
                byte[] data = new byte[bodyBuffer.remaining()];
                bodyBuffer.get(data);
                Request request = deserializeRequest(data);

                CompletableFuture<Response> processingFuture = new CompletableFuture<>();

                new Thread(() -> {
                    try {
                        Response response = handleRequest(request);
                        processingFuture.complete(response);
                    } catch (Exception e) {
                        processingFuture.completeExceptionally(e);
                    }
                }).start();

                processingFuture.thenAcceptAsync(response -> {
                    try {
                        sendResponse(client, response);
                    } catch (IOException e) {
                        logger.error("Ошибка отправки " + e.getMessage());
                    } finally {
                        synchronized (key) {
                            if (key.isValid()) {
                                key.interestOps(SelectionKey.OP_READ);
                                key.selector().wakeup();
                            }
                        }
                    }
                }, fixedPool);


            } catch (Exception e) {
                logger.error("Ошибка при чтении из клиента: " + e.getMessage());
                key.cancel();
                try { client.close(); } catch (IOException ignored) {}
            }
        });

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

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка хэширования", e);
        }
    }

    public Response handleRequest(Request request) {
        try {
            if (request == null) return new Response("Пустой запрос", null);

            String commandName = request.getCommandName();
            String login = request.getLogin();
            String password = hashPassword(request.getPassword());
            boolean isRegistration = request.isRegistration();
            if (commandName.equalsIgnoreCase("check_auth")) {
                if (isRegistration) {
                    boolean success = dbManager.registerUser(login, password);
                    return new Response(success ? "Регистрация успешна" : "Этот логин уже занят.", null, success);
                } else {
                    boolean success = dbManager.checkUser(login, password);
                    return new Response(success ? "Вход выполнен" : "Неверный логин или пароль.", null, success);
                }
            }

            if (!commandName.equalsIgnoreCase("get_commands") && !dbManager.checkUser(login, password)) {
                return new Response("Ошибка авторизации", null, false);
            }

            if (commandName.equalsIgnoreCase("get_commands")) {
                return new Response("Command synchronized", commandExecuter.getCommandsData());
            }

            Command cmd = commandExecuter.getCommand(commandName);
            if (cmd == null) return new Response("Команда не найдена", null);

            if (cmd.needArg()) {
                TypeOfArgument tp = (TypeOfArgument) cmd;
                if (tp.typeOfArgument().equals("String")) {
                    String key = request.getStringArg();
                    if (cmd.needMoreThanOneArg()) {
                        TypeOfSecondArgument tp2 = (TypeOfSecondArgument) cmd;
                        if (tp2.typeOfSecondArgument().equals("Movie")) {
                            Movie movie = (Movie) request.getObjectArg();
                            cmd.setArgs(new ArgsForCommands(key, movie, new Login(login)));
                        }
                    } else {
                        cmd.setArgs(new ArgsForCommands(key, new Login(login)));
                    }
                } else if (tp.typeOfArgument().equals("fileName")) {
                    FileName filename = new FileName(request.getStringArg());
                    cmd.setArgs(new ArgsForCommands(filename));
                }
            } else {
                cmd.setArgs(new ArgsForCommands(login));
            }

            String result = cmd.execute();
            return new Response(result, null);
        } catch (Exception e) {
            logger.error("Ошибка", e);
            return new Response("Ошибка сервера " + e.getMessage(), null);
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
                if (line.equalsIgnoreCase("exit")) {
                    logger.info("Завершение работы сервера");
                    System.exit(0);
                }
            }
        });
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

}

//  server:
//    image: eclipse-temurin:17-jre-alpine
//    container_name: lab7_server
//    volumes:
//      - ./build/libs:/app
//    working_dir: /app
//    command: java -jar lab7-server-1.0-SNAPSHOT.jar
//    depends_on:
//      db:
//        condition: service_healthy
//    ports:
//      - "12345:12345"
//    stdin_open: true
//    tty: true
//
//  client:
//    image: eclipse-temurin:17-jre-alpine
//    container_name: lab7_client
//    volumes:
//      - ./build/libs:/app
//    working_dir: /app
//    command: java -jar lab7-client-1.0-SNAPSHOT.jar
//    depends_on:
//      - server
//    stdin_open: true
//    tty: true
//
//volumes:
//  lab7_postgres_data: