-- V5: Import 110 reference flower types, rose cultivars, decorative foliage and add rich new products

ALTER TABLE catalog.flower_types ADD COLUMN IF NOT EXISTS record_type TEXT DEFAULT 'flower_group';
ALTER TABLE catalog.flower_types ADD COLUMN IF NOT EXISTS display_group TEXT;
ALTER TABLE catalog.flower_types ADD COLUMN IF NOT EXISTS parent_slug TEXT;
ALTER TABLE catalog.flower_types ADD COLUMN IF NOT EXISTS search_aliases TEXT[];

-- Insert 110 reference flower items
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases) VALUES
('31000000-0000-0000-0000-000000000001', 'Hoa hồng', 'hoa-hong', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['hong', 'rose']),
('31000000-0000-0000-0000-000000000002', 'Hoa ly / lily', 'hoa-ly-lily', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['ly', 'lily']),
('31000000-0000-0000-0000-000000000003', 'Tulip / uất kim hương', 'tulip-uat-kim-huong', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['tulip', 'uat kim huong']),
('31000000-0000-0000-0000-000000000004', 'Cẩm chướng', 'cam-chuong', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['cam chuong', 'carnation']),
('31000000-0000-0000-0000-000000000005', 'Đồng tiền', 'dong-tien', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['dong tien', 'gerbera']),
('31000000-0000-0000-0000-000000000006', 'Hoa cúc', 'hoa-cuc', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['cuc', 'chrysanthemum']),
('31000000-0000-0000-0000-000000000007', 'Cát tường', 'cat-tuong', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['cat tuong', 'lisianthus']),
('31000000-0000-0000-0000-000000000008', 'Cẩm tú cầu', 'cam-tu-cau', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['cam tu cau', 'hydrangea']),
('31000000-0000-0000-0000-000000000009', 'Hướng dương', 'huong-duong', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['huong duong', 'sunflower']),
('31000000-0000-0000-0000-00000000000a', 'Lay ơn / dơn', 'lay-on-don', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['lay on', 'don', 'gladiolus']),
('31000000-0000-0000-0000-00000000000b', 'Thủy tiên Peru', 'thuy-tien-peru', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['thuy tien peru', 'alstroemeria']),
('31000000-0000-0000-0000-00000000000c', 'Mao lương', 'mao-luong', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['mao luong', 'ranunculus']),
('31000000-0000-0000-0000-00000000000d', 'Mẫu đơn / peony', 'mau-don-peony', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['mau don', 'peony', 'paeonia']),
('31000000-0000-0000-0000-00000000000e', 'Thược dược', 'thuoc-duoc', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['thuoc duoc', 'dahlia']),
('31000000-0000-0000-0000-00000000000f', 'Calla / rum', 'calla-rum', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['calla', 'rum']),
('31000000-0000-0000-0000-000000000010', 'Hải quỳ / anemone', 'hai-quy-anemone', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['hai quy', 'anemone']),
('31000000-0000-0000-0000-000000000011', 'Lan Nam Phi / freesia', 'lan-nam-phi-freesia', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['lan nam phi', 'freesia']),
('31000000-0000-0000-0000-000000000012', 'Phi yến / delphinium', 'phi-yen-delphinium', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['phi yen', 'delphinium']),
('31000000-0000-0000-0000-000000000013', 'Mõm sói', 'mom-soi', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['mom soi', 'snapdragon']),
('31000000-0000-0000-0000-000000000014', 'Diên vĩ', 'dien-vi', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['dien vi', 'iris']),
('31000000-0000-0000-0000-000000000015', 'Stock / Matthiola', 'stock-matthiola', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['stock', 'matthiola']),
('31000000-0000-0000-0000-000000000016', 'Hoa chuông Campanula', 'hoa-chuong-campanula', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['hoa chuong', 'campanula']),
('31000000-0000-0000-0000-000000000017', 'Đậu thơm', 'dau-thom', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['dau thom', 'sweet pea']),
('31000000-0000-0000-0000-000000000018', 'Dạ lan hương', 'da-lan-huong', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['da lan huong', 'hyacinth']),
('31000000-0000-0000-0000-000000000019', 'Thủy tiên Narcissus', 'thuy-tien-narcissus', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['thuy tien', 'narcissus', 'daffodil']),
('31000000-0000-0000-0000-00000000001a', 'Chu đỉnh hồng', 'chu-dinh-hong', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['chu dinh hong', 'hippeastrum']),
('31000000-0000-0000-0000-00000000001b', 'Helleborus', 'helleborus', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['helleborus', 'hellebore']),
('31000000-0000-0000-0000-00000000001c', 'Scabiosa', 'scabiosa', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['scabiosa', 'scabious']),
('31000000-0000-0000-0000-00000000001d', 'Astilbe', 'astilbe', 'flower_group', 'hoa-phu', NULL, ARRAY['astilbe']),
('31000000-0000-0000-0000-00000000001e', 'Astrantia', 'astrantia', 'flower_group', 'hoa-phu', NULL, ARRAY['astrantia', 'masterwort']),
('31000000-0000-0000-0000-00000000001f', 'Ông lão / Clematis', 'ong-lao-clematis', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['ong lao', 'clematis']),
('31000000-0000-0000-0000-000000000020', 'Veronica', 'veronica', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['veronica']),
('31000000-0000-0000-0000-000000000021', 'Hành cảnh / Allium', 'hanh-canh-allium', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['hanh canh', 'allium']),
('31000000-0000-0000-0000-000000000022', 'Thanh anh / Agapanthus', 'thanh-anh-agapanthus', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['thanh anh', 'agapanthus']),
('31000000-0000-0000-0000-000000000023', 'Eremurus', 'eremurus', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['eremurus', 'foxtail lily']),
('31000000-0000-0000-0000-000000000024', 'Liatris', 'liatris', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['liatris', 'blazing star']),
('31000000-0000-0000-0000-000000000025', 'Baby / bi', 'baby-bi', 'flower_group', 'hoa-phu', NULL, ARRAY['baby', 'bi', 'gypsophila']),
('31000000-0000-0000-0000-000000000026', 'Thanh liễu', 'thanh-lieu', 'flower_group', 'hoa-phu', NULL, ARRAY['thanh lieu', 'waxflower']),
('31000000-0000-0000-0000-000000000027', 'Salem và các dòng Limonium', 'salem-va-cac-dong-limonium', 'flower_group', 'hoa-phu', NULL, ARRAY['salem', 'statice', 'limonium']),
('31000000-0000-0000-0000-000000000028', 'Solidago', 'solidago', 'flower_group', 'hoa-phu', NULL, ARRAY['solidago', 'goldenrod']),
('31000000-0000-0000-0000-000000000029', 'Cúc Tana', 'cuc-tana', 'flower_group', 'hoa-phu', NULL, ARRAY['cuc tana', 'tana', 'feverfew']),
('31000000-0000-0000-0000-00000000002a', 'Nhím biển / Eryngium', 'nhim-bien-eryngium', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['nhim bien', 'eryngium']),
('31000000-0000-0000-0000-00000000002b', 'Craspedia / cúc bi vàng', 'craspedia-cuc-bi-vang', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['craspedia', 'cuc bi vang']),
('31000000-0000-0000-0000-00000000002c', 'Ammi', 'ammi', 'flower_group', 'hoa-phu', NULL, ARRAY['ammi']),
('31000000-0000-0000-0000-00000000002d', 'Bupleurum', 'bupleurum', 'flower_group', 'hoa-phu', NULL, ARRAY['bupleurum']),
('31000000-0000-0000-0000-00000000002e', 'Nigella', 'nigella', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['nigella']),
('31000000-0000-0000-0000-00000000002f', 'Orlaya', 'orlaya', 'flower_group', 'hoa-phu', NULL, ARRAY['orlaya']),
('31000000-0000-0000-0000-000000000030', 'Cỏ thi / Achillea', 'co-thi-achillea', 'flower_group', 'hoa-phu', NULL, ARRAY['co thi', 'achillea', 'yarrow']),
('31000000-0000-0000-0000-000000000031', 'Mào gà', 'mao-ga', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['mao ga', 'celosia']),
('31000000-0000-0000-0000-000000000032', 'Dền cảnh / Amaranthus', 'den-canh-amaranthus', 'flower_group', 'hoa-ru', NULL, ARRAY['den canh', 'amaranthus']),
('31000000-0000-0000-0000-000000000033', 'Cúc lá nhám', 'cuc-la-nham', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['cuc la nham', 'zinnia']),
('31000000-0000-0000-0000-000000000034', 'Sao nhái / cánh bướm', 'sao-nhai-canh-buom', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['sao nhai', 'canh buom', 'cosmos']),
('31000000-0000-0000-0000-000000000035', 'Cúc bách nhật', 'cuc-bach-nhat', 'flower_group', 'hoa-phu', NULL, ARRAY['cuc bach nhat', 'gomphrena']),
('31000000-0000-0000-0000-000000000036', 'Lan hồ điệp', 'lan-ho-diep', 'flower_group', 'hoa-lan', NULL, ARRAY['lan ho diep', 'phalaenopsis']),
('31000000-0000-0000-0000-000000000037', 'Địa lan / Cymbidium', 'dia-lan-cymbidium', 'flower_group', 'hoa-lan', NULL, ARRAY['dia lan', 'cymbidium']),
('31000000-0000-0000-0000-000000000038', 'Lan Dendrobium', 'lan-dendrobium', 'flower_group', 'hoa-lan', NULL, ARRAY['lan dendro', 'dendrobium']),
('31000000-0000-0000-0000-000000000039', 'Lan Vanda', 'lan-vanda', 'flower_group', 'hoa-lan', NULL, ARRAY['lan vanda', 'vanda']),
('31000000-0000-0000-0000-00000000003a', 'Hồng môn', 'hong-mon', 'flower_group', 'hoa-nhiet-doi', NULL, ARRAY['hong mon', 'anthurium']),
('31000000-0000-0000-0000-00000000003b', 'Protea', 'protea', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['protea', 'hoa de vuong']),
('31000000-0000-0000-0000-00000000003c', 'Leucospermum', 'leucospermum', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['leucospermum', 'pincushion']),
('31000000-0000-0000-0000-00000000003d', 'Hoa sen', 'hoa-sen', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['sen', 'lotus']),
('31000000-0000-0000-0000-00000000003e', 'Hoa huệ trắng', 'hoa-hue-trang', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['hue trang', 'tuberose']),
('31000000-0000-0000-0000-00000000003f', 'Thiên điểu', 'thien-dieu', 'flower_group', 'hoa-nhiet-doi', NULL, ARRAY['thien dieu', 'strelitzia']),
('31000000-0000-0000-0000-000000000040', 'Heliconia / chuối mỏ két', 'heliconia-chuoi-mo-ket', 'flower_group', 'hoa-nhiet-doi', NULL, ARRAY['heliconia', 'chuoi mo ket']),
('31000000-0000-0000-0000-000000000041', 'Curcuma / tulip Xiêm', 'curcuma-tulip-xiem', 'flower_group', 'hoa-nhiet-doi', NULL, ARRAY['curcuma', 'tulip xiem']),
('31000000-0000-0000-0000-000000000042', 'Bouvardia', 'bouvardia', 'flower_group', 'hoa-phu', NULL, ARRAY['bouvardia']),
('31000000-0000-0000-0000-000000000043', 'Trachelium', 'trachelium', 'flower_group', 'hoa-phu', NULL, ARRAY['trachelium']),
('31000000-0000-0000-0000-000000000044', 'Ornithogalum', 'ornithogalum', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['ornithogalum']),
('31000000-0000-0000-0000-000000000045', 'Tử đinh hương', 'tu-dinh-huong', 'flower_group', 'canh-hoa', NULL, ARRAY['tu dinh huong', 'syringa', 'lilac']),
('31000000-0000-0000-0000-000000000046', 'Viburnum / snowball', 'viburnum-snowball', 'flower_group', 'canh-hoa', NULL, ARRAY['viburnum', 'snowball']),
('31000000-0000-0000-0000-000000000047', 'Hồng chùm Sofia', 'hong-chum-sofia', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['sofia', 'hong chum sofia']),
('31000000-0000-0000-0000-000000000048', 'Hồng chùm Plume', 'hong-chum-plume', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['plume', 'hong chum plume']),
('31000000-0000-0000-0000-000000000049', 'Hồng Pink O''Hara', 'hong-pink-o-hara', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['pink ohara', 'pink o''hara']),
('31000000-0000-0000-0000-00000000004a', 'Hồng White O''Hara', 'hong-white-o-hara', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['white ohara', 'white o''hara']),
('31000000-0000-0000-0000-00000000004b', 'Hồng Juliet', 'hong-juliet', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['juliet', 'hong juliet']),
('31000000-0000-0000-0000-00000000004c', 'Hồng Keira', 'hong-keira', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['keira', 'hong keira']),
('31000000-0000-0000-0000-00000000004d', 'Hồng Patience', 'hong-patience', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['patience', 'hong patience']),
('31000000-0000-0000-0000-00000000004e', 'Hồng Constance', 'hong-constance', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['constance', 'hong constance']),
('31000000-0000-0000-0000-00000000004f', 'Hồng Miranda', 'hong-miranda', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['miranda', 'hong miranda']),
('31000000-0000-0000-0000-000000000050', 'Hồng Beatrice', 'hong-beatrice', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['beatrice', 'hong beatrice']),
('31000000-0000-0000-0000-000000000051', 'Hồng Tess', 'hong-tess', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['tess', 'hong tess']),
('31000000-0000-0000-0000-000000000052', 'Hồng Darcey', 'hong-darcey', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['darcey', 'hong darcey']),
('31000000-0000-0000-0000-000000000053', 'Hồng Edith', 'hong-edith', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['edith', 'hong edith']),
('31000000-0000-0000-0000-000000000054', 'Hồng Effie', 'hong-effie', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['effie', 'hong effie']),
('31000000-0000-0000-0000-000000000055', 'Hồng Explorer', 'hong-explorer', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['explorer', 'hong explorer']),
('31000000-0000-0000-0000-000000000056', 'Hồng Freedom', 'hong-freedom', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['freedom', 'hong freedom']),
('31000000-0000-0000-0000-000000000057', 'Hồng Quicksand', 'hong-quicksand', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['quicksand', 'hong quicksand']),
('31000000-0000-0000-0000-000000000058', 'Hồng Mondial', 'hong-mondial', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['mondial', 'hong mondial']),
('31000000-0000-0000-0000-000000000059', 'Hồng Pink Mondial', 'hong-pink-mondial', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['pink mondial', 'hong pink mondial']),
('31000000-0000-0000-0000-00000000005a', 'Hồng Pink Floyd', 'hong-pink-floyd', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['pink floyd', 'hong pink floyd']),
('31000000-0000-0000-0000-00000000005b', 'Hồng Coffee Break', 'hong-coffee-break', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['coffee break', 'hong coffee break']),
('31000000-0000-0000-0000-00000000005c', 'Hồng Free Spirit', 'hong-free-spirit', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['free spirit', 'hong free spirit']),
('31000000-0000-0000-0000-00000000005d', 'Hồng Hermosa', 'hong-hermosa', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['hermosa', 'hong hermosa']),
('31000000-0000-0000-0000-00000000005e', 'Hồng Playa Blanca', 'hong-playa-blanca', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['playa blanca', 'hong playa blanca']),
('31000000-0000-0000-0000-00000000005f', 'Hồng Amnesia', 'hong-amnesia', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['amnesia', 'hong amnesia']),
('31000000-0000-0000-0000-000000000060', 'Hồng Deep Purple', 'hong-deep-purple', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['deep purple', 'hong deep purple']),
('31000000-0000-0000-0000-000000000061', 'Hồng Esperance', 'hong-esperance', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['esperance', 'hong esperance']),
('31000000-0000-0000-0000-000000000062', 'Hồng Sahara', 'hong-sahara', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['sahara', 'hong sahara']),
('31000000-0000-0000-0000-000000000063', 'Hồng Orange Crush', 'hong-orange-crush', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['orange crush', 'hong orange crush']),
('31000000-0000-0000-0000-000000000064', 'Hồng Proud', 'hong-proud', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['proud', 'hong proud']),
('31000000-0000-0000-0000-000000000065', 'Lá bạc / bạch đàn', 'la-bac-bach-dan', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['la bac', 'bach dan', 'eucalyptus']),
('31000000-0000-0000-0000-000000000066', 'Lá Ruscus', 'la-ruscus', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['ruscus']),
('31000000-0000-0000-0000-000000000067', 'Lá trầu bà Monstera', 'la-trau-ba-monstera', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['monstera', 'trau ba']),
('31000000-0000-0000-0000-000000000068', 'Lá Aspidistra', 'la-aspidistra', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['aspidistra']),
('31000000-0000-0000-0000-000000000069', 'Lá măng / Asparagus', 'la-mang-asparagus', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['la mang', 'asparagus']),
('31000000-0000-0000-0000-00000000006a', 'Lá Salal', 'la-salal', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['salal', 'gaultheria']),
('31000000-0000-0000-0000-00000000006b', 'Lá Pittosporum', 'la-pittosporum', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['pittosporum']),
('31000000-0000-0000-0000-00000000006c', 'Dương xỉ da', 'duong-xi-da', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['duong xi da', 'leatherleaf']),
('31000000-0000-0000-0000-00000000006d', 'Cành quả Hypericum', 'canh-qua-hypericum', 'decorative_foliage_or_fruit', 'canh-qua', NULL, ARRAY['hypericum']),
('31000000-0000-0000-0000-00000000006e', 'Đào đông / Ilex', 'dao-dong-ilex', 'decorative_foliage_or_fruit', 'canh-qua', NULL, ARRAY['dao dong', 'ilex', 'winterberry'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;

-- Insert new featured floral products
INSERT INTO catalog.products(id, category_id, sku, name, slug, description, price, status, weight_g, length_cm, width_cm, height_cm) VALUES
('10000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000001', 'FL-013', 'Hồng Juliet Quý Tộc', 'hong-juliet-quy-toc', 'Bó hoa hồng Juliet David Austin hoàng gia tone cam đào ngọt ngào, kết hợp cúc Tana và lá bạc Eucalyptus nhập khẩu.', 890000, 'ACTIVE', 600, 35, 25, 45),
('10000000-0000-0000-0000-000000000014', '00000000-0000-0000-0000-000000000001', 'FL-014', 'Mẫu Đơn Lạc Lối (Peony Charm)', 'mau-don-lac-loi', 'Bó hoa mẫu đơn Peony nhập khẩu kiêu sa kết hợp hoa Astilbe mềm mại và cẩm chướng viền hồng dịu dàng.', 1250000, 'ACTIVE', 600, 35, 25, 45),
('10000000-0000-0000-0000-000000000015', '00000000-0000-0000-0000-000000000003', 'FL-015', 'Hộp Hoa Pink O''Hara Hương Thơm', 'hop-hoa-pink-ohara-huong-thom', 'Hộp hoa hồng thơm Pink O''Hara Đà Lạt nồng nàn quyến rũ, điểm xuyết thanh liễu trắng và lá Ruscus cao cấp.', 780000, 'ACTIVE', 600, 35, 25, 45),
('10000000-0000-0000-0000-000000000016', '00000000-0000-0000-0000-000000000002', 'FL-016', 'Giỏ Hoa Bình Minh Rực Rỡ', 'gio-hoa-binh-minh-ruc-ro', 'Giỏ hoa cúc Craspedia bi vàng kết hợp hướng dương rạng ngời, cẩm chướng và lá măng tây xanh mướt.', 650000, 'ACTIVE', 600, 35, 25, 45),
('10000000-0000-0000-0000-000000000017', '00000000-0000-0000-0000-000000000002', 'FL-017', 'Bình Cát Tường & Phi Yến (Ocean Breeze)', 'binh-cat-tuong-phi-yen', 'Bình hoa phối màu xanh biển thanh mát từ phi yến Delphinium, cát tường trắng tinh khôi và hoa chuông Campanula.', 920000, 'ACTIVE', 600, 35, 25, 45),
('10000000-0000-0000-0000-000000000018', '00000000-0000-0000-0000-000000000004', 'FL-018', 'Kệ Khai Trương King Protea Đế Vương', 'ke-khai-truong-king-protea', 'Kệ hoa phong thủy cao cấp hoa đế vương Protea Nam Phi, thiên điểu rực lửa, hồng môn và cành đào đông Ilex đỏ may mắn.', 2400000, 'ACTIVE', 600, 35, 25, 45),
('10000000-0000-0000-0000-000000000019', '00000000-0000-0000-0000-000000000001', 'FL-019', 'Bó Hoa Sen Bách Diệp Thuần Khiết', 'bo-hoa-sen-bach-diep', 'Bó hoa sen hồng gấp cánh thủ công nghệ thuật truyền thống, kết hợp đài sen non và lá monstera sang trọng.', 520000, 'ACTIVE', 600, 35, 25, 45),
('10000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000001', 'FL-020', 'Bó Hồng Chùm Sofia Pastel', 'bo-hong-chum-sofia', 'Bó hồng chùm Sofia khổng lồ màu cam pastel thời thượng, phối hoa thanh anh Agapanthus và lá bạch đàn.', 850000, 'ACTIVE', 600, 35, 25, 45)
ON CONFLICT (id) DO NOTHING;

-- Product images
INSERT INTO catalog.product_images(id, product_id, url, alt_text, sort_order, is_primary) VALUES
('50000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000013', 'https://images.unsplash.com/photo-1563241527-3004b7be0ffd?auto=format&fit=crop&w=800&q=80', 'Hồng Juliet Quý Tộc', 1, true),
('50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000014', 'https://images.unsplash.com/photo-1526047932273-341f2a7631f9?auto=format&fit=crop&w=800&q=80', 'Mẫu Đơn Lạc Lối (Peony Charm)', 1, true),
('50000000-0000-0000-0000-00000000000f', '10000000-0000-0000-0000-000000000015', 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=800&q=80', 'Hộp Hoa Pink O''Hara Hương Thơm', 1, true),
('50000000-0000-0000-0000-000000000010', '10000000-0000-0000-0000-000000000016', 'https://images.unsplash.com/photo-1508610048659-a06b669e3321?auto=format&fit=crop&w=800&q=80', 'Giỏ Hoa Bình Minh Rực Rỡ', 1, true),
('50000000-0000-0000-0000-000000000011', '10000000-0000-0000-0000-000000000017', 'https://images.unsplash.com/photo-1591886960571-74d43a9d4166?auto=format&fit=crop&w=800&q=80', 'Bình Cát Tường & Phi Yến (Ocean Breeze)', 1, true),
('50000000-0000-0000-0000-000000000012', '10000000-0000-0000-0000-000000000018', 'https://images.unsplash.com/photo-1521737604893-d14cc237f11d?auto=format&fit=crop&w=800&q=80', 'Kệ Khai Trương King Protea Đế Vương', 1, true),
('50000000-0000-0000-0000-000000000013', '10000000-0000-0000-0000-000000000019', 'https://images.unsplash.com/photo-1582794543139-8ac9cb0f7b11?auto=format&fit=crop&w=800&q=80', 'Bó Hoa Sen Bách Diệp Thuần Khiết', 1, true),
('50000000-0000-0000-0000-000000000014', '10000000-0000-0000-0000-000000000020', 'https://images.unsplash.com/photo-1561181286-d3fee7d55364?auto=format&fit=crop&w=800&q=80', 'Bó Hồng Chùm Sofia Pastel', 1, true)
ON CONFLICT (id) DO NOTHING;

-- Map occasions
INSERT INTO catalog.product_occasions(product_id, occasion_id) VALUES
('10000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000001'),
('10000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000002'),
('10000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000005'),
('10000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000001'),
('10000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000002'),
('10000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000004'),
('10000000-0000-0000-0000-000000000015', '20000000-0000-0000-0000-000000000002'),
('10000000-0000-0000-0000-000000000015', '20000000-0000-0000-0000-000000000005'),
('10000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000003'),
('10000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000004'),
('10000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000001'),
('10000000-0000-0000-0000-000000000017', '20000000-0000-0000-0000-000000000004'),
('10000000-0000-0000-0000-000000000017', '20000000-0000-0000-0000-000000000005'),
('10000000-0000-0000-0000-000000000018', '20000000-0000-0000-0000-000000000003'),
('10000000-0000-0000-0000-000000000018', '20000000-0000-0000-0000-000000000004'),
('10000000-0000-0000-0000-000000000019', '20000000-0000-0000-0000-000000000005'),
('10000000-0000-0000-0000-000000000019', '20000000-0000-0000-0000-000000000004'),
('10000000-0000-0000-0000-000000000020', '20000000-0000-0000-0000-000000000001'),
('10000000-0000-0000-0000-000000000020', '20000000-0000-0000-0000-000000000002')
ON CONFLICT DO NOTHING;

-- Map colors
INSERT INTO catalog.product_colors(product_id, color_id) VALUES
('10000000-0000-0000-0000-000000000013', '40000000-0000-0000-0000-000000000007'),
('10000000-0000-0000-0000-000000000013', '40000000-0000-0000-0000-000000000002'),
('10000000-0000-0000-0000-000000000014', '40000000-0000-0000-0000-000000000002'),
('10000000-0000-0000-0000-000000000014', '40000000-0000-0000-0000-000000000003'),
('10000000-0000-0000-0000-000000000015', '40000000-0000-0000-0000-000000000002'),
('10000000-0000-0000-0000-000000000016', '40000000-0000-0000-0000-000000000004'),
('10000000-0000-0000-0000-000000000016', '40000000-0000-0000-0000-000000000007'),
('10000000-0000-0000-0000-000000000017', '40000000-0000-0000-0000-000000000003'),
('10000000-0000-0000-0000-000000000017', '40000000-0000-0000-0000-000000000006'),
('10000000-0000-0000-0000-000000000018', '40000000-0000-0000-0000-000000000001'),
('10000000-0000-0000-0000-000000000018', '40000000-0000-0000-0000-000000000004'),
('10000000-0000-0000-0000-000000000019', '40000000-0000-0000-0000-000000000002'),
('10000000-0000-0000-0000-000000000019', '40000000-0000-0000-0000-000000000003'),
('10000000-0000-0000-0000-000000000020', '40000000-0000-0000-0000-000000000007'),
('10000000-0000-0000-0000-000000000020', '40000000-0000-0000-0000-000000000002')
ON CONFLICT DO NOTHING;

-- Map flower types by slug lookup
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000013', id FROM catalog.flower_types WHERE slug = 'hong-juliet'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000013', id FROM catalog.flower_types WHERE slug = 'hoa-hong'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000013', id FROM catalog.flower_types WHERE slug = 'cuc-tana'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000013', id FROM catalog.flower_types WHERE slug = 'la-bac-bach-dan'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000014', id FROM catalog.flower_types WHERE slug = 'mau-don-peony'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000014', id FROM catalog.flower_types WHERE slug = 'astilbe'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000014', id FROM catalog.flower_types WHERE slug = 'cam-chuong'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000015', id FROM catalog.flower_types WHERE slug = 'hong-pink-o-hara'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000015', id FROM catalog.flower_types WHERE slug = 'hoa-hong'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000015', id FROM catalog.flower_types WHERE slug = 'thanh-lieu'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000015', id FROM catalog.flower_types WHERE slug = 'la-ruscus'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000016', id FROM catalog.flower_types WHERE slug = 'craspedia-cuc-bi-vang'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000016', id FROM catalog.flower_types WHERE slug = 'huong-duong'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000016', id FROM catalog.flower_types WHERE slug = 'cam-chuong'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000016', id FROM catalog.flower_types WHERE slug = 'la-mang-asparagus'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000017', id FROM catalog.flower_types WHERE slug = 'phi-yen-delphinium'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000017', id FROM catalog.flower_types WHERE slug = 'cat-tuong'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000017', id FROM catalog.flower_types WHERE slug = 'hoa-chuong-campanula'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000018', id FROM catalog.flower_types WHERE slug = 'protea'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000018', id FROM catalog.flower_types WHERE slug = 'thien-dieu'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000018', id FROM catalog.flower_types WHERE slug = 'hong-mon'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000018', id FROM catalog.flower_types WHERE slug = 'dao-dong-ilex'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000019', id FROM catalog.flower_types WHERE slug = 'hoa-sen'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000019', id FROM catalog.flower_types WHERE slug = 'la-trau-ba-monstera'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000020', id FROM catalog.flower_types WHERE slug = 'hong-chum-sofia'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000020', id FROM catalog.flower_types WHERE slug = 'thanh-anh-agapanthus'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000020', id FROM catalog.flower_types WHERE slug = 'la-bac-bach-dan'
ON CONFLICT DO NOTHING;
