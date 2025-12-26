package vn.duytruong.appbandienthoai.model;

import java.util.List;


public class VoucherListResponse {
    private boolean success;
    private String message;
    private boolean is_new_user;
    private double tong_tien;
    private List<Voucher> vouchers;
    private List<Voucher> vouchers_applicable;
    private List<Voucher> vouchers_not_applicable;
    private int total;
    private int total_applicable;
    private int total_not_applicable;

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

    public boolean isIs_new_user() {
        return is_new_user;
    }

    public void setIs_new_user(boolean is_new_user) {
        this.is_new_user = is_new_user;
    }

    public double getTong_tien() {
        return tong_tien;
    }

    public void setTong_tien(double tong_tien) {
        this.tong_tien = tong_tien;
    }

    public List<Voucher> getVouchers() {
        return vouchers;
    }

    public void setVouchers(List<Voucher> vouchers) {
        this.vouchers = vouchers;
    }

    public List<Voucher> getVouchers_applicable() {
        return vouchers_applicable;
    }

    public void setVouchers_applicable(List<Voucher> vouchers_applicable) {
        this.vouchers_applicable = vouchers_applicable;
    }

    public List<Voucher> getVouchers_not_applicable() {
        return vouchers_not_applicable;
    }

    public void setVouchers_not_applicable(List<Voucher> vouchers_not_applicable) {
        this.vouchers_not_applicable = vouchers_not_applicable;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotal_applicable() {
        return total_applicable;
    }

    public void setTotal_applicable(int total_applicable) {
        this.total_applicable = total_applicable;
    }

    public int getTotal_not_applicable() {
        return total_not_applicable;
    }

    public void setTotal_not_applicable(int total_not_applicable) {
        this.total_not_applicable = total_not_applicable;
    }
}

