package com.assignment.printxpress.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.assignment.printxpress.R;
import com.assignment.printxpress.model.SavedDesign;

import java.util.ArrayList;
import java.util.List;

public class SavedDesignAdapter extends RecyclerView.Adapter<SavedDesignAdapter.Holder> {
    public interface Listener {
        void onUse(SavedDesign design);
        void onDelete(SavedDesign design);
    }

    private final List<SavedDesign> items = new ArrayList<>();
    private final Listener listener;

    public SavedDesignAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<SavedDesign> designs) {
        items.clear();
        items.addAll(designs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_design, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        SavedDesign design = items.get(position);
        
        holder.nameText.setText(design.name);
        holder.notesText.setText(design.notes);
        holder.dateText.setText("Saved: " + design.createdAt);

        if (design.uri != null && !design.uri.isEmpty()) {
            try {
                holder.designImage.setImageURI(Uri.parse(design.uri));
            } catch (Exception e) {
                holder.designImage.setImageResource(R.drawable.ic_logo);
            }
        } else {
            holder.designImage.setImageResource(R.drawable.ic_logo);
        }

        holder.useButton.setOnClickListener(v -> listener.onUse(design));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(design));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView designImage;
        final TextView nameText;
        final TextView notesText;
        final TextView dateText;
        final View useButton;
        final View deleteButton;

        Holder(View itemView) {
            super(itemView);
            designImage = itemView.findViewById(R.id.designImage);
            nameText = itemView.findViewById(R.id.designName);
            notesText = itemView.findViewById(R.id.designNotes);
            dateText = itemView.findViewById(R.id.designDate);
            useButton = itemView.findViewById(R.id.useDesignButton);
            deleteButton = itemView.findViewById(R.id.deleteDesignButton);
        }
    }
}
