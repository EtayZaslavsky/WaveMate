CREATE DATABASE good_vibes
	COLLATE = 'utf8_general_ci'
	CHARACTER SET = 'utf8'
	DEFAULT CHARACTER SET 'utf8'
	COLLATE 'utf8_general_ci'
	DEFAULT COLLATE 'utf8_general_ci';
 
USE good_vibes;
 
CREATE TABLE IF NOT EXISTS `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(250) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `password_hash` text NOT NULL,
  `api_key` varchar(32) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
);
 
CREATE TABLE IF NOT EXISTS `goovies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playable` tinytext NOT NULL,
  `lat` tinytext NOT NULL,
  `lon` tinytext NOT NULL,
  `description` tinytext NOT NULL,
  `height` tinytext NOT NULL,
  `crowed` tinytext NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);
 
CREATE TABLE IF NOT EXISTS `user_goovies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `goovy_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `goovy_id` (`goovy_id`)
);
 
ALTER TABLE `user_goovies` ADD FOREIGN KEY ( `user_id` ) REFERENCES `good_vibes`.`users` (
`id`
) ON DELETE CASCADE ON UPDATE CASCADE ;
 
ALTER TABLE  `user_goovies` ADD FOREIGN KEY (  `goovy_id` ) REFERENCES  `good_vibes`.`goovies` (
`id`
) ON DELETE CASCADE ON UPDATE CASCADE ;