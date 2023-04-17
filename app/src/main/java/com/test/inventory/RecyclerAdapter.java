package com.test.inventory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private Context context;
    Activity activity;
    private ArrayList item_id, item_name, item_category, item_img, item_price, item_qty, item_exp;

    RecyclerAdapter(Activity activity, Context context, ArrayList item_id, ArrayList item_name, ArrayList item_category, ArrayList item_img, ArrayList item_price, ArrayList item_qty, ArrayList item_exp){
        this.activity = activity;
        this.context = context;
        this.item_id = item_id;
        this.item_name = item_name;
        this.item_category = item_category;
        this.item_img = item_img;
        this.item_price = item_price;
        this.item_qty = item_qty;
        this.item_exp = item_exp;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView item_name_txt, item_category_txt, item_price_txt, item_qty_txt, item_exp_txt;
        ImageView item_img_iv;
        LinearLayout recyclerLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            item_img_iv = itemView.findViewById(R.id.item_img_iv);
            item_name_txt = itemView.findViewById(R.id.item_name_txt);
            item_category_txt = itemView.findViewById(R.id.item_category_txt);
            item_price_txt = itemView.findViewById(R.id.item_price_txt);
            item_qty_txt = itemView.findViewById(R.id.item_qty_txt);
            item_exp_txt = itemView.findViewById(R.id.item_exp_txt);
            recyclerLayout = itemView.findViewById(R.id.recyclerLayout);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.MyViewHolder holder, int position) {
        holder.item_img_iv.setImageBitmap(getImage((byte[]) item_img.get(position))); //byte array stored in db convert to bitmap
        holder.item_name_txt.setText(String.valueOf(item_name.get(position)));
        holder.item_category_txt.setText(String.valueOf(item_category.get(position)));
        holder.item_price_txt.setText("RM " + String.valueOf(item_price.get(position)));
        holder.item_qty_txt.setText(String.valueOf(item_qty.get(position)));
        holder.item_exp_txt.setText("EXP: " + String.valueOf(item_exp.get(position)));
        holder.recyclerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UpdateActivity.class);
                intent.putExtra("id", String.valueOf(item_id.get(position)));
                activity.startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return item_id.size();
    }

    public void setFilter(ArrayList new_item_id, ArrayList new_item_name, ArrayList new_item_category, ArrayList new_item_img, ArrayList new_item_price, ArrayList new_item_qty, ArrayList new_item_exp){
        item_id = new ArrayList<>(new_item_id); //need for reset item count
        item_name = new ArrayList<>(new_item_name); //each attribute need for update the attribute arrayList and display correct data/position
        item_category = new ArrayList<>(new_item_category);
        item_img = new ArrayList<>(new_item_img);
        item_price = new ArrayList<>(new_item_price);
        item_qty = new ArrayList<>(new_item_qty);
        item_exp = new ArrayList<>(new_item_exp);
        notifyDataSetChanged();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
