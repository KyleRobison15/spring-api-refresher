insert into categories (id, name) values
(1, 'XC'),
(2, 'Trail'),
(3, 'Enduro'),
(4, 'Downhill');

insert into products (name, price, description, category_id) values
-- XC Bikes (Cross Country)
('Specialized Epic World Cup Pro', 8700.00,
 'Ultra-light carbon XC race bike featuring 110mm front/75mm rear travel and SRAM X0 AXS drivetrain.',
 1),
('Trek Supercaliber 9.8 GX AXS', 7200.00,
 'Fast, efficient XC race bike with IsoStrut rear suspension and carbon frame.',
 1),
('Cannondale Scalpel Carbon 2', 6500.00,
 'Full-suspension XC machine with FlexPivot suspension and Lefty Ocho fork.',
 1),

-- Trail Bikes
('Santa Cruz Hightower C R', 4300.00,
 'Versatile 29” trail bike with VPP suspension and 145mm rear travel.',
 2),
('Trek Fuel EX 8', 3800.00,
 'Popular trail bike with 150/140mm travel, alloy frame, and Shimano XT shifting.',
 2),
('Specialized Stumpjumper Comp Alloy', 3200.00,
 'Do-it-all trail bike known for balanced geometry and 140mm rear suspension.',
 2),

-- Enduro Bikes
('Transition Patrol Alloy GX', 5400.00,
 'Hard-charging enduro bike with 170mm travel and progressive geometry.',
 3),
('Yeti SB160 C2', 6600.00,
 'Race-focused enduro sled with Switch Infinity linkage and 160mm rear travel.',
 3),

-- Downhill Bikes
('Santa Cruz V10 DH S', 6100.00,
 'Legendary downhill race bike with VPP suspension, 215mm travel, and MX wheel setup.',
 4),
('Trek Session 9', 7200.00,
 'World Cup–proven downhill rig featuring high-pivot suspension and 200mm travel.',
 4);
