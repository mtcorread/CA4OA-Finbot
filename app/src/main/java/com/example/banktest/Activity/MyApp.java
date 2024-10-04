package com.example.banktest.Activity;

import android.app.Application;
import android.util.Log;

import com.example.banktest.database.ExpectedExpenditure;
import com.example.banktest.database.ExpenditureDatabase;
import com.example.banktest.database.Transactions;
import com.example.banktest.database.TransactionsDatabase;
import com.example.banktest.database.User;
import com.example.banktest.database.UserDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        TransactionsDatabase tdb = TransactionsDatabase.getDatabase(getApplicationContext());
        UserDatabase udb = UserDatabase.getDatabase((getApplicationContext()));
        ExpenditureDatabase eedb = ExpenditureDatabase.getDatabase(getApplicationContext());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            //FOR DEMO PURPOSES: this will always delete every entry on the databases, reset all IDs and will re-insert dummy transactions and expenses
            //Deletes all previous entries to database
            tdb.transactionDao().deleteAllTransactions();
            udb.userDao().deleteAllUsers();
            eedb.expectedExpenditureDao().deleteAllExpenses();

            // Reset IDs
            udb.userDao().resetUserIds();
            tdb.transactionDao().resetTransactionIds();
            eedb.expectedExpenditureDao().resetExpensesIds();

            // Recreate default users
            User user1 = new User("Martha", "334455", "2500");
            User user2 = new User("Anna", "667788", "1500");

            // Insert dummy transactions

            tdb.transactionDao().insert(new Transactions("George Wilson","33445566","29-12-96", "10", System.currentTimeMillis()));
            tdb.transactionDao().insert(new Transactions("William Johnson","11223344","09-11-96", "17", System.currentTimeMillis()));
            tdb.transactionDao().insert(new Transactions("Jack White","33445566","01-11-96", "15", System.currentTimeMillis()));
            tdb.transactionDao().insert(new Transactions("Jessica Green","11223344","22-09-95", "18", System.currentTimeMillis()));
            tdb.transactionDao().insert(new Transactions("Oscar Harris","33445566","19-08-98", "10", System.currentTimeMillis()));
            tdb.transactionDao().insert(new Transactions("Mia Roberts","11223344","03-06-98", "19", System.currentTimeMillis()));
            tdb.transactionDao().insert(new Transactions("Amelia Robinson","33445566","04-10-97", "20", System.currentTimeMillis()));
            tdb.transactionDao().insert(new Transactions("Olivia Wright","11223344","30-05-97", "11", System.currentTimeMillis()));
            tdb.transactionDao().insert(new Transactions("Emily Watson","33445566","19-08-96", "15", System.currentTimeMillis()));
            tdb.transactionDao().insert(new Transactions("Sophia Jackson","11223344","07-12-95", "14", System.currentTimeMillis()));

            //Insert dummy expenses
            eedb.expectedExpenditureDao().insert(new ExpectedExpenditure("Netflix", 12, "Subscription", System.currentTimeMillis()+2629743000L)); //get current time and always add 1 month
            eedb.expectedExpenditureDao().insert(new ExpectedExpenditure("Internet", 30, "Service", System.currentTimeMillis()+2629743000L));
            eedb.expectedExpenditureDao().insert(new ExpectedExpenditure("Energy", 150, "Service", System.currentTimeMillis()+2629743000L));
            eedb.expectedExpenditureDao().insert(new ExpectedExpenditure("Water", 30, "Service", System.currentTimeMillis()+2629743000L));
            eedb.expectedExpenditureDao().insert(new ExpectedExpenditure("Spotify", 12, "Subscription", System.currentTimeMillis()+2629743000L));
            eedb.expectedExpenditureDao().insert(new ExpectedExpenditure("Youtube", 12, "Service", System.currentTimeMillis()+2629743000L));
            eedb.expectedExpenditureDao().insert(new ExpectedExpenditure("Council Tax", 120, "Service", System.currentTimeMillis()+2629743000L));
            eedb.expectedExpenditureDao().insert(new ExpectedExpenditure("Water", 30, "Service", System.currentTimeMillis()+2629743000L));



            Log.d("MyApp", "Inserting users...");
            udb.userDao().insert(user1);
            udb.userDao().insert(user2);
            Log.d("MyApp", "Users inserted");

        });


    }
}

