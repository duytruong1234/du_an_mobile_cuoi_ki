package vn.duytruong.appbandienthoai.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.DecimalFormat;
import java.util.List;

import vn.duytruong.appbandienthoai.Interface.ItemClickListener;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.activity.ChiTietActivity;
import vn.duytruong.appbandienthoai.model.SanPhamMoi;
import vn.duytruong.appbandienthoai.utils.Utils;

public class DienThoaiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<SanPhamMoi> array;
    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_LOADING = 1;
    private static final String TAG = "DienThoaiAdapter";

    public DienThoaiAdapter(Context context, List<SanPhamMoi> array) {
        this.context = context;
        this.array = array;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATA) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dienthoai, parent, false);
            return new MyViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            MyViewHolder vh = (MyViewHolder) holder;
            SanPhamMoi sanPham = array.get(position);

            // Kiểm tra tồn kho để làm mờ sản phẩm hết hàng
            int tonkho = sanPham.getSoluongtonkho();
            boolean isOutOfStock = (tonkho <= 0);

            //  Làm mờ toàn bộ item nếu hết hàng
            if (isOutOfStock) {
                vh.itemView.setAlpha(0.4f); // Làm mờ 60%
                vh.itemView.setEnabled(false); // Vô hiệu hóa
                vh.itemView.setClickable(false); // Không cho click
                if (vh.txtOutOfStock != null) {
                    vh.txtOutOfStock.setVisibility(View.VISIBLE); // Hiển thị nhãn HẾT HÀNG
                }
            } else {
                vh.itemView.setAlpha(1.0f); // Hiển thị bình thường
                vh.itemView.setEnabled(true);
                vh.itemView.setClickable(true);
                if (vh.txtOutOfStock != null) {
                    vh.txtOutOfStock.setVisibility(View.GONE); // Ẩn nhãn HẾT HÀNG
                }
            }

            // 🔹 Gán tên sản phẩm
            vh.tensp.setText(sanPham.getTensp());

            // 🔹 Xử lý giá sản phẩm
            DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
            String rawPrice = sanPham.getGiasp();

            Log.d(TAG, "Product: " + sanPham.getTensp() + " | Raw Price: '" + rawPrice + "'");

            try {
                if (rawPrice != null && !rawPrice.trim().isEmpty()) {
                    // Parse trực tiếp - Double.parseDouble tự động xử lý "3290000.00"
                    double price = Double.parseDouble(rawPrice.trim());
                    vh.giasp.setText("Giá: " + decimalFormat.format(price) + " ₫");
                    Log.d(TAG, "Formatted Price: " + decimalFormat.format(price));
                } else {
                    vh.giasp.setText("Giá: Liên hệ");
                }
            } catch (NumberFormatException e) {
                vh.giasp.setText("Giá: Liên hệ");
                Log.e(TAG, "Cannot parse price: '" + rawPrice + "'", e);
            }

            // 🔹 Mô tả

            // 🔹 Load ảnh bằng Glide
            String imageUrl = sanPham.getHinhanh();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Luôn lấy phần cuối cùng bắt đầu bằng http:// hoặc https:// nếu có nhiều lần xuất hiện
                int lastHttp = imageUrl.lastIndexOf("http://");
                int lastHttps = imageUrl.lastIndexOf("https://");
                if (lastHttp > 0) {
                    imageUrl = imageUrl.substring(lastHttp);
                } else if (lastHttps > 0) {
                    imageUrl = imageUrl.substring(lastHttps);
                } else if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                    // URL tương đối, cần thêm BASE_URL
                    if (imageUrl.startsWith("images/")) {
                        imageUrl = imageUrl.substring(7); // bỏ "images/"
                    }
                    imageUrl = Utils.BASE_URL + "images/" + imageUrl;
                }
            } else {
                imageUrl = "";
            }
            Log.d(TAG, "Loading image: " + imageUrl);

            final String finalUrl = imageUrl;
            Glide.with(context)
                    .load(finalUrl)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model,
                                                    com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                    boolean isFirstResource) {
                            Log.e("IMG_LOAD_ERROR", "Failed to load image: " + finalUrl, e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                                       com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                       com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            Log.d("IMG_LOAD_SUCCESS", "Successfully loaded image: " + finalUrl);
                            return false;
                        }
                    })
                    .into(vh.hinhanh);

            vh.setItemClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int pos, boolean isLongClick) {
                    // ✅ Kiểm tra tồn kho trước khi cho phép click
                    int tonkho = sanPham.getSoluongtonkho();
                    if (tonkho <= 0) {
                        // Sản phẩm hết hàng - hiển thị thông báo
                        android.widget.Toast.makeText(context, "Sản phẩm hiện đã hết hàng!", android.widget.Toast.LENGTH_SHORT).show();
                        return; // Không làm gì thêm
                    }

                    if (!isLongClick) {
                        Intent intent = new Intent(context, ChiTietActivity.class);
                        intent.putExtra("chitiet", sanPham);
                         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        // You can pass data here if needed
                        context.startActivity(intent);
                    }
                }
            });
        } else {
            // 🔹 LoadingViewHolder – có thể hiển thị ProgressBar hoặc animation nếu muốn
        }


    }


    @Override
    public int getItemViewType(int position) {
        return array.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_DATA;
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tensp, giasp, mota, txtOutOfStock;
        ImageView hinhanh;
        ItemClickListener itemClickListener;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tensp = itemView.findViewById(R.id.itemdt_ten);
            giasp = itemView.findViewById(R.id.itemdt_gia);
            mota = itemView.findViewById(R.id.itemdt_mota);
            hinhanh = itemView.findViewById(R.id.itemdt_image);
            txtOutOfStock = itemView.findViewById(R.id.item_label_out_of_stock);
            itemView.setOnClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.onClick(v, getAdapterPosition(), false);
            }
        }
    }

    // Simple holder for loading view
    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            // If item_loading has views (e.g., ProgressBar), you can bind them here
        }
    }

}
