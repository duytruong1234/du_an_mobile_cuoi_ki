package vn.duytruong.appbandienthoai.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.duytruong.appbandienthoai.R;
import vn.duytruong.appbandienthoai.model.User;

public class NguoiDungAdapter extends RecyclerView.Adapter<NguoiDungAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onDeleteUser(User user, int position);
        void onToggleStatus(User user, int position);
    }

    public NguoiDungAdapter(Context context, List<User> userList, OnUserActionListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_nguoi_dung, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvUsername.setText(user.getUsername());
        holder.tvEmail.setText(user.getEmail());
        holder.tvMobile.setText(user.getMobile());

        // Hiển thị loại tài khoản
        String loginType = user.isGoogleAccount() ? "Google" : "Thường";
        holder.tvLoginType.setText("Loại: " + loginType);

        // Hiển thị role
        String roleText = user.getRole() == 1 ? "Admin" : "User";
        holder.tvRole.setText("Vai trò: " + roleText);

        // Hiển thị badge admin
        holder.tvAdminBadge.setVisibility(user.getRole() == 1 ? View.VISIBLE : View.GONE);

        // Hiển thị badge khóa nếu tài khoản bị khóa
        holder.tvStatusBadge.setVisibility(user.isLocked() ? View.VISIBLE : View.GONE);

        // Set switch trạng thái tài khoản
        holder.switchStatus.setOnCheckedChangeListener(null); // Clear listener trước
        holder.switchStatus.setChecked(user.isActive()); // Checked = Active, Unchecked = Locked
        holder.switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggleStatus(user, position);
            }
        });

        // Xóa người dùng
        holder.imgDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteUser(user, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail, tvMobile, tvLoginType, tvAdminBadge, tvRole, tvStatusBadge;
        Switch switchStatus;
        ImageView imgDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvMobile = itemView.findViewById(R.id.tvMobile);
            tvLoginType = itemView.findViewById(R.id.tvLoginType);
            tvAdminBadge = itemView.findViewById(R.id.tvAdminBadge);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            switchStatus = itemView.findViewById(R.id.switchStatus);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}

