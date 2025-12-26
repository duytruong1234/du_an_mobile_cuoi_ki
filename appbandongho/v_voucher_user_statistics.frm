TYPE=VIEW
query=select `u`.`id` AS `user_id`,`u`.`username` AS `username`,`u`.`email` AS `email`,count(distinct `vu`.`voucher_id`) AS `so_voucher_da_dung`,count(`vu`.`id`) AS `tong_lan_su_dung`,sum(`vu`.`gia_tri_giam`) AS `tong_tien_da_tiet_kiem`,max(`vu`.`ngay_su_dung`) AS `lan_su_dung_gan_nhat` from (`appbandongho`.`user` `u` left join `appbandongho`.`voucher_usage` `vu` on(`u`.`id` = `vu`.`user_id`)) group by `u`.`id`,`u`.`username`,`u`.`email`
md5=b5c87fde775b10954097ec488092cf2e
updatable=0
algorithm=0
definer_user=root
definer_host=localhost
suid=2
with_check_option=0
timestamp=0001762141659417797
create-version=2
source=SELECT \n    u.id AS user_id,\n    u.username,\n    u.email,\n    COUNT(DISTINCT vu.voucher_id) AS so_voucher_da_dung,\n    COUNT(vu.id) AS tong_lan_su_dung,\n    SUM(vu.gia_tri_giam) AS tong_tien_da_tiet_kiem,\n    MAX(vu.ngay_su_dung) AS lan_su_dung_gan_nhat\nFROM user u\nLEFT JOIN voucher_usage vu ON u.id = vu.user_id\nGROUP BY u.id, u.username, u.email
client_cs_name=utf8mb4
connection_cl_name=utf8mb4_general_ci
view_body_utf8=select `u`.`id` AS `user_id`,`u`.`username` AS `username`,`u`.`email` AS `email`,count(distinct `vu`.`voucher_id`) AS `so_voucher_da_dung`,count(`vu`.`id`) AS `tong_lan_su_dung`,sum(`vu`.`gia_tri_giam`) AS `tong_tien_da_tiet_kiem`,max(`vu`.`ngay_su_dung`) AS `lan_su_dung_gan_nhat` from (`appbandongho`.`user` `u` left join `appbandongho`.`voucher_usage` `vu` on(`u`.`id` = `vu`.`user_id`)) group by `u`.`id`,`u`.`username`,`u`.`email`
mariadb-version=100432
