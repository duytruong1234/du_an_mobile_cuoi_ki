package vn.duytruong.appbandienthoai.model;


public class VoucherCheckResponse {
    private boolean success;
    private String message;
    private Voucher voucher;
    private TinhToan tinh_toan;

    public static class TinhToan {
        private double tong_truoc_giam;
        private double gia_tri_giam;
        private double tong_sau_giam;

        public double getTong_truoc_giam() {
            return tong_truoc_giam;
        }

        public void setTong_truoc_giam(double tong_truoc_giam) {
            this.tong_truoc_giam = tong_truoc_giam;
        }

        public double getGia_tri_giam() {
            return gia_tri_giam;
        }

        public void setGia_tri_giam(double gia_tri_giam) {
            this.gia_tri_giam = gia_tri_giam;
        }

        public double getTong_sau_giam() {
            return tong_sau_giam;
        }

        public void setTong_sau_giam(double tong_sau_giam) {
            this.tong_sau_giam = tong_sau_giam;
        }
    }

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

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public TinhToan getTinh_toan() {
        return tinh_toan;
    }

    public void setTinh_toan(TinhToan tinh_toan) {
        this.tinh_toan = tinh_toan;
    }
}

