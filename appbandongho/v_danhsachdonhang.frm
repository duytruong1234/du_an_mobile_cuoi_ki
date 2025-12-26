TYPE=VIEW
query=select `dh`.`id` AS `id`,`dh`.`madonhang` AS `madonhang`,`u`.`username` AS `username`,`u`.`email` AS `email`,`dh`.`diachi` AS `diachi`,`dh`.`sodienthoai` AS `sodienthoai`,`dh`.`soluong` AS `soluong`,`dh`.`tongtien` AS `tongtien`,`dh`.`ngaydat` AS `ngaydat`,`dh`.`ngaygiaodukien` AS `ngaygiaodukien`,`dh`.`trangthai` AS `trangthai`,to_days(`dh`.`ngaygiaodukien`) - to_days(curdate()) AS `songayconlai` from (`appbandongho`.`donhang` `dh` join `appbandongho`.`user` `u` on(`dh`.`iduser` = `u`.`id`)) order by `dh`.`ngaydat` desc
md5=1e7278c296aaca9935e76f9cc6f74b0d
updatable=1
algorithm=0
definer_user=root
definer_host=localhost
suid=2
with_check_option=0
timestamp=0001761288595715750
create-version=2
source=SELECT\n    dh.id,\n    dh.madonhang,\n    u.username,\n    u.email,\n    dh.diachi,\n    dh.sodienthoai,\n    dh.soluong,\n    dh.tongtien,\n    dh.ngaydat,\n    dh.ngaygiaodukien,\n    dh.trangthai,\n    DATEDIFF(dh.ngaygiaodukien, CURDATE()) AS songayconlai\nFROM donhang dh\nINNER JOIN user u ON dh.iduser = u.id\nORDER BY dh.ngaydat DESC
client_cs_name=utf8mb4
connection_cl_name=utf8mb4_general_ci
view_body_utf8=select `dh`.`id` AS `id`,`dh`.`madonhang` AS `madonhang`,`u`.`username` AS `username`,`u`.`email` AS `email`,`dh`.`diachi` AS `diachi`,`dh`.`sodienthoai` AS `sodienthoai`,`dh`.`soluong` AS `soluong`,`dh`.`tongtien` AS `tongtien`,`dh`.`ngaydat` AS `ngaydat`,`dh`.`ngaygiaodukien` AS `ngaygiaodukien`,`dh`.`trangthai` AS `trangthai`,to_days(`dh`.`ngaygiaodukien`) - to_days(curdate()) AS `songayconlai` from (`appbandongho`.`donhang` `dh` join `appbandongho`.`user` `u` on(`dh`.`iduser` = `u`.`id`)) order by `dh`.`ngaydat` desc
mariadb-version=100432
