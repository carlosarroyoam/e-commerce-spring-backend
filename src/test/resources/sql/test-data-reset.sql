-- Vacia (TRUNCATE, no DROP) todas las tablas que puebla src/main/resources/data.sql.
--
-- Motivo: data.sql inserta con INSERT INTO planos (sin ON DUPLICATE KEY) y schema.sql
-- nunca hace DROP. AbstractIntegrationTest usa un contenedor Testcontainers con
-- withReuse(true); si un desarrollador activa testcontainers.reuse.enable=true en
-- ~/.testcontainers.properties, una segunda ejecucion de "mvn verify" reutilizaria el
-- mismo contenedor ya poblado y el re-insert de data.sql fallaria por clave primaria
-- duplicada. Este script se ejecuta (via spring.sql.init.schema-locations) despues de
-- schema.sql y antes de data.sql, dejando las tablas vacias y listas para el seed.
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE shipments;
TRUNCATE TABLE payments;
TRUNCATE TABLE order_status_history;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE carriers;
TRUNCATE TABLE movements;
TRUNCATE TABLE movement_types;
TRUNCATE TABLE variant_attribute_values;
TRUNCATE TABLE attributes;
TRUNCATE TABLE variant_images;
TRUNCATE TABLE variants;
TRUNCATE TABLE product_property_values;
TRUNCATE TABLE properties;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
TRUNCATE TABLE customer_addresses;
TRUNCATE TABLE customers;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE users;
TRUNCATE TABLE roles;

SET FOREIGN_KEY_CHECKS = 1;
