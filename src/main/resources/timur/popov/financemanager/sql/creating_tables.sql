CREATE TABLE IF NOT EXISTS User
(
    UserID           INTEGER PRIMARY KEY AUTOINCREMENT,
    Name             varchar(255) NOT NULL,
    Password         varchar(255) NOT NULL,
    RegistrationDate date         NOT NULL
);

CREATE TABLE IF NOT EXISTS Account
(
    AccountID      INTEGER PRIMARY KEY AUTOINCREMENT,
    Name           varchar(255) NOT NULL,
    Type           varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS UserAccount
(
    UserID    int,
    AccountID int,
    PRIMARY KEY (UserID, AccountID),
    FOREIGN KEY (UserID) REFERENCES User (UserID)
        ON DELETE CASCADE,
    FOREIGN KEY (AccountID) REFERENCES Account (AccountID)
        ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS Category
(
    CategoryID INTEGER PRIMARY KEY AUTOINCREMENT,
    Name       varchar(255) NOT NULL,
    Type       varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS Transactions
(
    TransactionID INTEGER PRIMARY KEY AUTOINCREMENT,
    Amount        decimal NOT NULL,
    CreationDate  date    NOT NULL,
    Description   varchar(255),
    AccountID     int,
    UserID        int,
    CategoryID    int,
    FOREIGN KEY (AccountID) REFERENCES Account (AccountID),
    FOREIGN KEY (UserID) REFERENCES User (UserID),
    FOREIGN KEY (CategoryID) REFERENCES Category (CategoryID)
);

