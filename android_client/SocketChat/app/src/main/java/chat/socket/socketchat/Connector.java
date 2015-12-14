package chat.socket.socketchat;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import de.greenrobot.event.EventBus;

public class Connector {

    private Socket  socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public void connect() {
        try {
            socket = new Socket(Const.SERVER_ADDRESS, Const.SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println("android"); //login
            writer.println("android"); // password
            reader.readLine();          //authorization result

            Resender resender = new Resender();
            resender.start();

        } catch (Exception e) {
            Log.d("connector"," " +  e.getMessage());
        }
    }

    public void post(String str) {
        writer.println(str);
    }

    public void close() {
        try {
            reader.close();
            writer.close();
            socket.close();
        } catch (Exception e) {
            Log.d("connector", e.getMessage());

        }
    }

    private class Resender extends Thread {
        private boolean stopped;

        public void setStopped(boolean stopped) {
            this.stopped = stopped;
        }

        @Override
        public void run() {
            try {
                while (!stopped) {
                    String str = reader.readLine();
                    if (str == null) {
                        continue;
                    }
                    if (".server_exit".equals(str)) {
                        close();
                        break;
                    }
                    EventBus.getDefault().post(str);
                }
            } catch (IOException e) {
                Log.d("connector", e.getMessage());

            }
        }
    }
}
