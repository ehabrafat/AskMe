package Controller;

import Model.QuestionModel;
import Model.UserModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class QuestionController {
    private Integer id;
    private Integer from;
    private Integer to;
    private String question;
    private String answer;
    private boolean AQ;
    private Integer parentThread;
    private ArrayList<Integer> childThreads;
    public QuestionController(Integer id, Integer from, Integer to, String question, String answer, boolean AQ, Integer parentThread, ArrayList<Integer> childThreads) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.question = question;
        this.answer = answer;
        this.AQ = AQ;
        this.parentThread = parentThread;
        this.childThreads = childThreads;
    }
    public Integer getId() {
        return id;
    }
    public Integer getFrom() {
        return from;
    }
    public Integer getTo() {
        return to;
    }
    public String getQuestion() {
        return question;
    }
    public String getAnswer() {
        return answer;
    }
    public boolean getAQ(){return AQ;}
    public Integer getParentThread() {
        return parentThread;
    }
    public List<Integer> getChildThreads() {
        return childThreads;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    public void addChildThread(Integer childId){
        childThreads.add(childId);
    }
    public static QuestionController construct(int questionId){
        return QuestionModel.constructQuestion(questionId);
    }
    public static int generateId(){
        File[] files =  QuestionModel.path.listFiles();
        int last = 0;
        if(files.length > 0)
             last = Integer.parseInt(files[files.length - 1].getName().replace(".txt", ""));
        return 1 + last;
    }
    public void delete(){
        if(parentThread != -1){
            QuestionController parentQuestion = QuestionModel.constructQuestion(parentThread);
            parentQuestion.childThreads.remove(id);
            QuestionModel.insertQuestion(parentQuestion);
        }
        delete(id);
    }
    public static List<QuestionController> getAllQuestions(){
        return QuestionModel.getAllQuestions();
    }
    public static List<QuestionController> getQuestionsTo(Integer userId){
        return QuestionModel.getAllQuestions()
                .stream()
                .filter((question) -> question.getTo().equals(userId))
                .toList();
    }
    public static List<QuestionController> getQuestionsFrom(Integer userId){
        return QuestionModel.getAllQuestions()
                .stream()
                .filter((question) -> question.getFrom().equals(userId))
                .toList();
    }
    public void add(){
        QuestionModel.insertQuestion(this);
    }
    public void update(){
        QuestionModel.insertQuestion(this);
    }

    @Override
    public String toString(){
        return String.format("Question id (%d) from user id (%d) to user id (%d) \t Question: %s \t %s",
                id, from, to, question, answer.isEmpty() ? "NOT answered yet" : "Answer: " + answer);
    }
    private void delete(Integer questionId){
        QuestionController questionController = QuestionModel.constructQuestion(questionId);
        for(Integer childId : questionController.childThreads){
            delete(childId);
        }
        QuestionModel.delete(questionId);
    }
}
