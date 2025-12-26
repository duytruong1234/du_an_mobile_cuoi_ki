TYPE=VIEW
query=select `d`.`id` AS `donhang_id`,`d`.`iduser` AS `iduser`,`u`.`username` AS `username`,`u`.`email` AS `email`,`d`.`voucher_id` AS `voucher_id`,`v`.`ma_voucher` AS `ma_voucher`,`v`.`ten_voucher` AS `ten_voucher`,`d`.`gia_tri_giam` AS `gia_tri_giam`,`d`.`tong_truoc_giam` AS `tong_truoc_giam`,`d`.`tongtien` AS `tong_sau_giam`,`d`.`trangthai` AS `trangthai`,`d`.`ngaygiaodukien` AS `ngaygiaodukien`,`d`.`diachi` AS `diachi`,`d`.`sodienthoai` AS `sodienthoai` from ((`appbandongho`.`donhang` `d` join `appbandongho`.`user` `u` on(`d`.`iduser` = `u`.`id`)) left join `appbandongho`.`voucher` `v` on(`d`.`voucher_id` = `v`.`id`)) where `d`.`voucher_id` is not null order by `d`.`id` desc
md5=a47ff59ccbf09e0ae818264e43dff636
updatable=0
algorithm=0
definer_user=root
definer_host=localhost
suid=2
with_check_option=0
timestamp=0001762141659413070
create-version=2
source=SELECT \n    d.id AS donhang_id,\n    d.iduser,\n    u.username,\n    u.email,\n    d.voucher_id,\n    v.ma_voucher,\n    v.ten_voucher,\n    d.gia_tri_giam,\n    d.tong_truoc_giam,\n    d.tongtien AS tong_sau_giam,\n    d.trangthai,\n    d.ngaygiaodukien,\n    d.diachi,\n    d.sodienthoai\nFROM donhang d\nINNER JOIN user u ON d.iduser = u.id\nLEFT JOIN voucher v ON d.voucher_id = v.id\nWHERE d.voucher_id IS NOT NULL\nORDER BY d.id DESC
client_cs_name=utf8mb4
connection_cl_name=utf8mb4_general_ci
view_body_utf8=select `d`.`id` AS `donhang_id`,`d`.`iduser` AS `iduser`,`u`.`username` AS `username`,`u`.`email` AS `email`,`d`.`voucher_id` AS `voucher_id`,`v`.`ma_voucher` AS `ma_voucher`,`v`.`ten_voucher` AS `ten_voucher`,`d`.`gia_tri_giam` AS `gia_tri_giam`,`d`.`tong_truoc_giam` AS `tong_truoc_giam`,`d`.`tongtien` AS `tong_sau_giam`,`d`.`trangthai` AS `trangthai`,`d`.`ngaygiaodukien` AS `ngaygiaodukien`,`d`.`diachi` AS `diachi`,`d`.`sodienthoai` AS `sodienthoai` from ((`appbandongho`.`donhang` `d` join `appbandongho`.`user` `u` on(`d`.`iduser` = `u`.`id`)) left join `appbandongho`.`voucher` `v` on(`d`.`voucher_id` = `v`.`id`)) where `d`.`voucher_id` is not null order by `d`.`id` desc
mariadb-version=100432
