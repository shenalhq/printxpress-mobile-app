package com.assignment.printxpress.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.assignment.printxpress.R;
import com.assignment.printxpress.model.NotificationItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays application data inside a RecyclerView.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.Holder> {
    private final List<NotificationItem> items = new ArrayList<>();

    public void submit(List<NotificationItem> notifications) {
        items.clear();
        items.addAll(notifications);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        NotificationItem notification = items.get(position);
        holder.titleText.setText(
                (notification.read ? "" : "● ") + notification.title
        );
        holder.messageText.setText(notification.message);
        holder.dateText.setText(notification.createdAt);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView titleText;
        final TextView messageText;
        final TextView dateText;

        Holder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            messageText = itemView.findViewById(R.id.messageText);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }
}
