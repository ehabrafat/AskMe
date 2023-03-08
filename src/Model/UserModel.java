package Model;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import Controller.UserController;

public class UserModel {
    public static final File path;
    static{
      path = new File(Paths.get("").toAbsolutePath().toString() + "/AskMeUsers");
      path.mkdir();
    }
    public static void insertUser(UserController user){
        String filePath = path + "/" + user.getId() + ".txt";
        try{
            File file = new File(filePath);
            file.createNewFile();
        } catch (IOException e){
            e.printStackTrace();
        }
        try(FileWriter out = new FileWriter(filePath)){
            out.write(user.getName() + "\n");
            out.write(user.getPass() + "\n");
            out.write(user.isAllowAQ() + "\n");
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static List<UserController> getAllUsers(){
        List<UserController> users = new ArrayList<>();
        File[] files = path.listFiles();
        for(File file : files){
            String[] tokens = new String[3];
            int i = 0;
            try(BufferedReader buf = new BufferedReader(new FileReader(file))){
                String line;
                while ((line = buf.readLine()) != null){
                    tokens[i++] = line.trim();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
            Integer id = Integer.parseInt(file.getName().replace(".txt", ""));
            users.add(new UserController(id, tokens[0], tokens[1], Boolean.parseBoolean(tokens[2])));
        }
        return users;
    }
    public static UserController getUser(Integer userId){
        List<UserController> users = getAllUsers();
        for(UserController user : users){
            if(user.getId().equals(userId)){
                return user;
            }
        }
        return null;
    }
    public static UserController getUser(String name, String pass){
        List<UserController> users = getAllUsers();
        for(UserController user : users){
            if(user.getName().equals(name) && user.getPass().equals(pass)){
                return user;
            }
        }
        return null;
    }
}
