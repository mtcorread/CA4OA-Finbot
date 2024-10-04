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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banktest.R;
import com.example.banktest.database.ExpectedExpenditureDAO;
import com.example.banktest.database.ExpenditureDatabase;
import com.example.banktest.database.TransactionDAO;
import com.example.banktest.database.Transactions;
import com.example.banktest.database.TransactionsDatabase;
import com.example.banktest.database.UserDAO;
import com.example.banktest.database.UserDatabase;
import com.example.banktest.databinding.ActivityTranshistBinding;
import com.example.banktest.helpers.TransactionAdapter;
import com.example.banktest.repositories.UserRepository;
import com.example.banktest.viewmodel.UserViewModel;
import com.example.banktest.viewmodel.UserViewModelFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.ViewModelProvider;

public class TransHistActivity extends BaseActivity implements TransactionAdapter.TransactionClickListener {
    private UserViewModel userViewModel;
    private ExecutorService executorService;
    private ActivityTranshistBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transhist);

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

        // Setup RecyclerView
        setupRecyclerView();

        userViewModel.getTransactions().observe(this, transactions -> {
            binding.transactionContainer2.setAdapter(new TransactionAdapter(this, transactions, true, this));
        });

        userViewModel.loadTransactions();
    }

    private void setupRecyclerView() {
        binding.transactionContainer2.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onTransactionClick(Transactions transaction) {
        createTransactionPopupWindow(transaction);
    }

    public void createTransactionPopupWindow(Transactions transaction) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.popup_trans_detail, findViewById(android.R.id.content), false);
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popUpView, width, height, focusable);
        ScrollView layout = findViewById(R.id.sv2);

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

        layout.post(() -> popupWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0));

        // BACK BUTTON BEHAVIOUR
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


