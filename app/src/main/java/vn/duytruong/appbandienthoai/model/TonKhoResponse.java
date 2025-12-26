package vn.duytruong.appbandienthoai.model;

public class TonKhoResponse {
    private boolean success;
    private String message;
    private TonKhoData data;

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

    public TonKhoData getData() {
        return data;
    }

    public void setData(TonKhoData data) {
        this.data = data;
    }

    public static class TonKhoData {
        private int id;
        private String tensp;
        private String hinhanh;
        private String giasp;
        private int soluongtonkho;
        private String mota;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
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

        public String getGiasp() {
            return giasp;
        }

        public void setGiasp(String giasp) {
            this.giasp = giasp;
        }

        public int getSoluongtonkho() {
            return soluongtonkho;
        }

        public void setSoluongtonkho(int soluongtonkho) {
            this.soluongtonkho = soluongtonkho;
        }

        public String getMota() {
            return mota;
        }

        public void setMota(String mota) {
            this.mota = mota;
        }
    }
}

