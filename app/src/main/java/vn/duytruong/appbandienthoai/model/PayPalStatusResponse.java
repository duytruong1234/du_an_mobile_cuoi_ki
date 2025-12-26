package vn.duytruong.appbandienthoai.model;

import com.google.gson.annotations.SerializedName;

public class PayPalStatusResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private PaymentData data;

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

    public PaymentData getData() {
        return data;
    }

    public void setData(PaymentData data) {
        this.data = data;
    }

    public static class PaymentData {
        @SerializedName("iddonhang")
        private int iddonhang;

        @SerializedName("madonhang")
        private String madonhang;

        @SerializedName("trangthai")
        private String trangthai;

        @SerializedName("paypal_order_id")
        private String paypalOrderId;

        @SerializedName("paypal_payer_id")
        private String paypalPayerId;

        @SerializedName("paypal_payment_date")
        private String paypalPaymentDate;

        public int getIddonhang() {
            return iddonhang;
        }

        public void setIddonhang(int iddonhang) {
            this.iddonhang = iddonhang;
        }

        public String getMadonhang() {
            return madonhang;
        }

        public void setMadonhang(String madonhang) {
            this.madonhang = madonhang;
        }

        public String getTrangthai() {
            return trangthai;
        }

        public void setTrangthai(String trangthai) {
            this.trangthai = trangthai;
        }

        public String getPaypalOrderId() {
            return paypalOrderId;
        }

        public void setPaypalOrderId(String paypalOrderId) {
            this.paypalOrderId = paypalOrderId;
        }

        public String getPaypalPayerId() {
            return paypalPayerId;
        }

        public void setPaypalPayerId(String paypalPayerId) {
            this.paypalPayerId = paypalPayerId;
        }

        public String getPaypalPaymentDate() {
            return paypalPaymentDate;
        }

        public void setPaypalPaymentDate(String paypalPaymentDate) {
            this.paypalPaymentDate = paypalPaymentDate;
        }
    }
}

