package Model;
import Controller.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionModel {
    public static final File path;
    static{
        path = new File(Paths.get("").toAbsolutePath().toString() + "/AskMeQuestions");
        path.mkdir();
    }
    public static QuestionController constructQuestion(File file) {
        String filePath = file.getAbsolutePath();
        String[] tokens = new String[7]; // from to question answer AQ parentThread childThreads
        int i = 0;
        try (BufferedReader buf = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = buf.readLine()) != null) {
                tokens[i++] = line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Integer id = Integer.parseInt(file.getName().replace(".txt", ""));
        Integer from = Integer.parseInt(tokens[0]);
        Integer to = Integer.parseInt(tokens[1]);
        String question = tokens[2];
        String answer = tokens[3];
        boolean AQ = Boolean.parseBoolean(tokens[4]);
        Integer parentThread = Integer.parseInt(tokens[5]);
        ArrayList<Integer> childThread = new ArrayList<>();
        if(tokens[6] != null)
             childThread = Arrays.stream(tokens[6].split(",")).map(Integer::parseInt).collect(Collectors.toCollection(ArrayList::new));
        return new QuestionController(id, from, to, question, answer, AQ, parentThread, childThread);
    }
    public static QuestionController constructQuestion(Integer questionId){
        File[] files = path.listFiles();
        for(File file : files){
            Integer id = Integer.parseInt(file.getName().replace(".txt", ""));
            if(id.equals(questionId)){
                return constructQuestion(file);
            }
        }
        return null;
    }
    public static List<QuestionController> getAllQuestions(){
        return Arrays.stream(path.listFiles())
                .map(QuestionModel::constructQuestion)
                .toList();
    }
    public static void insertQuestion(QuestionController q) {
        String filePath = path.getAbsolutePath() + "/" +  q.getId() + ".txt";
        File file = new File(filePath);
        try{
            file.createNewFile();
        } catch (IOException e){
            e.printStackTrace();
        }
        try(FileWriter out = new FileWriter(filePath)){
            out.write(q.getFrom() + "\n");
            out.write(q.getTo() + "\n");
            out.write(q.getQuestion() + "\n");
            out.write( q.getAnswer() + "\n");
            out.write( q.getAQ() + "\n");
            out.write( q.getParentThread() + "\n");
            StringBuilder childThread = new StringBuilder();
            for(Integer thread : q.getChildThreads()){
                if(!childThread.isEmpty()) childThread.append(",");
                childThread.append(thread);
            }
            out.write(childThread.toString());
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static boolean delete(Integer questionId){
        String filePath = path + "/" + questionId + ".txt";
        File file = new File(filePath);
        return file.delete();
    }

}
