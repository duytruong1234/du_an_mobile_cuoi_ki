package vn.duytruong.appbandienthoai.model;

public class ChiTietDonHang {
    private int id;
    private int iddonhang;
    private int idsp;
    private String tensp;
    private String hinhanh;
    private int soluong;
    private String gia;
    private String thanhtien;

    public ChiTietDonHang() {
    }

    public ChiTietDonHang(int id, int iddonhang, int idsp, String tensp, String hinhanh,
                          int soluong, String gia, String thanhtien) {
        this.id = id;
        this.iddonhang = iddonhang;
        this.idsp = idsp;
        this.tensp = tensp;
        this.hinhanh = hinhanh;
        this.soluong = soluong;
        this.gia = gia;
        this.thanhtien = thanhtien;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIddonhang() {
        return iddonhang;
    }

    public void setIddonhang(int iddonhang) {
        this.iddonhang = iddonhang;
    }

    public int getIdsp() {
        return idsp;
    }

    public void setIdsp(int idsp) {
        this.idsp = idsp;
    }

    public String getTensp() {
        return tensp;
    }

    public void setTensp(String tensp) {
        this.tensp = tensp;
    }

    public String getHinhanh() {
        return hinhanh;
    }

    public void setHinhanh(String hinhanh) {
        this.hinhanh = hinhanh;
    }

    public int getSoluong() {
        return soluong;
    }

    public void setSoluong(int soluong) {
        this.soluong = soluong;
    }

    public String getGia() {
        return gia;
    }

    public void setGia(String gia) {
        this.gia = gia;
    }

    public String getThanhtien() {
        return thanhtien;
    }

    public void setThanhtien(String thanhtien) {
        this.thanhtien = thanhtien;
    }
}

