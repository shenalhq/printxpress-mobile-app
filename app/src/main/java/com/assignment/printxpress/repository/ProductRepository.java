package com.assignment.printxpress.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.assignment.printxpress.data.DatabaseHelper;
import com.assignment.printxpress.model.Product;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads product information from SQLite and performs product searches.
 */
public class ProductRepository {
    private final DatabaseHelper helper;
    public ProductRepository(Context context) { helper = DatabaseHelper.getInstance(context); }

    public List<Product> getAll() { return query("SELECT id,name,category,description,base_price FROM products WHERE active=1 ORDER BY id", null); }

    public List<Product> search(String term, String category) {
        String q = "%" + term.trim().toLowerCase() + "%";
        if (category == null || category.equalsIgnoreCase("All")) {
            return query("SELECT id,name,category,description,base_price FROM products WHERE active=1 AND (lower(name) LIKE ? OR lower(category) LIKE ? OR lower(description) LIKE ?) ORDER BY name", new String[]{q,q,q});
        }
        return query("SELECT id,name,category,description,base_price FROM products WHERE active=1 AND category=? AND (lower(name) LIKE ? OR lower(description) LIKE ?) ORDER BY name", new String[]{category,q,q});
    }

    public Product find(long id) {
        List<Product> rows = query("SELECT id,name,category,description,base_price FROM products WHERE id=? AND active=1", new String[]{String.valueOf(id)});
        return rows.isEmpty() ? null : rows.get(0);
    }

    private List<Product> query(String sql, String[] args) {
        List<Product> out = new ArrayList<>(); SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) out.add(new Product(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getDouble(4)));
        }
        return out;
    }
}
