TYPE=VIEW
query=select `sp`.`id` AS `id`,`sp`.`tensp` AS `tensp`,`sp`.`hinhanh` AS `hinhanh`,`sp`.`giasp` AS `giasp`,`sp`.`soluongtonkho` AS `soluongtonkho`,`ls`.`tensanpham` AS `loaisanpham`,case when `sp`.`soluongtonkho` = 0 then \'Hết hàng\' when `sp`.`soluongtonkho` < 10 then \'Sắp hết\' else \'Còn hàng\' end AS `tinhtrang` from (`appbandongho`.`sanphammoi` `sp` left join `appbandongho`.`sanpham` `ls` on(`sp`.`loai` = `ls`.`id`))
md5=5eba1414fc0dce5dabc857c86b21bd58
updatable=0
algorithm=0
definer_user=root
definer_host=localhost
suid=2
with_check_option=0
timestamp=0001761288595711859
create-version=2
source=SELECT\n    sp.id,\n    sp.tensp,\n    sp.hinhanh,\n    sp.giasp,\n    sp.soluongtonkho,\n    ls.tensanpham AS loaisanpham,\n    CASE\n        WHEN sp.soluongtonkho = 0 THEN \'Hết hàng\'\n        WHEN sp.soluongtonkho < 10 THEN \'Sắp hết\'\n        ELSE \'Còn hàng\'\n    END AS tinhtrang\nFROM sanphammoi sp\nLEFT JOIN sanpham ls ON sp.loai = ls.id
client_cs_name=utf8mb4
connection_cl_name=utf8mb4_general_ci
view_body_utf8=select `sp`.`id` AS `id`,`sp`.`tensp` AS `tensp`,`sp`.`hinhanh` AS `hinhanh`,`sp`.`giasp` AS `giasp`,`sp`.`soluongtonkho` AS `soluongtonkho`,`ls`.`tensanpham` AS `loaisanpham`,case when `sp`.`soluongtonkho` = 0 then \'Hết hàng\' when `sp`.`soluongtonkho` < 10 then \'Sắp hết\' else \'Còn hàng\' end AS `tinhtrang` from (`appbandongho`.`sanphammoi` `sp` left join `appbandongho`.`sanpham` `ls` on(`sp`.`loai` = `ls`.`id`))
mariadb-version=100432
