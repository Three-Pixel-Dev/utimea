-- =============================================
-- 0. SAFETY CLEANUP
-- =============================================
-- DELETE FROM subject WHERE subject_year IS NULL;

-- =============================================
-- 1. MASTER CODES
-- =============================================
INSERT INTO code (id, name, constant_value, created_at, updated_at) VALUES
                                                                        (1, 'Timetable Days', 'TIMETABLE_DAYS', NOW(), NOW()),
                                                                        (2, 'Timetable Periods', 'TIMETABLE_PERIODS', NOW(), NOW()),
                                                                        (3, 'Academic Year', 'ACADEMIC_YEAR', NOW(), NOW()),
                                                                        (4, 'Room Type', 'ROOM_TYPE', NOW(), NOW()),
                                                                        (5, 'Major Section Year', 'MAJOR_SECTION_YEAR', NOW(), NOW()),
                                                                        (6, 'Department', 'DEPARTMENT', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- =============================================
-- 2. CODE VALUES
-- =============================================
INSERT INTO code_value (id, code_id, name, system_defined, created_at, updated_at) VALUES
                                                                                       (1, 1, 'Monday', true, NOW(), NOW()),
                                                                                       (2, 1, 'Tuesday', true, NOW(), NOW()),
                                                                                       (3, 1, 'Wednesday', true, NOW(), NOW()),
                                                                                       (4, 1, 'Thursday', true, NOW(), NOW()),
                                                                                       (5, 1, 'Friday', true, NOW(), NOW()),
                                                                                       (6, 2, '8:30 - 9:30', true, NOW(), NOW()),
                                                                                       (7, 2, '9:40 - 10:40', true, NOW(), NOW()),
                                                                                       (8, 2, '10:50 - 11:50', true, NOW(), NOW()),
                                                                                       (9, 2, '11:50 - 12:40', true, NOW(), NOW()), -- Lunch
                                                                                       (10, 2, '12:40 - 1:40', true, NOW(), NOW()),
                                                                                       (11, 2, '1:50 - 2:50', true, NOW(), NOW()),
                                                                                       (98, 2, '3:00 - 4:00', true, NOW(), NOW()),
                                                                                       (12, 3, '2025-2026 Academic Year', true, NOW(), NOW()),
                                                                                       (13, 4, 'Lecture', true, NOW(), NOW()),
                                                                                       (14, 4, 'PC', true, NOW(), NOW()),
                                                                                       (51, 5, 'First Year', true, NOW(), NOW()),
                                                                                       (52, 5, 'Second Year', true, NOW(), NOW()),
                                                                                       (53, 5, 'Third Year', true, NOW(), NOW()),
                                                                                       (54, 5, 'Fourth Year', true, NOW(), NOW()),
                                                                                       (15, 6, 'Information Technology', true, NOW(), NOW()),
                                                                                       (99, 5, 'GENERIC_YEAR_VAL', true, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- =============================================
-- 2.5 ROLES & USERS (For Teachers)
-- =============================================
-- Ensure Roles Exist (Assuming IDs: 1=Admin, 2=Teacher, 3=Student)
INSERT INTO role (id, name, created_at, updated_at) VALUES
                                                        (1, 'Admin', NOW(), NOW()),
                                                        (2, 'Teacher', NOW(), NOW()),
                                                        (3, 'Student', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Insert Users for Teachers (IDs 101-161 mapped to Profile IDs 1-61)
-- Default Password for all: "password" ($2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
-- NOTE: Table name updated to 'users' based on user confirmation
INSERT INTO users (id, email, password, role_id, created_at, updated_at) VALUES
                                                                             (101, 'teacher1@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (102, 'teacher2@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (103, 'teacher3@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (104, 'teacher4@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (105, 'teacher5@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (106, 'teacher6@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (107, 'teacher7@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (108, 'teacher8@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (109, 'teacher9@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (110, 'teacher10@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (111, 'teacher11@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (112, 'teacher12@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (113, 'teacher13@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (114, 'teacher14@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (115, 'teacher15@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (116, 'teacher16@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (117, 'teacher17@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (118, 'teacher18@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (119, 'teacher19@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (120, 'teacher20@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (121, 'teacher21@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (122, 'teacher22@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (123, 'teacher23@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (124, 'teacher24@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (125, 'teacher25@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (126, 'teacher26@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (127, 'teacher27@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (128, 'teacher28@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (129, 'teacher29@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (130, 'teacher30@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (131, 'teacher31@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (132, 'teacher32@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (133, 'teacher33@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (134, 'teacher34@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (135, 'teacher35@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (136, 'teacher36@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (137, 'teacher37@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (138, 'teacher38@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (139, 'teacher39@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (140, 'teacher40@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (141, 'teacher41@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (142, 'teacher42@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (143, 'teacher43@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (144, 'teacher44@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (145, 'teacher45@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (146, 'teacher46@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (147, 'teacher47@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (148, 'teacher48@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (149, 'teacher49@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (150, 'teacher50@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (151, 'teacher51@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (152, 'teacher52@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (153, 'teacher53@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (154, 'teacher54@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (155, 'teacher55@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (156, 'teacher56@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (157, 'teacher57@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (158, 'teacher58@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (159, 'teacher59@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (160, 'teacher60@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW()),
                                                                             (161, 'teacher61@utimea.com', '$2a$10$1pMGNyykTX7WYOTfJTe...HK6XwpskaRuMlGnQuP.Ay.GwZ5Wwwse', 2, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- =============================================
-- 3. TEACHERS (Pool of 61) - Updated with User IDs
-- =============================================
INSERT INTO profile (id, name, degree, department_id, user_id, created_at, updated_at) VALUES
-- Year 1 Lecturers (1-14)
(1, 'Daw Me Me Ko', 'M.C.Sc', 15, 101, NOW(), NOW()),
(2, 'Dr. Khin Hnin Hnin Thein', 'Ph.D(IT)', 15, 102, NOW(), NOW()),
(3, 'Daw Lay Myat Myat Thein', 'M.C.Sc', 15, 103, NOW(), NOW()),
(4, 'Dr. Zin Mar Kyu', 'Ph.D(IT)', 15, 104, NOW(), NOW()),
(5, 'Daw Akari Myint Soe', 'M.C.Sc', 15, 105, NOW(), NOW()),
(6, 'Daw Aye Thant Thant Moe', 'M.C.Sc', 15, 106, NOW(), NOW()),
(7, 'Daw Su Su Naing', 'M.C.Sc', 15, 107, NOW(), NOW()),
(8, 'Daw Khin Cho Latt', 'M.A(Eng)', 15, 108, NOW(), NOW()),
(9, 'Daw Me Me Swe Win', 'M.A(Eng)', 15, 109, NOW(), NOW()),
(10, 'Dr. Tin Oo', 'Ph.D(Myan)', 15, 110, NOW(), NOW()),
(11, 'Daw Hnin Thidar', 'M.A(Myan)', 15, 111, NOW(), NOW()),
(12, 'Dr. Kyi May San', 'Ph.D(Phys)', 15, 112, NOW(), NOW()),
(13, 'Daw Nan Thazin', 'M.Sc(Phys)', 15, 113, NOW(), NOW()),
(14, 'Dr. Khin Lei Lei Kyaw', 'Ph.D(Phys)', 15, 114, NOW(), NOW()),
-- Year 2 Lecturers (15-29)
(15, 'Daw Laet Laet Lin', 'M.C.Sc', 15, 115, NOW(), NOW()),
(16, 'Daw Thae Nu Aye', 'M.C.Sc', 15, 116, NOW(), NOW()),
(17, 'Dr. May Thet Htun', 'Ph.D(IT)', 15, 117, NOW(), NOW()),
(18, 'Dr. Ei Moh Moh Aung', 'Ph.D(IT)', 15, 118, NOW(), NOW()),
(19, 'Daw Ni Win Bo', 'M.C.Sc', 15, 119, NOW(), NOW()),
(20, 'Daw Aye Aye Aung', 'M.C.Sc', 15, 120, NOW(), NOW()),
(21, 'Dr. Khin Myo Myo Min', 'Ph.D(IT)', 15, 121, NOW(), NOW()),
(22, 'Daw Aye Nyein San', 'M.C.Sc', 15, 122, NOW(), NOW()),
(23, 'Daw Kay Zin Htun', 'M.C.Sc', 15, 123, NOW(), NOW()),
(24, 'Daw Ciin Zam Man', 'M.C.Sc', 15, 124, NOW(), NOW()),
(25, 'Daw San San Nwe', 'M.C.Sc', 15, 125, NOW(), NOW()),
(26, 'Daw Htay Htay Win', 'M.C.Sc', 15, 126, NOW(), NOW()),
(27, 'Daw Win PaPa May Phyoe Aung', 'M.C.Sc', 15, 127, NOW(), NOW()),
(28, 'Daw Mon Zar Kyaw', 'M.A(Eng)', 15, 128, NOW(), NOW()),
(29, 'Daw Mi Khin Thi Htun', 'M.A(Eng)', 15, 129, NOW(), NOW()),
-- Year 3 Lecturers (30-42)
(30, 'Daw May Thet Swe', 'M.C.Sc', 15, 130, NOW(), NOW()),
(31, 'Daw Khin Mar Wai', 'M.C.Sc', 15, 131, NOW(), NOW()),
(32, 'Dr. Thinn Thinn Wai', 'Ph.D(IT)', 15, 132, NOW(), NOW()),
(33, 'Dr. Thet Thet Zin', 'Ph.D(IT)', 15, 133, NOW(), NOW()),
(34, 'Dr. May Thu Myint', 'Ph.D(IT)', 15, 134, NOW(), NOW()),
(35, 'Dr. Win Win Myo', 'Ph.D(IT)', 15, 135, NOW(), NOW()),
(36, 'Daw Phyu Phyu Aung', 'M.C.Sc', 15, 136, NOW(), NOW()),
(37, 'Daw Lei Yi Win Lwin', 'M.C.Sc', 15, 137, NOW(), NOW()),
(38, 'Dr. Ohnmar Nhway', 'Ph.D(IT)', 15, 138, NOW(), NOW()),
(39, 'Dr. Hnin Thiri Zaw', 'Ph.D(IT)', 15, 139, NOW(), NOW()),
(40, 'Dr. Tha Pyay Win', 'Ph.D(IT)', 15, 140, NOW(), NOW()),
(41, 'Daw Shwe Sin Myat Than', 'M.C.Sc', 15, 141, NOW(), NOW()),
(42, 'Daw Ohnmar Myint', 'M.C.Sc', 15, 142, NOW(), NOW()),
-- Year 4 Lecturers (43-59)
(43, 'Dr. Win Win Thant', 'Ph.D(IT)', 15, 143, NOW(), NOW()),
(44, 'Daw Khin Sandi Bo', 'M.C.Sc', 15, 144, NOW(), NOW()),
(45, 'Dr. Kyawe Kyawe San', 'Ph.D(IT)', 15, 145, NOW(), NOW()),
(46, 'Daw Lei Lei Lynn', 'M.C.Sc', 15, 146, NOW(), NOW()),
(47, 'Dr. Nwe Nwe Myint Thein', 'Ph.D(IT)', 15, 147, NOW(), NOW()),
(48, 'Dr. Thiri Thitsar Khaing', 'Ph.D(IT)', 15, 148, NOW(), NOW()),
(49, 'Dr. Myat Pwint Phyu', 'Ph.D(IT)', 15, 149, NOW(), NOW()),
(50, 'Dr. Hlaing Htake Khaung Tin', 'Ph.D(IT)', 15, 150, NOW(), NOW()),
(51, 'Dr. Aung Nway Oo', 'Ph.D(IT)', 15, 151, NOW(), NOW()),
(52, 'Dr. Aye Chan Mon', 'Ph.D(IT)', 15, 152, NOW(), NOW()),
(53, 'Dr. Mhway Mhway Tar', 'Ph.D(IT)', 15, 153, NOW(), NOW()),
(54, 'Dr. Aye Myat Myat Paing', 'Ph.D(IT)', 15, 154, NOW(), NOW()),
(55, 'Dr. Than Than Nwe', 'Ph.D(IT)', 15, 155, NOW(), NOW()),
(56, 'Daw Khin Ei Ei Chaw', 'M.C.Sc', 15, 156, NOW(), NOW()),
(57, 'Dr. Myat Thida Mon', 'Ph.D(IT)', 15, 157, NOW(), NOW()),
(58, 'Dr. Aung Htein Maw', 'Ph.D(IT)', 15, 158, NOW(), NOW()),
(59, 'Dr. Hay Mar Moh Moh Lwin', 'Ph.D(IT)', 15, 159, NOW(), NOW()),
-- New CSec Lecturers (60-61)
(60, 'Dr. Sandar Win', 'Ph.D(IT)', 15, 160, NOW(), NOW()),
(61, 'Dr. Myint Myint Lwin', 'Ph.D(IT)', 15, 161, NOW(), NOW())
    ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
                            degree = EXCLUDED.degree,
                            department_id = EXCLUDED.department_id,
                            user_id = EXCLUDED.user_id,
                            updated_at = NOW();

-- =============================================
-- 4. ROOMS
-- =============================================
INSERT INTO room (id, name, capacity, room_type_id, is_special_room, created_at, updated_at) VALUES
-- Specific CSec Rooms
(100, 'Room 426', 50, 13, false, NOW(), NOW()), (101, 'Room 433', 50, 13, false, NOW(), NOW()),
(102, 'Room 432', 50, 13, false, NOW(), NOW()), (103, 'Room 431', 50, 13, false, NOW(), NOW()),
(104, 'Room 345', 50, 13, false, NOW(), NOW()),
-- Normal Classrooms
(1, 'Room 245', 50, 13, false, NOW(), NOW()), (2, 'Room 321', 50, 13, false, NOW(), NOW()),
(3, 'Room 322', 50, 13, false, NOW(), NOW()), (4, 'Room 231', 50, 13, false, NOW(), NOW()),
(5, 'Room 323', 50, 13, false, NOW(), NOW()), (6, 'Room 334', 50, 13, false, NOW(), NOW()),
(7, 'Room 324', 50, 13, false, NOW(), NOW()), (8, 'Room 234', 50, 13, false, NOW(), NOW()),
(9, 'Room 325', 50, 13, false, NOW(), NOW()), (10, 'Room 232', 50, 13, false, NOW(), NOW()),
(11, 'Room 326', 50, 13, false, NOW(), NOW()), (12, 'Room 331', 50, 13, false, NOW(), NOW()),
(13, 'Room 332', 50, 13, false, NOW(), NOW()), (14, 'Room 335', 50, 13, false, NOW(), NOW()),
(15, 'Room 333', 50, 13, false, NOW(), NOW()), (16, 'Room 336', 50, 13, false, NOW(), NOW()),
(17, 'Room 214', 50, 13, false, NOW(), NOW()), (18, 'Room 215', 50, 13, false, NOW(), NOW()),
(19, 'Room 346', 50, 13, false, NOW(), NOW()), (20, 'Room 342', 50, 13, false, NOW(), NOW()),
(21, 'Room 352', 50, 13, false, NOW(), NOW()), (22, 'Room 353', 50, 13, false, NOW(), NOW()),
(23, 'Room 233', 50, 13, false, NOW(), NOW()), (24, 'Room 235', 50, 13, false, NOW(), NOW()),
-- Computer Labs
(25, 'Room 244', 40, 14, true, NOW(), NOW()), (26, 'Room 236', 40, 14, true, NOW(), NOW()),
(27, 'Room 351', 40, 14, true, NOW(), NOW()), (28, 'Lab-D', 40, 14, true, NOW(), NOW()),
(29, 'Lab-E', 40, 14, true, NOW(), NOW()), (30, 'Lab-F', 40, 14, true, NOW(), NOW()),
-- Physics Labs
(220, 'Physics Lab 1', 40, 13, true, NOW(), NOW()), (230, 'Physics Lab 2', 40, 13, true, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- =============================================
-- 5. SUBJECTS (With Self-Healing UPDATE)
-- =============================================
INSERT INTO subject (id, code, description, room_type_id, special_room_count, is_first_sem, subject_year, created_at, updated_at) VALUES
-- Sem 1
(1, 'CST-1201', 'Calculus', 13, 0, true, 'FIRST_YEAR', NOW(), NOW()),
(2, 'CST-1301', 'Intro to Business & IS', 13, 0, true, 'FIRST_YEAR', NOW(), NOW()),
(3, 'CST-1401', 'Digital Fundamentals', 13, 0, true, 'FIRST_YEAR', NOW(), NOW()),
(4, 'CST-1501', 'Critical Thought', 13, 1, true, 'FIRST_YEAR', NOW(), NOW()),
(5, 'CST-1601', 'Myanmar Literature', 13, 0, true, 'FIRST_YEAR', NOW(), NOW()),
(6, 'CST-1701', 'Physics', 13, 2, true, 'FIRST_YEAR', NOW(), NOW()),
(7, 'CST-3102', 'Data Structures', 13, 0, true, 'SECOND_YEAR', NOW(), NOW()),
(8, 'CST-3103', 'OS Fundamentals', 13, 0, true, 'SECOND_YEAR', NOW(), NOW()),
(9, 'CST-3202', 'Discrete Structure I', 13, 0, true, 'SECOND_YEAR', NOW(), NOW()),
(10, 'CST-3302', 'Software Modeling', 13, 0, true, 'SECOND_YEAR', NOW(), NOW()),
(11, 'CST-3303', 'Intro to Business IS', 13, 0, true, 'SECOND_YEAR', NOW(), NOW()),
(12, 'CST-3503', 'English III', 13, 1, true, 'SECOND_YEAR', NOW(), NOW()),
-- Sem 2 (Year 1)
(201, 'CST-2101', 'Programming in C++ (Cornerstone)', 14, 4, false, 'FIRST_YEAR', NOW(), NOW()),
(202, 'CST-2202', 'Discrete Mathematics', 13, 0, false, 'FIRST_YEAR', NOW(), NOW()),
(203, 'CST-2302', 'Web Technology (Cornerstone)', 14, 4, false, 'FIRST_YEAR', NOW(), NOW()),
(204, 'CST-2303', 'HCI and Info Security', 13, 0, false, 'FIRST_YEAR', NOW(), NOW()),
(205, 'CST-2402', 'Networking Fundamentals', 13, 0, false, 'FIRST_YEAR', NOW(), NOW()),
(206, 'CST-2502', 'Professional Communications', 13, 0, false, 'FIRST_YEAR', NOW(), NOW()),
-- Sem 2 (Year 2)
(211, 'CST-4104', 'Artificial Intelligence', 13, 0, false, 'SECOND_YEAR', NOW(), NOW()),
(212, 'CST-4204', 'Linear Algebra', 13, 0, false, 'SECOND_YEAR', NOW(), NOW()),
(213, 'CST-4306', 'Mgmt Principles & Econ', 13, 0, false, 'SECOND_YEAR', NOW(), NOW()),
(214, 'CST-4404', 'Net Design & Eng', 13, 0, false, 'SECOND_YEAR', NOW(), NOW()),
(215, 'CST-4405', 'Computer Arch & Org', 13, 0, false, 'SECOND_YEAR', NOW(), NOW()),
(216, 'CST-4307', 'Adv Web Tech (PHP)', 14, 4, false, 'SECOND_YEAR', NOW(), NOW()),
-- Sem 2 (Year 3)
(301, 'CST-6114', 'Distributed Programming', 14, 4, false, 'THIRD_YEAR', NOW(), NOW()),
(302, 'CST-6316', 'Data Analysis and Mgmt', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(303, 'CST-6506', 'Professional Communications', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(304, 'CST-6210', 'Performance and Reliability', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(311, 'CS-6117', 'Computer Vision', 14, 4, false, 'THIRD_YEAR', NOW(), NOW()),
(312, 'CS-6211', 'Mathematical Theory of Games', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(313, 'CS-6317', 'Software Construction & Evo', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(320, 'KE-6118', 'Robotic Systems', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(321, 'HPC-6115', 'Mobile & Ubiquitous Computing', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(322, 'HPC-6116', 'HPC Technology', 14, 4, false, 'THIRD_YEAR', NOW(), NOW()),
(323, 'BIS-6209', 'Modeling and Decision Analysis', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(324, 'BIS-6318', 'Marketing Principles', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(325, 'BIS-6319', 'Business IS Analysis', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(326, 'CN-6414', 'Multimedia Communications', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(327, 'CT-6415', 'Network and Internet Security', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(328, 'ES-6420', 'VHDL Design', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(329, 'ES-6421', 'Applied Control Theory', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(330, 'ES-6422', 'Microcontroller Programming', 14, 4, false, 'THIRD_YEAR', NOW(), NOW()),
(331, 'ES-6423', 'Digital Signal Processing', 13, 0, false, 'THIRD_YEAR', NOW(), NOW()),
(332, 'CSec-6424', 'Cybersecurity Operations', 14, 4, false, 'THIRD_YEAR', NOW(), NOW())
    ON CONFLICT (id) DO UPDATE SET
    subject_year = EXCLUDED.subject_year,
                            code = EXCLUDED.code,
                            description = EXCLUDED.description,
                            room_type_id = EXCLUDED.room_type_id,
                            special_room_count = EXCLUDED.special_room_count,
                            is_first_sem = EXCLUDED.is_first_sem,
                            updated_at = NOW();

-- =============================================
-- 6. SUBJECT MAPPINGS
-- =============================================
INSERT INTO subject_teacher_mapping (subject_id, teacher_id) VALUES
                                                                 (1,1),(1,2),(2,3),(2,4),(3,5),(3,6),(3,7),(4,8),(4,9),(5,10),(5,11),(6,12),(6,13),(6,14),
                                                                 (7,15),(7,16),(7,17),(8,18),(8,19),(8,20),(9,21),(9,22),(10,23),(10,24),(10,25),(11,26),(11,27),(12,28),(12,29),
                                                                 (201, 1), (201, 2), (202, 3), (202, 4), (203, 5), (203, 6), (204, 7), (204, 8), (205, 9), (205, 10), (206, 11), (206, 12),
                                                                 (211, 15), (211, 16), (212, 17), (212, 18), (213, 19), (213, 20), (214, 21), (214, 22), (215, 23), (215, 24), (216, 25), (216, 26),
                                                                 (301, 30), (301, 31), (302, 32), (302, 33), (303, 28), (303, 29), (304, 34), (304, 35), (311, 36), (311, 37), (312, 38), (312, 39),
                                                                 (313, 40), (313, 41), (320, 18), (321, 42), (322, 43), (323, 44), (324, 45), (325, 46), (326, 47), (327, 48), (328, 49),
                                                                 (329, 50), (330, 51), (331, 52), (332, 60)
    ON CONFLICT DO NOTHING;

-- =============================================
-- 7. MAJOR SECTIONS
-- =============================================
INSERT INTO major_section (name, major_section_year_id, created_at, updated_at) VALUES
                                                                                    ('Year 1 - Section A', 99, NOW(), NOW()), ('Year 1 - Section B', 99, NOW(), NOW()),
                                                                                    ('Year 1 - Section C', 99, NOW(), NOW()), ('Year 1 - Section D', 99, NOW(), NOW()),
                                                                                    ('Year 1 - Section E', 99, NOW(), NOW()),
                                                                                    ('Year 2 - Section A', 52, NOW(), NOW()), ('Year 2 - Section B', 52, NOW(), NOW()),
                                                                                    ('Year 2 - Section C', 52, NOW(), NOW()), ('Year 2 - Section D', 52, NOW(), NOW()),
                                                                                    ('Year 2 - Section E', 52, NOW(), NOW()),
                                                                                    ('Year 3 (SE) - Section A', 53, NOW(), NOW()), ('Year 3 (KE) - Section A', 53, NOW(), NOW()),
                                                                                    ('Year 3 (HPC) - Section A', 53, NOW(), NOW()), ('Year 3 (BIS) - Section A', 53, NOW(), NOW()),
                                                                                    ('Year 3 (CN) - Section A', 53, NOW(), NOW()), ('Year 3 (ES) - Section A', 53, NOW(), NOW()),
                                                                                    ('Year 3 (CSec) - Section A', 53, NOW(), NOW()),
                                                                                    ('Year 4 (SE) - Section A', 54, NOW(), NOW()), ('Year 4 (KE) - Section A', 54, NOW(), NOW()),
                                                                                    ('Year 4 (HPC) - Section A', 54, NOW(), NOW()), ('Year 4 (BIS) - Section A', 54, NOW(), NOW()),
                                                                                    ('Year 4 (CN) - Section A', 54, NOW(), NOW()), ('Year 4 (ES) - Section A', 54, NOW(), NOW()),
                                                                                    ('Year 4 (CSec) - Section A', 54, NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- =============================================
-- 8. SEQUENCE RESET
-- =============================================
SELECT setval('code_id_seq', (SELECT MAX(id) FROM code));
SELECT setval('code_value_id_seq', (SELECT MAX(id) FROM code_value));
SELECT setval('room_id_seq', (SELECT MAX(id) FROM room));
SELECT setval('profile_id_seq', (SELECT MAX(id) FROM profile));
SELECT setval('subject_id_seq', (SELECT MAX(id) FROM subject));
SELECT setval('major_section_id_seq', (SELECT MAX(id) FROM major_section));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));