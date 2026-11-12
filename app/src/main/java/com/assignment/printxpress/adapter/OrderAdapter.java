package com.assignment.printxpress.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.assignment.printxpress.R;
import com.assignment.printxpress.model.Order;
import com.assignment.printxpress.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays application data inside a RecyclerView.
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.Holder> {
    public interface Listener {
        void onOrder(Order order);
    }

    private final List<Order> items = new ArrayList<>();
    private final Listener listener;

    public OrderAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Order> orders) {
        items.clear();
        items.addAll(orders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Order order = items.get(position);
        holder.codeText.setText(order.code);
        holder.statusText.setText(order.status);
        holder.dateText.setText(order.createdAt + " · " + order.fulfillment);
        holder.totalText.setText(FormatUtils.money(order.total));
        holder.itemView.setOnClickListener(v -> listener.onOrder(order));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView codeText;
        final TextView statusText;
        final TextView dateText;
        final TextView totalText;

        Holder(View itemView) {
            super(itemView);
            codeText = itemView.findViewById(R.id.codeText);
            statusText = itemView.findViewById(R.id.statusText);
            dateText = itemView.findViewById(R.id.dateText);
            totalText = itemView.findViewById(R.id.totalText);
        }
    }
}
