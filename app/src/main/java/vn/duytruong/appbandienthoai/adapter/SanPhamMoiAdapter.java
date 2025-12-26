package vn.duytruong.appbandienthoai.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;
import java.util.List;

import vn.duytruong.appbandienthoai.Interface.ItemClickListener;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.activity.ChiTietActivity;
import vn.duytruong.appbandienthoai.model.SanPhamEvent;
import vn.duytruong.appbandienthoai.model.SanPhamMoi;
import vn.duytruong.appbandienthoai.utils.Utils;

public class SanPhamMoiAdapter extends RecyclerView.Adapter<SanPhamMoiAdapter.MyViewHolder> {
    Context context;
    List<SanPhamMoi> array;  // danh sách sản phẩm
    boolean isAdminMode; //  Thêm flag để biết admin đang dùng hay không

    // Constructor mặc định (cho user thường)
    public SanPhamMoiAdapter(Context context, List<SanPhamMoi> array) {
        this.context = context;
        this.array = array;
        this.isAdminMode = false; // Mặc định là user thường
    }

    //  Constructor mới cho admin
    public SanPhamMoiAdapter(Context context, List<SanPhamMoi> array, boolean isAdminMode) {
        this.context = context;
        this.array = array;
        this.isAdminMode = isAdminMode;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sp_moi, parent, false);
        return new MyViewHolder(item);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnCreateContextMenuListener {
        TextView txtgia, txtten, txtOutOfStock;
        ImageView imghinhanh;
        private ItemClickListener itemClickListener;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtgia = itemView.findViewById(R.id.item_gia);
            txtten = itemView.findViewById(R.id.item_tensp);
            imghinhanh = itemView.findViewById(R.id.item_image_main);
            txtOutOfStock = itemView.findViewById(R.id.item_label_out_of_stock);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onClick(view, getAdapterPosition(), false);
        }

        @Override
        public boolean onLongClick(View view) {
            itemClickListener.onClick(view, getAdapterPosition(), true);
            return false;
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.add(0, 0, getAdapterPosition(), "Sửa");
            contextMenu.add(0, 1, getAdapterPosition(), "Xóa");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (array == null || position < 0 || position >= array.size()) return;
        SanPhamMoi sanPhamMoi = array.get(position);

        // Kiểm tra tồn kho để làm mờ sản phẩm hết hàng
        int tonkho = sanPhamMoi.getSoluongtonkho();
        boolean isOutOfStock = (tonkho <= 0);

        //  Làm mờ toàn bộ item nếu hết hàng
        if (isOutOfStock) {
            holder.itemView.setAlpha(0.4f); // Làm mờ 60%
            holder.txtOutOfStock.setVisibility(View.VISIBLE); // Hiển thị nhãn HẾT HÀNG

            // Nếu là admin, vẫn cho phép click để sửa/xóa
            if (isAdminMode) {
                holder.itemView.setEnabled(true); // Admin vẫn click được
                holder.itemView.setClickable(true);
            } else {
                holder.itemView.setEnabled(false); // User thường không click được
                holder.itemView.setClickable(false);
            }
        } else {
            holder.itemView.setAlpha(1.0f); // Hiển thị bình thường
            holder.itemView.setEnabled(true);
            holder.itemView.setClickable(true);
            holder.txtOutOfStock.setVisibility(View.GONE); // Ẩn nhãn HẾT HÀNG
        }

        holder.txtten.setText(sanPhamMoi.getTensp() != null ? sanPhamMoi.getTensp() : "");
        String giaStr = sanPhamMoi.getGiasp();
        String giaHienThi;
        // Defensive: if price equals the search placeholder or contains "tìm kiếm", treat as missing
        String searchPlaceholder = context.getString(R.string.place_holder_search).toLowerCase();
        String searchLabel = context.getString(R.string.search).toLowerCase();
        if (giaStr != null) {
            String lower = giaStr.toLowerCase();
            if (lower.contains(searchPlaceholder) || lower.contains(searchLabel) || lower.contains("tìm kiếm")) {
                Log.w("SanPhamMoiAdapter", "Product price contains search placeholder, treating as missing: raw='" + giaStr + "' for product='" + sanPhamMoi.getTensp() + "'");
                giaStr = null; // force fallback
            }
        }
        if (giaStr != null) {
            try {
                double gia = Double.parseDouble(giaStr);
                DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
                giaHienThi = "Giá: " + decimalFormat.format(gia) + " ₫";
            } catch (Exception e) {
                giaHienThi = "Giá: Đang cập nhật";
            }
        } else {
            giaHienThi = "Giá: Đang cập nhật";
        }
        holder.txtgia.setText(giaHienThi);

        String url = sanPhamMoi.getHinhanh();
        if (url != null && !url.isEmpty()) {
            // Luôn lấy phần cuối cùng bắt đầu bằng http:// hoặc https:// nếu có nhiều lần xuất hiện
            int lastHttp = url.lastIndexOf("http://");
            int lastHttps = url.lastIndexOf("https://");
            if (lastHttp > 0) {
                url = url.substring(lastHttp);
            } else if (lastHttps > 0) {
                url = url.substring(lastHttps);
            } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
                // URL tương đối, cần thêm BASE_URL
                if (url.startsWith("images/")) {
                    url = url.substring(7); // bỏ "images/"
                }
                url = Utils.BASE_URL + "images/" + url;
            }

            final String finalUrl = url;
            Log.d("SanPhamMoiAdapter", "Loading image: " + finalUrl);
            Glide.with(context)
                    .load(finalUrl)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
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
                                                      com.bumptech.glide.load.DataSource dataSource,
                                                      boolean isFirstResource) {
                            Log.d("IMG_LOAD_SUCCESS", "Successfully loaded image: " + finalUrl);
                            return false;
                        }
                    })
                    .into(holder.imghinhanh);
        } else {
            Log.w("IMG_URL", "Image URL is null or empty for product: " + sanPhamMoi.getTensp());
            holder.imghinhanh.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int pos, boolean isLongClick) {
                int tonkho = sanPhamMoi.getSoluongtonkho();

                // Nếu là long-click (admin sửa/xóa) → luôn cho phép
                if (isLongClick) {
                    // Admin long-click để sửa/xóa - cho phép ngay cả khi hết hàng
                    EventBus.getDefault().postSticky(new SanPhamEvent(sanPhamMoi));
                    return;
                }

                // Nếu là short-click và hết hàng
                if (tonkho <= 0) {
                    // Nếu là admin → hiển thị thông báo nhưng không cho xem chi tiết
                    // Nếu là user thường → cũng hiển thị thông báo
                    android.widget.Toast.makeText(context, "Sản phẩm hiện đã hết hàng!", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                //  Short-click bình thường - xem chi tiết sản phẩm
                Log.d("SanPhamMoiAdapter", "Clicked product: " + sanPhamMoi.getTensp() + ", raw price='" + sanPhamMoi.getGiasp() + "'");
                Intent intent = new Intent(context, ChiTietActivity.class);
                intent.putExtra("chitiet", sanPhamMoi);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (array == null) ? 0 : array.size();
    }


}
