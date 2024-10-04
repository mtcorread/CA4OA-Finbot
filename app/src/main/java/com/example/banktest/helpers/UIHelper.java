package com.example.banktest.helpers;

import android.app.Activity;
import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class UIHelper {


    public UIHelper(){

    }

    // Method to hide the keyboard
    public void hideKeyboard(Activity activity) {
        View currentFocusView = activity.getCurrentFocus();
        if (currentFocusView != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocusView.getWindowToken(), 0);
        }
    }

    // Method to show the keyboard and request focus on a specific EditText
    public void showKeyboardAndRequestFocus(Activity activity, EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public void yesnoHandler(String mode, Button no, Button yes, EditText textbox, Button send, Button speak){
        switch (mode) {
            case "hide":
                no.setVisibility(View.GONE);
                yes.setVisibility(View.GONE);
                textbox.setVisibility(View.VISIBLE);
                send.setVisibility(View.VISIBLE);
                speak.setVisibility(View.VISIBLE);
                break;
            case "show":
                no.setVisibility(View.VISIBLE);
                yes.setVisibility(View.VISIBLE);
                textbox.setVisibility(View.GONE);
                send.setVisibility(View.GONE);
                speak.setVisibility(View.GONE);
                break;
            case "disable":
                no.setEnabled(false);
                yes.setEnabled(false);
                break;
            case "enable":
                no.setEnabled(true);
                yes.setEnabled(true);
                break;
        }
    }
    public void sendspeakEnabler(String mode, Button send, Button speak){
        if (mode.equals("disable")){
            send.setEnabled(false);
            speak.setEnabled(false);
        } else if (mode.equals("enable")) {
            send.setEnabled(true);
            speak.setEnabled(true);
        }
    }

    public void inputTypeRequired(String mode, EditText textBox, Button noBtn, Button yesBtn, Button sendBtn, Button speakBtn, Activity activity){
        switch (mode) {
            case "Text":
                yesnoHandler("hide", noBtn, yesBtn, textBox,sendBtn,speakBtn);
                textBox.setInputType(InputType.TYPE_CLASS_TEXT);
                showKeyboardAndRequestFocus(activity, textBox);
                break;
            case "Number":
                yesnoHandler("hide", noBtn, yesBtn, textBox,sendBtn,speakBtn);
                textBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                showKeyboardAndRequestFocus(activity, textBox);
                break;
            case "YesNo":
                hideKeyboard(activity);
                yesnoHandler("show", noBtn, yesBtn, textBox,sendBtn,speakBtn);
                break;
            default:

                break;
        }
    }

    public String botRequestDetailsHandler(int curState){
        String BotResponse = "";
        switch (curState){
            case 0: // ASK NAME
                BotResponse = "Please enter the name of the person you would like to transfer money to.";
                break;
            case 1: // ASK ACCOUNT NUMBER
                BotResponse = "What is the account number?";
                break;
            case 2: // ASK SORT CODE
                BotResponse = "What is the sort code?";
                break;
            case 4: // ASK FOR AMOUNT
                BotResponse = "Enter the amount of money you would like to transfer.";
                break;

        }

        return BotResponse;
    }

}
