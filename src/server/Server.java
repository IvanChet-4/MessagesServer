package server;

import client.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class Server {
        static ArrayList<User> users = new ArrayList<>();
        static String db_url = "jdbc:mysql://localhost:3306/db_users_chat29?user=root&password=Gateway14";
        static Connection connection;
        public static void main(String[] args) {
            try {
                ServerSocket serverSocket = new ServerSocket(9443); // запускаем сервер
                System.out.println("Сервер запущен");
                Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
                    while (true) {
                        Socket socket = serverSocket.accept(); // Ожидаем подключения клиента
                        System.out.println("Клиент подключился");
                        User currentUser = new User(socket);
                        users.add(currentUser);
                        Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //"{login: "ivan@mail.ru", pass: "123"}"
                                JSONParser jsonParser = new JSONParser();
                                boolean isAuth = false;
                                String name ="";
                                    while(!isAuth) {
                                        String result = "error";
                                    String userData = currentUser.getIs().readUTF();
                                    JSONObject authData =(JSONObject) jsonParser.parse(userData);
                                    String login = authData.get("login").toString();
                                    String pass = authData.get("pass").toString();
                                    String tokenFromUser = authData.get("token").toString();
                                    connection = DriverManager.getConnection(db_url);
                                    Statement statement = connection.createStatement();
                                    ResultSet resultSet;
                                        if (tokenFromUser.equals("")) {
                                         resultSet = statement.executeQuery("SELECT * FROM users WHERE login='" + login + "' AND pass='" + pass + "'");
                                        } else {
                                         resultSet = statement.executeQuery("SELECT * FROM users WHERE token='" + tokenFromUser + "'");
                                    }
                                    JSONObject jsonObject = new JSONObject();
                                    if (resultSet.next()) {
                                        //Если совпал логин и пароль
                                        int id = resultSet.getInt("id");
                                        name = resultSet.getString("name");
                                        currentUser.setId(id);
                                        isAuth = true;
                                        result ="succses";
                                        String token = UUID.randomUUID().toString();
                                        statement.executeUpdate("UPDATE users SET token='"+token+"' WHERE id='"+id+"'");
                                        jsonObject.put("token", token);
                                    }
                                    jsonObject.put("authResult", result);
                                    currentUser.getOut().writeUTF(jsonObject.toJSONString());
                                   System.out.println(result);
                                }
                                currentUser.setName(name);
                                sendOnlineUsers();
                                sendHistoryChat(currentUser);
                                    while (true){
                                        JSONObject jsonObject = new JSONObject();
                                        String message = currentUser.getIs().readUTF();
                                        JSONObject jsonMessage = (JSONObject) jsonParser.parse(message);
                                    if (message.equals("/getOnlineUsers")) {
                                        JSONObject jsonObjectOnlineUsers = new JSONObject();
                                        JSONArray jsonArray = new JSONArray();
                                    for (User user : users) {
                                        jsonArray.add(user.getName());
                                    }
                                        jsonObjectOnlineUsers.put("users", jsonArray);
                                        currentUser.getOut().writeUTF(jsonObjectOnlineUsers.toJSONString());
                                    } else if (jsonMessage.get("getHistoryMessage") !=null) {
                                        int toId = Integer.parseInt(jsonMessage.get("getHistoryMessage").toString());
                                        int fromId = currentUser.getId();
                                        Statement statement = connection.createStatement();
                                        ResultSet resultSet = statement.executeQuery("SELECT * FROM messages WHERE from_id = '"+fromId+"' and  to_id = '"+toId+"' or  from_id = '"+toId+"' and  to_id = '"+fromId+"'");
                                        JSONArray jsonMessages = new JSONArray();
                                    while (resultSet.next()) {
                                            JSONObject singleJsonMessage = new JSONObject();
                                            singleJsonMessage.put("msg", resultSet.getString("msg"));
                                            jsonMessages.add(singleJsonMessage);
                                        }
                                        JSONObject jsonResult = new JSONObject();
                                        jsonResult.put("privateMessages",jsonMessages);
                                        currentUser.getOut().writeUTF(jsonResult.toJSONString());
                                    } else {
                                        Statement statement = connection.createStatement();
                                        int id = currentUser.getId();
                                        String msg = jsonMessage.get("msg").toString();
                                        int toId = Integer.parseInt(jsonMessage.get("to_id").toString());
                                        statement.executeUpdate("INSERT INTO `messages` (`msg`,`from_id`, `to_id`) VALUES ('"+message+"','"+id+"','"+toId+"'");
                                    for (User user:users) {
                                            System.out.println(message.toUpperCase(Locale.ROOT));
                                            jsonObject.put("msg", name + ": " + message);
                                    if (!user.getUuid().toString().equals(currentUser.getUuid().toString()))
                                            user.getOut().writeUTF(jsonObject.toJSONString());
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("Клиент отключился...\n");
                                users.remove(currentUser);
                                sendOnlineUsers();
                            }
                        }
                    });
                    thread.start();
                }
            } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        public static void sendHistoryChat(User user) throws Exception {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT users.id, users.name, messages.msg FROM users, messages WHERE users.id = messages.from_id AND to_id=0");
            JSONObject jsonObject = new JSONObject();
            JSONArray messages = new JSONArray();
        while(resultSet.next()) {
            JSONObject message = new JSONObject();
            message.put("name",resultSet.getString("name"));
            message.put("user_id",resultSet.getInt("id"));
            message.put("msg",resultSet.getString("msg"));
            messages.add(message);
  }
            jsonObject.put("messages",messages);
            user.getOut().writeUTF(jsonObject.toJSONString());
  }





        public static void sendOnlineUsers() {
            JSONArray userList = new JSONArray();
            for (User user:users) {
                JSONObject jsonUserInfo = new JSONObject();
                jsonUserInfo.put("name", user.getName());
                jsonUserInfo.put("id", user.getId());
                userList.add(jsonUserInfo);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("users", userList);
            for (User user:users) {
                try {
                    user.getOut().writeUTF(jsonObject.toJSONString());
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }