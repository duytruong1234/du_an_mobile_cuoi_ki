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

import java.text.DecimalFormat;
import java.util.List;

import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.model.TonKhoItem;
import vn.duytruong.appbandienthoai.utils.Utils;

public class TonKhoAdapter extends RecyclerView.Adapter<TonKhoAdapter.ViewHolder> {
    private final Context context;
    private final List<TonKhoItem> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditStock(TonKhoItem item, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TonKhoAdapter(Context context, List<TonKhoItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ton_kho, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TonKhoItem item = list.get(position);
        holder.tvName.setText(item.getTensp());
        DecimalFormat df = new DecimalFormat("###,###,###");
        try { holder.tvPrice.setText(df.format(Long.parseLong(item.getGiasp().replaceAll("[^0-9]", ""))) + " ₫"); } catch (Exception e) { holder.tvPrice.setText(item.getGiasp()); }
        holder.tvStock.setText(String.valueOf(item.getSoluongtonkho()));
        holder.tvStatus.setText(item.getTinhtrang());
        String img = item.getHinhanh();
        String url = (img != null && img.startsWith("http")) ? img : Utils.BASE_URL + "images/" + img;
        Glide.with(context).load(url).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_foreground).into(holder.img);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEditStock(item, position);
        });
    }

    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvStock, tvStatus;
        ImageView img;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTonKhoName);
            tvPrice = itemView.findViewById(R.id.tvTonKhoPrice);
            tvStock = itemView.findViewById(R.id.tvTonKhoStock);
            tvStatus = itemView.findViewById(R.id.tvTonKhoStatus);
            img = itemView.findViewById(R.id.imgTonKho);
        }
    }
}
