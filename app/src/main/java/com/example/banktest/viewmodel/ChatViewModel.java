package com.example.banktest.viewmodel;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.banktest.database.ExpenditureDatabase;
import com.example.banktest.database.TransactionsDatabase;
import com.example.banktest.helpers.OpenAIHelper;
import com.example.banktest.helpers.Message;
import com.example.banktest.database.User;
import com.example.banktest.database.UserDatabase;
import com.example.banktest.repositories.QueryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;

public class ChatViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Message>> messageListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<String> toastMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> scrollToPositionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isButtonsEnabledLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> isSkipBtnVisibleLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> clearInputFieldLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isWelcomeVisibleLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<String> speakLiveData = new MutableLiveData<>();
    private final MutableLiveData<CountDownLatch> latchLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> uiBOTResponseLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> uiUSERResponseLiveData = new MutableLiveData<>();
    private MutableLiveData<String> queryResult = new MutableLiveData<>();;


    private User currentUser;
    private final ExecutorService executorService;
    private final Handler handler;
    private final OpenAIHelper openAIHelper;
    private Thread processingThread;
    private volatile boolean isSplit = false;
    private String completeResponse, currentQuestion = "";
    private boolean isBankRelated = false, prevType = false;
    private QueryRepository queryRepository;


    public ChatViewModel(@NonNull Application application) {
        super(application);
        queryRepository = new QueryRepository(application);

        // Initialize your variables here
        openAIHelper = new OpenAIHelper(new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).build(), "sk-yourkey");
        executorService = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        // Load current user in a background thread
        executorService.execute(() -> {
            UserDatabase udb = UserDatabase.getDatabase(application.getApplicationContext());
            currentUser = udb.userDao().findUserById(1);

        });

    }

    public LiveData<List<Message>> getMessageListLiveData() {
        return messageListLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getToastMessageLiveData() {
        return toastMessageLiveData;
    }

    public LiveData<Integer> getScrollToPositionLiveData() {
        return scrollToPositionLiveData;
    }

    public LiveData<Boolean> getIsButtonsEnabledLiveData() { return isButtonsEnabledLiveData; }

    public LiveData<Boolean> getIsSkipBtnVisibleLiveData() { return isSkipBtnVisibleLiveData; }

    public LiveData<Boolean> getClearInputFieldLiveData() { return clearInputFieldLiveData; }

    public LiveData<Boolean> getIsWelcomeVisibleLiveData() { return isWelcomeVisibleLiveData; }

    public LiveData<String> getSpeakLiveData() { return speakLiveData; }

    public LiveData<CountDownLatch> getLatchLiveData() { return latchLiveData; }

    public LiveData<String> getUiBOTResponseLiveData() { return uiBOTResponseLiveData; }

    public LiveData<String> getUiUSERResponseLiveData() { return uiUSERResponseLiveData; }

    public String getCompleteResponse() {
        return completeResponse;
    }

    public void clearInputFieldDone() { clearInputFieldLiveData.setValue(false); }

    public void setUiBOTResponse(String response) {
        uiBOTResponseLiveData.setValue(response);
    }

    public void setUiUSERResponse(String response) {
        uiUSERResponseLiveData.setValue(response);
    }

    public void setSkipBtnVisibleLiveData(boolean isVisible) { isSkipBtnVisibleLiveData.setValue(isVisible); }

    private boolean isTtsEnabled() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("app_prefs", Application.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isTtsEnabled", true);
    }

    public void retrieveAndCreate(String assistantId) {
        isLoadingLiveData.setValue(true);
        isButtonsEnabledLiveData.setValue(false);

        openAIHelper.retrieveAndCreate(assistantId, () -> {
            handler.post(() -> {
                isLoadingLiveData.setValue(false);
                isButtonsEnabledLiveData.setValue(true);
                Log.d("ChatViewModel", "retrieveAndCreate complete. Thread ID: " + openAIHelper.getThreadId());
            });
        });
    }

    public void handleUserInput(String input) {
        String question;
        if (isSplit) {
            removeLastChunkAndDisplayEntireMessage(completeResponse);
        }

        if (input != null && !input.trim().isEmpty()) {
            question = input.trim();
        } else {
            question = "";
            clearInputFieldLiveData.setValue(true);
        }

        if (!question.isEmpty()) {
            setUiUSERResponse(question);
            Log.d("HandleUserInput", "Received input: " + question);
            currentQuestion = question;
            isBankRelatedFilter(question);
            isWelcomeVisibleLiveData.setValue(false); // Hide welcome message

        }
    }

    public void handleBotOutput(String response) {
        Log.d("RESPUESTA FINAL", response);
        completeResponse = response;

        final int MAX_LENGTH = 200; // Maximum characters per chunk

        if(!isTtsEnabled()){
            removeLastChunkAndDisplayEntireMessage(completeResponse);
        }else {
            // Split response into chunks
            processingThread = new Thread(() -> {
                try {
                    String[] lines = response.split("(?<=\\.)|\\n"); // Splits response into lines when there is a period or a line jump.
                    isSplit = lines.length > 1;
                    final boolean[] isFirstRun = {true}; // Array to hold the first-run flag
                    for (String line : lines) {
                        int start = 0; // We start at character 0
                        while (start < line.length()) { // While start character is less than the size of the line
                            int end = (start + MAX_LENGTH < line.length()) ?
                                    (isSplit = true) && (end = line.lastIndexOf(' ', start + MAX_LENGTH)) != -1 && end > start ? end : start + MAX_LENGTH :
                                    line.length();
                            end = (end <= start) ? Math.min(start + MAX_LENGTH, line.length()) : end;

                            String chunk = line.substring(start, end).trim();

                            if (!chunk.isEmpty()) {
                                final String finalChunk = chunk;

                                // Synchronization mechanism
                                final CountDownLatch latch = new CountDownLatch(1);

                                handler.post(() -> {
                                    List<Message> messageList = messageListLiveData.getValue();
                                    if (messageList != null && !messageList.isEmpty()) {
                                        messageList.set(messageList.size() - 1, new Message(finalChunk, Message.SENT_BY_BOT));
                                        messageListLiveData.postValue(messageList);
                                        if (isSplit) {
                                            setSkipBtnVisibleLiveData(true);
                                        }
                                    }
                                    // Check if TTS is enabled before speaking
                                    if (isTtsEnabled()) {
                                        speakLiveData.setValue(finalChunk);
                                        latchLiveData.setValue(latch);
                                    } else {
                                        latch.countDown(); // If TTS is not enabled, immediately count down the latch
                                    }
                                });
                                latch.await(); // Wait for the TTS to finish speaking or immediately proceed if TTS is disabled
                            }
                            start = end + 1;
                        }
                        isFirstRun[0] = false; // Update the first-run flag to false after the first iteration
                    }
                    // Once all chunks have been displayed, remove the last chunk and add the full response
                    if (isSplit) {
                        removeLastChunkAndDisplayEntireMessage(response);
                    }
                } catch (InterruptedException e) {
                    // Handle thread interruption
                    Log.e("ERROR_Thread_Interrupt", "Thread Interrupted", e);
                    Thread.currentThread().interrupt();
                }
            });
            processingThread.start();
        }
    }

    public void removeLastChunkAndDisplayEntireMessage(String completeResponse) {
        // Interrupt the processing thread
        if (processingThread != null && processingThread.isAlive()) {
            processingThread.interrupt();
        }

        handler.post(() -> {
            List<Message> messageList = messageListLiveData.getValue();
            if (messageList != null && !messageList.isEmpty()) {
                messageList.remove(messageList.size() - 1); // Remove the last chunk
                messageListLiveData.postValue(messageList);
            }

            // Add the complete response as a new message
            setUiBOTResponse(completeResponse);
            setSkipBtnVisibleLiveData(false);
            isSplit = false;
        });
    }


    // Entry point for checking if the question is bank-related
    void isBankRelatedFilter(String question) {
        setUiBOTResponse("Typing...");
        String prompt = createBankRelatedPrompt(question);
        openAIHelper.makeChatGPTCall("system", prompt, "gpt-3.5-turbo-0125", result -> {
            handleBankRelatedResponse(question, result);
        });
        prevType = isBankRelated;
        System.out.println("prevType: " + prevType);
    }

    // Helper method to create the prompt for bank-related questions
    private String createBankRelatedPrompt(String question) {
        return "You are a bank assistant in a banking application, does this question relate in any way to any payment or banking related query or functioning of this app such as asking for transaction details, balances, future payments (to people or services), expected expenditure, money related, etc? Just answer 'yes' or 'no'. Question: \"" + question + "\"";
    }

    // Handle the response for bank-related check
    private void handleBankRelatedResponse(String question, String result) {
        String lowercaseResult = result.toLowerCase();
        if (lowercaseResult.contains("yes")) {
            Log.d("is bank related", "True");
            isBankRelated = true;
            subjectChanged(isBankRelated);
            checkBankingQuestionType(question);
        } else if (lowercaseResult.contains("no")) {
            Log.d("is bank related", "False");
            isBankRelated = false;
            subjectChanged(isBankRelated);
            filter1(question);
        }
    }

    // Check the type of banking question (user type or transaction type)
    private void checkBankingQuestionType(String question) {
        String prompt = createBankingQuestionTypePrompt(question);
        openAIHelper.makeChatGPTCall("user", prompt, "gpt-3.5-turbo-0125", result -> {
            handleBankingQuestionTypeResponse(question, result);
        });
    }

    // Helper method to create the prompt for banking question type check
    private String createBankingQuestionTypePrompt(String question) {
        return "I want to identify if this is a 'user type' banking question. A 'user type' is when the user asks about their balance or if they ask about their account number. Anything else (like asking about transactions, transfers or future payments) will fall into a different category. Is this a 'user type' question, yes or no?: \"" + question + "\"";
    }

    // Handle the response for banking question type check
    private void handleBankingQuestionTypeResponse(String question, String result2) {
        String lowercaseResult2 = result2.toLowerCase();
        Log.d("entró a filtro de tipo de query", "SI");
        if (lowercaseResult2.contains("yes")) {
            System.out.println("TYPE OF BANKING QUESTION?: USER");
            respondToUserTypeQuestion(question);
        } else {
            System.out.println("TYPE OF BANKING QUESTION?: TRANSFER/EXPENSES");
            respondToTransactionTypeQuestion(question);
        }
    }

    // Respond to user type banking question
    private void respondToUserTypeQuestion(String question) {
        String instr = "For User info, return the needed method specified in 'USER ACCOUNT INSTRUCTIONS'. NEVER access 'Banking instructions'.";
        openAIHelper.sendMessage(question, instr, 0f, response -> evaluateResponse(response));
    }

    // Respond to transaction type banking question
    private void respondToTransactionTypeQuestion(String question) {
        String instr = "For queries about transactions and payments/expenditure, write back a SQL Query to search for the values, found in 'Banking Instructions'.";
        openAIHelper.sendMessage(question, instr, 0f, response -> evaluateResponse(response));
    }

    // Notify if the subject has changed and create a new thread if needed
    private void subjectChanged(boolean isBankRelated) {
        if (prevType != isBankRelated) {
            System.out.println("Subject Changed!!!");
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    openAIHelper.createThread();
                } catch (Exception e) {
                    Log.e("CreateThread", "Error in createThread", e);
                }
            });
        }
    }

    // Filter 1 for general questions
    void filter1(String question) {
        String prompt = createSensitiveTopicPrompt(question);
        openAIHelper.makeChatGPTCall("system", prompt, "gpt-3.5-turbo-0125", result -> handleSensitiveTopicResponse(question, result));
    }

    // Create a prompt for sensitive topic filter
    private String createSensitiveTopicPrompt(String question) {
        return "This is a filter for general purposes questions. Don't allow any kind of sensitive topic. Does the following question include religion, beliefs, sexual orientation, health, medical question, " +
                "self-harm, suicide, violence, hate crimes, etc? Just reply \"yes\" or \"no\". Question: \"" + question + "\"";
    }

    // Handle the response for sensitive topic filter
    private void handleSensitiveTopicResponse(String question, String result) {
        String lowercaseResult = result.toLowerCase();
        if (lowercaseResult.contains("yes")) {
            Log.d("API", "Filter 1");
            handleBotOutput("Sorry, I cannot assist you with this.");
        } else if (lowercaseResult.contains("no")) {
            Log.d("API", "Filter 1");
            filter2(question);
        }
    }

    // Filter 2 for checking personal details
    void filter2(String question) {
        String prompt = createPersonalDetailsPrompt(question);
        openAIHelper.makeChatGPTCall("system", prompt, "gpt-3.5-turbo-0125", result -> handlePersonalDetailsResponse(question, result));
    }

    // Create a prompt for personal details filter
    private String createPersonalDetailsPrompt(String question) {
        return "Does the following question include any kind of personal details or private information about the person asking or their family? This includes location, " +
                "names, date of birth, relationships, etc. Just reply \"yes\" or \"no\". Question: \"" + question + "\"";
    }

    // Handle the response for personal details filter
    private void handlePersonalDetailsResponse(String question, String result) {
        String lowercaseResult = result.toLowerCase();
        if (lowercaseResult.contains("yes")) {
            Log.d("API", "Filter 2");
            handleBotOutput("Sorry, I cannot assist you with this.");
        } else if (lowercaseResult.contains("no")) {
            Log.d("API", "Filter 2");
            handleGeneralResponse(question);
        }
    }

    // Handle the general response for non-sensitive, non-personal questions
    private void handleGeneralResponse(String question) {
        String instr = "I want to have a security filter on a general purpose chatbot, that never mentions things that happen in the background like document/file searching." +
                "You will act as an assistant based in the UK. Don't mention you are an AI. Answer to this question concisely (less than 100 words). Don't include any formatting (bolds or italics)." +
                "Recipes and small talk are okay, most topics will be fine, but if any question includes topics of religion, beliefs, sexual orientation, " +
                "medical advice, self-harm, suicide, violence, hate crimes, etc. just reply \"Sorry, I cannot assist you with this\"";
        openAIHelper.sendMessage(question, instr, 1f, this::evaluateResponse);
    }

    // Main method to evaluate the response and act accordingly
    private void evaluateResponse(String response) {
        Log.d("Response", response);
        String extractedMethod = extractFirstMethodCall(response);
        logExtraction("Extracted method", extractedMethod);

        String extractedSQL = extractSqlQuery(response);
        logExtraction("Extracted SQL Query", extractedSQL);

        if (extractedMethod != null) {
            processMethodCall(extractedMethod);
        } else if (extractedSQL != null) {
            processSQLQuery(extractedSQL);
        } else {
            handleBotOutput(response);
        }
    }

    private void logExtraction(String label, String value) {
        Log.d(label, value != null ? value : "null");
    }

    // Process the extracted method call
    private void processMethodCall(String extractedMethod) {
        System.out.println(extractedMethod);
        String result = User.invokeGetter(currentUser, extractedMethod);
        System.out.println(result);
        if (!result.isEmpty() && !result.equals("null") && !result.equals(" ")){
            handler.post(() -> requestGPTTemplate(result, true, null));
        }else{
            handler.post(() -> requestGPTTemplate(result, false, null));
        }
    }

    // Process the extracted SQL query
    private void processSQLQuery(String extractedSQL) {
        System.out.println("Extracted SQL: " + extractedSQL);

        int queryType = identifyQueryType(extractedSQL);
        if (queryType != 0) {
            queryRepository.executeQuery(extractedSQL, queryType, new QueryRepository.QueryCallback() {
                @Override
                public void onQueryCompleted(String result) {
                    handleQueryResult(result);
                }

                @Override
                public void onQueryFailed(String error) {
                    handleQueryFailure(error);
                }
            });
        }
    }

    // Extract the first method call from the response
    public static String extractFirstMethodCall(String input) {
        String regex = "\\b\\w+\\.\\w+\\(\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String methodFound = matcher.group();
            if (methodFound.endsWith("()")) {
                methodFound = methodFound.substring(0, methodFound.length() - 2);
            }
            int lastDotIndex = methodFound.lastIndexOf('.');
            if (lastDotIndex != -1) {
                return methodFound.substring(lastDotIndex + 1);
            }
        }
        return null;
    }

    // Extract SQL query from the text
    public static String extractSqlQuery(String text) {
        String sqlPattern = "(SELECT.*?;)";
        Pattern pattern = Pattern.compile(sqlPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String multiLineQuery = Objects.requireNonNull(matcher.group(1)).trim();
            return multiLineQuery.replaceAll("\\s+", " ");
        }
        return null;
    }

    // Identify the type of SQL query
    public static int identifyQueryType(String sqlQuery) {
        if (sqlQuery == null) {
            return 0;
        }

        String lowerCaseQuery = sqlQuery.toLowerCase();
        if (lowerCaseQuery.contains("transactions")) {
            return 1;
        } else if (lowerCaseQuery.contains("expected_expenditure")) {
            return 2;
        }
        return 0;
    }

    private void handleQueryResult(String result) {
        if (!result.isEmpty() && !result.equals("null") && !result.equals(" ")) {
            System.out.println("Result obtained by the SQL Query: " + result);
            handler.post(() -> requestGPTTemplate(result, true, finalResponse -> {
                Log.d("Final GPT Response", finalResponse);
                queryResult.setValue(finalResponse);
            }));
        } else {
            System.out.println("No results obtained from the SQL query.");

            handler.post(() -> requestGPTTemplate(result, false, finalResponse -> {
                Log.d("Final GPT Response", finalResponse);
            }));
        }
    }

    private void handleQueryFailure(String error) {
        System.out.println("Error executing query: " + error);
        handler.post(() -> handleBotOutput("Sorry, I couldn't find the answer right now. Please try again asking with different words."));
    }

    // Request GPT template
    private void requestGPTTemplate(String result, Boolean knows, Consumer<String> onResponse) {
        System.out.println("ENTRY TO REQUESTGPTTEMPLATE: SUCCESSFUL");
        String prompt = createGPTTemplatePrompt(knows);
        openAIHelper.makeChatGPTCall("user", prompt, "gpt-4o", answer -> saveResponseByGPT(answer, result));
    }

    private String createGPTTemplatePrompt(Boolean knows) {
        if (knows) {
            return "Generate a straight-forward template of what you would respond to a question if you knew the answer and" +
                    " replace the answer with [insert]: \"" + currentQuestion + "\" . It is imperative that the solution is left blank, even if you think you already know the answer. " +
                    "You can only have one blank space per template. Currencies managed are always GBP, so add GBP after the blank if the blank should be a number representing money. " +
                    "Generate the template with '<Template> </Template>' tags.";
        } else {
            return "I want to receive a concise answer to a user question. This answer is role-playing as a banking assistant, so ability to access information is assumed. " +
                    "However, the user might have had a typo in the data or the data might not exist and that's why you couldn't find it. More information is not needed. " +
                    "If you refer to a subject of the question, always do it with quotation marks and respecting lowercases. Give suggestion to the user so you can find the correct answer. \"" + currentQuestion + "\"";
        }
    }

    // Save the response generated by GPT
    private void saveResponseByGPT(String answer, String result) {
        System.out.println("ENTRY TO SAVERESPONSEBYGPT: SUCCESSFUL");
        String finalResponse = fillTemplate(answer, result);
        handleBotOutput(finalResponse);
    }

    private String fillTemplate(String answer, String result) {
        String regex = "<Template>(.*?)</Template>";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(answer);

        if (matcher.find()) {
            String template = matcher.group(1).trim();
            return template.replace("[insert]", result);
        } else {
            return answer.replace("[insert]", result);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
