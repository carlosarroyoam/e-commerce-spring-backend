-- E-commerce Management System MySQL Schema

CREATE DATABASE IF NOT EXISTS `spring-boot-e-commerce`;

USE `spring-boot-e-commerce`;

CREATE TABLE IF NOT EXISTS roles (
    id TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    type VARCHAR(32) NOT NULL,
    description VARCHAR(256) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    email VARCHAR(64) NOT NULL,
    password_hash VARCHAR(96) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    INDEX idx_users_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT UNSIGNED NOT NULL,
    role_id TINYINT UNSIGNED NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user_id
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role_id
        FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_refresh_tokens (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    token VARCHAR(254) NOT NULL,
    fingerprint VARCHAR(36) NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    last_used_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_refresh_tokens_token (token),
    UNIQUE KEY uk_user_refresh_tokens_user_fingerprint (user_id, fingerprint),
    CONSTRAINT fk_user_refresh_tokens_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_reset_password (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    token_hash VARCHAR(254) NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    expires_on TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_reset_password_token_hash (token_hash),
    CONSTRAINT fk_user_reset_password_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(10) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'DELETED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_customers_email (email),
    UNIQUE KEY uk_customers_phone_number (phone_number),
    INDEX idx_customers_status (status),
    INDEX idx_customers_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS customer_addresses (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    street_name VARCHAR(64) NOT NULL,
    street_number VARCHAR(5) NOT NULL,
    apartament_number VARCHAR(5),
    sublocality VARCHAR(45) NOT NULL,
    locality VARCHAR(45) NOT NULL,
    state VARCHAR(45) NOT NULL,
    postal_code VARCHAR(5) NOT NULL,
    country VARCHAR(2) DEFAULT 'MX',
    phone_number VARCHAR(10) NOT NULL,
    is_default TINYINT NOT NULL DEFAULT 1 CHECK (is_default IN (0, 1)),
    customer_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_customer_addresses_customer_id (customer_id),
    CONSTRAINT fk_customer_addresses_customer_id
        FOREIGN KEY (customer_id) REFERENCES customers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS customer_refresh_tokens (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    token VARCHAR(254) NOT NULL,
    fingerprint VARCHAR(36) NOT NULL,
    customer_id BIGINT UNSIGNED NOT NULL,
    last_used_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_customer_refresh_tokens_token (token),
    UNIQUE KEY uk_customer_refresh_tokens_fingerprint (customer_id, fingerprint),
    CONSTRAINT fk_customer_refresh_tokens_customer_id
        FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS customer_reset_password (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    token_hash VARCHAR(254) NOT NULL,
    customer_id BIGINT UNSIGNED NOT NULL,
    expires_on TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_customer_reset_password_token_hash (token_hash),
    CONSTRAINT fk_customer_reset_password_customer_id
        FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categories (
    id TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title VARCHAR(45) NOT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_title (title),
    INDEX idx_categories_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title VARCHAR(96) NOT NULL,
    slug VARCHAR(96) NOT NULL,
    description TEXT,
    is_featured TINYINT NOT NULL DEFAULT 0 CHECK (is_featured IN (0, 1)),
    is_active TINYINT NOT NULL DEFAULT 0 CHECK (is_active IN (0, 1)),
    category_id TINYINT UNSIGNED NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_products_slug (slug),
    FULLTEXT idx_products_title_description (title, description),
    INDEX idx_products_category_id (category_id),
    CONSTRAINT fk_products_category_id
        FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS properties (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title VARCHAR(45) NOT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_properties_title (title),
    INDEX idx_properties_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_property_values (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    value VARCHAR(45) NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,
    property_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_property (product_id, property_id),
    INDEX idx_product_property_product_id (product_id),
    INDEX idx_product_property_property_id (property_id),
    CONSTRAINT fk_product_property_product_id
        FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_product_property_property_id
        FOREIGN KEY (property_id) REFERENCES properties (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS variants (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    sku VARCHAR(64) NOT NULL,
    price DECIMAL(10,2) NOT NULL DEFAULT '0',
    compared_at_price DECIMAL(10,2) NOT NULL DEFAULT '0',
    cost_per_item DECIMAL(10,2) NOT NULL DEFAULT '0',
    quantity_on_stock INT UNSIGNED NOT NULL DEFAULT '0',
    product_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_variants_sku (sku),
    INDEX idx_variants_product_id (product_id),
    CONSTRAINT fk_variants_product_id
        FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS variant_images (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    url VARCHAR(128) NOT NULL,
    variant_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_variant_images_url (url),
    INDEX idx_variant_images_variant_id (variant_id),
    CONSTRAINT fk_variant_images_variant_id
        FOREIGN KEY (variant_id) REFERENCES variants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS attributes (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title VARCHAR(45) NOT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_attributes_title (title),
    INDEX idx_attributes_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS variant_attribute_values (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    value VARCHAR(45) NOT NULL,
    variant_id BIGINT UNSIGNED NOT NULL,
    attribute_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_variant_attribute (variant_id, attribute_id),
    INDEX idx_variant_attribute_variant_id (variant_id),
    INDEX idx_variant_attribute_attribute_id (attribute_id),
    CONSTRAINT fk_variant_attribute_variant_id
        FOREIGN KEY (variant_id) REFERENCES variants (id),
    CONSTRAINT fk_variant_attribute_attribute_id
        FOREIGN KEY (attribute_id) REFERENCES attributes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS movement_types (
    id TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title VARCHAR(45) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_movement_types_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS movements (
    id TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title VARCHAR(45) NOT NULL,
    movement_type_id TINYINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_movements_title (title),
    INDEX idx_movements_type_id (movement_type_id),
    CONSTRAINT fk_movements_type_id
        FOREIGN KEY (movement_type_id) REFERENCES movement_types (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory_movements (
    id BIGINT UNSIGNED NOT NULL,
    quantity INT NOT NULL DEFAULT '0',
    variant_id BIGINT UNSIGNED NOT NULL,
    movement_id TINYINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_inventory_movements_variant_id (variant_id),
    INDEX idx_inventory_movements_movement_id (movement_id),
    CONSTRAINT fk_inventory_movements_variant_id
        FOREIGN KEY (variant_id) REFERENCES variants (id),
    CONSTRAINT fk_inventory_movements_movement_id
        FOREIGN KEY (movement_id) REFERENCES movements (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS carriers (
    id TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(45) NOT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_carriers_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_number VARCHAR(36) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    tax_total DECIMAL(10,2) NOT NULL DEFAULT '0',
    shipping_total DECIMAL(10,2) NOT NULL DEFAULT '0',
    total DECIMAL(10,2) NOT NULL,
    notes TEXT,
    status VARCHAR(32) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED')),
    customer_id BIGINT UNSIGNED NOT NULL,
    shipping_address_id BIGINT UNSIGNED NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_order_number (order_number),
    INDEX idx_orders_customer_id (customer_id),
    INDEX idx_orders_shipping_address_id (shipping_address_id),
    CONSTRAINT fk_orders_customer_id FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_orders_shipping_address_id FOREIGN KEY (shipping_address_id) REFERENCES customer_addresses (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,
    variant_id BIGINT UNSIGNED NOT NULL,
    quantity INT UNSIGNED NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_order_items_order_id (order_id),
    INDEX idx_order_items_product_id (product_id),
    INDEX idx_order_items_variant_id (variant_id),
    CONSTRAINT fk_order_items_order_id FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_product_id FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_order_items_variant_id FOREIGN KEY (variant_id) REFERENCES variants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    notes TEXT,
    order_id BIGINT UNSIGNED NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_order_status_history_order_id (order_id),
    CONSTRAINT fk_order_status_history_order_id FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    amount DECIMAL(10,2) NOT NULL,
    reference VARCHAR(255),
    description TEXT,
    method VARCHAR(32) NOT NULL CHECK (method IN ('CASH_ON_DELIVERY', 'CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER')),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED')),
    order_id BIGINT UNSIGNED NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_payments_order_id (order_id),
    CONSTRAINT fk_payments_order_id FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS shipments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tracking_number VARCHAR(128),
    order_id BIGINT UNSIGNED NOT NULL,
    carrier_id TINYINT UNSIGNED NOT NULL,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_payments_order_id (order_id),
    INDEX idx_payments_carrier_id (carrier_id),
    CONSTRAINT fk_shipments_order_id FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_shipments_carrier_id FOREIGN KEY (carrier_id) REFERENCES carriers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS refunds (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    amount DECIMAL(10,2) NOT NULL,
    reason TEXT,
    order_id BIGINT UNSIGNED NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_refunds_order_id (order_id),
    CONSTRAINT fk_refunds_order_id FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS refund_items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    quantity INT UNSIGNED NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    refund_id BIGINT UNSIGNED NOT NULL,
    order_item_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_refund_items_refund_id (refund_id),
    INDEX idx_refund_items_order_item_id (order_item_id),
    CONSTRAINT fk_refund_items_refund_id FOREIGN KEY (refund_id) REFERENCES refunds (id),
    CONSTRAINT fk_refund_items_order_item_id FOREIGN KEY (order_item_id) REFERENCES order_items (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
