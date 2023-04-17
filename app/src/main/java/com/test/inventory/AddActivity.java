package com.test.inventory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.text.MLLocalTextSetting;
import com.huawei.hms.mlsdk.text.MLText;
import com.huawei.hms.mlsdk.text.MLTextAnalyzer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddActivity extends AppCompatActivity {
    private static final String TAG = "AddActivityTag";

    EditText name_input, category_input, price_input, qty_input, exp_input;
    ImageView bitmap_iv;
    TextView result_tv;
    Button add_button;
    FloatingActionButton ml_scan_btn;

    private static final int GET_IMAGE_REQUEST_CODE = 22;

    private MLTextAnalyzer mTextAnalyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        name_input = findViewById(R.id.name_input);
        category_input = findViewById(R.id.category_input);
        price_input = findViewById(R.id.price_input);
        qty_input = findViewById(R.id.qty_input);
        exp_input = findViewById(R.id.exp_input);
        add_button = findViewById(R.id.add_button);
        bitmap_iv = findViewById(R.id.bitmap_iv);
        result_tv = findViewById(R.id.result_tv);
        ml_scan_btn = findViewById(R.id.ml_scan_btn);

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check empty input
                if(TextUtils.isEmpty(name_input.getText().toString())){
                    name_input.setError("No item name");
                    return;
                }
                if(TextUtils.isEmpty(category_input.getText().toString())){
                    category_input.setError("No item category");
                    return;
                }
                if(bitmap_iv.getDrawable() == null){
                    Toast.makeText(AddActivity.this, "No image", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(price_input.getText().toString())){
                    price_input.setError("No item price");
                    return;
                }
                if(TextUtils.isEmpty(qty_input.getText().toString())){
                    qty_input.setError("No item quantity");
                    return;
                }
                if(TextUtils.isEmpty(exp_input.getText().toString())){
                    exp_input.setError("No item expiry date");
                    return;
                }

                DatabaseHelper db = new DatabaseHelper(AddActivity.this);
                db.addItem(name_input.getText().toString().trim(),
                        category_input.getText().toString().trim(),
                        getBytes(((BitmapDrawable)bitmap_iv.getDrawable()).getBitmap()), //get bitmap from iv & convert to byte array
                        Double.valueOf(price_input.getText().toString().trim()),
                        Integer.valueOf(qty_input.getText().toString().trim()),
                        exp_input.getText().toString().trim());
                Intent intent = new Intent(AddActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        bitmap_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage();
            }
        });

        createMLTextAnalyzer();

        ml_scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Bitmap bm = ((BitmapDrawable)bitmap_iv.getDrawable()).getBitmap();
                    Log.d(TAG, "clicked scan button, bm=" + bm);
                    asyncAnalyzeText(bm);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(AddActivity.this, "No image", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {return;}
        if (requestCode == GET_IMAGE_REQUEST_CODE) {
            Uri imageUri = data.getData();
            try {
                Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri); //get img from phone storage
                bitmap_iv.setImageBitmap(selectedBitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GET_IMAGE_REQUEST_CODE);
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    private void createMLTextAnalyzer() {
        MLLocalTextSetting setting = new MLLocalTextSetting.Factory()
                .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
                .setLanguage("en")
                .create();

        mTextAnalyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting);
        Log.d(TAG, "createMLTextAnalyzer done, mTextAnalyzer=" + mTextAnalyzer);
    }

    //this method takes in Bitmap, converts Bitmap to MLFrame, MLTextAnalyzer consumes this MLFrame and produces an output
    private void asyncAnalyzeText(Bitmap bitmap) {
        if (mTextAnalyzer == null) {
            createMLTextAnalyzer();
        }

        MLFrame frame = MLFrame.fromBitmap(bitmap);

        Task<MLText> task = mTextAnalyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(new OnSuccessListener<MLText>() {
            @Override
            public void onSuccess(MLText text) {
                result_tv.setText(text.getStringValue());
                Log.d(TAG, "result test = " + text);
                Log.d(TAG, "text view = " + result_tv.getText().toString().trim());
                /*ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", text.getStringValue());
                clipboard.setPrimaryClip(clip);*/
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "asyncAnalyzeText failed, message=" + e);
                result_tv.setText(e.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mTextAnalyzer != null)
                mTextAnalyzer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}