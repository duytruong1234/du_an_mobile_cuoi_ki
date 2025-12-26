package vn.duytruong.appbandienthoai.model;

public class VNPayResponse {
    boolean success;
    String message;
    String payment_url;
    String madonhang;
    String iddonhang;

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

    public String getPayment_url() {
        return payment_url;
    }

    public void setPayment_url(String payment_url) {
        this.payment_url = payment_url;
    }

    public String getMadonhang() {
        return madonhang;
    }

    public void setMadonhang(String madonhang) {
        this.madonhang = madonhang;
    }

    public String getIddonhang() {
        return iddonhang;
    }

    public void setIddonhang(String iddonhang) {
        this.iddonhang = iddonhang;
    }

    @Override
    public String toString() {
        return "VNPayResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", payment_url='" + payment_url + '\'' +
                ", madonhang='" + madonhang + '\'' +
                ", iddonhang='" + iddonhang + '\'' +
                '}';
    }
}
