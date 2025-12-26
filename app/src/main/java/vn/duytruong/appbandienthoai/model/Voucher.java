package vn.duytruong.appbandienthoai.model;


public class Voucher {
    private int id;
    private String ma_voucher;
    private String ten_voucher;
    private String mo_ta;
    private String loai_giam; // percent, fixed, freeship
    private double gia_tri_giam;
    private Double giam_toi_da;
    private double don_toi_thieu;
    private String ap_dung_cho; // all, new_user, old_user, first_order
    private Integer so_luong;
    private int da_su_dung;
    private int gioi_han_moi_user;
    private String ngay_bat_dau;
    private String ngay_het_han;
    private int trang_thai;
    private String ngay_tao;

    // Các field bổ sung từ API
    private String text_giam;
    private String text_dieu_kien;
    private Integer con_lai;
    private int user_used_count;
    private int con_luot;
    private boolean co_the_dung;
    private double thieu;
    private boolean is_expired;
    private boolean is_active;

    // Constructor mặc định
    public Voucher() {
    }

    // Constructor đầy đủ
    public Voucher(int id, String ma_voucher, String ten_voucher, String mo_ta,
                   String loai_giam, double gia_tri_giam, Double giam_toi_da,
                   double don_toi_thieu) {
        this.id = id;
        this.ma_voucher = ma_voucher;
        this.ten_voucher = ten_voucher;
        this.mo_ta = mo_ta;
        this.loai_giam = loai_giam;
        this.gia_tri_giam = gia_tri_giam;
        this.giam_toi_da = giam_toi_da;
        this.don_toi_thieu = don_toi_thieu;
    }

    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMa_voucher() {
        return ma_voucher;
    }

    public void setMa_voucher(String ma_voucher) {
        this.ma_voucher = ma_voucher;
    }

    public String getTen_voucher() {
        return ten_voucher;
    }

    public void setTen_voucher(String ten_voucher) {
        this.ten_voucher = ten_voucher;
    }

    public String getMo_ta() {
        return mo_ta;
    }

    public void setMo_ta(String mo_ta) {
        this.mo_ta = mo_ta;
    }

    public String getLoai_giam() {
        return loai_giam;
    }

    public void setLoai_giam(String loai_giam) {
        this.loai_giam = loai_giam;
    }

    public double getGia_tri_giam() {
        return gia_tri_giam;
    }

    public void setGia_tri_giam(double gia_tri_giam) {
        this.gia_tri_giam = gia_tri_giam;
    }

    public Double getGiam_toi_da() {
        return giam_toi_da;
    }

    public void setGiam_toi_da(Double giam_toi_da) {
        this.giam_toi_da = giam_toi_da;
    }

    public double getDon_toi_thieu() {
        return don_toi_thieu;
    }

    public void setDon_toi_thieu(double don_toi_thieu) {
        this.don_toi_thieu = don_toi_thieu;
    }

    public String getAp_dung_cho() {
        return ap_dung_cho;
    }

    public void setAp_dung_cho(String ap_dung_cho) {
        this.ap_dung_cho = ap_dung_cho;
    }

    public Integer getSo_luong() {
        return so_luong;
    }

    public void setSo_luong(Integer so_luong) {
        this.so_luong = so_luong;
    }

    public int getDa_su_dung() {
        return da_su_dung;
    }

    public void setDa_su_dung(int da_su_dung) {
        this.da_su_dung = da_su_dung;
    }

    public int getGioi_han_moi_user() {
        return gioi_han_moi_user;
    }

    public void setGioi_han_moi_user(int gioi_han_moi_user) {
        this.gioi_han_moi_user = gioi_han_moi_user;
    }

    public String getNgay_bat_dau() {
        return ngay_bat_dau;
    }

    public void setNgay_bat_dau(String ngay_bat_dau) {
        this.ngay_bat_dau = ngay_bat_dau;
    }

    public String getNgay_het_han() {
        return ngay_het_han;
    }

    public void setNgay_het_han(String ngay_het_han) {
        this.ngay_het_han = ngay_het_han;
    }

    public int getTrang_thai() {
        return trang_thai;
    }

    public void setTrang_thai(int trang_thai) {
        this.trang_thai = trang_thai;
    }

    public String getText_giam() {
        return text_giam;
    }

    public void setText_giam(String text_giam) {
        this.text_giam = text_giam;
    }

    public String getText_dieu_kien() {
        return text_dieu_kien;
    }

    public void setText_dieu_kien(String text_dieu_kien) {
        this.text_dieu_kien = text_dieu_kien;
    }

    public Integer getCon_lai() {
        return con_lai;
    }

    public void setCon_lai(Integer con_lai) {
        this.con_lai = con_lai;
    }

    public int getUser_used_count() {
        return user_used_count;
    }

    public void setUser_used_count(int user_used_count) {
        this.user_used_count = user_used_count;
    }

    public int getCon_luot() {
        return con_luot;
    }

    public void setCon_luot(int con_luot) {
        this.con_luot = con_luot;
    }

    public boolean isCo_the_dung() {
        return co_the_dung;
    }

    public void setCo_the_dung(boolean co_the_dung) {
        this.co_the_dung = co_the_dung;
    }

    public double getThieu() {
        return thieu;
    }

    public void setThieu(double thieu) {
        this.thieu = thieu;
    }

    public String getNgay_tao() {
        return ngay_tao;
    }

    public void setNgay_tao(String ngay_tao) {
        this.ngay_tao = ngay_tao;
    }

    public boolean isIs_expired() {
        return is_expired;
    }

    public void setIs_expired(boolean is_expired) {
        this.is_expired = is_expired;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }
}

