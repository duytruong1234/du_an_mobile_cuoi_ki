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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.model.ChiTietDonHang;
import vn.duytruong.appbandienthoai.utils.Utils;

public class ChiTietDonHangAdapter extends RecyclerView.Adapter<ChiTietDonHangAdapter.ViewHolder> {
    private Context context;
    private List<ChiTietDonHang> list;

    public ChiTietDonHangAdapter(Context context, List<ChiTietDonHang> list) {
        this.context = context;
        // Ensure list is non-null to avoid NPEs later
        this.list = (list != null) ? list : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chi_tiet_don_hang, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChiTietDonHang chiTiet = list.get(position);

        holder.tvTenSP.setText(chiTiet.getTensp());
        holder.tvSoLuong.setText("x" + chiTiet.getSoluong());

        // Parse giá and thanhtien safely (API may return decimals like "1863000.00")
        long giaLong = 0;
        long thanhtienLong = 0;
        try {
            String giaStr = chiTiet.getGia();
            if (giaStr == null || giaStr.trim().isEmpty()) giaStr = "0";
            // Use BigDecimal to parse securely and avoid NumberFormatException for decimals
            BigDecimal g = new BigDecimal(giaStr);
            giaLong = g.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
        } catch (Exception e) {
            // fallback: try to parse as long or use 0
            try {
                giaLong = Long.parseLong(chiTiet.getGia().replaceAll("[^0-9]", ""));
            } catch (Exception ex) {
                giaLong = 0;
            }
        }

        try {
            String ttStr = chiTiet.getThanhtien();
            if (ttStr == null || ttStr.trim().isEmpty()) ttStr = "0";
            BigDecimal t = new BigDecimal(ttStr);
            thanhtienLong = t.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
        } catch (Exception e) {
            try {
                thanhtienLong = Long.parseLong(chiTiet.getThanhtien().replaceAll("[^0-9]", ""));
            } catch (Exception ex) {
                thanhtienLong = 0;
            }
        }

        // Format giá
        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");

        holder.tvGia.setText(decimalFormat.format(giaLong) + " ₫");
        holder.tvThanhTien.setText(decimalFormat.format(thanhtienLong) + " ₫");

        // Load hình ảnh (Glide is async — safe for main thread)
        String imageUrl = chiTiet.getHinhanh();
        imageUrl = imageUrl.startsWith("http") ? imageUrl : (Utils.BASE_URL + "images/" + imageUrl);
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.imgHinhAnh);
    }

    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHinhAnh;
        TextView tvTenSP, tvSoLuong, tvGia, tvThanhTien;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHinhAnh = itemView.findViewById(R.id.imgHinhAnh);
            tvTenSP = itemView.findViewById(R.id.tvTenSP);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
            tvGia = itemView.findViewById(R.id.tvGia);
            tvThanhTien = itemView.findViewById(R.id.tvThanhTien);
        }
    }
}
