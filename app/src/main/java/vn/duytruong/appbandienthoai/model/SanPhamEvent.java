package vn.duytruong.appbandienthoai.model;

import vn.duytruong.appbandienthoai.model.SanPhamMoi;

public class SanPhamEvent {
    private SanPhamMoi sanPhamMoi;

    public SanPhamEvent(SanPhamMoi sanPhamMoi) {
        this.sanPhamMoi = sanPhamMoi;
    }

    public SanPhamMoi getSanPhamMoi() {
        return sanPhamMoi;
    }

    public void setSanPhamMoi(SanPhamMoi sanPhamMoi) {
        this.sanPhamMoi = sanPhamMoi;
    }
}
