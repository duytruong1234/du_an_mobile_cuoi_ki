TYPE=VIEW
query=select `vu`.`id` AS `usage_id`,`vu`.`voucher_id` AS `voucher_id`,`v`.`ma_voucher` AS `ma_voucher`,`v`.`ten_voucher` AS `ten_voucher`,`vu`.`user_id` AS `user_id`,`u`.`username` AS `username`,`u`.`email` AS `email`,`vu`.`donhang_id` AS `donhang_id`,`vu`.`ma_donhang` AS `ma_donhang`,`vu`.`gia_tri_don_hang` AS `gia_tri_don_hang`,`vu`.`gia_tri_giam` AS `gia_tri_giam`,`vu`.`ngay_su_dung` AS `ngay_su_dung`,`d`.`trangthai` AS `trang_thai_don_hang`,`d`.`tongtien` AS `tong_tien_don_hang` from (((`appbandongho`.`voucher_usage` `vu` join `appbandongho`.`voucher` `v` on(`vu`.`voucher_id` = `v`.`id`)) join `appbandongho`.`user` `u` on(`vu`.`user_id` = `u`.`id`)) left join `appbandongho`.`donhang` `d` on(`vu`.`donhang_id` = `d`.`id`)) order by `vu`.`ngay_su_dung` desc
md5=11c6e699bfafcc6382b1d206f1601c7c
updatable=0
algorithm=0
definer_user=root
definer_host=localhost
suid=2
with_check_option=0
timestamp=0001762141659409722
create-version=2
source=SELECT \n    vu.id AS usage_id,\n    vu.voucher_id,\n    v.ma_voucher,\n    v.ten_voucher,\n    vu.user_id,\n    u.username,\n    u.email,\n    vu.donhang_id,\n    vu.ma_donhang,\n    vu.gia_tri_don_hang,\n    vu.gia_tri_giam,\n    vu.ngay_su_dung,\n    d.trangthai AS trang_thai_don_hang,\n    d.tongtien AS tong_tien_don_hang\nFROM voucher_usage vu\nINNER JOIN voucher v ON vu.voucher_id = v.id\nINNER JOIN user u ON vu.user_id = u.id\nLEFT JOIN donhang d ON vu.donhang_id = d.id\nORDER BY vu.ngay_su_dung DESC
client_cs_name=utf8mb4
connection_cl_name=utf8mb4_general_ci
view_body_utf8=select `vu`.`id` AS `usage_id`,`vu`.`voucher_id` AS `voucher_id`,`v`.`ma_voucher` AS `ma_voucher`,`v`.`ten_voucher` AS `ten_voucher`,`vu`.`user_id` AS `user_id`,`u`.`username` AS `username`,`u`.`email` AS `email`,`vu`.`donhang_id` AS `donhang_id`,`vu`.`ma_donhang` AS `ma_donhang`,`vu`.`gia_tri_don_hang` AS `gia_tri_don_hang`,`vu`.`gia_tri_giam` AS `gia_tri_giam`,`vu`.`ngay_su_dung` AS `ngay_su_dung`,`d`.`trangthai` AS `trang_thai_don_hang`,`d`.`tongtien` AS `tong_tien_don_hang` from (((`appbandongho`.`voucher_usage` `vu` join `appbandongho`.`voucher` `v` on(`vu`.`voucher_id` = `v`.`id`)) join `appbandongho`.`user` `u` on(`vu`.`user_id` = `u`.`id`)) left join `appbandongho`.`donhang` `d` on(`vu`.`donhang_id` = `d`.`id`)) order by `vu`.`ngay_su_dung` desc
mariadb-version=100432
