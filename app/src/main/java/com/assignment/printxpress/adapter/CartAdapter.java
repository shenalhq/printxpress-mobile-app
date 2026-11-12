package com.assignment.printxpress.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.assignment.printxpress.R;
import com.assignment.printxpress.model.CartItem;
import com.assignment.printxpress.util.FormatUtils;
import com.assignment.printxpress.util.ProductImageResolver;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays application data inside a RecyclerView.
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.Holder> {
    public interface Listener {
        void onQuantity(CartItem item, int newQuantity);
        void onRemove(CartItem item);
    }

    private final List<CartItem> items = new ArrayList<>();
    private final Listener listener;

    public CartAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<CartItem> rows) {
        items.clear();
        items.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CartItem item = items.get(position);
        holder.cartProductImage.setImageResource(ProductImageResolver.forProductName(item.productName));
        holder.nameText.setText(item.productName);
        holder.specText.setText(
                item.size + " · " + item.material + " · " + item.finish + "\n" + item.fulfillment
        );
        holder.priceText.setText(FormatUtils.money(item.lineTotal()));
        holder.quantityText.setText(String.valueOf(item.quantity));

        holder.minusButton.setOnClickListener(
                v -> listener.onQuantity(item, item.quantity - 1)
        );
        holder.plusButton.setOnClickListener(
                v -> listener.onQuantity(item, item.quantity + 1)
        );
        holder.removeButton.setOnClickListener(v -> listener.onRemove(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView cartProductImage;
        final TextView nameText;
        final TextView specText;
        final TextView priceText;
        final TextView quantityText;
        final MaterialButton minusButton;
        final MaterialButton plusButton;
        final MaterialButton removeButton;

        Holder(View itemView) {
            super(itemView);
            cartProductImage = itemView.findViewById(R.id.cartProductImage);
            nameText = itemView.findViewById(R.id.nameText);
            specText = itemView.findViewById(R.id.specText);
            priceText = itemView.findViewById(R.id.priceText);
            quantityText = itemView.findViewById(R.id.quantityText);
            minusButton = itemView.findViewById(R.id.minusButton);
            plusButton = itemView.findViewById(R.id.plusButton);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}
