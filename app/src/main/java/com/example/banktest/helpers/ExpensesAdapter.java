package com.example.banktest.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banktest.Activity.BalanceActivity;
import com.example.banktest.Activity.UpcomingPaymentsActivity;
import com.example.banktest.R;
import com.example.banktest.database.ExpectedExpenditure;

import java.util.List;
import java.util.Locale;

public class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.ExpectedExpenditureViewHolder> {

    private final List<ExpectedExpenditure> expenses;
    private final Context context;
    private final boolean displayAll;
    private final ExpenseClickListener expenseClickListener;

    public interface ExpenseClickListener {
        void onExpenseClick(ExpectedExpenditure expense);
    }

    public ExpensesAdapter(Context context, List<ExpectedExpenditure> expenses, boolean displayAll, ExpenseClickListener expenseClickListener) {
        this.context = context;
        this.expenses = expenses;
        this.displayAll = displayAll;
        this.expenseClickListener = expenseClickListener;
    }

    @NonNull
    @Override
    public ExpectedExpenditureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new ExpectedExpenditureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpectedExpenditureViewHolder holder, int position) {
        ExpectedExpenditure expense = expenses.get(position);
        double amount = expense.getAmount();
        String formattedAmount;

        if (amount == (long) amount) {
            formattedAmount = String.format(Locale.UK, "£%d", (long) amount);
        } else {
            formattedAmount = String.format(Locale.UK, "£%.1f", amount);
        }

        if (!displayAll && position < 3) {
            holder.recipientName.setText(expense.getRecipient());
            holder.amount.setText(formattedAmount);
            holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));

            holder.itemView.setOnClickListener(v -> expenseClickListener.onExpenseClick(expense));
        } else if (!displayAll && position == 3) {
            holder.recipientName.setText(context.getString(R.string.show_more));
            holder.llPadre.setBackgroundColor(Color.parseColor("#05000000"));
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.showMoreTextColor, typedValue, true);
            holder.recipientName.setTextColor(typedValue.data);
            holder.recipientName.setTypeface(holder.recipientName.getTypeface(), Typeface.BOLD);
            holder.amount.setText("");

            holder.itemView.setOnClickListener(v -> {
                if (context instanceof BalanceActivity) {
                    Intent intent = new Intent(context, UpcomingPaymentsActivity.class);
                    context.startActivity(intent);
                }
            });
        } else {
            holder.recipientName.setText(expense.getRecipient());
            holder.amount.setText(formattedAmount);
            holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));
            holder.itemView.setOnClickListener(v -> expenseClickListener.onExpenseClick(expense));
        }
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ExpectedExpenditureViewHolder extends RecyclerView.ViewHolder {
        TextView recipientName, amount;
        LinearLayout llPadre;

        public ExpectedExpenditureViewHolder(@NonNull View itemView) {
            super(itemView);
            recipientName = itemView.findViewById(R.id.recipientNameTextView);
            amount = itemView.findViewById(R.id.amountTextView);
            llPadre = itemView.findViewById(R.id.LLpadre);
        }
    }
}

