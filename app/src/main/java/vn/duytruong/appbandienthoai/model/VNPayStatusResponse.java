package vn.duytruong.appbandienthoai.model;

import com.google.gson.annotations.SerializedName;


public class VNPayStatusResponse {
    private boolean success;
    private String message;
    private OrderData data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OrderData getData() {
        return data;
    }

    public void setData(OrderData data) {
        this.data = data;
    }

    public static class OrderData {
        private String id;
        private String madonhang;
        private String iduser;
        private String diachi;
        private String sodienthoai;
        private String soluong;
        private String tongtien;
        private String trangthai;

        @SerializedName("vnpay_transaction_no")
        private String vnpayTransactionNo;

        @SerializedName("vnpay_bank_code")
        private String vnpayBankCode;

        @SerializedName("vnpay_pay_date")
        private String vnpayPayDate;

        private String ngaydat;
        private String ngaygiaodukien;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMadonhang() {
            return madonhang;
        }

        public void setMadonhang(String madonhang) {
            this.madonhang = madonhang;
        }

        public String getIduser() {
            return iduser;
        }

        public void setIduser(String iduser) {
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

        public String getSoluong() {
            return soluong;
        }

        public void setSoluong(String soluong) {
            this.soluong = soluong;
        }

        public String getTongtien() {
            return tongtien;
        }

        public void setTongtien(String tongtien) {
            this.tongtien = tongtien;
        }

        public String getTrangthai() {
            return trangthai;
        }

        public void setTrangthai(String trangthai) {
            this.trangthai = trangthai;
        }

        public String getVnpayTransactionNo() {
            return vnpayTransactionNo;
        }

        public void setVnpayTransactionNo(String vnpayTransactionNo) {
            this.vnpayTransactionNo = vnpayTransactionNo;
        }

        public String getVnpayBankCode() {
            return vnpayBankCode;
        }

        public void setVnpayBankCode(String vnpayBankCode) {
            this.vnpayBankCode = vnpayBankCode;
        }

        public String getVnpayPayDate() {
            return vnpayPayDate;
        }

        public void setVnpayPayDate(String vnpayPayDate) {
            this.vnpayPayDate = vnpayPayDate;
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
    }

    @Override
    public String toString() {
        return "VNPayStatusResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + (data != null ? data.toString() : "null") +
                '}';
    }
}

