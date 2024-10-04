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
import androidx.recyclerview.widget.RecyclerView;

import com.example.banktest.Activity.BalanceActivity;
import com.example.banktest.Activity.TransHistActivity;
import com.example.banktest.R;
import com.example.banktest.database.Transactions;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private final List<Transactions> transactions;
    private final Context context;
    private final boolean displayAll;
    private final TransactionClickListener transactionClickListener;

    public interface TransactionClickListener {
        void onTransactionClick(Transactions transaction);
    }

    public TransactionAdapter(Context context, List<Transactions> transactions, boolean displayAll, TransactionClickListener transactionClickListener) {
        this.context = context;
        this.transactions = transactions;
        this.displayAll = displayAll;
        this.transactionClickListener = transactionClickListener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        if (!displayAll && position < 3) {
            Transactions transaction = transactions.get(position);
            holder.recipientName.setText(transaction.getRecipientName());
            String formattedAmount = context.getString(R.string.negative_amount, transaction.getAmount());
            holder.amount.setText(formattedAmount);

            // Set click listener for the transaction item
            holder.itemView.setOnClickListener(v -> transactionClickListener.onTransactionClick(transaction));
        } else if (!displayAll && position == 3) {
            holder.recipientName.setText(context.getString(R.string.show_more));
            holder.llPadre.setBackgroundColor(Color.parseColor("#05000000"));
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.showMoreTextColor, typedValue, true);
            holder.recipientName.setTextColor(typedValue.data);
            holder.recipientName.setTypeface(holder.recipientName.getTypeface(), Typeface.BOLD);
            holder.amount.setText("");

            // Set click listener for "Display All" option
            holder.itemView.setOnClickListener(v -> {
                if (context instanceof BalanceActivity) {
                    Intent intent = new Intent(context, TransHistActivity.class);
                    context.startActivity(intent);
                }
            });
        } else {
            Transactions transaction = transactions.get(position);
            holder.recipientName.setText(transaction.getRecipientName());
            String formattedAmount = context.getString(R.string.negative_amount, transaction.getAmount());
            holder.amount.setText(formattedAmount);

            holder.itemView.setOnClickListener(v -> transactionClickListener.onTransactionClick(transaction));
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView recipientName, amount;
        LinearLayout llPadre;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            recipientName = itemView.findViewById(R.id.recipientNameTextView);
            amount = itemView.findViewById(R.id.amountTextView);
            llPadre = itemView.findViewById(R.id.LLpadre);
        }
    }
}


