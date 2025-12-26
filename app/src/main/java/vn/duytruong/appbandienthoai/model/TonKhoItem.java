package vn.duytruong.appbandienthoai.model;

import java.io.Serializable;

public class TonKhoItem implements Serializable {
    private int id;
    private String tensp;
    private String hinhanh;
    private String giasp;
    private int soluongtonkho;
    private String loaisanpham;
    private String tinhtrang;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTensp() { return tensp; }
    public void setTensp(String tensp) { this.tensp = tensp; }

    public String getHinhanh() { return hinhanh; }
    public void setHinhanh(String hinhanh) { this.hinhanh = hinhanh; }

    public String getGiasp() { return giasp; }
    public void setGiasp(String giasp) { this.giasp = giasp; }

    public int getSoluongtonkho() { return soluongtonkho; }
    public void setSoluongtonkho(int soluongtonkho) { this.soluongtonkho = soluongtonkho; }

    public String getLoaisanpham() { return loaisanpham; }
    public void setLoaisanpham(String loaisanpham) { this.loaisanpham = loaisanpham; }

    public String getTinhtrang() { return tinhtrang; }
    public void setTinhtrang(String tinhtrang) { this.tinhtrang = tinhtrang; }
}

