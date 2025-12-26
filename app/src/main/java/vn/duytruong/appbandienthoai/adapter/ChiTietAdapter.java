package vn.duytruong.appbandienthoai.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.model.Item;

public class ChiTietAdapter extends RecyclerView.Adapter<ChiTietAdapter.MyViewHolder> {
    List<Item> itemList;
    Context context ;


    public ChiTietAdapter(List<Item> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chitiet, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.txtidsp.setText("ID: " + item.getIdsp());
        holder.txtten.setText(item.getTensp() + "");
        holder.txtsoluong.setText("Số lượng: " + item.getSoluong() + "");

        String imageUrl = item.getHinhanh();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            // Nếu không có hình ảnh, dùng ảnh mặc định
            holder.imagechitiet.setImageResource(R.drawable.ic_person);
        } else {
            // Xây dựng URL đầy đủ cho hình ảnh
            if (!imageUrl.startsWith("http")) {
                imageUrl = vn.duytruong.appbandienthoai.utils.Utils.BASE_URL + "images/" + imageUrl;
            }
            android.util.Log.d("ChiTietAdapter", "Loading image from URL: " + imageUrl);
            Glide.with(context)
                .load(imageUrl)
                .fitCenter()
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(holder.imagechitiet);
        }
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imagechitiet;
        TextView txtidsp, txtten, txtsoluong;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imagechitiet = itemView.findViewById(R.id.item_imgchitiet);
            txtidsp = itemView.findViewById(R.id.item_idspchitiet);
            txtten = itemView.findViewById(R.id.item_tenspchitiet);
            txtsoluong = itemView.findViewById(R.id.item_soluongchitiet);
        }
    }
}
