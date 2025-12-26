package vn.duytruong.appbandienthoai.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.activity.ChiTietDonHangActivity;
import vn.duytruong.appbandienthoai.model.DonHang;
import vn.duytruong.appbandienthoai.utils.Utils;

public class DonHangAdapter extends RecyclerView.Adapter<DonHangAdapter.ViewHolder> {
    private Context context;
    private List<DonHang> list;
    private boolean isAdmin = false; // default false

    // Volley queue for lightweight requests to fetch preview image
    private RequestQueue requestQueue;
    // Cache mapping madonhang -> hinhanh (first image filename) to avoid duplicate requests
    private Map<String, String> previewImageCache = new HashMap<>();

    // Existing constructor (kept for compatibility)
    public DonHangAdapter(Context context, List<DonHang> list) {
        this.context = context;
        this.list = list;
        this.isAdmin = false;
        this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    // New constructor that allows specifying admin mode
    public DonHangAdapter(Context context, List<DonHang> list, boolean isAdmin) {
        this.context = context;
        this.list = list;
        this.isAdmin = isAdmin;
        this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use admin item layout (card) rather than whole activity layout to avoid duplicate activity widgets
        int layout = isAdmin ? R.layout.item_donhang_admin : R.layout.item_don_hang;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonHang donHang = list.get(position);
        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");

        // Debug log: which position is being bound and total size
        android.util.Log.d("DonHangAdapter", "onBindViewHolder position=" + position + " total=" + getItemCount() + " madonhang=" + donHang.getMadonhang());

        if (isAdmin) {
            if (holder.tvMaDonHang != null) holder.tvMaDonHang.setText("Mã: " + safeString(donHang.getMadonhang()));
            if (holder.tvTrangThai != null) holder.tvTrangThai.setText("Trạng thái: " + formatTrangThai(donHang.getTrangthai()));
            if (holder.tvNgayDat != null) holder.tvNgayDat.setText("Ngày đặt: " + safeString(donHang.getNgaydat()));
            if (holder.tvNgayGiao != null) holder.tvNgayGiao.setText("Ngày giao dự kiến: " + safeString(donHang.getNgaygiaodukien()));
            if (holder.tvDiaChi != null) holder.tvDiaChi.setText("Địa chỉ: " + safeString(donHang.getDiachi()));
            if (holder.tvSoDienThoai != null) holder.tvSoDienThoai.setText("SĐT: " + safeString(donHang.getSodienthoai()));

            long tongtien = parseLongSafe(donHang.getTongtien());
            if (holder.tvTongTien != null) holder.tvTongTien.setText("Tổng tiền: " + decimalFormat.format(tongtien) + " ₫");

            // Bind preview quantity (use total soluong returned by API)
            if (holder.tvPreviewQuantity != null) {
                holder.tvPreviewQuantity.setText("Số lượng: " + donHang.getSoluong() + " SP");
            }

            // Set tag so async response can verify holder still represents this order
            String madon = safeString(donHang.getMadonhang());
            holder.itemView.setTag(madon);

            // If we have cached image filename for this order, load it
            if (madon.length() > 0 && previewImageCache.containsKey(madon)) {
                String img = previewImageCache.get(madon);
                String url = img.startsWith("http") ? img : (Utils.BASE_URL + "images/" + img);
                if (holder.imgPreview != null) {
                    Glide.with(context).load(url).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_foreground).into(holder.imgPreview);
                }
            } else {
                // Otherwise request the order details to get first item's image
                if (madon.length() > 0) {
                    try {
                        String enc = URLEncoder.encode(madon, "UTF-8");
                        String url = Utils.URL_GET_CHI_TIET_DON_HANG + "?madonhang=" + enc;

                        StringRequest sr = new StringRequest(Request.Method.GET, url,
                                response -> {
                                    try {
                                        JSONObject jo = new JSONObject(response);
                                        if (jo.optBoolean("success", false)) {
                                            JSONArray items = jo.optJSONArray("items");
                                            if (items != null && items.length() > 0) {
                                                JSONObject first = items.getJSONObject(0);
                                                String hinhanh = first.optString("hinhanh", "");
                                                if (hinhanh != null && !hinhanh.isEmpty()) {
                                                    previewImageCache.put(madon, hinhanh);
                                                    // Only update the view if holder still bound to this madon
                                                    Object tag = holder.itemView.getTag();
                                                    if (tag != null && tag.equals(madon) && holder.imgPreview != null) {
                                                        // Check if URL already starts with http to avoid duplicate base URL
                                                        String full = hinhanh.startsWith("http") ? hinhanh : (Utils.BASE_URL + "images/" + hinhanh);
                                                        Glide.with(context).load(full).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_foreground).into(holder.imgPreview);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        // ignore parse errors for preview
                                    }
                                },
                                error -> {
                                    // ignore network errors for preview
                                });

                        requestQueue.add(sr);
                    } catch (Exception e) {
                        // encoding error — ignore
                    }
                }
            }

        } else {
            // Bind fields for item_don_hang.xml
            if (holder.tvMaDonHang != null) holder.tvMaDonHang.setText("Mã: " + safeString(donHang.getMadonhang()));
            if (holder.tvTrangThai != null) holder.tvTrangThai.setText(formatTrangThai(donHang.getTrangthai()));
            if (holder.tvNgayDat != null) holder.tvNgayDat.setText("Ngày đặt: " + safeString(donHang.getNgaydat()));
            if (holder.tvNgayGiao != null) holder.tvNgayGiao.setText("Ngày giao: " + safeString(donHang.getNgaygiaodukien()));
            if (holder.tvSoLuong != null) holder.tvSoLuong.setText("Số lượng: " + donHang.getSoluong() + " SP");

            long tongtien = parseLongSafe(donHang.getTongtien());
            if (holder.tvTongTien != null) holder.tvTongTien.setText(decimalFormat.format(tongtien) + " ₫");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChiTietDonHangActivity.class);
                intent.putExtra("madonhang", donHang.getMadonhang());
                // If the adapter was created with application context, starting an activity requires FLAG_ACTIVITY_NEW_TASK
                if (!(context instanceof android.app.Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        int size = (list != null) ? list.size() : 0;
        android.util.Log.d("DonHangAdapter", "getItemCount = " + size);
        return size;
    }

    private String safeString(String s) {
        return (s == null) ? "" : s;
    }

    private long parseLongSafe(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return 0L;
            return Long.parseLong(s.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0L;
        }
    }

    // Map server 'trangthai' (which may be a number like "1","2" or a text) to a human-readable label.
    private String formatTrangThai(String trangthai) {
        if (trangthai == null) return "";
        String t = trangthai.trim();
        if (t.isEmpty()) return "";
        // Try parse as integer (server may store 1..N)
        try {
            int v = Integer.parseInt(t);
            if (v >= 1 && v <= Utils.STATUS_LABELS.length) {
                return Utils.STATUS_LABELS[v - 1];
            }
        } catch (Exception ignored) {}

        // Fallback: inspect text for keywords and map to known labels
        String lower = t.toLowerCase();

        // Chuẩn hóa: Loại bỏ các suffix phương thức thanh toán như "PayPal", "VNPay"
        // VD: "Đã thanh toán PayPal" -> "Đã thanh toán"
        if (lower.contains("đã thanh toán") || lower.contains("da thanh toan")) {
            return "Đã thanh toán";
        }

        if (lower.contains("hủy") || lower.contains("huy")) return Utils.STATUS_LABELS[4];
        if (lower.contains("thành công") || lower.contains("thanh cong")) return Utils.STATUS_LABELS[3];
        if (lower.contains("giao")) return Utils.STATUS_LABELS[2];
        if (lower.contains("chuẩn bị") || lower.contains("chuan bi")) return Utils.STATUS_LABELS[1];

        // Otherwise return original string (already human-readable)
        return t;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Shared IDs present in both layouts
        TextView tvMaDonHang, tvTrangThai, tvNgayDat, tvNgayGiao, tvTongTien;
        // Fields only in normal layout
        TextView tvSoLuong;
        // Fields in admin layout
        TextView tvDiaChi, tvSoDienThoai;
        ImageView imgPreview;
        TextView tvPreviewQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaDonHang = itemView.findViewById(R.id.tvMaDonHang);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvNgayDat = itemView.findViewById(R.id.tvNgayDat);
            tvNgayGiao = itemView.findViewById(R.id.tvNgayGiao);
            tvTongTien = itemView.findViewById(R.id.tvTongTien);

            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);

            // IDs inside activity_chi_tiet_don_hang.xml / admin layout
            tvDiaChi = itemView.findViewById(R.id.tvDiaChi);
            tvSoDienThoai = itemView.findViewById(R.id.tvSoDienThoai);

            // Admin preview views (may be null in non-admin layout)
            imgPreview = itemView.findViewById(R.id.imgPreview);
            tvPreviewQuantity = itemView.findViewById(R.id.tvPreviewQuantity);
        }
    }
}
