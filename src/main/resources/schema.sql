CREATE TABLE IF NOT EXISTS Customer (
  CustomerID     VARCHAR(10) PRIMARY KEY,
  Name           VARCHAR(50)  NOT NULL,
  Age            INT          NOT NULL,
  Address        VARCHAR(100),
  Contact_Number VARCHAR(15)  NOT NULL UNIQUE,
  Password       VARCHAR(100) NOT NULL,
  Role           VARCHAR(10)  NOT NULL DEFAULT 'CUSTOMER'
);

CREATE TABLE IF NOT EXISTS Vehicle (
  Numberplate VARCHAR(15) PRIMARY KEY,
  Type        VARCHAR(20) NOT NULL,
  Brand       VARCHAR(30) NOT NULL,
  BasePrice   DECIMAL(8,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS Reservation (
  ReservationID VARCHAR(10) PRIMARY KEY,
  ReservDate    DATE NOT NULL,
  ReservPeriod  INT  NOT NULL,
  Numberplate   VARCHAR(15) NOT NULL,
  CustomerID    VARCHAR(10) NOT NULL,
  FOREIGN KEY (Numberplate) REFERENCES Vehicle(Numberplate),
  FOREIGN KEY (CustomerID)  REFERENCES Customer(CustomerID)
);

CREATE TABLE IF NOT EXISTS Bill (
  BillID        VARCHAR(10) PRIMARY KEY,
  BillDate      DATE NOT NULL,
  Numberplate   VARCHAR(15) NOT NULL,
  CustomerID    VARCHAR(10) NOT NULL,
  ReservationID VARCHAR(10) NOT NULL UNIQUE,
  FOREIGN KEY (Numberplate)   REFERENCES Vehicle(Numberplate),
  FOREIGN KEY (CustomerID)    REFERENCES Customer(CustomerID),
  FOREIGN KEY (ReservationID) REFERENCES Reservation(ReservationID)
);

