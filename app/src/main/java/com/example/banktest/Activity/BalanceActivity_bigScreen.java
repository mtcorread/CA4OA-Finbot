package com.example.banktest.Activity;


import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banktest.database.ExpectedExpenditure;
import com.example.banktest.database.ExpenditureDatabase;
import com.example.banktest.database.Transactions;
import com.example.banktest.database.TransactionsDatabase;
import com.example.banktest.database.User;
import com.example.banktest.database.UserDatabase;
import com.example.banktest.R;
import com.example.banktest.helpers.ExpensesAdapter;
import com.example.banktest.helpers.ExpensesAdapter_bigScreen;
import com.example.banktest.helpers.TransactionAdapter;
import com.example.banktest.helpers.TransactionAdapter_bigScreen;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BalanceActivity_bigScreen extends BaseActivity {
    private User currentUser;
    TextView tvBalance, tvBalanceTitle, tvTranHist, tvExpectedExp;
    private ExecutorService executor;
    private RecyclerView transactionRecyclerView, expensesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
        tvBalance = findViewById(R.id.balanceNum);
        tvBalanceTitle = findViewById(R.id.balanceTitle);
        tvTranHist = findViewById(R.id.transactionHistoryName);
        tvExpectedExp = findViewById(R.id.expectedExpName);

        FrameLayout transactionContainer = findViewById(R.id.transactionContainer);
        transactionRecyclerView = new RecyclerView(this);
        transactionContainer.addView(transactionRecyclerView);
        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FrameLayout expensesContainer = findViewById(R.id.expectedExpContainer);
        expensesRecyclerView = new RecyclerView(this);
        expensesContainer.addView(expensesRecyclerView);
        expensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            UserDatabase udb = UserDatabase.getDatabase(getApplicationContext());
            currentUser = udb.userDao().findUserById(1);
            runOnUiThread(() -> {
                if (currentUser != null) {
                    updateUIWithUserNameAndBalance(currentUser);
                } else {
                    tvBalance.setText("-");
                }
            });

            fetchAndDisplayTransactionHistory(transactionRecyclerView);
            fetchAndDisplayExpenses(expensesRecyclerView);
        });


    }

    public void CreateTransactionPopupWindow(Transactions transaction) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.popup_trans_detail, findViewById(android.R.id.content), false);
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);
        ScrollView layout = findViewById(R.id.sv);

        ImageButton backBtn = popUpView.findViewById(R.id.backButton);

        // Populate the detailed view with transaction data
        TextView recipientNameTextView = popUpView.findViewById(R.id.actualNameTV);
        TextView accountNumberTextView = popUpView.findViewById(R.id.actualAccountNumberTV);
        TextView sortCodeTextView = popUpView.findViewById(R.id.actualSortCodeTV);
        TextView transactionTimeTextView = popUpView.findViewById(R.id.actualTimeTransactionTV);
        TextView amountTextView = popUpView.findViewById(R.id.amountTextView);
        recipientNameTextView.setText(transaction.getRecipientName());
        accountNumberTextView.setText(transaction.getBankAccount());
        sortCodeTextView.setText(transaction.getSortCode());
        transactionTimeTextView.setText(ConvertToDate(transaction.getTimestamp()));
        String formattedAmount = getString(R.string.negative_amount, transaction.getAmount());
        amountTextView.setText(formattedAmount);

        layout.post(() -> popupWindow.showAtLocation(layout, Gravity.BOTTOM, 0,0));

        //BACK BUTTON BEHAVIOUR
        backBtn.setOnClickListener(view -> popupWindow.dismiss());

    }

    public void CreateExpensePopupWindow(ExpectedExpenditure expense) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.popup_expense_detail, findViewById(android.R.id.content), false);
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);
        ScrollView layout = findViewById(R.id.sv);

        ImageButton backBtn = popUpView.findViewById(R.id.backButton);

        // Populate the detailed view with transaction data
        double amount = expense.getAmount();
        String formattedAmount;

        if (amount == (long) amount) {
            // If the amount is effectively an integer, cast to long to remove decimal part
            formattedAmount = String.format(Locale.UK, "£%d", (long) amount);
        } else {
            // Otherwise, format with one decimal place
            formattedAmount = String.format(Locale.UK, "£%.1f", amount);
        }

        TextView recipientNameTextView = popUpView.findViewById(R.id.actualNameTV);
        TextView typeTextView = popUpView.findViewById(R.id.actualTypeTV);
        TextView paymentTimeTextView = popUpView.findViewById(R.id.actualTimeExpenseTV);
        TextView amountTextView = popUpView.findViewById(R.id.amountTextView);
        recipientNameTextView.setText(expense.getRecipient());
        typeTextView.setText(expense.getType());
        paymentTimeTextView.setText(ConvertToDate(expense.getTime()));
        amountTextView.setText(formattedAmount);

        layout.post(() -> popupWindow.showAtLocation(layout, Gravity.BOTTOM, 0,0));

        //BACK BUTTON BEHAVIOUR
        backBtn.setOnClickListener(view -> popupWindow.dismiss());

    }

    private void updateUIWithUserNameAndBalance(User user) {
        String balanceTitle = getString(R.string.balance_title, user.getName());
        tvBalanceTitle.setText(balanceTitle);
        String balanceText = "£" + user.getBalance();
        tvBalance.setText(balanceText);
    }

    public void fetchAndDisplayTransactionHistory(RecyclerView transRecView) {
        // Execute the database operation in a background thread
        executor.execute(() -> {
            // Perform database operation to fetch transactions
            List<Transactions> transactions = TransactionsDatabase.getDatabase(getApplicationContext()).transactionDao().getAllTransactions();

            // Extract the first three transactions
            List<Transactions> firstSixTransactions = transactions.subList(0, Math.min(transactions.size(), 6));

            // Add a placeholder item for "Display All" if there are more than three transactions
            if (transactions.size() > 6) {
                firstSixTransactions.add(new Transactions()); // Add an empty Transactions object as a placeholder
            }

            // Update UI on the main thread
            runOnUiThread(() -> {
                // Update UI with fetched transactions
                transRecView.setAdapter(new TransactionAdapter_bigScreen(this, firstSixTransactions, transactions.size() < 6));
            });
        });
    }


    public void fetchAndDisplayExpenses(RecyclerView expensesRecView) {
        // Execute the database operation in a background thread
        executor.execute(() -> {
            // Perform database operation to fetch transactions
            List<ExpectedExpenditure> expenses = ExpenditureDatabase.getDatabase(getApplicationContext()).expectedExpenditureDao().getAllExpectedExpenditures();

            List<ExpectedExpenditure> firstSixExpenses = expenses.subList(0, Math.min(expenses.size(), 6));

            // Add a placeholder item for "Display All" if there are more than three transactions
            if (expenses.size() > 6) {
                firstSixExpenses.add(new ExpectedExpenditure()); // Add an empty Transactions object as a placeholder
            }

            // Update UI on the main thread
            runOnUiThread(() -> {
                // Update UI with fetched transactions
                expensesRecView.setAdapter(new ExpensesAdapter_bigScreen(this,firstSixExpenses, expenses.size() < 6));
            });
        });
    }

    public String ConvertToDate (long CTM){
        Date date = new Date(CTM);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.UK);
        return sdf.format(date);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdownNow(); // Ensure executor shutdown to avoid memory leaks
        }
    }

}