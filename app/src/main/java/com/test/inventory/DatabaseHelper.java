package com.test.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "Inventory.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "my_inventory";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "item_name";
    private static final String COLUMN_CATEGORY = "item_category";
    private static final String COLUMN_IMAGE = "item_img";
    private static final String COLUMN_PRICE = "item_price";
    private static final String COLUMN_QTY = "item_qty";
    private static final String COLUMN_EXP = "item_exp";

    DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_IMAGE + " BLOB, " +
                COLUMN_PRICE + " REAL, " +
                COLUMN_QTY + " INTEGER, " +
                COLUMN_EXP + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    void addItem(String name, String category, byte[] img, double price, int qty, String exp){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_CATEGORY, category);
        cv.put(COLUMN_IMAGE, img);
        cv.put(COLUMN_PRICE, price);
        cv.put(COLUMN_QTY, qty);
        cv.put(COLUMN_EXP, exp);
        long result = db.insert(TABLE_NAME, null, cv);
        if (result == -1){
            Toast.makeText(context, "Failed to insert data", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show();
        }
    }

    void updateItem(String row_id, String name, String category, byte[] img, double price, int qty, String exp){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_CATEGORY, category);
        cv.put(COLUMN_IMAGE, img);
        cv.put(COLUMN_PRICE, price);
        cv.put(COLUMN_QTY, qty);
        cv.put(COLUMN_EXP, exp);
        long result = db.update(TABLE_NAME, cv,"_id=?", new String[]{row_id});
        if (result == -1){
            Toast.makeText(context, "Failed to update data", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show();
        }
    }

    Cursor readAllData(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    Cursor readOneRow(String row_id){
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE _id = " + row_id; // + ";"
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    void deleteOneRow(String row_id){
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_NAME, "_id=?", new String[]{row_id});
        if(result == -1){
            Toast.makeText(context, "Failed to Delete.", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Successfully Deleted.", Toast.LENGTH_SHORT).show();
        }
    }

    void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }

    ArrayList<String> getCategoryList(){
        String query = "SELECT DISTINCT " + COLUMN_CATEGORY + " FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<String> category = new ArrayList<>();
        category.add("All");

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);

            if(cursor.getCount() == 0){
                //Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
            }else{
                while(cursor.moveToNext()){
                    category.add(cursor.getString(0));
                }
            }
        }
        return category;
    }
}
