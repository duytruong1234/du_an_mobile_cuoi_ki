TYPE=VIEW
query=select `c`.`id` AS `cart_id`,`c`.`iduser` AS `iduser`,`u`.`username` AS `username`,`u`.`email` AS `email`,count(`ci`.`id`) AS `total_items`,sum(`ci`.`soluong`) AS `total_quantity`,sum(`ci`.`giasp` * `ci`.`soluong`) AS `total_value`,`c`.`created_at` AS `cart_created`,`c`.`updated_at` AS `cart_updated` from ((`appbandongho`.`cart` `c` left join `appbandongho`.`cart_items` `ci` on(`c`.`id` = `ci`.`idcart`)) left join `appbandongho`.`user` `u` on(`c`.`iduser` = `u`.`id`)) group by `c`.`id`,`c`.`iduser`,`u`.`username`,`u`.`email`,`c`.`created_at`,`c`.`updated_at`
md5=74d2914f4b23ce0073c46d7974e33dbb
updatable=0
algorithm=0
definer_user=root
definer_host=localhost
suid=2
with_check_option=0
timestamp=0001764568654137929
create-version=2
source=SELECT\n    c.id as cart_id,\n    c.iduser,\n    u.username,\n    u.email,\n    COUNT(ci.id) as total_items,\n    SUM(ci.soluong) as total_quantity,\n    SUM(ci.giasp * ci.soluong) as total_value,\n    c.created_at as cart_created,\n    c.updated_at as cart_updated\nFROM `cart` c\nLEFT JOIN `cart_items` ci ON c.id = ci.idcart\nLEFT JOIN `user` u ON c.iduser = u.id\nGROUP BY c.id, c.iduser, u.username, u.email, c.created_at, c.updated_at
client_cs_name=utf8mb4
connection_cl_name=utf8mb4_general_ci
view_body_utf8=select `c`.`id` AS `cart_id`,`c`.`iduser` AS `iduser`,`u`.`username` AS `username`,`u`.`email` AS `email`,count(`ci`.`id`) AS `total_items`,sum(`ci`.`soluong`) AS `total_quantity`,sum(`ci`.`giasp` * `ci`.`soluong`) AS `total_value`,`c`.`created_at` AS `cart_created`,`c`.`updated_at` AS `cart_updated` from ((`appbandongho`.`cart` `c` left join `appbandongho`.`cart_items` `ci` on(`c`.`id` = `ci`.`idcart`)) left join `appbandongho`.`user` `u` on(`c`.`iduser` = `u`.`id`)) group by `c`.`id`,`c`.`iduser`,`u`.`username`,`u`.`email`,`c`.`created_at`,`c`.`updated_at`
mariadb-version=100432
