CREATE TABLE `vip_records` (
	`id` INT unsigned NOT NULL AUTO_INCREMENT,
	`uuid` VARCHAR(36) NOT NULL,
	`type` VARCHAR(10) NOT NULL,
	`until` BIGINT unsigned NOT NULL,
	`created_by` VARCHAR(20) NOT NULL,
	`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`id`)
);