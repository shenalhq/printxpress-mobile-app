package com.assignment.printxpress.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.assignment.printxpress.util.PasswordUtils;

/**
 * Creates the SQLite tables, inserts demo products and handles database upgrades.
 */
public class  DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "printxpress.db";
    public static final int DB_VERSION = 3;

    private static volatile DatabaseHelper instance;

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        seedProducts(db);
        seedDemoUser(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "email TEXT NOT NULL UNIQUE," +
                        "phone TEXT NOT NULL UNIQUE," +
                        "password_hash TEXT NOT NULL," +
                        "salt TEXT NOT NULL," +
                        "address TEXT DEFAULT ''," +
                        "profile_uri TEXT DEFAULT ''," +
                        "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)"
        );

        db.execSQL(
                "CREATE TABLE products (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "category TEXT NOT NULL," +
                        "description TEXT NOT NULL," +
                        "base_price REAL NOT NULL," +
                        "active INTEGER NOT NULL DEFAULT 1)"
        );

        db.execSQL(
                "CREATE TABLE addresses (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER NOT NULL," +
                        "label TEXT NOT NULL," +
                        "address TEXT NOT NULL," +
                        "is_default INTEGER NOT NULL DEFAULT 0," +
                        "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)"
        );

        db.execSQL(
                "CREATE TABLE saved_designs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER NOT NULL," +
                        "name TEXT NOT NULL," +
                        "uri TEXT NOT NULL," +
                        "notes TEXT DEFAULT ''," +
                        "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)"
        );

        db.execSQL(
                "CREATE TABLE cart_items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER NOT NULL," +
                        "product_id INTEGER NOT NULL," +
                        "quantity INTEGER NOT NULL CHECK(quantity > 0)," +
                        "size TEXT NOT NULL," +
                        "material TEXT NOT NULL," +
                        "finish TEXT NOT NULL," +
                        "instructions TEXT DEFAULT ''," +
                        "artwork_uri TEXT DEFAULT ''," +
                        "fulfillment TEXT NOT NULL," +
                        "added_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE," +
                        "FOREIGN KEY(product_id) REFERENCES products(id))"
        );

        db.execSQL(
                "CREATE TABLE orders (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "order_code TEXT NOT NULL UNIQUE," +
                        "user_id INTEGER NOT NULL," +
                        "status TEXT NOT NULL," +
                        "total REAL NOT NULL," +
                        "fulfillment TEXT NOT NULL," +
                        "address TEXT DEFAULT ''," +
                        "payment_method TEXT NOT NULL," +
                        "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)"
        );

        db.execSQL(
                "CREATE TABLE order_items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "order_id INTEGER NOT NULL," +
                        "product_id INTEGER," +
                        "product_name TEXT NOT NULL," +
                        "quantity INTEGER NOT NULL," +
                        "unit_price REAL NOT NULL," +
                        "size TEXT NOT NULL," +
                        "material TEXT NOT NULL," +
                        "finish TEXT NOT NULL," +
                        "instructions TEXT DEFAULT ''," +
                        "artwork_uri TEXT DEFAULT ''," +
                        "FOREIGN KEY(order_id) REFERENCES orders(id) ON DELETE CASCADE)"
        );

        db.execSQL(
                "CREATE TABLE notifications (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER NOT NULL," +
                        "title TEXT NOT NULL," +
                        "message TEXT NOT NULL," +
                        "type TEXT NOT NULL DEFAULT 'info'," +
                        "is_read INTEGER NOT NULL DEFAULT 0," +
                        "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)"
        );

        db.execSQL(
                "CREATE TABLE support_requests (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER NOT NULL," +
                        "subject TEXT NOT NULL," +
                        "message TEXT NOT NULL," +
                        "status TEXT NOT NULL DEFAULT 'Open'," +
                        "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)"
        );
    }

    private void seedProducts(SQLiteDatabase db) {
        Object[][] rows = new Object[][]{
                {
                        "Premium Business Cards",
                        "Business Cards",
                        "350gsm matte or gloss, full colour, 100 pieces",
                        1800.0
                },
                {
                        "Flyers",
                        "Flyers",
                        "130gsm or 170gsm, single/double side, 100 pieces",
                        2600.0
                },
                {
                        "Posters",
                        "Posters",
                        "High-resolution colour poster on premium stock",
                        950.0
                },
                {
                        "Outdoor Banner",
                        "Banners",
                        "Weather-resistant banner printing, priced per square foot",
                        750.0
                },
                {
                        "Vinyl Logo Stickers",
                        "Stickers",
                        "Durable die-cut vinyl stickers, 50 pieces",
                        1400.0
                },
                {
                        "Custom Ceramic Mug",
                        "Merchandise",
                        "11oz white ceramic mug with full-colour transfer",
                        1650.0
                },
                {
                        "Custom Cotton T-Shirt",
                        "Merchandise",
                        "Front print on comfortable cotton T-shirt",
                        2400.0
                }
        };

        for (Object[] row : rows) {
            ContentValues values = new ContentValues();
            values.put("name", (String) row[0]);
            values.put("category", (String) row[1]);
            values.put("description", (String) row[2]);
            values.put("base_price", (Double) row[3]);
            db.insertOrThrow("products", null, values);
        }
    }

    private void seedDemoUser(SQLiteDatabase db) {
        String salt = PasswordUtils.newSalt();
        ContentValues user = new ContentValues();
        user.put("name", "Demo Customer");
        user.put("email", "customer@printxpress.lk");
        user.put("phone", "0771234567");
        user.put("salt", salt);
        user.put("password_hash", PasswordUtils.hash("Print123", salt));
        user.put("address", "Matara, Sri Lanka");
        long userId = db.insertOrThrow("users", null, user);

        ContentValues address = new ContentValues();
        address.put("user_id", userId);
        address.put("label", "Home");
        address.put("address", "Matara, Sri Lanka");
        address.put("is_default", 1);
        db.insertOrThrow("addresses", null, address);

        addNotification(
                db,
                userId,
                "Welcome to PrintXpress",
                "Your account is ready. Browse a product and create your first print order."
        );
        addNotification(
                db,
                userId,
                "Weekend print deal",
                "Get 15% off selected business-card packages this weekend."
        );
        addNotification(
                db,
                userId,
                "Design tip",
                "For best print quality, use a 300 DPI PDF and include 3 mm bleed."
        );
    }

    private void addNotification(
            SQLiteDatabase db,
            long userId,
            String title,
            String message
    ) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("title", title);
        values.put("message", message);
        db.insertOrThrow("notifications", null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2 && !columnExists(db, "users", "profile_uri")) {
            db.execSQL("ALTER TABLE users ADD COLUMN profile_uri TEXT DEFAULT ''");
        }
        if (oldVersion < 3) {
            // Version 3 is a stability migration. Keep existing data and only
            // repair the profile column if an older development build missed it.
            if (!columnExists(db, "users", "profile_uri")) {
                db.execSQL("ALTER TABLE users ADD COLUMN profile_uri TEXT DEFAULT ''");
            }
        }
    }

    private boolean columnExists(SQLiteDatabase db, String table, String column) {
        try (android.database.Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            int nameIndex = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                if (nameIndex >= 0 && column.equalsIgnoreCase(cursor.getString(nameIndex))) {
                    return true;
                }
            }
        }
        return false;
    }
}
