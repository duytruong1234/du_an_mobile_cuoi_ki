package vn.duytruong.appbandienthoai.model;

public class LoaiSp {
    int id;
    String tensanpham;
    String hinhanh;

    // Constructor mặc định cho Gson parse JSON
    public LoaiSp() {
    }

    // Constructor để tạo thủ công (cho Trang chủ, Đăng xuất...)
    public LoaiSp(String tensanpham, String hinhanh) {
        this.tensanpham = tensanpham;
        this.hinhanh = hinhanh;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTensanpham() {
        return tensanpham;
    }

    public void setTensanpham(String tensanpham) {
        this.tensanpham = tensanpham;
    }

    public String getHinhanh() {
        return hinhanh;
    }

    public void setHinhanh(String hinhanh) {
        this.hinhanh = hinhanh;
    }
}
