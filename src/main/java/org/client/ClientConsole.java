package org.client;

import org.common.CommandInfo;
import org.common.Request;
import org.common.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Scanner;


public class ClientConsole {
    private static final String SERVER_HOST = "0.0.0.0";
    private static final int SERVER_PORT = 35555;
    private static final Logger logger = LogManager.getLogger(ClientConsole.class);
    private Map<String, CommandInfo> serverCommands;
    private boolean isRegistration;
    private String currentLogin;
    private String currentPassword;



    private void syncCommands(SocketChannel client) throws Exception {
        logger.debug("Запрос списка команд у сервера");
        Request request = new Request("get_commands", null, null, isRegistration, currentLogin, currentPassword);
        byte[] requestBytes = serializeRequest(request);
        ByteBuffer buffer = ByteBuffer.allocate(4 + requestBytes.length);
        buffer.putInt(requestBytes.length);
        buffer.put(requestBytes);
        buffer.flip();
        while (buffer.hasRemaining()) {
            client.write(buffer);
        }


        Response response = readResponse(client);
        this.serverCommands = (Map<String, CommandInfo>) response.getData();
        logger.info("Команды синхронизированы");

    }

    public static void main(String[] args) {
        ClientConsole client = new ClientConsole();
        client.run();
    }

    private void registerPpl(Scanner scanner, SocketChannel client) throws Exception {
        while (true) {
            System.out.println("Вы зарегистрированы? Y/N ");
            String answer = scanner.nextLine().trim().toLowerCase();

            if (answer.equals("exit")) System.exit(0);

            boolean isRegAction = answer.equals("n");

            System.out.print("Введите логин: ");
            String login = scanner.nextLine().trim();
            System.out.print("Введите пароль: ");
            String password = scanner.nextLine().trim();

            String hashedPassword = password;

            Request request = new Request("check_auth", null, null, isRegAction, login, hashedPassword);

            byte[] requestBytes = serializeRequest(request);
            ByteBuffer buffer = ByteBuffer.allocate(4 + requestBytes.length);
            buffer.putInt(requestBytes.length);
            buffer.put(requestBytes);
            buffer.flip();
            while (buffer.hasRemaining()) {
                client.write(buffer);
            }
            Response response = readResponse(client);
            if (response.isSuccess()) {
                System.out.println("Успешно! " + response.getMessage());
                this.currentLogin = login;
                this.currentPassword = hashedPassword;;
                break;
            } else {
                System.out.println("Ошибка: " + response.getMessage());
                System.out.println("Попробуйте еще раз.\n");
            }
        }
    }

    public void run(){
        logger.info("Попытка подключения к серверу {}:{}", SERVER_HOST, SERVER_PORT);
        try (SocketChannel client = SocketChannel.open(new InetSocketAddress(SERVER_HOST, SERVER_PORT))) {
            Scanner scanner = new Scanner(System.in);
            logger.info("Соединение с сервером успешно установлено.");
            registerPpl(scanner, client);
            syncCommands(client);
            clientProcess(client, scanner);
        } catch (Exception e) {
            logger.error("Критическая ошибка клиента: {}", e.getMessage());

        }
    }

    private void clientProcess(SocketChannel client, Scanner scanner) throws IOException, ClassNotFoundException {
        while (true) {
            process(client, scanner,false);
        }

    }

    private Response deserializeResponse(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        return (Response) ois.readObject();
    }

    private byte[] serializeRequest(Request request) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(request);
        oos.flush();
        return baos.toByteArray();
    }
    private Response readResponse(SocketChannel client) throws IOException, ClassNotFoundException {

        ByteBuffer lengthBuf = ByteBuffer.allocate(4);
        while (lengthBuf.hasRemaining()) {
            if (client.read(lengthBuf) == -1) throw new IOException("Server closed connection");
        }
        lengthBuf.flip();
        int length = lengthBuf.getInt();
        logger.debug("Получен заголовок ответа. Размер тела: {} байт", length);

        ByteBuffer bodyBuf = ByteBuffer.allocate(length);
        while (bodyBuf.hasRemaining()) {
            if (client.read(bodyBuf) == -1) throw new IOException("Connection lost during reading");
        }

        bodyBuf.flip();
        byte[] resBytes = new byte[bodyBuf.remaining()];
        bodyBuf.get(resBytes);
        logger.debug("Ответ сервера успешно прочитан из канала.");
        return deserializeResponse(resBytes);
    }

    public void process(SocketChannel client, Scanner scanner, boolean isScript) throws IOException, ClassNotFoundException {
        String[] commandAndKey = scanner.nextLine().trim().split("\\s+");
        String cmdName = commandAndKey[0];

        String key = (commandAndKey.length > 1) ? (commandAndKey[1]) : null;

        if (cmdName.equals("execute_script")) {
            logger.info("Начало выполнения скрипта из файла: {}", key);
            if (key == null) {
                System.out.println("U must send path to file");
                return;
            }
            File scriptFile = new File(key);
            try (Scanner fileScanner = new Scanner(scriptFile)) {
                System.out.println("Executing script: " + key);
                while (fileScanner.hasNextLine()) {
                    process(client, fileScanner,true);
                }
                System.out.println("Script " + key + " finished");
            } catch (FileNotFoundException e) {
                System.out.println("File not found");
            }
            return;
        }

        Object movieArg = null;
        CommandInfo info = serverCommands.get(cmdName);

        if (info != null) {
            if (info.needObject()) {
                System.out.println("Create Movie: ");
                CreateMovie cm = new CreateMovie(scanner,isScript);
                movieArg = cm.createMovie();
            }
        } else if (!cmdName.equals("get_commands")) {
            System.out.println("Haven't this command");
            return;
        }
        logger.info("Отправка команды '{}' на сервер...", cmdName);
        Request request = new Request(cmdName, key, movieArg, isRegistration, currentLogin, currentPassword);

        byte[] requestBytes = serializeRequest(request);
        ByteBuffer buffer = ByteBuffer.allocate(4 + requestBytes.length);
        buffer.putInt(requestBytes.length);
        buffer.put(requestBytes);
        buffer.flip();
        while (buffer.hasRemaining()) {
            client.write(buffer);
        }
        Response response = readResponse(client);
        logger.info("Получен ответ: {}", response.getMessage());
        if(cmdName.equals("exit")){
            client.close();
            System.exit(0);
        }
    }

}