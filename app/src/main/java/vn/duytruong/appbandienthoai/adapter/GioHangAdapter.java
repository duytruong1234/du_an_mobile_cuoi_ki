package vn.duytruong.appbandienthoai.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;
import java.util.List;

import vn.duytruong.appbandienthoai.Interface.IImageClickListenner;
import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.model.EventBus.TinhTongEvent;
import vn.duytruong.appbandienthoai.model.GioHang;
import vn.duytruong.appbandienthoai.utils.Utils;

public class GioHangAdapter extends RecyclerView.Adapter<GioHangAdapter.MyViewHolder> {
    Context context;
    List<GioHang> gioHangList;

    public GioHangAdapter(Context context, List<GioHang> gioHangList) {
        this.context = context;
        this.gioHangList = gioHangList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_giohang, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        GioHang gioHang = gioHangList.get(position);
        holder.item_giohang_tensp.setText(gioHang.getTensp());
        holder.item_giohang_soluong.setText(String.valueOf(gioHang.getSoluong()));

        // Xử lý URL hình ảnh
        String imageUrl = gioHang.getHinhsp();
        Log.d("GioHangAdapter", "Original image URL: " + imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            imageUrl = imageUrl.trim();

            // Kiểm tra xem có bị duplicate URL không (chứa http:// nhiều lần)
            int firstHttp = imageUrl.indexOf("http://");
            int firstHttps = imageUrl.indexOf("https://");
            int secondHttp = imageUrl.indexOf("http://", firstHttp + 7);
            int secondHttps = imageUrl.indexOf("https://", firstHttps + 8);

            // Nếu có duplicate (xuất hiện http:// hoặc https:// nhiều lần)
            if ((firstHttp >= 0 && secondHttp > firstHttp) || (firstHttps >= 0 && secondHttps > firstHttps)) {
                // Lấy phần URL cuối cùng (phần đúng)
                int lastHttpIndex = Math.max(secondHttp, secondHttps);
                if (lastHttpIndex > 0) {
                    imageUrl = imageUrl.substring(lastHttpIndex);
                    Log.d("GioHangAdapter", "Removed duplicate URL prefix");
                }
            }
            // Nếu URL đã là đầy đủ (có http/https ở đầu), dùng trực tiếp
            else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                Log.d("GioHangAdapter", "URL is already full");
            }
            // URL tương đối, cần thêm BASE_URL
            else {
                if (imageUrl.startsWith("images/")) {
                    imageUrl = imageUrl.substring(7); // bỏ "images/"
                }
                if (imageUrl.startsWith("/")) {
                    imageUrl = imageUrl.substring(1); // bỏ "/" đầu
                }
                imageUrl = Utils.BASE_URL + "images/" + imageUrl;
                Log.d("GioHangAdapter", "Added BASE_URL to relative path");
            }
        } else {
            imageUrl = "";
        }

