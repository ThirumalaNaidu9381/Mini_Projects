DROP TABLE IF EXISTS SalesTransactions;
DROP TABLE IF EXISTS Products;
DROP TABLE IF EXISTS Categories;

CREATE TABLE Categories (
    CategoryID INT PRIMARY KEY,
    CategoryName VARCHAR(100) NOT NULL
);

CREATE TABLE Products (
    ProductID INT PRIMARY KEY,
    CategoryID INT,
    ProductName VARCHAR(100) NOT NULL,
    StockCount INT,
    ExpiryDate DATE,
    FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID)
);

CREATE TABLE SalesTransactions (
    TransactionID INT PRIMARY KEY,
    ProductID INT,
    SaleDate DATE,
    QuantitySold INT,
    SalePrice DECIMAL(10, 2),
    FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
);

-- populate test data
INSERT INTO Categories (CategoryID, CategoryName) VALUES
(1, 'Dairy'),
(2, 'Produce'),
(3, 'Canned Goods'),
(4, 'Bakery');

INSERT INTO Products (ProductID, CategoryID, ProductName, StockCount, ExpiryDate) VALUES
(101, 1, 'Whole Milk', 75, DATE_ADD(CURDATE(), INTERVAL 4 DAY)),
(102, 1, 'Aged Cheddar', 30, DATE_ADD(CURDATE(), INTERVAL 90 DAY)),
(103, 2, 'Organic Bananas', 120, DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
(104, 3, 'Tomato Soup', 200, DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
(105, 4, 'Sourdough Loaf', 10, DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
(106, 2, 'Avocados', 80, DATE_ADD(CURDATE(), INTERVAL 14 DAY));

INSERT INTO SalesTransactions (TransactionID, ProductID, SaleDate, QuantitySold, SalePrice) VALUES
(1001, 101, CURDATE(), 5, 4.50),
(1002, 102, DATE_SUB(CURDATE(), INTERVAL 1 MONTH), 10, 3.00),
(1003, 103, CURDATE(), 15, 1.50),
(1004, 105, DATE_SUB(CURDATE(), INTERVAL 1 MONTH), 5, 5.00),
(1005, 106, DATE_SUB(CURDATE(), INTERVAL 70 DAY), 20, 2.00);


-- 1. "Expiring Soon" Query: expires in next 7 days & stock > 50
SELECT ProductName, StockCount, ExpiryDate
FROM Products
WHERE ExpiryDate BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
  AND StockCount > 50;

-- 2. "Dead Stock" Analysis: zero sales in the last 60 days
SELECT p.ProductID, p.ProductName
FROM Products p
WHERE p.ProductID NOT IN (
    SELECT ProductID 
    FROM SalesTransactions 
    WHERE SaleDate >= DATE_SUB(CURDATE(), INTERVAL 60 DAY)
);

-- 3. Revenue Contribution: highest revenue category last month
SELECT c.CategoryName, SUM(s.QuantitySold * s.SalePrice) AS TotalRevenue
FROM Categories c
JOIN Products p ON c.CategoryID = p.CategoryID
JOIN SalesTransactions s ON p.ProductID = s.ProductID
WHERE YEAR(s.SaleDate) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))
  AND MONTH(s.SaleDate) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))
GROUP BY c.CategoryName
ORDER BY TotalRevenue DESC
LIMIT 1;
