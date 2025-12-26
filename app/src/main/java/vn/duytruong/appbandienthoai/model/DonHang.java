package vn.duytruong.appbandienthoai.model;

public class DonHang {
    private int id;
    private String madonhang;
    private int iduser;
    private String diachi;
    private String sodienthoai;
    private int soluong;
    private String tongtien;
    private String ngaydat;
    private String ngaygiaodukien;
    private String trangthai;

    public DonHang() {
    }

    public DonHang(int id, String madonhang, int iduser, String diachi, String sodienthoai,
                   int soluong, String tongtien, String ngaydat, String ngaygiaodukien, String trangthai) {
        this.id = id;
        this.madonhang = madonhang;
        this.iduser = iduser;
        this.diachi = diachi;
        this.sodienthoai = sodienthoai;
        this.soluong = soluong;
        this.tongtien = tongtien;
        this.ngaydat = ngaydat;
        this.ngaygiaodukien = ngaygiaodukien;
        this.trangthai = trangthai;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMadonhang() {
        return madonhang;
    }

    public void setMadonhang(String madonhang) {
        this.madonhang = madonhang;
    }

    public int getIduser() {
        return iduser;
    }

    public void setIduser(int iduser) {
        this.iduser = iduser;
    }

    public String getDiachi() {
        return diachi;
    }

    public void setDiachi(String diachi) {
        this.diachi = diachi;
    }

    public String getSodienthoai() {
        return sodienthoai;
    }

    public void setSodienthoai(String sodienthoai) {
        this.sodienthoai = sodienthoai;
    }

    public int getSoluong() {
        return soluong;
    }

    public void setSoluong(int soluong) {
        this.soluong = soluong;
    }

    public String getTongtien() {
        return tongtien;
    }

    public void setTongtien(String tongtien) {
        this.tongtien = tongtien;
    }

    public String getNgaydat() {
        return ngaydat;
    }

    public void setNgaydat(String ngaydat) {
        this.ngaydat = ngaydat;
    }

    public String getNgaygiaodukien() {
        return ngaygiaodukien;
    }

    public void setNgaygiaodukien(String ngaygiaodukien) {
        this.ngaygiaodukien = ngaygiaodukien;
    }

    public String getTrangthai() {
        return trangthai;
    }

    public void setTrangthai(String trangthai) {
        this.trangthai = trangthai;
    }
}


