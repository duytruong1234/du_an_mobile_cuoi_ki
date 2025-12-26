package vn.duytruong.appbandienthoai.retrofit;

import io.reactivex.Observable;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import vn.duytruong.appbandienthoai.model.DonHangModel;
import vn.duytruong.appbandienthoai.model.LoaiSpModel;
import vn.duytruong.appbandienthoai.model.MessageModel;
import vn.duytruong.appbandienthoai.model.PayPalResponse;
import vn.duytruong.appbandienthoai.model.PayPalStatusResponse;
import vn.duytruong.appbandienthoai.model.SanPhamMoiModel;
import vn.duytruong.appbandienthoai.model.ThongKeModel;
import vn.duytruong.appbandienthoai.model.TonKhoResponse;
import vn.duytruong.appbandienthoai.model.UserModel;
import vn.duytruong.appbandienthoai.model.VNPayResponse;
import vn.duytruong.appbandienthoai.model.VNPayStatusResponse;
import vn.duytruong.appbandienthoai.model.VoucherCheckResponse;
import vn.duytruong.appbandienthoai.model.VoucherListResponse;

public interface ApiBanHang {
    @GET("getloaisp.php")
    Observable<LoaiSpModel> getLoaiSp();

    @GET("getspmoi.php")
    Observable<SanPhamMoiModel> getSpMoi();

    @POST("chitiet.php")
    @FormUrlEncoded
    Observable<SanPhamMoiModel> getSanPham(
            @Field("page") int page,
            @Field("loai") int loai
    );


    @POST("dangki.php")
    @FormUrlEncoded
    Observable<UserModel> dangKi(
            @Field("email") String email,
            @Field("pass") String pass,
            @Field("username") String username,
            @Field("mobile") String mobile
    );
    @POST("dangnhap.php")
    @FormUrlEncoded
    Observable<UserModel> dangNhap(
            @Field("email") String email,
            @Field("pass") String pass
    );

    // ============================================
    // ĐƠN HÀNG APIs
    // ============================================
    // Lưu ý: Đơn hàng COD sử dụng Volley trong DatHangActivity.datHang()
    // gọi trực tiếp đến taoDonHang.php, không qua Retrofit

    @POST("xemdonhang.php")
    @FormUrlEncoded
    Observable<DonHangModel> xemDonHang(
            @Field("iduser") int id,
            @Field("isadmin") int isadmin,
            @Field("viewmode") String viewmode
    );

    @POST("timkiem.php")
    @FormUrlEncoded
    Observable<SanPhamMoiModel> search(
            @Field("search") String search
    );

    @POST("updatemomo.php")
    @FormUrlEncoded
    Observable<MessageModel> updateMomo(
        @Field("iddonhang") int iddonhang,
        @Field("token") String token
    );

    @POST("reset.php")
    @FormUrlEncoded
    Observable<UserModel> resetPass(
            @Field("email") String email
    );

    // API gửi OTP để reset password
    @POST("reset_pass.php")
    @FormUrlEncoded
    Observable<UserModel> sendResetOTP(
            @Field("email") String email
    );

    // API xác thực OTP và đổi mật khẩu mới
    @POST("verify_otp_reset_pass.php")
    @FormUrlEncoded
    Observable<UserModel> verifyOTPAndResetPassword(
            @Field("email") String email,
            @Field("otp") String otp,
            @Field("new_password") String newPassword
    );
    @POST("insertsp.php")
    @FormUrlEncoded
    Observable<MessageModel> insertSp(
            @Field("tensp") String tensp,
            @Field("giasp") String gia,
            @Field("hinhanh") String hinhanh,
            @Field("mota") String mota,
            @Field("loai") int loai,
            @Field("soluongtonkho") int soluongtonkho,
            @Field("soluong") int soluong
    );


    @POST("xoa.php")
    @FormUrlEncoded
    Observable<SanPhamMoiModel> xoaSanPham(
            @Field("id") int id
    );
    @POST("updatesp.php")
    @FormUrlEncoded
    Observable<MessageModel> update(
            @Field("tensp") String tenSp,
            @Field("giasp") String giaSp,
            @Field("hinhanh") String hinhAnh,
            @Field("mota") String moTa,
            @Field("id") int id,
            @Field("loai") int loai,
            @Field("soluongtonkho") int soluongtonkho,
            @Field("soluong") int soluong
    );
    @POST("updateorder.php")
    @FormUrlEncoded
    Observable<MessageModel> updateOrder(
            @Field("id") int id,
            @Field("trangthai") int trangthai
    );

    @GET("kiemTraTonKho.php")
    Observable<TonKhoResponse> kiemTraTonKho(
            @retrofit2.http.Query("idsp") int idsp
    );

