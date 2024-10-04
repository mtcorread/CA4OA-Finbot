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
import com.example.banktest.Activity.BalanceActivity_bigScreen;
import com.example.banktest.Activity.UpcomingPaymentsActivity;
import com.example.banktest.R;
import com.example.banktest.database.ExpectedExpenditure;

import java.util.List;
import java.util.Locale;

public class ExpensesAdapter_bigScreen extends RecyclerView.Adapter<ExpensesAdapter_bigScreen.ExpectedExpenditureViewHolder> {

    private final List<ExpectedExpenditure> expenses;
    private final Context context;
    private final boolean displayAll;

    public ExpensesAdapter_bigScreen(Context context, List<ExpectedExpenditure> expenses, boolean displayAll){
        this.context = context;
        this.expenses = expenses;
        this.displayAll = displayAll;
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
        double amount = expense.getAmount(); // Assume this gets the amount from your Expense object
        String formattedAmount;

        if (amount == (long) amount) {
            // If the amount is effectively an integer, cast to long to remove decimal part
            formattedAmount = String.format(Locale.UK, "£%d", (long) amount);
        } else {
            // Otherwise, format with one decimal place
            formattedAmount = String.format(Locale.UK, "£%.1f", amount);
        }


        if (!displayAll && position < 6) {
            holder.recipientName.setText(expense.getRecipient());
            holder.amount.setText(formattedAmount);
            holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));

            // Set click listener on itemView
            if (context instanceof BalanceActivity_bigScreen) {
                holder.itemView.setOnClickListener(v -> ((BalanceActivity_bigScreen) context).CreateExpensePopupWindow(expense));
            }
        }else if (!displayAll && position == 6) {
            // Display "Display All" option
            holder.recipientName.setText(context.getString(R.string.show_more)); //Shows Show more...
            //holder.recipientName.setTextColor(Color.rgb(76,0,153)); // Set the text color to red
            holder.llPadre.setBackgroundColor(Color.parseColor("#05000000"));
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.showMoreTextColor, typedValue, true);
            holder.recipientName.setTextColor(typedValue.data);
            holder.recipientName.setTypeface(holder.recipientName.getTypeface(), Typeface.BOLD);
            holder.amount.setText("");

            // Set click listener for "Display All" option
            holder.itemView.setOnClickListener(v -> {
                if (context instanceof BalanceActivity_bigScreen) {
                    Intent intent = new Intent(context, UpcomingPaymentsActivity.class); // Replace YourNewActivity with the name of your new activity class
                    context.startActivity(intent);
                }
            });
        }else {
            // Adapted code for displaying all transactions without "Show more..."
            //ExpectedExpenditure expense = expenses.get(position);
            holder.recipientName.setText(expense.getRecipient());
            holder.amount.setText(formattedAmount);
            holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));
            // Other binding logic as required
            holder.itemView.setOnClickListener(v -> {
                if (context instanceof UpcomingPaymentsActivity) {
                    //((UpcomingPaymentsActivity) context).CreateExpensePopupWindow(expense);
                }else if (context instanceof BalanceActivity_bigScreen) {
                    ((BalanceActivity_bigScreen) context).CreateExpensePopupWindow(expense);
                }
            });
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
