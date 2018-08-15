
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GreetingsServer {

    public static void main(String[] args) {
        new GreetingsServer().startServer();
    }

    public void startServer() {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

        Runnable serverTask = () -> {
            try {
                int port = 5678;
                InetAddress addr = InetAddress.getByName("192.168.5.1");
                ServerSocket serverSocket = new ServerSocket(port, 0, addr);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    clientProcessingPool.submit(new ClientTask(clientSocket));
                }
            } catch (IOException e) {
                System.err.println("Unable to process client request");
                e.printStackTrace();
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }

    private class ClientTask implements Runnable {
        private final Socket clientSocket;

        private ClientTask(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                InputStream is = clientSocket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String message = br.readLine();

                ClientMessage clientMessage = new ClientMessage();
                clientMessage.parseFromJsonString(message);
                TimeUnit.SECONDS.sleep(1);
                System.out.println("message from " + clientMessage.getName() + ": " + clientMessage.getMessage());

                String returnMessage = "{ \"message\" : \"Hello," + clientMessage.getName() + "!\" }";

                OutputStream os = clientSocket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write(returnMessage);
                bw.flush();

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    protected class ClientMessage {

        public void parseFromJsonString (String source) {
            String [] fields = source.split("\\\"");
            for (int i = 1; i< fields.length-2; i+=2) {
                if(fields[i].equals("name"))
                    setName(fields[i+2]);
                else if (fields[i].equals("message"))
                    setMessage(fields[i+2]);
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        private String name;
        private String message;
    }


}
