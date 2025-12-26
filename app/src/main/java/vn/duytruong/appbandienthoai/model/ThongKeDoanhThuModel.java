package vn.duytruong.appbandienthoai.model;

import java.util.List;

public class ThongKeDoanhThuModel {
    private boolean success;
    private String message;
    private String type;
    private int year;
    private List<ThongKeDoanhThu> result;
    private TongQuanDoanhThu tongquan;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<ThongKeDoanhThu> getResult() {
        return result;
    }

    public void setResult(List<ThongKeDoanhThu> result) {
        this.result = result;
    }

    public TongQuanDoanhThu getTongquan() {
        return tongquan;
    }

    public void setTongquan(TongQuanDoanhThu tongquan) {
        this.tongquan = tongquan;
    }
}

