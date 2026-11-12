package com.assignment.printxpress.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.assignment.printxpress.R;
import com.assignment.printxpress.model.Product;
import com.assignment.printxpress.util.FormatUtils;
import com.assignment.printxpress.util.ProductImageResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays application data inside a RecyclerView.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.Holder> {
    public interface Listener {
        void onProduct(Product product);
    }

    private final List<Product> items = new ArrayList<>();
    private final Listener listener;

    public ProductAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Product> products) {
        items.clear();
        items.addAll(products);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Product product = items.get(position);
        holder.productImage.setImageResource(ProductImageResolver.forProductName(product.name));
        holder.nameText.setText(product.name);
        holder.categoryText.setText(product.category + " · " + product.description);
        holder.priceText.setText("From " + FormatUtils.money(product.basePrice));
        holder.itemView.setOnClickListener(v -> listener.onProduct(product));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView productImage;
        final TextView nameText;
        final TextView categoryText;
        final TextView priceText;

        Holder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            nameText = itemView.findViewById(R.id.nameText);
            categoryText = itemView.findViewById(R.id.categoryText);
            priceText = itemView.findViewById(R.id.priceText);
        }
    }
}
