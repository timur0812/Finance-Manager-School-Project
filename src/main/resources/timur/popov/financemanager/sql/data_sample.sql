-- Reset existing data so this script can be re-run safely.
DELETE FROM Transactions;
DELETE FROM UserAccount;
DELETE FROM Account;
DELETE FROM Category;
DELETE FROM User;

-- Reset AUTOINCREMENT counters so IDs start from 1 again.
DELETE FROM sqlite_sequence WHERE name IN ('Transactions', 'UserAccount', 'Account', 'Category', 'User');

-- 1. Populate Users (5 users)
INSERT INTO User (Name, Password, RegistrationDate) VALUES
('Timur Popov', '4ada1e69', '2026-01-05'),
('Jane Doe', '4ada1e69', '2026-01-07'),
('Alex Morgan', '4ada1e69', '2026-01-10'),
('Maria Petrova', '4ada1e69', '2026-01-12'),
('John Smith', '4ada1e69', '2026-01-15');

-- 2. Populate Accounts (several accounts)
INSERT INTO Account (Name, Type) VALUES
('Main Checking', 'Debit'),
('Savings', 'Savings'),
('Credit Card', 'Credit'),
('Cash Wallet', 'Cash'),
('Salary Card', 'Debit'),
('Business Account', 'Debit'),
('Travel Card', 'Debit');

-- 3. Populate UserAccount (many-to-many mappings)
INSERT INTO UserAccount (UserID, AccountID) VALUES
(1, 1), (1, 2),
(2, 3), (2, 7),
(3, 5),
(4, 4),
(5, 6), (5, 1);

-- 4. Populate Categories
INSERT INTO Category (Name, Type) VALUES
('Groceries', 'Expense'),
('Salary', 'Income'),
('Rent', 'Expense'),
('Transport', 'Expense'),
('Utilities', 'Expense'),
('Freelance', 'Income'),
('Entertainment', 'Expense');

-- 5. Populate Transactions (20 rows, February-April 2026)
INSERT INTO Transactions (Amount, CreationDate, Description, AccountID, UserID, CategoryID) VALUES
(3200.00, '2026-02-01', 'February salary', 1, 1, 2),
(1200.00, '2026-02-03', 'Apartment rent', 1, 1, 3),
(145.80, '2026-02-05', 'Supermarket groceries', 1, 1, 1),
(89.40, '2026-02-07', 'Electricity bill', 3, 2, 5),
(46.50, '2026-02-09', 'Taxi rides', 7, 2, 4),
(4100.00, '2026-02-10', 'February salary', 5, 3, 2),
(62.30, '2026-02-12', 'Cinema and snacks', 4, 4, 7),
(2500.00, '2026-02-15', 'Client payment', 6, 5, 6),
(178.20, '2026-02-20', 'Family groceries', 1, 5, 1),
(3250.00, '2026-03-01', 'March salary', 1, 1, 2),
(1210.00, '2026-03-03', 'March rent', 1, 1, 3),
(52.00, '2026-03-06', 'Metro card top-up', 7, 2, 4),
(95.10, '2026-03-08', 'Utilities payment', 3, 2, 5),
(4150.00, '2026-03-10', 'March salary', 5, 3, 2),
(74.90, '2026-03-14', 'Groceries', 4, 4, 1),
(2700.00, '2026-03-18', 'Freelance project', 6, 5, 6),
(3300.00, '2026-04-01', 'April salary', 1, 1, 2),
(1220.00, '2026-04-04', 'April rent', 1, 1, 3),
(4300.00, '2026-04-10', 'April salary', 5, 3, 2),
(138.60, '2026-04-13', 'Groceries and household', 1, 5, 1);
