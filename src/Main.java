import Controller.QuestionController;
import Controller.UserController;
import Model.UserModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;


public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static void printQuestion(QuestionController question, Predicate<QuestionController> pre, Function<QuestionController, String> printer, StringBuilder space, HashSet<Integer> seen){
        for(Integer childId : question.getChildThreads()){
            QuestionController childQuestion = QuestionController.construct(childId);
            if(seen.contains(childId)) continue;
            seen.add(childId);
            if(pre.test(childQuestion))
            {
                System.out.println(space + "Thread: " + printer.apply(childQuestion));
                space.append("\t");
                printQuestion(childQuestion, pre, printer, space, seen);
                space.deleteCharAt(space.length()-1);
            } else
                printQuestion(childQuestion, pre, printer, space, seen);
        }
    }
    public static void runAskMe(UserController user){
        while(true){
            System.out.println("Menu:\n" +
                    "\t1. Print Questions To Me\n" +
                    "\t2. Print Questions From Me\n" +
                    "\t3. Answer Questions\n" +
                    "\t4. Delete Questions\n" +
                    "\t5. Ask Questions\n" +
                    "\t6. List System Users\n" +
                    "\t7. List Questions\n" +
                    "\t8. Logout"
            );
            System.out.println("Your choice: ");
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice){
                case 1: {
                    List<QuestionController> questionsToMe = QuestionController.getQuestionsTo(user.getId());
                    if(questionsToMe.isEmpty()){
                        System.out.println("No questions to you");
                        continue;
                    }
                    HashSet<Integer> seen = new HashSet<>();
                    Predicate<QuestionController> pre = q -> q.getTo().equals(user.getId());
                    Function<QuestionController, String> printer = (q) ->{
                        String questionIdText = String.format("Question id (%d)\t", q.getId());
                        String fromText = q.getAQ() ?  "" : String.format("from user id (%d)\t", q.getFrom());
                        String questionText = String.format("Question: %s\t", q.getQuestion());
                        String answerText = q.getAnswer().isEmpty() ? "NOT Answered yet" : String.format("Answer: %s", q.getAnswer());
                        return questionIdText + fromText + questionText + answerText;
                    };
                    for(QuestionController question : questionsToMe){
                        if(!seen.contains(question.getId())){
                            seen.add(question.getId());
                            System.out.println(printer.apply(question));
                            printQuestion(question, pre, printer, new StringBuilder("\t"), seen);
                        }
                     }
                    break;
                }
                case 2: {
                    List<QuestionController> questionsFromMe =  QuestionController.getQuestionsFrom(user.getId());
                    if(questionsFromMe.isEmpty()){
                        System.out.println("No questions from you");
                        continue;
                    }
                    HashSet<Integer> seen = new HashSet<>();
                    Predicate<QuestionController> pre = q -> q.getFrom().equals(user.getId());
                    Function<QuestionController, String> printer = (q) ->{
                        String questionIdText = String.format("Question id (%d)\t", q.getId());
                        String AQText = !q.getAQ() ? "!AQ\t" : "";
                        String toText = String.format("to user id (%d)\t",q.getTo());
                        String questionText = String.format("Question: %s\t", q.getQuestion());
                        String answerText = q.getAnswer().isEmpty() ? "NOT Answered yet" : String.format("Answer: %s", q.getAnswer());
                        return questionIdText + AQText + toText +  questionText + answerText;
                    };;
                    for(QuestionController question : questionsFromMe){
                        if(!seen.contains(question.getId())){
                            seen.add(question.getId());
                            System.out.println(printer.apply(question));
                            printQuestion(question, pre, printer, new StringBuilder("\t"), seen);
                        }
                    }
                    break;
                }
                case 3: {
                    System.out.println("Question id to answer: ");
                    int questionId = Integer.parseInt(scanner.nextLine());
                    QuestionController question = QuestionController.construct(questionId);
                    if(question == null){
                        System.out.println("ERROR: question doesn't exist");
                        continue;
                    }
                    if(!question.getTo().equals(user.getId())){
                        System.out.println("ERROR: This question Not asked to you");
                        continue;
                    }
                    if(!question.getAnswer().isEmpty()){
                        System.out.println("Already answered, update the answer (y/n):");
                        String ans = scanner.nextLine().toLowerCase();
                        if(ans.equals("n") || ans.equals("no")) continue;
                    }
                    System.out.println("Your answer: ");
                    String answer = scanner.nextLine();
                    user.answerQuestion(question, answer);
                    break;
                }
                case 4: {
                    System.out.println("Enter question id");
                    int questionId = Integer.parseInt(scanner.nextLine());
                    QuestionController question = QuestionController.construct(questionId);
                    if(question == null){
                        System.out.println("ERROR: This question doesn't exist");
                    }
                    if(!question.getFrom().equals(user.getId())){
                        System.out.println("ERROR: This question not asked from you");
                    }
                    question.delete();
                    break;
                }
                case 5: {
                    System.out.println("Enter user id: ");
                    Integer userIdToAsk = Integer.parseInt(scanner.nextLine());
                    UserController userToAsk = UserController.getUser(userIdToAsk);
                    if(userToAsk == null){
                        System.out.println("ERROR: user doesn't exist");
                        continue;
                    }
                    if(userIdToAsk.equals(user.getId())){
                        System.out.println("ERROR: You can't ask yourself");
                        continue;
                    }
                    Integer newQuestionId = QuestionController.generateId();
                    System.out.println("For thread question Enter question id or -1 for new question: ");
                    int threadId = Integer.parseInt(scanner.nextLine());
                    if(threadId != -1){
                        QuestionController parentQuestion = QuestionController.construct(threadId);
                        if(parentQuestion == null){
                            System.out.println("ERROR: Thread question doesn't exist");
                            continue;
                        }
                        long ctn = QuestionController.getQuestionsTo(userIdToAsk)
                                .stream()
                                .map((QuestionController::getId))
                                .filter(id -> id == threadId)
                                .count();
                        if(ctn == 0){
                            System.out.format("ERROR: user id (%s) doesn't have this thread question\n", userIdToAsk);
                            continue;
                        }
                        parentQuestion.addChildThread(newQuestionId);
                        parentQuestion.update();
                    }
                    boolean AQ = false;
                    if(userToAsk.isAllowAQ()){
                        int AQChoice;
                        while (true){
                            System.out.println("Is this anonymous question (0 or 1)");
                            try{
                                AQChoice = Integer.parseInt(scanner.nextLine()) ;
                                if(AQChoice < 0 || AQChoice > 1){
                                    System.out.println("ERROR: Enter a number in range 0 - 1");
                                } else
                                    break;
                            } catch (NumberFormatException e){
                                System.out.println("ERROR: Only Numbers are allowed");
                            }
                        }
                        AQ = AQChoice == 1;
                    } else{
                        System.out.println("WARNING: This user doesn't allow anonymous questions");
                    }
                    System.out.println("Enter Question: ");
                    String questionText = scanner.nextLine();
                    QuestionController newQuestion = new QuestionController(
                            newQuestionId,
                            user.getId(),
                            userIdToAsk,
                            questionText,
                            "",
                            AQ,
                            threadId,
                            new ArrayList<>()
                    );

                    newQuestion.add();
                    break;
                }
                case 6:
                    UserController.getAllUsers().forEach(System.out::println);
                    break;
                case 7:{
                    List<QuestionController> questions = QuestionController.getAllQuestions();
                    if(questions.isEmpty()){
                        System.out.println("No questions");
                        continue;
                    }
                    Function<QuestionController, String> printer = (q) ->{
                        String questionIdText = String.format("Question id (%d)\t", q.getId());
                        String fromText = q.getAQ() ?  "" : String.format("from user id (%d)\t", q.getFrom());
                        String toText = String.format("to user id (%d)\t", q.getTo());
                        String questionText = String.format("Question: %s\t", q.getQuestion());
                        String answerText = q.getAnswer().isEmpty() ? "NOT Answered yet" : String.format("Answer: %s", q.getAnswer());
                        return questionIdText + fromText + toText + questionText + answerText;
                    };
                    questions.stream()
                            .filter(question -> question.getParentThread().equals(-1))
                            .forEach(question -> {
                                System.out.println(printer.apply(question));
                                printQuestion(question, q -> true, printer, new StringBuilder("\t"), new HashSet<>());
                            });
                }
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Wrong Choice");
            }
        }
    }
    public static void main(String[] args) {
       while (true){
           System.out.println("Menu:\n" +
                   "\t1: Login\n" +
                   "\t2: Sign Up\n" +
                   "Enter number in range 1 - 2: "
           );
           int choice;
           try{
              choice = Integer.parseInt(scanner.nextLine());
           } catch (NumberFormatException ex){
               System.out.println("ERROR: Only Numbers are allowed");
               main(args);
               return;
           }
           if(choice < 1 || choice > 2){
               System.out.println("ERROR: Wrong Choice..try again");
               main(args);
               return;
           }
           System.out.println("Enter name: " );
           String name = scanner.nextLine();
           System.out.println("Enter password: " );
           String pass = scanner.nextLine();
           UserController user;
           if(choice == 1) {
               user = UserController.getUser(name, pass);
               if(user == null){
                   System.out.println("ERROR: incorrect username or password..");
                   main(args);
                   return;
               }
           } else {
               System.out.println("Allow anonymous questions (0 or 1)");
               boolean AQChoice = Integer.parseInt(scanner.nextLine()) == 1;
               user = new UserController(UserController.generateId(), name, pass, AQChoice);
               UserModel.insertUser(user);
           }
           runAskMe(user);
       }
    }
}