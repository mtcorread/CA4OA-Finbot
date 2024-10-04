package com.example.banktest.Activity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banktest.R;
import com.example.banktest.database.ExpectedExpenditure;
import com.example.banktest.database.ExpectedExpenditureDAO;
import com.example.banktest.database.ExpenditureDatabase;
import com.example.banktest.database.TransactionDAO;
import com.example.banktest.database.Transactions;
import com.example.banktest.database.TransactionsDatabase;
import com.example.banktest.database.User;
import com.example.banktest.database.UserDAO;
import com.example.banktest.database.UserDatabase;
import com.example.banktest.helpers.ExpensesAdapter;
import com.example.banktest.helpers.TransactionAdapter;
import com.example.banktest.viewmodel.UserViewModel;
import com.example.banktest.viewmodel.UserViewModelFactory;
import com.example.banktest.databinding.ActivityBalanceBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BalanceActivity extends BaseActivity implements TransactionAdapter.TransactionClickListener, ExpensesAdapter.ExpenseClickListener {
    private UserViewModel userViewModel;
    private ExecutorService executorService;
    private ActivityBalanceBinding binding;
    private RecyclerView transactionRecyclerView;
    private RecyclerView expensesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_balance);

        // Initialize ViewModel
        executorService = Executors.newSingleThreadExecutor();
        UserDAO userDao = UserDatabase.getDatabase(getApplicationContext()).userDao();
        TransactionDAO transactionDao = TransactionsDatabase.getDatabase(getApplicationContext()).transactionDao();
        ExpectedExpenditureDAO expectedExpenditureDao = ExpenditureDatabase.getDatabase(getApplicationContext()).expectedExpenditureDao();

        UserViewModelFactory factory = new UserViewModelFactory(userDao, transactionDao, expectedExpenditureDao, executorService);
        userViewModel = new ViewModelProvider(this, factory).get(UserViewModel.class);

        // Set ViewModel in binding
        binding.setViewModel(userViewModel);

        // Ensure LiveData is lifecycle aware
        binding.setLifecycleOwner(this);

        // Setup RecyclerViews
        setupRecyclerViews();

        userViewModel.loadUser(1); // Assuming user ID is 1
        userViewModel.loadExpenses();
        userViewModel.loadTransactions();

        // Observing LiveData from ViewModel
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                updateUIWithUserNameAndBalance(user);
            } else {
                binding.balanceNum.setText("-");
            }
        });

        userViewModel.getTransactions().observe(this, transactions -> {
            List<Transactions> firstThreeTransactions = transactions.subList(0, Math.min(transactions.size(), 3));
            if (transactions.size() > 3) {
                firstThreeTransactions = new ArrayList<>(firstThreeTransactions);
                firstThreeTransactions.add(new Transactions());
            }
            transactionRecyclerView.setAdapter(new TransactionAdapter(this, firstThreeTransactions, transactions.size() <= 3, this));
        });

        userViewModel.getExpenses().observe(this, expenses -> {
            List<ExpectedExpenditure> firstThreeExpenses = expenses.subList(0, Math.min(expenses.size(), 3));
            if (expenses.size() > 3) {
                firstThreeExpenses = new ArrayList<>(firstThreeExpenses);
                firstThreeExpenses.add(new ExpectedExpenditure());
            }
            expensesRecyclerView.setAdapter(new ExpensesAdapter(this, firstThreeExpenses, expenses.size() <= 3, this));
        });
    }

    private void setupRecyclerViews() {
        transactionRecyclerView = findViewById(R.id.transactionContainer);
        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        expensesRecyclerView = findViewById(R.id.expectedExpContainer);
        expensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onTransactionClick(Transactions transaction) {
        createTransactionPopupWindow(transaction);
    }

    @Override
    public void onExpenseClick(ExpectedExpenditure expense) {
        createExpensePopupWindow(expense);
    }

    private void updateUIWithUserNameAndBalance(User user) {
        String balanceTitle = getString(R.string.balance_title, user.getName());
        binding.balanceTitle.setText(balanceTitle);
        String balanceText = "£" + user.getBalance();
        binding.balanceNum.setText(balanceText);
    }

    public void createTransactionPopupWindow(Transactions transaction) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.popup_trans_detail, findViewById(android.R.id.content), false);
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);
        ScrollView layout = findViewById(R.id.sv);

        ImageButton backBtn = popUpView.findViewById(R.id.backButton);

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

        layout.post(() -> popupWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0));

        backBtn.setOnClickListener(view -> popupWindow.dismiss());
    }

    public void createExpensePopupWindow(ExpectedExpenditure expense) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.popup_expense_detail, findViewById(android.R.id.content), false);
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);
        ScrollView layout = findViewById(R.id.sv);

        ImageButton backBtn = popUpView.findViewById(R.id.backButton);

        double amount = expense.getAmount();
        String formattedAmount;

        if (amount == (long) amount) {
            formattedAmount = String.format(Locale.UK, "£%d", (long) amount);
        } else {
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

        layout.post(() -> popupWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0));

        backBtn.setOnClickListener(view -> popupWindow.dismiss());
    }

    public String ConvertToDate(long CTM) {
        Date date = new Date(CTM);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.UK);
        return sdf.format(date);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdownNow(); // Ensure executor shutdown to avoid memory leaks
        }
    }
}