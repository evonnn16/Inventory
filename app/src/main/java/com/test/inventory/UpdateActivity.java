package com.test.inventory;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class UpdateActivity extends AppCompatActivity {

    EditText name_input, category_input, price_input, qty_input, exp_input;
    Button update_button;
    ImageView bitmap_iv;
    String id, name, category, price, qty, exp;
    Bitmap img;
    DatabaseHelper db;
    ActivityResultLauncher<Intent> pickImageFromGalleryForResult;
    private static final int SCAN_REQUEST_CODE = 0X01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        name_input = findViewById(R.id.name_input);
        category_input = findViewById(R.id.category_input);
        bitmap_iv = findViewById(R.id.bitmap_iv);
        price_input = findViewById(R.id.price_input);
        qty_input = findViewById(R.id.qty_input);
        exp_input = findViewById(R.id.exp_input);
        update_button = findViewById(R.id.update_button);

        id = getIntent().getStringExtra("id");

        db = new DatabaseHelper(UpdateActivity.this);
        Cursor cursor = db.readOneRow(id); //get data of the specific id/position
        if(cursor.getCount() == 0){
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
        }else{
            while(cursor.moveToNext()){
                name = cursor.getString(1);
                category = cursor.getString(2);
                img = getImage(cursor.getBlob(3));
                price = cursor.getString(4);
                qty = cursor.getString(5);
                exp = cursor.getString(6);
            }
        }

        name_input.setText(name);
        category_input.setText(category);
        bitmap_iv.setImageBitmap(img);
        price_input.setText(price);
        qty_input.setText(qty);
        exp_input.setText(exp);

        //Set actionbar title dynamically to item name after getAndSetIntentData method
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(name);
        }

        bitmap_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setImage();
            }
        });

        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper db = new DatabaseHelper(UpdateActivity.this);
                db.updateItem(id, name_input.getText().toString().trim(),
                        category_input.getText().toString().trim(),
                        getBytes(((BitmapDrawable)bitmap_iv.getDrawable()).getBitmap()),
                        Double.valueOf(price_input.getText().toString().trim()),
                        Integer.valueOf(qty_input.getText().toString().trim()),
                        exp_input.getText().toString().trim());
                finish();
                overridePendingTransition( 0, 0);
                startActivity(getIntent());
                overridePendingTransition( 0, 0);
                //recreate();
                /*startActivity(getIntent());
                finish();*/
            }
        });

        //implement the result of every requests in a separate ActivityResultContract
        //requestCode is useless in the new approach
        pickImageFromGalleryForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            Uri imageUri = data.getData();
                            try {
                                Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri); //get img from phone storage
                                bitmap_iv.setImageBitmap(selectedBitmap);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    private void setImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageFromGalleryForResult.launch(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.update_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.scan){
            scanItemCode();
        } else if(item.getItemId() == R.id.del){
            confirmDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void scanItemCode(){
        ScanUtil.startScan(
                UpdateActivity.this,
                SCAN_REQUEST_CODE,
                new HmsScanAnalyzerOptions.Creator().setHmsScanTypes(
                        HmsScan.ALL_SCAN_TYPE,
                        HmsScan.CODE128_SCAN_TYPE).create());
    }

    void confirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete " + name);
        builder.setMessage("Are you sure you want to delete " + name + "?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseHelper myDB = new DatabaseHelper(UpdateActivity.this);
                myDB.deleteOneRow(id);
                finish(); //return back to main activity aft del
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {return;}
        if (requestCode == SCAN_REQUEST_CODE) {
            Object obj = data.getParcelableExtra(ScanUtil.RESULT);
            //result u will get
            if (obj instanceof HmsScan) {
                if (!TextUtils.isEmpty(((HmsScan) obj).getOriginalValue())) {
                    Toast.makeText(this, ((HmsScan) obj).getOriginalValue(), Toast.LENGTH_SHORT).show();

                    String searchTerm = ((HmsScan) obj).getOriginalValue();
                    if(!searchTerm.equals("")){
                        searchNet(searchTerm);
                    }
                }
            }
        }
    }

    //search internet with default search app
    private void searchNet(String words){
        try {
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, words);
            startActivity(intent);
        } catch (ActivityNotFoundException e){
            e.printStackTrace();
            searchNetCompat(words);
        }
    }

    //search browser if no default search app
    private void searchNetCompat(String words){
        try {
            Uri uri = Uri.parse("https://www.google.com/search?q=" + words);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (ActivityNotFoundException e){
            e.printStackTrace();
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }
}