     @POST("capNhatTonKho.php")
    @FormUrlEncoded
    Observable<MessageModel> capNhatTonKho(
            @Field("idsp") int idsp,
            @Field("soluong") int soluong
    );
    @GET("thongke.php")
    Observable<ThongKeModel> getthongke();

    // API thống kê doanh thu
    @GET("thongke_doanhthu.php")
    Observable<vn.duytruong.appbandienthoai.model.ThongKeDoanhThuModel> getThongKeDoanhThu(
            @retrofit2.http.Query("type") String type,
            @retrofit2.http.Query("year") int year
    );

    // VNPay APIs (returns VNPayResponse for create)
    @POST("vnpay_create_payment.php")
    @FormUrlEncoded
    Observable<VNPayResponse> createVNPayPayment(
            @Field("iduser") int iduser,
            @Field("diachi") String diachi,
            @Field("sodienthoai") String sodienthoai,
            @Field("soluong") int soluong,
            @Field("tongtien") String tongtien,
            @Field("cartItems") String cartItems,
            @Field("ngaygiaodukien") String ngaygiaodukien,
            @Field("order_info") String orderInfo,
            @Field("voucher_id") Integer voucherId,
            @Field("ma_voucher") String maVoucher,
            @Field("gia_tri_giam") Long giaTriGiam,
            @Field("tong_truoc_giam") Long tongTruocGiam
    );

    @POST("vnpay_check_status.php")
    @FormUrlEncoded
    Observable<VNPayStatusResponse> checkVNPayStatus(
            @Field("madonhang") String madonhang
    );

    // API tiếp tục thanh toán VNPay cho đơn hàng đã tồn tại
    @POST("vnpay_continue_payment.php")
    @FormUrlEncoded
    Observable<VNPayResponse> continueVNPayPayment(
            @Field("madonhang") String madonhang,
            @Field("tongtien") String tongtien
    );

    // API hủy đơn hàng
    @POST("huyDonHang.php")
    @FormUrlEncoded
    Observable<MessageModel> huyDonHang(
            @Field("madonhang") String madonhang,
            @Field("iduser") int iduser,
            @Field("lydo") String lydo
    );

    // API quản lý người dùng (chỉ dành cho admin)
    @GET("getAllUsers.php")
    Observable<UserModel> getAllUsers();

    @POST("updateUserRole.php")
    @FormUrlEncoded
    Observable<MessageModel> updateUserRole(
            @Field("userid") int userid,
            @Field("role") int role
    );

    @POST("deleteUser.php")
    @FormUrlEncoded
    Observable<MessageModel> deleteUser(
            @Field("userid") int userid
    );

    @POST("createUser.php")
    @FormUrlEncoded
    Observable<UserModel> createUser(
            @Field("email") String email,
            @Field("username") String username,
            @Field("password") String password,
            @Field("mobile") String mobile,
            @Field("role") int role
    );

    @POST("updateUserStatus.php")
    @FormUrlEncoded
    Observable<MessageModel> updateUserStatus(
            @Field("userId") int userId,
            @Field("status") int status
    );

    // PayPal APIs
    @POST("paypal_create_payment.php")
    @FormUrlEncoded
    Observable<PayPalResponse> createPayPalPayment(
            @Field("iduser") int iduser,
            @Field("diachi") String diachi,
            @Field("sodienthoai") String sodienthoai,
            @Field("soluong") int soluong,
            @Field("tongtien") String tongtien,
            @Field("cartItems") String cartItems,
            @Field("ngaygiaodukien") String ngaygiaodukien,
            @Field("order_info") String orderInfo,
            @Field("voucher_id") Integer voucherId,
            @Field("ma_voucher") String maVoucher,
            @Field("gia_tri_giam") Long giaTriGiam,
            @Field("tong_truoc_giam") Long tongTruocGiam
    );

    @POST("paypal_check_status.php")
    @FormUrlEncoded
    Observable<PayPalStatusResponse> checkPayPalStatus(
            @Field("madonhang") String madonhang
    );

    @POST("paypal_execute_payment.php")
    @FormUrlEncoded
    Observable<PayPalStatusResponse> executePayPalPayment(
            @Field("madonhang") String madonhang,
            @Field("paymentId") String paymentId,
            @Field("payerId") String payerId
    );

    // ============================================
    // VOUCHER APIs - Hệ thống mã giảm giá
    // ============================================

    // Kiểm tra và áp dụng voucher
    @POST("checkVoucher.php")
    @FormUrlEncoded
    Observable<VoucherCheckResponse> checkVoucher(
            @Field("ma_voucher") String maVoucher,
            @Field("user_id") int userId,
            @Field("tong_tien") double tongTien
    );

