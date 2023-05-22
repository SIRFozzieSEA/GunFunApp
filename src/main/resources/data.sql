INSERT INTO USERS (USER_NAME, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, ACTIVE) VALUES (
	'steve', 'Steve', 'C', 'sirfozzie@github.com', 'password', 1);
	
INSERT INTO ROLES (ROLE_PK, ROLE) VALUES (1, 'ADMIN');

INSERT INTO VALID_CALIBERS (CALIBER, SHOOTS_CALIBER) VALUES 
	('12 Ga', '12 Ga'), 
	('22 LR', '22 LR'), 
	('223 Rem', '223 Rem'), 
	('357 Magnum', '357 Magnum|38 Special'), 
	('38 Special', '38 Special'), 
	('380 Auto', '380 Auto'), 
	('40 S&W', '40 S&W'), 
	('45 Auto', '45 Auto'), 
	('9mm', '9mm');

INSERT INTO trivia_question_templates_custom (question_type, question, question_responses, correct_response, image_location, nickname) 
	VALUES ('MULTIPLE_CHOICE', 'What was my first gun?', 'ALL_GUNNAMES', 'Eileen', 'REGISTRY', 'Eileen');
INSERT INTO trivia_question_templates_custom (question_type, question, question_responses, correct_response, image_location, nickname) 
	VALUES ('MULTIPLE_CHOICE', 'What caliber can also shoot a 38 special?', 'ALL_CALIBERS', '357 Magnum', '', '');
INSERT INTO trivia_question_templates_custom (question_type, question, question_responses, correct_response, image_location, nickname) 
	VALUES ('MULTIPLE_CHOICE', 'What gun brand does James Bond traditionally use?', 'ALL_MAKES', 'Walther', '', '');
INSERT INTO trivia_question_templates_custom (question_type, question, question_responses, correct_response, image_location, nickname) 
	VALUES ('MULTIPLE_CHOICE', 'What gun model does James Bond traditionally use?', 'PPK|PPP|PPS|PPQ', 'PPK', '', '');

INSERT INTO REGISTRY (NICKNAME, MAKE, MODEL, SERIAL, CALIBER, BARREL_LENGTH, FRAME_MATERIAL, PURCHASE_COST, PURCHASE_DATE, SIGHTED_DATE, 
	MARKET_COST, MARKET_COST_DATE, MARKET_URL, GUN_IS_DIRTY, LAST_CARRIED_DATE, LAST_CLEANED_DATE, LAST_FIRED_DATE, NOTES, GUN_TYPE) VALUES (
	'Teresa', 'Walther', 'PPK/S', 'SERIAL_01', '380 Auto', 3.0, 'Metal', '500.00', '2021-01-01', '2021-02-02', 600.00, 
	'2022-03-03', 'http://www.codef.com', false, '1970-01-01', '1970-01-01', '1970-01-01', '', 'Handgun');
INSERT INTO REGISTRY (NICKNAME, MAKE, MODEL, SERIAL, CALIBER, BARREL_LENGTH, FRAME_MATERIAL, PURCHASE_COST, PURCHASE_DATE, SIGHTED_DATE, 
	MARKET_COST, MARKET_COST_DATE, MARKET_URL, GUN_IS_DIRTY, LAST_CARRIED_DATE, LAST_CLEANED_DATE, LAST_FIRED_DATE, NOTES, GUN_TYPE) VALUES (
	'Alex', 'Beretta', 'APX', 'SERIAL_02', '9mm', 4.0, 'Composite', '600.00', '2000-01-01', '2000-02-02', 600.00, 
	'2001-03-03', 'http://www.codef.com', false, '1970-01-01', '1970-01-01', '1970-01-01', '', 'Handgun');
INSERT INTO REGISTRY (NICKNAME, MAKE, MODEL, SERIAL, CALIBER, BARREL_LENGTH, FRAME_MATERIAL, PURCHASE_COST, PURCHASE_DATE, SIGHTED_DATE, 
	MARKET_COST, MARKET_COST_DATE, MARKET_URL, GUN_IS_DIRTY, LAST_CARRIED_DATE, LAST_CLEANED_DATE, LAST_FIRED_DATE, NOTES, GUN_TYPE) VALUES (
	'Valerie', 'Heckler & Koch', 'VP9', 'SERIAL_03', '9mm', 4.0, 'Composite', '700.00', '2010-01-01', '2010-02-02', 800.00, 
	'2011-03-03', 'http://www.codef.com', false, '1970-01-01', '1970-01-01', '1970-01-01', '', 'Handgun');
INSERT INTO REGISTRY (NICKNAME, MAKE, MODEL, SERIAL, CALIBER, BARREL_LENGTH, FRAME_MATERIAL, PURCHASE_COST, PURCHASE_DATE, SIGHTED_DATE, 
	MARKET_COST, MARKET_COST_DATE, MARKET_URL, GUN_IS_DIRTY, LAST_CARRIED_DATE, LAST_CLEANED_DATE, LAST_FIRED_DATE, NOTES, GUN_TYPE) VALUES (
	'Harriet', 'Smith & Wesson', '686+', 'SERIAL_04', '357 Magnum', 4.0, 'Metal', '750.00', '2010-01-05', '2010-02-05', 850.00, 
	'2011-03-05', 'http://www.codef.com', false, '1970-01-01', '1970-01-01', '1970-01-01', '', 'Handgun');
	
INSERT INTO PREFERENCES (PREFERENCE_KEY, PREFERENCE_TYPE, PREFERENCE_VALUE) VALUES
	('SAMPLE_ASSETS_BUILT', 'Boolean', 'false'), 
	('MAX_LOG_DAYS_CARRY', 'Long', '-30'), 
	('MAX_LOG_DAYS_CLEANING', 'Long', '-60'), 
	('MAX_LOG_DAYS_SHOT', 'Long', '-30'), 
	('DISABLE_LOGINS', 'Boolean', 'true'), 
	('TAX_RATE', 'Double', '10.2'), 
	('DEFAULT_EDC', 'String', 'Teresa'), 
	('DELETE_MASTER_PASSWORD', 'String', 'boboshan69!'), 
	('MAX_LOG_DAYS_PURCHASED', 'Long', '-120');
	
/*	
INSERT INTO AMMUNITION (CALIBER, NO_OF_ROUNDS, PURCHASE_COST, PURCHASE_DATE) 
	VALUES ('9mm', '10', '10.00', '2022-07-01');
INSERT INTO AMMUNITION (CALIBER, NO_OF_ROUNDS, PURCHASE_COST, PURCHASE_DATE) 
	VALUES ('45', '20', '20.00', '2022-07-02');
INSERT INTO AMMUNITION (CALIBER, NO_OF_ROUNDS, PURCHASE_COST, PURCHASE_DATE) 
	VALUES ('12 gauge', '30', '30.00', '2022-07-03');
INSERT INTO AMMUNITION (CALIBER, NO_OF_ROUNDS, PURCHASE_COST, PURCHASE_DATE) 
	VALUES ('357', '40', '40.00', '2022-07-04');
*/