        Log.d("GioHangAdapter", "Final cart image URL: " + imageUrl);

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.item_giohang_image);

        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");


        long gia = gioHang.getSoluong() * gioHang.getGiasp();
        holder.item_giohang_giasp2.setText(decimalFormat.format(gia) + " ₫");

        // Khởi tạo danh sách mua hàng nếu chưa có
        if (Utils.mangmuahang == null) {
            Utils.mangmuahang = new java.util.ArrayList<>();
        }

        // Reset checkbox state để tránh bug khi recycle view
        holder.checckBox.setOnCheckedChangeListener(null);
        holder.checckBox.setChecked(false);

        // Kiểm tra xem sản phẩm này đã được chọn chưa
        for (GioHang item : Utils.mangmuahang) {
            if (item.getIdsp() == gioHang.getIdsp()) {
                holder.checckBox.setChecked(true);
                break;
            }
        }

        // Set listener cho checkbox
        holder.checckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // Thêm vào danh sách mua hàng nếu chưa có
                    boolean exists = false;
                    for (GioHang item : Utils.mangmuahang) {
                        if (item.getIdsp() == gioHang.getIdsp()) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        Utils.mangmuahang.add(gioHang);
                    }
                } else {
                    // Xóa khỏi danh sách mua hàng
                    for (int i = 0; i < Utils.mangmuahang.size(); i++) {
                        if (Utils.mangmuahang.get(i).getIdsp() == gioHang.getIdsp()) {
                            Utils.mangmuahang.remove(i);
                            break;
                        }
                    }
                }
                // Gửi sự kiện để cập nhật tổng tiền
                EventBus.getDefault().postSticky(new TinhTongEvent());
            }
        });

        holder.setListenner(new IImageClickListenner() {
            @Override
            public void onImageClick(View view, int pos, int giatri) {
                Log.d("TAG", "onImageClick: " + pos + "..." + giatri);
                if (giatri == 1) {
                    if (gioHangList.get(pos).getSoluong() > 1) {
                        int soluongmoi = gioHangList.get(pos).getSoluong() - 1;
                        gioHangList.get(pos).setSoluong(soluongmoi);
                        holder.item_giohang_soluong.setText(gioHangList.get(pos).getSoluong() + " ");
                        long gia = gioHangList.get(pos).getSoluong() * gioHangList.get(pos).getGiasp();
                        holder.item_giohang_giasp2.setText(decimalFormat.format(gia) + " Đ");

                        // Cập nhật số lượng trong danh sách mua hàng nếu sản phẩm đã được check
                        for (int i = 0; i < Utils.mangmuahang.size(); i++) {
                            if (Utils.mangmuahang.get(i).getIdsp() == gioHang.getIdsp()) {
                                Utils.mangmuahang.get(i).setSoluong(soluongmoi);
                                break;
                            }
                        }
                        EventBus.getDefault().postSticky(new TinhTongEvent());
                    } else if(gioHangList.get(pos).getSoluong() == 1){
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                        builder.setTitle("Thông báo");
                        builder.setMessage("Bạn có muốn xóa sản phẩm này khỏi giỏ hàng");
                        builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Xóa khỏi danh sách mua hàng
                                for (int j = 0; j < Utils.mangmuahang.size(); j++) {
                                    if (Utils.mangmuahang.get(j).getIdsp() == gioHang.getIdsp()) {
                                        Utils.mangmuahang.remove(j);
                                        break;
                                    }
                                }
                                Utils.manggiohang.remove(pos);
                                notifyDataSetChanged();
                                EventBus.getDefault().postSticky(new TinhTongEvent());
                            }
                        });
                        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.show();
                    }
                } else if (giatri == 2) {
                    if (gioHangList.get(pos).getSoluong() < 11) {
                        int soluongmoi = gioHangList.get(pos).getSoluong() + 1;
                        gioHangList.get(pos).setSoluong(soluongmoi);
                        holder.item_giohang_soluong.setText(gioHangList.get(pos).getSoluong() + " ");
                        long gia = gioHangList.get(pos).getSoluong() * gioHangList.get(pos).getGiasp();
                        holder.item_giohang_giasp2.setText(decimalFormat.format(gia) + " Đ");

                        // Cập nhật số lượng trong danh sách mua hàng nếu sản phẩm đã được check
                        for (int i = 0; i < Utils.mangmuahang.size(); i++) {
                            if (Utils.mangmuahang.get(i).getIdsp() == gioHang.getIdsp()) {
                                Utils.mangmuahang.get(i).setSoluong(soluongmoi);
                                break;
                            }
                        }
                        EventBus.getDefault().postSticky(new TinhTongEvent());
                    }
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return gioHangList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView item_giohang_image;
        FrameLayout imgtru, imgcong;
        TextView item_giohang_tensp, item_giohang_soluong, item_giohang_giasp2;

        IImageClickListenner listenner;
        CheckBox checckBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            item_giohang_image = itemView.findViewById(R.id.item_giohang_image);
            item_giohang_tensp = itemView.findViewById(R.id.item_giohang_tensp);
            item_giohang_soluong = itemView.findViewById(R.id.item_giohang_soluong);
            item_giohang_giasp2 = itemView.findViewById(R.id.item_giohang_giasp2);

            imgtru = itemView.findViewById(R.id.item_giohang_tru);
            imgcong = itemView.findViewById(R.id.item_giohang_cong);
            imgtru.setOnClickListener(this);
            imgcong.setOnClickListener(this);
            checckBox = itemView.findViewById(R.id.item_giohang_check);
        }
        public void setListenner(IImageClickListenner listenner) {
            this.listenner = listenner;
        }
        @Override
        public void onClick(View view) {
            if (view == imgtru) {
                listenner.onImageClick(view, getAdapterPosition(), 1);
                // 1 tru
            } else if (view == imgcong) {
                // 2 cong
                listenner.onImageClick(view, getAdapterPosition(), 2);
            }
        }

    }
}
