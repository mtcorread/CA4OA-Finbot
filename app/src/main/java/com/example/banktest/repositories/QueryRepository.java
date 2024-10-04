package com.example.banktest.repositories;

import android.app.Application;
import android.database.Cursor;

import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.banktest.database.ExpenditureDatabase;
import com.example.banktest.database.TransactionsDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueryRepository {
    private TransactionsDatabase transactionsDatabase;
    private ExpenditureDatabase expenditureDatabase;
    private ExecutorService executorService;

    public QueryRepository(Application application) {
        transactionsDatabase = TransactionsDatabase.getDatabase(application.getApplicationContext());
        expenditureDatabase = ExpenditureDatabase.getDatabase(application.getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    public void executeQuery(String query, int queryType, QueryCallback callback) {
        executorService.execute(() -> {
            Cursor cursor = null;
            try {
                SupportSQLiteQuery sqLiteQuery = new SimpleSQLiteQuery(query);
                if (queryType == 1) {
                    cursor = transactionsDatabase.query(sqLiteQuery);
                } else {
                    cursor = expenditureDatabase.query(sqLiteQuery);
                }

                StringBuilder sb = new StringBuilder();
                if (cursor.moveToFirst()) {
                    do {
                        for (int i = 0; i < cursor.getColumnCount(); i++) {
                            sb.append(cursor.getString(i)).append(" ");
                        }
                        sb.append("\n");
                    } while (cursor.moveToNext());
                }

                String result = sb.toString().trim();
                callback.onQueryCompleted(result);

            } catch (Exception e) {
                callback.onQueryFailed(e.getMessage());
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        });
    }

    public interface QueryCallback {
        void onQueryCompleted(String result);
        void onQueryFailed(String error);
    }
}

