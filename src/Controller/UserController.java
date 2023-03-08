package Controller;

import Model.QuestionModel;
import Model.UserModel;
import java.io.File;
import java.util.List;

public class UserController {
    private Integer id;
    private String name;
    private String pass;
    boolean allowAQ;
    public UserController(Integer id, String name, String pass, boolean allowAQ) {
        this.id = id;
        this.name = name;
        this.pass = pass;
        this.allowAQ = allowAQ;
    }
    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getPass() {
        return pass;
    }
    public boolean isAllowAQ() {
        return allowAQ;
    }
    public static int generateId(){
        File[] files =  UserModel.path.listFiles();
        int last = 0;
        if(files.length > 0)
            last = Integer.parseInt(files[files.length - 1].getName().replace(".txt", ""));
        return 1 + last;
    }
    public void answerQuestion(QuestionController question, String answer){
        question.setAnswer(answer);
        QuestionModel.insertQuestion(question);
    }
    public static UserController getUser(Integer userId){
       return UserModel.getUser(userId);
    }
    public static UserController getUser(String name, String pass){
        return UserModel.getUser(name, pass);
    }
    public static List<UserController> getAllUsers(){
        return UserModel.getAllUsers();
    }

    @Override
    public String toString(){
        return String.format("Id: %d, Name: %s", id, name);
    }

}
