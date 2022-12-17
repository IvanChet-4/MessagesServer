package client;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class User {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream is;
    private String name;
    private UUID uuid;
    private int id;

    public User(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new DataOutputStream(socket.getOutputStream());
        this.is = new DataInputStream(socket.getInputStream());
        this.uuid = UUID.randomUUID();
    }

    public void setName(String name) {
        this.name = name;
    }
    public DataOutputStream getOut() {
        return out;
    }
    public DataInputStream getIs() {
        return is;
    }
    public String getName() {
        return name;
    }
    public UUID getUuid() {
        return uuid;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
}