    // Lấy danh sách voucher khả dụng
    @POST("getVouchers.php")
    @FormUrlEncoded
    Observable<VoucherListResponse> getVouchers(
            @Field("user_id") int userId,
            @Field("tong_tien") double tongTien
    );

    // Lưu lịch sử sử dụng voucher
    @POST("saveVoucherUsage.php")
    @FormUrlEncoded
    Observable<MessageModel> saveVoucherUsage(
            @Field("voucher_id") int voucherId,
            @Field("user_id") int userId,
            @Field("donhang_id") int donhangId,
            @Field("ma_donhang") String maDonhang,
            @Field("gia_tri_don_hang") double giaTriDonHang,
            @Field("gia_tri_giam") double giaTriGiam
    );



    // ============================================
    // GIỎ HÀNG APIs - Lưu trữ giỏ hàng trên server
    // ============================================

    // Thêm sản phẩm vào giỏ hàng
    @POST("themGioHang.php")
    @FormUrlEncoded
    Observable<MessageModel> themGioHang(
            @Field("iduser") int iduser,
            @Field("idsp") int idsp,
            @Field("tensp") String tensp,
            @Field("giasp") long giasp,
            @Field("hinhsp") String hinhsp,
            @Field("soluong") int soluong
    );

    // Lấy giỏ hàng của user
    @POST("getGioHang.php")
    @FormUrlEncoded
    Observable<MessageModel> getGioHang(
            @Field("iduser") int iduser
    );

    // Xóa sản phẩm khỏi giỏ hàng
    @POST("xoaGioHang.php")
    @FormUrlEncoded
    Observable<MessageModel> xoaGioHang(
            @Field("iduser") int iduser,
            @Field("idsp") int idsp
    );

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    @POST("capNhatGioHang.php")
    @FormUrlEncoded
    Observable<MessageModel> capNhatGioHang(
            @Field("iduser") int iduser,
            @Field("idsp") int idsp,
            @Field("soluong") int soluong
    );

    // Xóa toàn bộ giỏ hàng
    @POST("xoaToanBoGioHang.php")
    @FormUrlEncoded
    Observable<MessageModel> xoaToanBoGioHang(
            @Field("iduser") int iduser
    );

    @POST("getAllVouchers.php")
    @FormUrlEncoded
    Observable<VoucherListResponse> getAllVouchers(
            @Field("status") String status,
            @Field("loai_giam") String loaiGiam,
            @Field("search") String search
    );

    @POST("addVoucher.php")
    @FormUrlEncoded
    Observable<MessageModel> addVoucher(
            @Field("role") int role,
            @Field("ma_voucher") String maVoucher,
            @Field("ten_voucher") String tenVoucher,
            @Field("mo_ta") String moTa,
            @Field("loai_giam") String loaiGiam,
            @Field("gia_tri_giam") double giaTriGiam,
            @Field("giam_toi_da") Double giamToiDa,
            @Field("don_toi_thieu") double donToiThieu,
            @Field("ap_dung_cho") String apDungCho,
            @Field("so_luong") Integer soLuong,
            @Field("gioi_han_moi_user") int gioiHanMoiUser,
            @Field("ngay_bat_dau") String ngayBatDau,
            @Field("ngay_het_han") String ngayHetHan,
            @Field("trang_thai") int trangThai
    );

    @POST("updateVoucher.php")
    @FormUrlEncoded
    Observable<MessageModel> updateVoucher(
            @Field("id") int id,
            @Field("role") int role,
            @Field("ma_voucher") String maVoucher,
            @Field("ten_voucher") String tenVoucher,
            @Field("mo_ta") String moTa,
            @Field("loai_giam") String loaiGiam,
            @Field("gia_tri_giam") double giaTriGiam,
            @Field("giam_toi_da") Double giamToiDa,
            @Field("don_toi_thieu") double donToiThieu,
            @Field("ap_dung_cho") String apDungCho,
            @Field("so_luong") Integer soLuong,
            @Field("gioi_han_moi_user") int gioiHanMoiUser,
            @Field("ngay_bat_dau") String ngayBatDau,
            @Field("ngay_het_han") String ngayHetHan,
            @Field("trang_thai") int trangThai
    );


    // Xóa voucher (admin)
    @POST("deleteVoucher.php")
    @FormUrlEncoded
    Observable<MessageModel> deleteVoucher(
            @Field("id") int id,
            @Field("role") int role,
            @Field("hard_delete") String hardDelete
    );

    // Bật/Tắt voucher (admin)
    @POST("toggleVoucher.php")
    @FormUrlEncoded
    Observable<MessageModel> toggleVoucher(
            @Field("id") int id
    );

    // Thống kê voucher (admin)
    @GET("getVoucherStats.php")
    Observable<MessageModel> getVoucherStats(
            @retrofit2.http.Query("voucher_id") Integer voucherId
    );


}
