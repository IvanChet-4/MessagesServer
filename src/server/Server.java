package server;

import client.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;

public class Server {




        static ArrayList<User> users = new ArrayList<>();

        public static void main(String[] args) {

            try {
                ServerSocket serverSocket = new ServerSocket(9443); // запускаем сервер
                System.out.println("Сервер запущен");
                while (true){
                    Socket socket = serverSocket.accept(); // Ожидаем подключения клиента
                    System.out.println("Клиент подключился");
                    User currentUser = new User(socket);
                    users.add(currentUser);

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject =new JSONObject();
                                jsonObject.put("msg", "Введите имя");
                                currentUser.getOut().writeUTF(jsonObject.toJSONString());
                                String name = currentUser.getIs().readUTF();
                                currentUser.setName(name);
                                sendOnlineUsers();
                                while (true){
                                    String message = currentUser.getIs().readUTF();
                                    if (message.equals("/getOnlineUsers")) {
                                        JSONObject jsonObjectOnlineUsers = new JSONObject();
                                        JSONArray jsonArray = new JSONArray();
                                        for (User user : users) {
                                            jsonArray.add(user.getName());
                                        }
                                        jsonObjectOnlineUsers.put("users", jsonArray);
                                        currentUser.getOut().writeUTF(jsonObjectOnlineUsers.toJSONString());
                                    }else
                                        for (User user:users) {
                                            System.out.println(message.toUpperCase(Locale.ROOT));
                                            jsonObject.put("msg", name + ": "+ message);
                                            if (!user.getUuid().toString().equals(currentUser.getUuid().toString()))
                                                user.getOut().writeUTF(jsonObject.toJSONString());
                                        }
                                }
                            }catch (Exception e){
                                System.out.println("Клиент отключился");
                                sendOnlineUsers();
                                users.remove(currentUser);
                            }
                        }
                    });
                    thread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public static void sendOnlineUsers() {
            JSONArray userList = new JSONArray();
            for (User user:users) {
                JSONObject jsonUserInfo = new JSONObject();
                jsonUserInfo.put("name", user.getName());
                userList.add(jsonUserInfo);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("users", userList);
            for (User user:users) {
                try {
                    user.getOut().writeUTF(jsonObject.toJSONString());
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }