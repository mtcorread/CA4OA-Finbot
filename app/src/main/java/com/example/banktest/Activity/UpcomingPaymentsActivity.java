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

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banktest.R;
import com.example.banktest.database.ExpectedExpenditure;
import com.example.banktest.database.ExpectedExpenditureDAO;
import com.example.banktest.database.ExpenditureDatabase;
import com.example.banktest.database.TransactionDAO;
import com.example.banktest.database.TransactionsDatabase;
import com.example.banktest.database.UserDAO;
import com.example.banktest.database.UserDatabase;
import com.example.banktest.helpers.ExpensesAdapter;
import com.example.banktest.viewmodel.UserViewModel;
import com.example.banktest.viewmodel.UserViewModelFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.ViewModelProvider;

import androidx.databinding.DataBindingUtil;
import com.example.banktest.databinding.ActivityUpcomingpaymentsBinding;

public class UpcomingPaymentsActivity extends BaseActivity implements ExpensesAdapter.ExpenseClickListener {
    private UserViewModel userViewModel;
    private ExecutorService executorService;
    private ActivityUpcomingpaymentsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upcomingpayments);

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

        // Observe the total expenditure LiveData and set the formatted text
        userViewModel.getTotalExpenditure().observe(this, totalExpenditure -> {
            String formattedTotal = getString(R.string.total_expenditure, totalExpenditure);
            binding.actualtotalTV.setText(formattedTotal);
        });

        // Setup RecyclerView
        RecyclerView upcomingRecyclerView = findViewById(R.id.upcomingRecyclerView);
        upcomingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userViewModel.getExpenses().observe(this, expenses -> {
            upcomingRecyclerView.setAdapter(new ExpensesAdapter(this, expenses, true, this));
        });

        userViewModel.loadExpenses();
    }

    @Override
    public void onExpenseClick(ExpectedExpenditure expense) {
        createExpensePopupWindow(expense);
    }

    public void createExpensePopupWindow(ExpectedExpenditure expense) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.popup_expense_detail, findViewById(android.R.id.content), false);
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);
        ScrollView layout = findViewById(R.id.sv3);

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

    public String ConvertToDate(long timestamp) {
        Date date = new Date(timestamp);
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
