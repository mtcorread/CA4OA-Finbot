package com.example.banktest.viewmodel;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.banktest.Activity.MainActivity;
import com.example.banktest.Activity.TransferActivity;
import com.example.banktest.database.Transactions;
import com.example.banktest.database.TransactionsDatabase;
import com.example.banktest.helpers.UIHelper;
import com.example.banktest.database.ExpenditureDatabase;
import com.example.banktest.database.User;
import com.example.banktest.database.UserDatabase;
import com.example.banktest.helpers.Message;
import com.example.banktest.repositories.TransactionRepository;
import com.example.banktest.repositories.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TransferViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Message>> messageListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> scrollToPositionLiveData = new MutableLiveData<>();

    private final MutableLiveData<String> speakLiveData = new MutableLiveData<>();
    private final MutableLiveData<CountDownLatch> latchLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> yesNoButtonsEnabledLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> yesNoButtonsVisibleLiveData = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> isSendSpeakButtonsEnabledLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> sendSpeakButtonsVisibleLiveData = new MutableLiveData<>(true);

    private final MutableLiveData<Integer> inputTypeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> clearInputFieldLiveData = new MutableLiveData<>(false);


    private final MutableLiveData<Boolean> inputTextBoxVisibleLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<String> uiBOTResponseLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> uiUSERResponseLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToMainActivity = new MutableLiveData<>();



    private User currentUser;
    private UserRepository userRepository;
    private TransactionRepository transactionRepository;

    private final ExecutorService executorService;
    private final Handler handler;
    private final UIHelper uiHelper;

    private String recipientName, accountNumber, sortCode, transferAmount, ttsAN;
    private static final int STATE_REBOOT = -1, STATE_ASK_NAME = 0, STATE_ASK_ACCOUNT_NUMBER = 1, STATE_ASK_SORT_CODE = 2,
            STATE_CONFIRMATION1 = 3, STATE_ASK_AMOUNT = 4, STATE_CONFIRMATION2 = 5, STATE_ENDING = 6, STATE_FINALISED = 7;
    private int currentState = STATE_ASK_NAME, readState = STATE_ASK_NAME;
    double totalExpenditure;


    public TransferViewModel(@NonNull Application application) {
        super(application);

        UserDatabase userDatabase = UserDatabase.getDatabase(application);
        TransactionsDatabase transactionsDatabase = TransactionsDatabase.getDatabase(application);
        ExpenditureDatabase expenditureDatabase = ExpenditureDatabase.getDatabase(application);

        // Initialize your variables here
        executorService = Executors.newSingleThreadExecutor();
        userRepository = new UserRepository(userDatabase.userDao(), transactionsDatabase.transactionDao(), expenditureDatabase.expectedExpenditureDao(), executorService);
        transactionRepository = new TransactionRepository(transactionsDatabase.transactionDao(), executorService);


        handler = new Handler(Looper.getMainLooper());
        uiHelper = new UIHelper();

        // Load current user in a background thread
        executorService.execute(() -> {
            UserDatabase udb = UserDatabase.getDatabase(application.getApplicationContext());
            currentUser = udb.userDao().findUserById(1);
            ExpenditureDatabase eedb = ExpenditureDatabase.getDatabase(application.getApplicationContext());
            totalExpenditure = eedb.expectedExpenditureDao().getTotalExpenditure();

        });
    }

    public LiveData<List<Message>> getMessageListLiveData() {
        return messageListLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<Integer> getScrollToPositionLiveData() {
        return scrollToPositionLiveData;
    }

    public LiveData<Boolean> getIsSendSpeakButtonsEnabledLiveData() { return isSendSpeakButtonsEnabledLiveData; }

    public LiveData<Boolean> getClearInputFieldLiveData() { return clearInputFieldLiveData; }

    public LiveData<String> getSpeakLiveData() { return speakLiveData; }

    public LiveData<CountDownLatch> getLatchLiveData() { return latchLiveData; }

    public void clearInputFieldDone() { clearInputFieldLiveData.setValue(false); }

    public LiveData<Boolean> getYesNoButtonsEnabledLiveData() { return yesNoButtonsEnabledLiveData; }

    public LiveData<Integer> getInputTypeLiveData() { return inputTypeLiveData; }

    public LiveData<Boolean> getYesNoButtonsVisibleLiveData() { return yesNoButtonsVisibleLiveData; }

    public LiveData<Boolean> getSendSpeakButtonsVisibleLiveData() { return sendSpeakButtonsVisibleLiveData; }

    public LiveData<Boolean> getInputTextBoxVisibleLiveData() { return inputTextBoxVisibleLiveData; }

    public LiveData<String> getUiBOTResponseLiveData() { return uiBOTResponseLiveData; }
    public LiveData<String> getUiUSERResponseLiveData() { return uiUSERResponseLiveData; }


    public LiveData<Boolean> getNavigateToMainActivity() { return navigateToMainActivity; }

    public void setLoading(boolean isLoading) {
        isLoadingLiveData.setValue(isLoading);
    }

    public void setYesNoButtonsVisible(boolean isVisible) {
        yesNoButtonsVisibleLiveData.setValue(isVisible);
    }

    public void setYesNoButtonsEnabledLiveData(boolean isEnabled) {
        yesNoButtonsEnabledLiveData.setValue(isEnabled);
    }
    public void setSendSpeakButtonsEnabledLiveData(boolean isEnabled) {
        isSendSpeakButtonsEnabledLiveData.setValue(isEnabled);
    }

    public void setUiBOTResponse(String response) {
        uiBOTResponseLiveData.setValue(response);
    }

    public void setUiUSERResponse(String response) {
        uiUSERResponseLiveData.setValue(response);
    }

    private boolean isTtsEnabled() {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("app_prefs", Application.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isTtsEnabled", true);
    }

    public void onTransferComplete() {
        navigateToMainActivity.postValue(true); // Use postValue to update LiveData from a background thread
    }

    public void doneNavigating() {
        navigateToMainActivity.setValue(false);
    }



    public void handleUserInput(String input) {

        setUiUSERResponse(input);
        clearInputFieldLiveData.setValue(true);
        new Handler().postDelayed(() -> botReplies(input), 500);

    }

    private void inputTypeRequired(String mode) {
        switch (mode) {
            case "Text":
                setYesNoButtonsVisible(false);
                sendSpeakButtonsVisibleLiveData.setValue(true);
                inputTextBoxVisibleLiveData.setValue(true);

                inputTypeLiveData.setValue(InputType.TYPE_CLASS_TEXT);
                break;
            case "Number":
                setYesNoButtonsVisible(false);
                sendSpeakButtonsVisibleLiveData.setValue(true);
                inputTextBoxVisibleLiveData.setValue(true);
                inputTypeLiveData.setValue(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case "YesNo":
                setYesNoButtonsVisible(true);
                sendSpeakButtonsVisibleLiveData.setValue(false);
                inputTextBoxVisibleLiveData.setValue(false);
                inputTypeLiveData.setValue(InputType.TYPE_NULL);
                break;
        }
    }

    private void botReplies(String input) {
        String UIresponse;
        String ttsResponse = "";
        String extractedNumbers;
        boolean doubleMsg = false;

        UIresponse = uiHelper.botRequestDetailsHandler(currentState);

        switch (currentState) {
            case STATE_ASK_NAME:
                currentState = STATE_ASK_ACCOUNT_NUMBER;
                break;

            case STATE_ASK_ACCOUNT_NUMBER:
                if (isValidName(input)) {
                    recipientName = input;
                    inputTypeRequired("Number");
                    currentState = STATE_ASK_SORT_CODE;
                } else {
                    UIresponse = "Sorry, this is not a valid name.";
                    doubleMsg = true;
                    readState = STATE_ASK_NAME;
                }
                break;

            case STATE_ASK_SORT_CODE:
                extractedNumbers = extractNumbers(input, "AN");
                if (isValidAccountNumber(extractedNumbers)) {
                    accountNumber = formatNumbers(extractedNumbers, "AN");
                    ttsAN = prepareNumberForTTS(accountNumber, "AN");

                    inputTypeRequired("Number");
                    currentState = STATE_CONFIRMATION1;
                } else {
                    UIresponse = "Sorry, this is not a valid 8-digit account number.";
                    doubleMsg = true;
                    readState = STATE_ASK_ACCOUNT_NUMBER;
                }
                break;

            case STATE_CONFIRMATION1:
                extractedNumbers = extractNumbers(input, "SC");
                if (isValidSortCode(extractedNumbers)) {
                    sortCode = formatNumbers(extractedNumbers, "SC");
                    String ttsSC = prepareNumberForTTS(sortCode, "SC");

                    UIresponse = "You are transferring to " + recipientName + " with account number " + accountNumber + " and sort code " + sortCode + ". Is this correct?";
                    ttsResponse = "You are transferring to " + recipientName + " with account number " + ttsAN + " and sort code " + ttsSC + ". Is this correct?";

                    inputTypeRequired("YesNo");
                    currentState = STATE_ASK_AMOUNT;

                } else {
                    UIresponse = "Sorry, this is not a valid 6-digit sort code.";
                    doubleMsg = true;
                    readState = STATE_ASK_SORT_CODE;
                }
                break;

            case STATE_ASK_AMOUNT:
                if (input.equalsIgnoreCase("yes")) {
                    UIresponse = "Enter the amount of money you would like to transfer.";
                    inputTypeRequired("Number");
                    currentState = STATE_CONFIRMATION2;
                } else {
                    UIresponse = "No problem! Let's start over.";
                    doubleMsg = true;
                    setSendSpeakButtonsEnabledLiveData(false);
                    inputTypeRequired("Text");
                    currentState = STATE_ASK_ACCOUNT_NUMBER;
                    readState = STATE_ASK_NAME;
                }
                break;

            case STATE_CONFIRMATION2:
                String extractedAmount = extractAmountWithPence(input);
                if (isValidAmount(extractedAmount)) {
                    transferAmount = extractedAmount;

                    BigDecimal amountToTransfer = new BigDecimal(transferAmount);
                    BigDecimal currentBalance = new BigDecimal(currentUser.getBalance());

                    if (currentBalance.compareTo(amountToTransfer) >= 0) {
                        if (currentBalance.subtract(amountToTransfer).compareTo(BigDecimal.valueOf(totalExpenditure)) >= 0) {
                            UIresponse = "You are about to transfer " + transferAmount + " GBP to " + recipientName + ". Is this correct?";
                            inputTypeRequired("YesNo");
                        } else {
                            UIresponse = "You are about to transfer " + transferAmount + " GBP to " + recipientName + ". \n\nWarning: " +
                                    "This will reduce your balance to less than your anticipated expenses for the month. Would you like to proceed?";
                            inputTypeRequired("YesNo");
                            setYesNoButtonsEnabledLiveData(false);

                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                setYesNoButtonsEnabledLiveData(true);
                            }, 3000);
                        }
                        currentState = STATE_ENDING;
                    } else {
                        UIresponse = "Sorry, there are insufficient funds for this transfer.";
                        doubleMsg = true;
                        readState = STATE_ASK_AMOUNT;
                    }
                } else {
                    UIresponse = "Sorry, this is not a valid amount.";
                    doubleMsg = true;
                    readState = STATE_ASK_AMOUNT;
                }
                break;

            case STATE_ENDING:
                if (input.equalsIgnoreCase("yes")) {

                    BigDecimal amount = new BigDecimal(transferAmount);
                    UIresponse = "Thank you, your transfer of " + transferAmount + " GBP to " + recipientName + " is being processed. Would you like to make another transfer?";
                    inputTypeRequired("YesNo");
                    currentState = STATE_FINALISED;

                    currentUser.subtractFromBalance(amount);
                    userRepository.updateUser(currentUser);

                    Transactions newTransaction = new Transactions(recipientName, accountNumber, sortCode, transferAmount, System.currentTimeMillis());
                    transactionRepository.insertTransaction(newTransaction);

                } else {
                    UIresponse = "No problem! Let's go back.";
                    doubleMsg = true;
                    setSendSpeakButtonsEnabledLiveData(false);
                    inputTypeRequired("Number");
                    currentState = STATE_CONFIRMATION2;
                    readState = STATE_ASK_AMOUNT;
                }
                break;

            case STATE_FINALISED:
                if (input.equalsIgnoreCase("yes")) {
                    doubleMsg = true;
                    inputTypeRequired("Text");
                    readState = STATE_ASK_NAME;
                    currentState = STATE_ASK_ACCOUNT_NUMBER;
                } else {
                    String finalResponse = "Understood. I'll bring you back to the main menu.";
                    currentState = STATE_REBOOT;
                    setYesNoButtonsEnabledLiveData(false);
                    setUiBOTResponse(finalResponse);

                    if (isTtsEnabled()) {
                        CountDownLatch latch = new CountDownLatch(1);
                        speakLiveData.setValue(finalResponse);
                        latchLiveData.setValue(latch);
                        new Thread(() -> {
                            try {
                                latch.await();  // Wait for TTS to finish
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            onTransferComplete();  // Notify the ViewModel
                        }).start();
                    } else {
                        new Handler(Looper.getMainLooper()).postDelayed(this::onTransferComplete, estimateTtsDuration(finalResponse));
                    }

                }
                break;
        }

        final String finalResponse = UIresponse;
        final String finalResponse2 = uiHelper.botRequestDetailsHandler(readState);
        final boolean finaldoubleMsg = doubleMsg;
        final String finalttsResponse = ttsResponse.isEmpty() ? finalResponse : ttsResponse;

        if (currentState != STATE_REBOOT) {
            new Handler(Looper.getMainLooper()).post(() -> {
                // Immediately update the UI with the first response
                setUiBOTResponse(finalResponse);

                if (isTtsEnabled()) {
                    // Handle TTS operation separately
                    CountDownLatch latch = new CountDownLatch(1);
                    speakLiveData.setValue(finalttsResponse); // Use finalttsResponse for TTS if not empty
                    latchLiveData.setValue(latch);

                    if (finaldoubleMsg) {
                        // Separate block for handling the second response after TTS finishes
                        new Thread(() -> {
                            try {
                                latch.await();  // Wait for TTS to finish
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    setSendSpeakButtonsEnabledLiveData(true);
                                    setUiBOTResponse(finalResponse2);
                                    speakLiveData.setValue(finalResponse2);
                                });
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                    }
                } else {
                    // Handle the second response if TTS is not enabled
                    if (finaldoubleMsg) {
                        setSendSpeakButtonsEnabledLiveData(true);
                        setUiBOTResponse(finalResponse2);
                    }
                }
            });
        }

    }

    private int estimateTtsDuration(String text) {
        final int WORDS_PER_MINUTE = 150; // Average TTS reading speed
        final int WORDS_PER_SECOND = WORDS_PER_MINUTE / 60;
        int wordCount = text.split("\\s+").length;
        return (wordCount * 1000) / WORDS_PER_SECOND; // Duration in milliseconds
    }

    public String extractNumbers(String input, String mode) {
        // Remove all non-digit characters
        String digits = input.replaceAll("\\D+", "");

        if (mode.equals("SC")) {
            digits = digits.substring(0, Math.min(digits.length(), 6));
        } else if (mode.equals("AN")) {
            digits = digits.substring(0, Math.min(digits.length(), 8));
        }
        return digits;
    }

    public String formatNumbers(String input, String mode){
        if(mode.equals("SC")){
            return input.replaceAll("(\\d{2})(?=\\d)", "$1-");
        }
        else {
            return input;
        }
    }
    public String prepareNumberForTTS(String input, String mode) {
        if (mode.equals("SC")) {
            // For Sort Code: Transform "xx-xx-xx" into "xx xx xx" and ensure it's read as whole numbers
            return input.replaceAll("-", " ");
        } else if (mode.equals("AN")) {
            // For Account Number: Ensure each digit is read individually
            return input.replaceAll("(\\d)", "$1 ").trim();
        } else {
            // Default case if the mode is neither "SC" nor "AC"
            return input;
        }
    }

    public String extractAmountWithPence(String input) {
        if (input.contains("$") || input.contains("€") || input.contains("dollars") || input.contains("euros")) {
            return "";
        }

        // Check for "x pounds and y pence" or "x pounds with y pence" pattern
        Pattern patternFull = Pattern.compile("^(\\d+)\\s*(pounds|gbp|british pounds|quid)\\s*(and|with)\\s*(\\d+)\\s*pence$", Pattern.CASE_INSENSITIVE);
        Matcher matcherFull = patternFull.matcher(input);

        if (matcherFull.find()) {
            String pounds = matcherFull.group(1);
            String pence = matcherFull.group(4);
            if (pence != null) {
                return pounds + "." + (pence.length() == 1 ? "0" + pence : pence);
            } else {
                return pounds + ".00"; // Assuming you want to default to "00" if pence is null
            }        }

        // Check for spoken form like "three fifty" for 3.50
        Pattern patternSpoken = Pattern.compile("^(one|two|three|four|five|six|seven|eight|nine)?\\s*(hundred)?\\s*(ten|twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety)?\\s*(one|two|three|four|five|six|seven|eight|nine)?\\s*(fifty|forty|thirty|twenty|ten)?$", Pattern.CASE_INSENSITIVE);
        Matcher matcherSpoken = patternSpoken.matcher(input);

        if (matcherSpoken.find()) {
            // Convert spoken words to numbers
            String poundsPart = Optional.ofNullable(matcherSpoken.group(1)).map(this::wordToNumber).orElse("0");
            String tensPart = Optional.ofNullable(matcherSpoken.group(3)).map(this::wordToNumber).orElse("0");
            String unitsPart = Optional.ofNullable(matcherSpoken.group(4)).map(this::wordToNumber).orElse("0");
            String pencePart = Optional.ofNullable(matcherSpoken.group(5)).map(this::wordToNumber).orElse("0");

            return poundsPart + "." + tensPart + unitsPart + pencePart;
        }

        // Fallback to original amount extraction if no pattern matches
        return extractAmount(input);
    }

    private String wordToNumber(String word) {
        switch (word.toLowerCase()) {
            case "one":
                return "1";
            case "two":
                return "2";
            case "three":
                return "3";
            case "four":
                return "4";
            case "five":
                return "5";
            case "six":
                return "6";
            case "seven":
                return "7";
            case "eight":
                return "8";
            case "nine":
                return "9";
            case "ten":
                return "10";
            case "twenty":
                return "20";
            case "thirty":
                return "30";
            case "forty":
                return "40";
            case "fifty":
                return "50";
            case "sixty":
                return "60";
            case "seventy":
                return "70";
            case "eighty":
                return "80";
            case "ninety":
                return "90";
            default:
                return "0";
        }
    }

    public String extractAmount(String input) {
        // Reject input if it contains other currency symbols or does not contain explicit pound-related keywords
        if (input.contains("$") || input.contains("€")) {
            return "";
        }
        // Updated method for extracting amount, allowing for commas as thousand separators
        Pattern pattern = Pattern.compile("(\\d+(,\\d{3})*(\\.\\d+)?)(\\s*(pounds|gbp|british pounds|quid))?\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String matchedAmount = matcher.group(1);
            // Remove any commas from the matched amount before returning
            if (matchedAmount != null) {
                return matchedAmount.replace(",", "");
            } else {
                // Handle the case where matchedAmount is null, maybe return a default value or throw an exception
                return ""; // or any other default/fallback value you see fit
            }
        } else {
            return "";
        }
    }

    private boolean isValidName(String name) {
        return name.matches("[A-Za-z ]+");
    }

    private boolean isValidAccountNumber(String accountNumber) {
        // Validate an 8-digit account number
        return accountNumber.matches("\\d{8}");
    }

    private boolean isValidSortCode(String sortCode) {
        // Validate a 6-digit sort code
        return sortCode.matches("\\d{6}");
    }

    private boolean isValidAmount(String amount) {
        try {
            double value = Double.parseDouble(amount);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }



}
