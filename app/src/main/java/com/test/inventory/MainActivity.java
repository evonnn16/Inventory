package com.test.inventory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;

import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "myTag";

    private static final String[] RUNTIME_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
            Manifest.permission.INTERNET};
    private static final int REQUEST_CODE = 100;

    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;

    ImageView empty_imgView;
    TextView empty_txtView;
    FloatingActionButton addBtn;
    Spinner category_spinner;

    DatabaseHelper db;
    ArrayList<String> item_id, item_name, item_category, item_price, item_qty, item_exp, category_list,
            new_item_id, new_item_name, new_item_category, new_item_price, new_item_qty, new_item_exp;
    ArrayList item_img, new_item_img;
    //String currCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!hasPermissions(this, RUNTIME_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE);
        }

        recyclerView = findViewById(R.id.recyclerView);
        category_spinner = findViewById(R.id.category_spinner);
        addBtn = findViewById(R.id.addBtn);
        empty_imgView = findViewById(R.id.empty_imageView);
        empty_txtView = findViewById(R.id.empty_textView);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });

        db = new DatabaseHelper(MainActivity.this);
        item_id = new ArrayList<>();
        item_name = new ArrayList<>();
        item_category = new ArrayList<>();
        item_img = new ArrayList<>();
        item_price = new ArrayList<>();
        item_qty = new ArrayList<>();
        item_exp = new ArrayList<>();

        storeDataInArrays();

        recyclerAdapter = new RecyclerAdapter(MainActivity.this, this, item_id, item_name, item_category, item_img, item_price, item_qty, item_exp);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        categorySpinner();
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            recreate(); //refreshing main activity, start again
        }
    }

    void storeDataInArrays() {
        Cursor cursor = db.readAllData();
        if(cursor.getCount() == 0){
            //Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
            empty_imgView.setVisibility(View.VISIBLE);
            empty_txtView.setVisibility(View.VISIBLE);
        }else{
            while(cursor.moveToNext()){
                item_id.add(cursor.getString(0));
                item_name.add(cursor.getString(1));
                item_category.add(cursor.getString(2));
                item_img.add(cursor.getBlob(3));
                item_price.add(cursor.getString(4));
                item_qty.add(cursor.getString(5));
                item_exp.add(cursor.getString(6));
            }

            empty_imgView.setVisibility(View.GONE);
            empty_txtView.setVisibility(View.GONE);
        }
    }

    void categorySpinner(){
        //dropdown filter item category
        category_list = db.getCategoryList();
        category_spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, category_list));

        category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position >=0 && position < category_list.size()){ //select valid category
                    if(position == 0){ //All
                        recyclerAdapter = new RecyclerAdapter(MainActivity.this, MainActivity.this, item_id, item_name, item_category, item_img, item_price, item_qty, item_exp);
                        recyclerView.setAdapter(recyclerAdapter);
                    } else{ //Other categories
                        new_item_id = new ArrayList<>();
                        new_item_name = new ArrayList<>();
                        new_item_category = new ArrayList<>();
                        new_item_img = new ArrayList<>();
                        new_item_price = new ArrayList<>();
                        new_item_qty = new ArrayList<>();
                        new_item_exp = new ArrayList<>();

                        for(int i=0; i<item_id.size(); i++){
                            //currCategory = category_list.get(position);
                            if(item_category.get(i).equals(category_list.get(position))){ //match selected category
                                new_item_id.add(item_id.get(i));
                                new_item_name.add(item_name.get(i));
                                new_item_category.add(item_category.get(i));
                                new_item_img.add(item_img.get(i));
                                new_item_price.add(item_price.get(i));
                                new_item_qty.add(item_qty.get(i));
                                new_item_exp.add(item_exp.get(i));
                            }
                        }
                        recyclerAdapter.setFilter(new_item_id, new_item_name, new_item_category, new_item_img, new_item_price, new_item_qty, new_item_exp);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Selected category does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        getMenuInflater().inflate(R.menu.my_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //trigger only when press search button
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            //trigger everytime write a single char
            @Override
            public boolean onQueryTextChange(String newText) {
                newText.toLowerCase();
                new_item_id = new ArrayList<>();
                new_item_name = new ArrayList<>();
                new_item_category = new ArrayList<>();
                new_item_img = new ArrayList<>();
                new_item_price = new ArrayList<>();
                new_item_qty = new ArrayList<>();
                new_item_exp = new ArrayList<>();

                for(int i=0; i<item_id.size(); i++){
                    String name = item_name.get(i).toLowerCase();
                    if(name.contains(newText)){ //compare user input with db data
                        //Log.d(TAG, "i=" + i + ", item_name=" + name);
                        new_item_id.add(item_id.get(i));
                        new_item_name.add(item_name.get(i));
                        new_item_category.add(item_category.get(i));
                        new_item_img.add(item_img.get(i));
                        new_item_price.add(item_price.get(i));
                        new_item_qty.add(item_qty.get(i));
                        new_item_exp.add(item_exp.get(i));
                    }
                }
                recyclerAdapter.setFilter(new_item_id, new_item_name, new_item_category, new_item_img, new_item_price, new_item_qty, new_item_exp);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.delete_all){
            confirmDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    void confirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete All?");
        builder.setMessage("Are you sure you want to delete all Data?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseHelper myDB = new DatabaseHelper(MainActivity.this);
                myDB.deleteAllData();
                //Refresh Activity
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}
        });
        builder.create().show();
    }
}