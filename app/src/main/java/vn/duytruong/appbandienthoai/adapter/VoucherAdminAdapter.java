package vn.duytruong.appbandienthoai.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.model.Voucher;

public class VoucherAdminAdapter extends RecyclerView.Adapter<VoucherAdminAdapter.VoucherViewHolder> {

    private Context context;
    private List<Voucher> voucherList;
    private VoucherAdminListener listener;

    public interface VoucherAdminListener {
        void onEdit(Voucher voucher);
        void onDelete(Voucher voucher);
        void onToggle(Voucher voucher);
    }

    public VoucherAdminAdapter(Context context, List<Voucher> voucherList, VoucherAdminListener listener) {
        this.context = context;
        this.voucherList = voucherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_voucher_admin, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);

        holder.tvMaVoucher.setText(voucher.getMa_voucher());
        holder.tvTenVoucher.setText(voucher.getTen_voucher());

        // Hiển thị loại và giá trị giảm
        String giaTriText = "";
        DecimalFormat decimalFormat = new DecimalFormat("#,###");

        if ("percent".equals(voucher.getLoai_giam())) {
            giaTriText = "Giảm " + voucher.getGia_tri_giam() + "%";
            if (voucher.getGiam_toi_da() != null) {
                giaTriText += " (max " + decimalFormat.format(voucher.getGiam_toi_da()) + " ₫)";
            }
        } else if ("fixed".equals(voucher.getLoai_giam())) {
            giaTriText = "Giảm " + decimalFormat.format(voucher.getGia_tri_giam()) + " ₫";
        } else {
            giaTriText = "Miễn phí vận chuyển";
        }
        holder.tvGiaTriGiam.setText(giaTriText);

        holder.tvDieuKien.setText("Đơn tối thiểu: " + decimalFormat.format(voucher.getDon_toi_thieu()) + " ₫");
        holder.tvDieuKien.setText("Đơn tối thiểu: " + decimalFormat.format(voucher.getDon_toi_thieu()) + "đ");

        // Số lượng
        String soLuongText;
        if (voucher.getSo_luong() != null) {
            soLuongText = voucher.getDa_su_dung() + "/" + voucher.getSo_luong();
        } else {
            soLuongText = voucher.getDa_su_dung() + "/∞";
        }
        holder.tvSoLuong.setText("Đã dùng: " + soLuongText);

        // Ngày hết hạn
        try {
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat sdfOutput = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdfInput.parse(voucher.getNgay_het_han());
            holder.tvNgayHetHan.setText("HSD: " + sdfOutput.format(date));
        } catch (ParseException e) {
            holder.tvNgayHetHan.setText("HSD: " + voucher.getNgay_het_han());
        }

        // Trạng thái
        String status = getVoucherStatus(voucher);
        holder.tvTrangThai.setText(status);

        if ("Hoạt động".equals(status)) {
            holder.tvTrangThai.setBackgroundColor(Color.parseColor("#4CAF50"));
            holder.tvTrangThai.setTextColor(Color.WHITE);
        } else if ("Hết hạn".equals(status)) {
            holder.tvTrangThai.setBackgroundColor(Color.parseColor("#F44336"));
            holder.tvTrangThai.setTextColor(Color.WHITE);
        } else if ("Hết lượt".equals(status)) {
            holder.tvTrangThai.setBackgroundColor(Color.parseColor("#FF9800"));
            holder.tvTrangThai.setTextColor(Color.WHITE);
        } else {
            holder.tvTrangThai.setBackgroundColor(Color.parseColor("#666666"));
            holder.tvTrangThai.setTextColor(Color.WHITE);
        }

        // Nút toggle
        if (voucher.getTrang_thai() == 1) {
            holder.btnToggle.setText("Tắt");
            holder.btnToggle.setBackgroundColor(Color.parseColor("#FF9800"));
        } else {
            holder.btnToggle.setText("Bật");
            holder.btnToggle.setBackgroundColor(Color.parseColor("#4CAF50"));
        }

        // Click listeners
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(voucher));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(voucher));
        holder.btnToggle.setOnClickListener(v -> listener.onToggle(voucher));
    }

    @Override
    public int getItemCount() {
        return voucherList != null ? voucherList.size() : 0;
    }

    private String getVoucherStatus(Voucher voucher) {
        if (voucher.getTrang_thai() == 0) {
            return "Đã tắt";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date expiry = sdf.parse(voucher.getNgay_het_han());
            if (expiry != null && expiry.before(new Date())) {
                return "Hết hạn";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (voucher.getSo_luong() != null && voucher.getDa_su_dung() >= voucher.getSo_luong()) {
            return "Hết lượt";
        }

        return "Hoạt động";
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaVoucher, tvTenVoucher, tvGiaTriGiam, tvDieuKien;
        TextView tvSoLuong, tvNgayHetHan, tvTrangThai;
        Button btnEdit, btnDelete, btnToggle;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaVoucher = itemView.findViewById(R.id.tvMaVoucher);
            tvTenVoucher = itemView.findViewById(R.id.tvTenVoucher);
            tvGiaTriGiam = itemView.findViewById(R.id.tvGiaTriGiam);
            tvDieuKien = itemView.findViewById(R.id.tvDieuKien);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
            tvNgayHetHan = itemView.findViewById(R.id.tvNgayHetHan);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnToggle = itemView.findViewById(R.id.btnToggle);
        }
    }
}

