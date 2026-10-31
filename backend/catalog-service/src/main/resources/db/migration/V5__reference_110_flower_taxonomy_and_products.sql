-- V5: Import 110 reference flower types, rose cultivars, decorative foliage and add rich new products
SET client_encoding = 'UTF8';

ALTER TABLE catalog.flower_types ADD COLUMN IF NOT EXISTS record_type TEXT DEFAULT 'flower_group';
ALTER TABLE catalog.flower_types ADD COLUMN IF NOT EXISTS display_group TEXT;
ALTER TABLE catalog.flower_types ADD COLUMN IF NOT EXISTS parent_slug TEXT;
ALTER TABLE catalog.flower_types ADD COLUMN IF NOT EXISTS search_aliases TEXT[];

-- Update/Insert 110 reference flower items
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000001', 'Hoa hồng', 'hoa-hong', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['hong', 'rose'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000002', 'Hoa ly / lily', 'hoa-ly-lily', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['ly', 'lily'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000003', 'Tulip / uất kim hương', 'tulip-uat-kim-huong', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['tulip', 'uat kim huong'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000004', 'Cẩm chướng', 'cam-chuong', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['cam chuong', 'carnation'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000005', 'Đồng tiền', 'dong-tien', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['dong tien', 'gerbera'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000006', 'Hoa cúc', 'hoa-cuc', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['cuc', 'chrysanthemum'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000007', 'Cát tường', 'cat-tuong', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['cat tuong', 'lisianthus'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000008', 'Cẩm tú cầu', 'cam-tu-cau', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['cam tu cau', 'hydrangea'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000009', 'Hướng dương', 'huong-duong', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['huong duong', 'sunflower'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000000a', 'Lay ơn / dơn', 'lay-on-don', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['lay on', 'don', 'gladiolus'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000000b', 'Thủy tiên Peru', 'thuy-tien-peru', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['thuy tien peru', 'alstroemeria'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000000c', 'Mao lương', 'mao-luong', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['mao luong', 'ranunculus'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000000d', 'Mẫu đơn / peony', 'mau-don-peony', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['mau don', 'peony', 'paeonia'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000000e', 'Thược dược', 'thuoc-duoc', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['thuoc duoc', 'dahlia'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000000f', 'Calla / rum', 'calla-rum', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['calla', 'rum'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000010', 'Hải quỳ / anemone', 'hai-quy-anemone', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['hai quy', 'anemone'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000011', 'Lan Nam Phi / freesia', 'lan-nam-phi-freesia', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['lan nam phi', 'freesia'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000012', 'Phi yến / delphinium', 'phi-yen-delphinium', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['phi yen', 'delphinium'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000013', 'Mõm sói', 'mom-soi', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['mom soi', 'snapdragon'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000014', 'Diên vĩ', 'dien-vi', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['dien vi', 'iris'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000015', 'Stock / Matthiola', 'stock-matthiola', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['stock', 'matthiola'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000016', 'Hoa chuông Campanula', 'hoa-chuong-campanula', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['hoa chuong', 'campanula'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000017', 'Đậu thơm', 'dau-thom', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['dau thom', 'sweet pea'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000018', 'Dạ lan hương', 'da-lan-huong', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['da lan huong', 'hyacinth'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000019', 'Thủy tiên Narcissus', 'thuy-tien-narcissus', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['thuy tien', 'narcissus', 'daffodil'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000001a', 'Chu đỉnh hồng', 'chu-dinh-hong', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['chu dinh hong', 'hippeastrum'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000001b', 'Helleborus', 'helleborus', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['helleborus', 'hellebore'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000001c', 'Scabiosa', 'scabiosa', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['scabiosa', 'scabious'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000001d', 'Astilbe', 'astilbe', 'flower_group', 'hoa-phu', NULL, ARRAY['astilbe'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000001e', 'Astrantia', 'astrantia', 'flower_group', 'hoa-phu', NULL, ARRAY['astrantia', 'masterwort'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000001f', 'Ông lão / Clematis', 'ong-lao-clematis', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['ong lao', 'clematis'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000020', 'Veronica', 'veronica', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['veronica'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000021', 'Hành cảnh / Allium', 'hanh-canh-allium', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['hanh canh', 'allium'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000022', 'Thanh anh / Agapanthus', 'thanh-anh-agapanthus', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['thanh anh', 'agapanthus'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000023', 'Eremurus', 'eremurus', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['eremurus', 'foxtail lily'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000024', 'Liatris', 'liatris', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['liatris', 'blazing star'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000025', 'Baby / bi', 'baby-bi', 'flower_group', 'hoa-phu', NULL, ARRAY['baby', 'bi', 'gypsophila'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000026', 'Thanh liễu', 'thanh-lieu', 'flower_group', 'hoa-phu', NULL, ARRAY['thanh lieu', 'waxflower'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000027', 'Salem và các dòng Limonium', 'salem-va-cac-dong-limonium', 'flower_group', 'hoa-phu', NULL, ARRAY['salem', 'statice', 'limonium'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000028', 'Solidago', 'solidago', 'flower_group', 'hoa-phu', NULL, ARRAY['solidago', 'goldenrod'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000029', 'Cúc Tana', 'cuc-tana', 'flower_group', 'hoa-phu', NULL, ARRAY['cuc tana', 'tana', 'feverfew'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000002a', 'Nhím biển / Eryngium', 'nhim-bien-eryngium', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['nhim bien', 'eryngium'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000002b', 'Craspedia / cúc bi vàng', 'craspedia-cuc-bi-vang', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['craspedia', 'cuc bi vang'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000002c', 'Ammi', 'ammi', 'flower_group', 'hoa-phu', NULL, ARRAY['ammi'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000002d', 'Bupleurum', 'bupleurum', 'flower_group', 'hoa-phu', NULL, ARRAY['bupleurum'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000002e', 'Nigella', 'nigella', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['nigella'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000002f', 'Orlaya', 'orlaya', 'flower_group', 'hoa-phu', NULL, ARRAY['orlaya'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000030', 'Cỏ thi / Achillea', 'co-thi-achillea', 'flower_group', 'hoa-phu', NULL, ARRAY['co thi', 'achillea', 'yarrow'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000031', 'Mào gà', 'mao-ga', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['mao ga', 'celosia'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000032', 'Dền cảnh / Amaranthus', 'den-canh-amaranthus', 'flower_group', 'hoa-ru', NULL, ARRAY['den canh', 'amaranthus'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000033', 'Cúc lá nhám', 'cuc-la-nham', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['cuc la nham', 'zinnia'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000034', 'Sao nhái / cánh bướm', 'sao-nhai-canh-buom', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['sao nhai', 'canh buom', 'cosmos'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000035', 'Cúc bách nhật', 'cuc-bach-nhat', 'flower_group', 'hoa-phu', NULL, ARRAY['cuc bach nhat', 'gomphrena'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000036', 'Lan hồ điệp', 'lan-ho-diep', 'flower_group', 'hoa-lan', NULL, ARRAY['lan ho diep', 'phalaenopsis'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000037', 'Địa lan / Cymbidium', 'dia-lan-cymbidium', 'flower_group', 'hoa-lan', NULL, ARRAY['dia lan', 'cymbidium'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000038', 'Lan Dendrobium', 'lan-dendrobium', 'flower_group', 'hoa-lan', NULL, ARRAY['lan dendro', 'dendrobium'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000039', 'Lan Vanda', 'lan-vanda', 'flower_group', 'hoa-lan', NULL, ARRAY['lan vanda', 'vanda'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000003a', 'Hồng môn', 'hong-mon', 'flower_group', 'hoa-nhiet-doi', NULL, ARRAY['hong mon', 'anthurium'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000003b', 'Protea', 'protea', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['protea', 'hoa de vuong'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000003c', 'Leucospermum', 'leucospermum', 'flower_group', 'hoa-diem-nhan', NULL, ARRAY['leucospermum', 'pincushion'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000003d', 'Hoa sen', 'hoa-sen', 'flower_group', 'hoa-chu-dao', NULL, ARRAY['sen', 'lotus'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000003e', 'Hoa huệ trắng', 'hoa-hue-trang', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['hue trang', 'tuberose'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000003f', 'Thiên điểu', 'thien-dieu', 'flower_group', 'hoa-nhiet-doi', NULL, ARRAY['thien dieu', 'strelitzia'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000040', 'Heliconia / chuối mỏ két', 'heliconia-chuoi-mo-ket', 'flower_group', 'hoa-nhiet-doi', NULL, ARRAY['heliconia', 'chuoi mo ket'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000041', 'Curcuma / tulip Xiêm', 'curcuma-tulip-xiem', 'flower_group', 'hoa-nhiet-doi', NULL, ARRAY['curcuma', 'tulip xiem'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000042', 'Bouvardia', 'bouvardia', 'flower_group', 'hoa-phu', NULL, ARRAY['bouvardia'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000043', 'Trachelium', 'trachelium', 'flower_group', 'hoa-phu', NULL, ARRAY['trachelium'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000044', 'Ornithogalum', 'ornithogalum', 'flower_group', 'hoa-dang-canh', NULL, ARRAY['ornithogalum'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000045', 'Tử đinh hương', 'tu-dinh-huong', 'flower_group', 'canh-hoa', NULL, ARRAY['tu dinh huong', 'syringa', 'lilac'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000046', 'Viburnum / snowball', 'viburnum-snowball', 'flower_group', 'canh-hoa', NULL, ARRAY['viburnum', 'snowball'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000047', 'Hồng chùm Sofia', 'hong-chum-sofia', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['sofia', 'hong chum sofia'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000048', 'Hồng chùm Plume', 'hong-chum-plume', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['plume', 'hong chum plume'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000049', 'Hồng Pink O''Hara', 'hong-pink-o-hara', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['pink ohara', 'pink o''hara'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000004a', 'Hồng White O''Hara', 'hong-white-o-hara', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['white ohara', 'white o''hara'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000004b', 'Hồng Juliet', 'hong-juliet', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['juliet', 'hong juliet'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000004c', 'Hồng Keira', 'hong-keira', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['keira', 'hong keira'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000004d', 'Hồng Patience', 'hong-patience', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['patience', 'hong patience'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000004e', 'Hồng Constance', 'hong-constance', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['constance', 'hong constance'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000004f', 'Hồng Miranda', 'hong-miranda', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['miranda', 'hong miranda'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000050', 'Hồng Beatrice', 'hong-beatrice', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['beatrice', 'hong beatrice'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000051', 'Hồng Tess', 'hong-tess', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['tess', 'hong tess'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000052', 'Hồng Darcey', 'hong-darcey', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['darcey', 'hong darcey'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000053', 'Hồng Edith', 'hong-edith', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['edith', 'hong edith'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000054', 'Hồng Effie', 'hong-effie', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['effie', 'hong effie'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000055', 'Hồng Explorer', 'hong-explorer', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['explorer', 'hong explorer'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000056', 'Hồng Freedom', 'hong-freedom', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['freedom', 'hong freedom'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000057', 'Hồng Quicksand', 'hong-quicksand', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['quicksand', 'hong quicksand'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000058', 'Hồng Mondial', 'hong-mondial', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['mondial', 'hong mondial'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000059', 'Hồng Pink Mondial', 'hong-pink-mondial', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['pink mondial', 'hong pink mondial'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000005a', 'Hồng Pink Floyd', 'hong-pink-floyd', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['pink floyd', 'hong pink floyd'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000005b', 'Hồng Coffee Break', 'hong-coffee-break', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['coffee break', 'hong coffee break'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000005c', 'Hồng Free Spirit', 'hong-free-spirit', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['free spirit', 'hong free spirit'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000005d', 'Hồng Hermosa', 'hong-hermosa', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['hermosa', 'hong hermosa'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000005e', 'Hồng Playa Blanca', 'hong-playa-blanca', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['playa blanca', 'hong playa blanca'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000005f', 'Hồng Amnesia', 'hong-amnesia', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['amnesia', 'hong amnesia'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000060', 'Hồng Deep Purple', 'hong-deep-purple', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['deep purple', 'hong deep purple'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000061', 'Hồng Esperance', 'hong-esperance', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['esperance', 'hong esperance'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000062', 'Hồng Sahara', 'hong-sahara', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['sahara', 'hong sahara'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000063', 'Hồng Orange Crush', 'hong-orange-crush', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['orange crush', 'hong orange crush'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000064', 'Hồng Proud', 'hong-proud', 'rose_cultivar_or_trade_name', 'giong-hong', 'hoa-hong', ARRAY['proud', 'hong proud'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000065', 'Lá bạc / bạch đàn', 'la-bac-bach-dan', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['la bac', 'bach dan', 'eucalyptus'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000066', 'Lá Ruscus', 'la-ruscus', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['ruscus'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000067', 'Lá trầu bà Monstera', 'la-trau-ba-monstera', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['monstera', 'trau ba'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000068', 'Lá Aspidistra', 'la-aspidistra', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['aspidistra'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-000000000069', 'Lá măng / Asparagus', 'la-mang-asparagus', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['la mang', 'asparagus'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000006a', 'Lá Salal', 'la-salal', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['salal', 'gaultheria'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000006b', 'Lá Pittosporum', 'la-pittosporum', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['pittosporum'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000006c', 'Dương xỉ da', 'duong-xi-da', 'decorative_foliage_or_fruit', 'la-trang-tri', NULL, ARRAY['duong xi da', 'leatherleaf'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000006d', 'Cành quả Hypericum', 'canh-qua-hypericum', 'decorative_foliage_or_fruit', 'canh-qua', NULL, ARRAY['hypericum'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;
INSERT INTO catalog.flower_types(id, name, slug, record_type, display_group, parent_slug, search_aliases)
VALUES ('31000000-0000-0000-0000-00000000006e', 'Đào đông / Ilex', 'dao-dong-ilex', 'decorative_foliage_or_fruit', 'canh-qua', NULL, ARRAY['dao dong', 'ilex', 'winterberry'])
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  record_type = EXCLUDED.record_type,
  display_group = EXCLUDED.display_group,
  parent_slug = EXCLUDED.parent_slug,
  search_aliases = EXCLUDED.search_aliases;

-- Fix images for FL-008 and FL-009
UPDATE catalog.product_images
SET url = 'https://images.unsplash.com/photo-1518895949257-7621c3c786d7?auto=format&fit=crop&w=800&q=80',
    alt_text = 'Hộp Hoa Trái Tim Scarlet'
WHERE product_id = '10000000-0000-0000-0000-000000000008';

UPDATE catalog.product_images
SET url = 'https://images.unsplash.com/photo-1614594975525-e45190c55d0b?auto=format&fit=crop&w=800&q=80',
    alt_text = 'Kệ Khai Trương Phát Tài'
WHERE product_id = '10000000-0000-0000-0000-000000000009';

INSERT INTO catalog.products(id, category_id, sku, name, slug, description, price, status, weight_g, length_cm, width_cm, height_cm)
SELECT '10000000-0000-0000-0000-000000000013', c.id, 'FL-013', 'Hồng Juliet Quý Tộc', 'hong-juliet-quy-toc', 'Bó hoa hồng Juliet David Austin hoàng gia tone cam đào ngọt ngào, kết hợp cúc Tana và lá bạc Eucalyptus nhập khẩu.', 890000, 'ACTIVE', 600, 35, 30, 50
FROM catalog.categories c WHERE c.slug = 'hoa-bo'
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  price = EXCLUDED.price,
  slug = EXCLUDED.slug;

-- Upsert Primary Image
DELETE FROM catalog.product_images WHERE product_id = '10000000-0000-0000-0000-000000000013';
INSERT INTO catalog.product_images(id, product_id, url, alt_text, sort_order, is_primary)
VALUES (gen_random_uuid(), '10000000-0000-0000-0000-000000000013', 'https://images.unsplash.com/photo-1563241527-3004b7be0ffd?auto=format&fit=crop&w=800&q=80', 'Hồng Juliet Quý Tộc', 1, true);
DELETE FROM catalog.product_colors WHERE product_id = '10000000-0000-0000-0000-000000000013';
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000013', id FROM catalog.colors WHERE name = 'Cam'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000013', id FROM catalog.colors WHERE name = 'Hồng'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_occasions WHERE product_id = '10000000-0000-0000-0000-000000000013';
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000013', id FROM catalog.occasions WHERE slug = 'sinh-nhat'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000013', id FROM catalog.occasions WHERE slug = 'tinh-yeu-ky-niem'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000013', id FROM catalog.occasions WHERE slug = 'chuc-mung'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_flower_types WHERE product_id = '10000000-0000-0000-0000-000000000013';
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000013', id FROM catalog.flower_types WHERE slug = 'hong-juliet'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000013', id FROM catalog.flower_types WHERE slug = 'cuc-tana'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000013', id FROM catalog.flower_types WHERE slug = 'la-bac-bach-dan'
ON CONFLICT DO NOTHING;

INSERT INTO catalog.products(id, category_id, sku, name, slug, description, price, status, weight_g, length_cm, width_cm, height_cm)
SELECT '10000000-0000-0000-0000-000000000014', c.id, 'FL-014', 'Mẫu Đơn Lạc Lối (Peony Charm)', 'mau-don-lac-loi', 'Bó hoa mẫu đơn Peony nhập khẩu kiêu sa kết hợp hoa Astilbe mềm mại và cẩm chướng viền hồng dịu dàng.', 1250000, 'ACTIVE', 600, 35, 30, 50
FROM catalog.categories c WHERE c.slug = 'hoa-bo'
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  price = EXCLUDED.price,
  slug = EXCLUDED.slug;

-- Upsert Primary Image
DELETE FROM catalog.product_images WHERE product_id = '10000000-0000-0000-0000-000000000014';
INSERT INTO catalog.product_images(id, product_id, url, alt_text, sort_order, is_primary)
VALUES (gen_random_uuid(), '10000000-0000-0000-0000-000000000014', 'https://images.unsplash.com/photo-1526047932273-341f2a7631f9?auto=format&fit=crop&w=800&q=80', 'Mẫu Đơn Lạc Lối (Peony Charm)', 1, true);
DELETE FROM catalog.product_colors WHERE product_id = '10000000-0000-0000-0000-000000000014';
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000014', id FROM catalog.colors WHERE name = 'Hồng'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000014', id FROM catalog.colors WHERE name = 'Trắng'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_occasions WHERE product_id = '10000000-0000-0000-0000-000000000014';
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000014', id FROM catalog.occasions WHERE slug = 'sinh-nhat'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000014', id FROM catalog.occasions WHERE slug = 'tinh-yeu-ky-niem'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000014', id FROM catalog.occasions WHERE slug = 'tri-an-phu-nu'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_flower_types WHERE product_id = '10000000-0000-0000-0000-000000000014';
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000014', id FROM catalog.flower_types WHERE slug = 'mau-don-peony'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000014', id FROM catalog.flower_types WHERE slug = 'astilbe'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000014', id FROM catalog.flower_types WHERE slug = 'cam-chuong'
ON CONFLICT DO NOTHING;

INSERT INTO catalog.products(id, category_id, sku, name, slug, description, price, status, weight_g, length_cm, width_cm, height_cm)
SELECT '10000000-0000-0000-0000-000000000015', c.id, 'FL-015', 'Hộp Hoa Pink O''Hara Hương Thơm', 'hop-hoa-pink-ohara', 'Hộp hoa hồng thơm Pink O''Hara Đà Lạt nồng nàn quyến rũ, điểm xuyết thanh liễu trắng và lá Ruscus cao cấp.', 780000, 'ACTIVE', 600, 35, 30, 50
FROM catalog.categories c WHERE c.slug = 'hop-hoa-nghe-thuat'
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  price = EXCLUDED.price,
  slug = EXCLUDED.slug;

-- Upsert Primary Image
DELETE FROM catalog.product_images WHERE product_id = '10000000-0000-0000-0000-000000000015';
INSERT INTO catalog.product_images(id, product_id, url, alt_text, sort_order, is_primary)
VALUES (gen_random_uuid(), '10000000-0000-0000-0000-000000000015', 'https://images.unsplash.com/photo-1559563458-527698bf5295?auto=format&fit=crop&w=800&q=80', 'Hộp Hoa Pink O''Hara Hương Thơm', 1, true);
DELETE FROM catalog.product_colors WHERE product_id = '10000000-0000-0000-0000-000000000015';
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000015', id FROM catalog.colors WHERE name = 'Hồng'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000015', id FROM catalog.colors WHERE name = 'Trắng'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_occasions WHERE product_id = '10000000-0000-0000-0000-000000000015';
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000015', id FROM catalog.occasions WHERE slug = 'sinh-nhat'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000015', id FROM catalog.occasions WHERE slug = 'tinh-yeu-ky-niem'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_flower_types WHERE product_id = '10000000-0000-0000-0000-000000000015';
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000015', id FROM catalog.flower_types WHERE slug = 'hong-pink-o-hara'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000015', id FROM catalog.flower_types WHERE slug = 'thanh-lieu'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000015', id FROM catalog.flower_types WHERE slug = 'la-ruscus'
ON CONFLICT DO NOTHING;

INSERT INTO catalog.products(id, category_id, sku, name, slug, description, price, status, weight_g, length_cm, width_cm, height_cm)
SELECT '10000000-0000-0000-0000-000000000016', c.id, 'FL-016', 'Giỏ Hoa Bình Minh Rực Rỡ', 'gio-hoa-binh-minh-ruc-ro', 'Giỏ hoa cúc Craspedia bi vàng kết hợp hướng dương rạng ngời, cẩm chướng và lá măng tây xanh mướt.', 650000, 'ACTIVE', 600, 35, 30, 50
FROM catalog.categories c WHERE c.slug = 'gio-hoa'
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  price = EXCLUDED.price,
  slug = EXCLUDED.slug;

-- Upsert Primary Image
DELETE FROM catalog.product_images WHERE product_id = '10000000-0000-0000-0000-000000000016';
INSERT INTO catalog.product_images(id, product_id, url, alt_text, sort_order, is_primary)
VALUES (gen_random_uuid(), '10000000-0000-0000-0000-000000000016', 'https://images.unsplash.com/photo-1508610048659-a06b669e3321?auto=format&fit=crop&w=800&q=80', 'Giỏ Hoa Bình Minh Rực Rỡ', 1, true);
DELETE FROM catalog.product_colors WHERE product_id = '10000000-0000-0000-0000-000000000016';
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000016', id FROM catalog.colors WHERE name = 'Vàng'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000016', id FROM catalog.colors WHERE name = 'Cam'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_occasions WHERE product_id = '10000000-0000-0000-0000-000000000016';
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000016', id FROM catalog.occasions WHERE slug = 'sinh-nhat'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000016', id FROM catalog.occasions WHERE slug = 'chuc-mung'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000016', id FROM catalog.occasions WHERE slug = 'tri-an-phu-nu'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_flower_types WHERE product_id = '10000000-0000-0000-0000-000000000016';
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

INSERT INTO catalog.products(id, category_id, sku, name, slug, description, price, status, weight_g, length_cm, width_cm, height_cm)
SELECT '10000000-0000-0000-0000-000000000017', c.id, 'FL-017', 'Bình Cát Tường & Phi Yến (Ocean Breeze)', 'binh-cat-tuong-phi-yen', 'Bình hoa phối màu xanh biển thanh mát từ phi yến Delphinium, cát tường trắng và lá trầu bà Monstera.', 920000, 'ACTIVE', 600, 35, 30, 50
FROM catalog.categories c WHERE c.slug = 'gio-hoa'
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  price = EXCLUDED.price,
  slug = EXCLUDED.slug;

-- Upsert Primary Image
DELETE FROM catalog.product_images WHERE product_id = '10000000-0000-0000-0000-000000000017';
INSERT INTO catalog.product_images(id, product_id, url, alt_text, sort_order, is_primary)
VALUES (gen_random_uuid(), '10000000-0000-0000-0000-000000000017', 'https://images.unsplash.com/photo-1507290439931-a861b5a38200?auto=format&fit=crop&w=800&q=80', 'Bình Cát Tường & Phi Yến (Ocean Breeze)', 1, true);
DELETE FROM catalog.product_colors WHERE product_id = '10000000-0000-0000-0000-000000000017';
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000017', id FROM catalog.colors WHERE name = 'Trắng'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000017', id FROM catalog.colors WHERE name = 'Tím'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_occasions WHERE product_id = '10000000-0000-0000-0000-000000000017';
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000017', id FROM catalog.occasions WHERE slug = 'chuc-mung'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000017', id FROM catalog.occasions WHERE slug = 'tri-an-phu-nu'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_flower_types WHERE product_id = '10000000-0000-0000-0000-000000000017';
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000017', id FROM catalog.flower_types WHERE slug = 'phi-yen-delphinium'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000017', id FROM catalog.flower_types WHERE slug = 'cat-tuong'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000017', id FROM catalog.flower_types WHERE slug = 'la-trau-ba-monstera'
ON CONFLICT DO NOTHING;

INSERT INTO catalog.products(id, category_id, sku, name, slug, description, price, status, weight_g, length_cm, width_cm, height_cm)
SELECT '10000000-0000-0000-0000-000000000018', c.id, 'FL-018', 'Kệ Khai Trương King Protea Đế Vương', 'ke-khai-truong-king-protea', 'Kệ hoa phong thủy cao cấp hoa đế vương Protea Nam Phi, thiên điểu rực rỡ và hồng môn đỏ may mắn.', 2400000, 'ACTIVE', 600, 35, 30, 50
FROM catalog.categories c WHERE c.slug = 'ke-hoa-khai-truong'
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  price = EXCLUDED.price,
  slug = EXCLUDED.slug;

-- Upsert Primary Image
DELETE FROM catalog.product_images WHERE product_id = '10000000-0000-0000-0000-000000000018';
INSERT INTO catalog.product_images(id, product_id, url, alt_text, sort_order, is_primary)
VALUES (gen_random_uuid(), '10000000-0000-0000-0000-000000000018', 'https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?auto=format&fit=crop&w=800&q=80', 'Kệ Khai Trương King Protea Đế Vương', 1, true);
DELETE FROM catalog.product_colors WHERE product_id = '10000000-0000-0000-0000-000000000018';
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000018', id FROM catalog.colors WHERE name = 'Đỏ'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000018', id FROM catalog.colors WHERE name = 'Cam'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000018', id FROM catalog.colors WHERE name = 'Vàng'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_occasions WHERE product_id = '10000000-0000-0000-0000-000000000018';
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000018', id FROM catalog.occasions WHERE slug = 'khai-truong'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000018', id FROM catalog.occasions WHERE slug = 'chuc-mung'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_flower_types WHERE product_id = '10000000-0000-0000-0000-000000000018';
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000018', id FROM catalog.flower_types WHERE slug = 'protea'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000018', id FROM catalog.flower_types WHERE slug = 'thien-dieu'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000018', id FROM catalog.flower_types WHERE slug = 'hong-mon'
ON CONFLICT DO NOTHING;

INSERT INTO catalog.products(id, category_id, sku, name, slug, description, price, status, weight_g, length_cm, width_cm, height_cm)
SELECT '10000000-0000-0000-0000-000000000019', c.id, 'FL-019', 'Bó Hoa Sen Bách Diệp Thuần Khiết', 'bo-hoa-sen-bach-diep', 'Bó hoa sen bách diệp gấp cánh nghệ thuật, phối cùng đài sen non và lá dong mang đậm nét thanh tao Việt Nam.', 750000, 'ACTIVE', 600, 35, 30, 50
FROM catalog.categories c WHERE c.slug = 'hoa-bo'
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  price = EXCLUDED.price,
  slug = EXCLUDED.slug;

-- Upsert Primary Image
DELETE FROM catalog.product_images WHERE product_id = '10000000-0000-0000-0000-000000000019';
INSERT INTO catalog.product_images(id, product_id, url, alt_text, sort_order, is_primary)
VALUES (gen_random_uuid(), '10000000-0000-0000-0000-000000000019', 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=800&q=80', 'Bó Hoa Sen Bách Diệp Thuần Khiết', 1, true);
DELETE FROM catalog.product_colors WHERE product_id = '10000000-0000-0000-0000-000000000019';
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000019', id FROM catalog.colors WHERE name = 'Hồng'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000019', id FROM catalog.colors WHERE name = 'Trắng'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_occasions WHERE product_id = '10000000-0000-0000-0000-000000000019';
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000019', id FROM catalog.occasions WHERE slug = 'tri-an-phu-nu'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000019', id FROM catalog.occasions WHERE slug = 'chuc-mung'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_flower_types WHERE product_id = '10000000-0000-0000-0000-000000000019';
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000019', id FROM catalog.flower_types WHERE slug = 'hoa-sen'
ON CONFLICT DO NOTHING;

INSERT INTO catalog.products(id, category_id, sku, name, slug, description, price, status, weight_g, length_cm, width_cm, height_cm)
SELECT '10000000-0000-0000-0000-000000000020', c.id, 'FL-020', 'Bó Hồng Chùm Sofia Pastel', 'bo-hong-chum-sofia', 'Bó hoa hồng chùm Sofia nhập khẩu màu hồng vintage ngọt ngào, phối lá bạc khuynh diệp và hoa baby nhỏ xinh.', 680000, 'ACTIVE', 600, 35, 30, 50
FROM catalog.categories c WHERE c.slug = 'hoa-bo'
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  price = EXCLUDED.price,
  slug = EXCLUDED.slug;

-- Upsert Primary Image
DELETE FROM catalog.product_images WHERE product_id = '10000000-0000-0000-0000-000000000020';
INSERT INTO catalog.product_images(id, product_id, url, alt_text, sort_order, is_primary)
VALUES (gen_random_uuid(), '10000000-0000-0000-0000-000000000020', 'https://images.unsplash.com/photo-1561181286-d3fee7d55364?auto=format&fit=crop&w=800&q=80', 'Bó Hồng Chùm Sofia Pastel', 1, true);
DELETE FROM catalog.product_colors WHERE product_id = '10000000-0000-0000-0000-000000000020';
INSERT INTO catalog.product_colors(product_id, color_id)
SELECT '10000000-0000-0000-0000-000000000020', id FROM catalog.colors WHERE name = 'Hồng'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_occasions WHERE product_id = '10000000-0000-0000-0000-000000000020';
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000020', id FROM catalog.occasions WHERE slug = 'sinh-nhat'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_occasions(product_id, occasion_id)
SELECT '10000000-0000-0000-0000-000000000020', id FROM catalog.occasions WHERE slug = 'tinh-yeu-ky-niem'
ON CONFLICT DO NOTHING;
DELETE FROM catalog.product_flower_types WHERE product_id = '10000000-0000-0000-0000-000000000020';
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000020', id FROM catalog.flower_types WHERE slug = 'hong-chum-sofia'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000020', id FROM catalog.flower_types WHERE slug = 'la-bac-bach-dan'
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_flower_types(product_id, flower_type_id)
SELECT '10000000-0000-0000-0000-000000000020', id FROM catalog.flower_types WHERE slug = 'hoa-baby'
ON CONFLICT DO NOTHING;
