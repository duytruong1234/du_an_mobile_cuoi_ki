package vn.duytruong.appbandienthoai.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.model.Voucher;

public class VoucherUserAdapter extends RecyclerView.Adapter<VoucherUserAdapter.VoucherViewHolder> {

    private Context context;
    private List<Voucher> voucherList;
    private OnVoucherClickListener listener;
    private boolean isApplicableList; // true = có thể dùng, false = không thể dùng

    public interface OnVoucherClickListener {
        void onVoucherSelected(Voucher voucher);
    }

    public VoucherUserAdapter(Context context, List<Voucher> voucherList, boolean isApplicableList, OnVoucherClickListener listener) {
        this.context = context;
        this.voucherList = voucherList;
        this.isApplicableList = isApplicableList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_voucher_user, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        DecimalFormat formatter = new DecimalFormat("#,###");

        // Mã voucher
        holder.tvMaVoucher.setText(voucher.getMa_voucher());

        // Tên voucher
        holder.tvTenVoucher.setText(voucher.getTen_voucher());

        // Text giảm giá
        holder.tvTextGiam.setText(voucher.getText_giam());

        // Điều kiện
        holder.tvDieuKien.setText(voucher.getText_dieu_kien());

        // Số lượng còn lại
        if (voucher.getCon_lai() != null) {
            holder.tvConLai.setText("Còn " + voucher.getCon_lai() + " lượt");
        } else {
            holder.tvConLai.setText("Không giới hạn");
        }

        // Hạn sử dụng
        try {
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat sdfOutput = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdfInput.parse(voucher.getNgay_het_han());
            if (date != null) {
                holder.tvHetHan.setText("HSD: " + sdfOutput.format(date));
            }
        } catch (Exception e) {
            holder.tvHetHan.setText("HSD: " + voucher.getNgay_het_han());
        }

        // Xử lý UI theo loại voucher (có thể dùng / không thể dùng)
        if (isApplicableList) {
            // Voucher có thể sử dụng
            holder.btnChonVoucher.setVisibility(View.VISIBLE);
            holder.tvKhongDung.setVisibility(View.GONE);
            holder.tvLyDoKhongDung.setVisibility(View.GONE);

            holder.btnChonVoucher.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVoucherSelected(voucher);
                }
            });
        } else {
            // Voucher chưa đủ điều kiện
            holder.btnChonVoucher.setVisibility(View.GONE);
            holder.tvKhongDung.setVisibility(View.VISIBLE);
            holder.tvLyDoKhongDung.setVisibility(View.VISIBLE);

            // Hiển thị lý do không dùng được
            if (voucher.getThieu() > 0) {
                String lyDo = " Còn thiếu " + formatter.format(voucher.getThieu()) + " ₫";
                holder.tvLyDoKhongDung.setText(lyDo);
            } else {
                holder.tvLyDoKhongDung.setText("Chưa đủ điều kiện");
            }

            // Làm mờ item
            holder.itemView.setAlpha(0.6f);
        }
    }

    @Override
    public int getItemCount() {
        return voucherList != null ? voucherList.size() : 0;
    }

    public static class VoucherViewHolder extends RecyclerView.ViewHolder {
        ImageView imgVoucherIcon;
        TextView tvMaVoucher, tvTenVoucher, tvTextGiam, tvDieuKien;
        TextView tvConLai, tvHetHan, tvLyDoKhongDung, tvKhongDung;
        Button btnChonVoucher;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            imgVoucherIcon = itemView.findViewById(R.id.imgVoucherIcon);
            tvMaVoucher = itemView.findViewById(R.id.tvMaVoucher);
            tvTenVoucher = itemView.findViewById(R.id.tvTenVoucher);
            tvTextGiam = itemView.findViewById(R.id.tvTextGiam);
            tvDieuKien = itemView.findViewById(R.id.tvDieuKien);
            tvConLai = itemView.findViewById(R.id.tvConLai);
            tvHetHan = itemView.findViewById(R.id.tvHetHan);
            tvLyDoKhongDung = itemView.findViewById(R.id.tvLyDoKhongDung);
            tvKhongDung = itemView.findViewById(R.id.tvKhongDung);
            btnChonVoucher = itemView.findViewById(R.id.btnChonVoucher);
        }
    }
}

