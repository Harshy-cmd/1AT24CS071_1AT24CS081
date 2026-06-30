# 📋 Complaint Management System (CMS)

> A production-quality desktop application for managing complaints in an organisation.  
> Built with **Java 17 · Swing · JDBC · MySQL 8** following strict **MVC architecture** and **SOLID principles**.

---

## 🖥️ Features

| Feature | Details |
|---|---|
| **Splash Screen** | Animated progress-bar splash with gradient background |
| **Login** | SHA-256 hashed passwords, show/hide toggle, background auth thread |
| **Dashboard** | 4 stat cards, Java2D bar chart, recent complaints table, activity feed |
| **Register Complaint** | Validated form — title, description, category, priority, location, department |
| **View Complaints** | Filterable/searchable table; double-click to open; context menu |
| **Search** | Keyword search across all fields or a specific field |
| **Complaint Detail** | Full metadata, status update, employee assignment, history timeline |
| **Reports** | Filter + preview table, CSV export (UTF-8 BOM), print via system dialog |
| **Profile** | Edit profile, change password (verifies current password) |
| **Settings** | Dark / Light theme toggle |
| **About** | App info, tech stack badges |

---

## 🏗️ Architecture

```
src/
├── Main.java                        # Entry point — L&F, DB check, Splash → Login
├── model/                           # Entities, enums, interfaces
│   ├── Person.java  (abstract)
│   ├── User.java    (extends Person)
│   ├── Admin.java   (extends User)
│   ├── Employee.java(extends User)
│   ├── Complaint.java
│   ├── ComplaintHistory.java
│   ├── ActivityLog.java
│   ├── IComplaint.java
│   ├── Status.java
│   ├── Priority.java
│   └── ComplaintCategory.java
├── database/
│   └── DatabaseConnection.java      # Singleton JDBC connection manager
├── dao/
│   ├── IComplaintDAO.java
│   ├── IUserDAO.java
│   ├── IReportDAO.java
│   └── implementation/
│       ├── ComplaintDAOImpl.java
│       ├── UserDAOImpl.java
│       └── ReportDAOImpl.java
├── service/
│   ├── ComplaintService.java
│   ├── UserService.java
│   └── ReportService.java
├── controller/
│   ├── ComplaintController.java
│   ├── UserController.java
│   └── ReportController.java
├── reports/
│   ├── IReport.java                 # Strategy interface
│   ├── CSVExporter.java
│   └── PrintManager.java
├── components/                      # Reusable Swing components
│   ├── ThemeManager.java            # Full design system
│   ├── RoundedButton.java
│   ├── RoundedPanel.java
│   ├── ShadowPanel.java
│   ├── Sidebar.java
│   ├── HeaderPanel.java
│   ├── DashboardCard.java
│   ├── SearchBar.java
│   ├── StatusBadge.java
│   └── NotificationPanel.java
├── ui/                              # Application screens
│   ├── SplashScreen.java
│   ├── LoginPanel.java
│   ├── ForgotPasswordPanel.java
│   ├── MainFrame.java
│   ├── DashboardPanel.java
│   ├── RegisterComplaintPanel.java
│   ├── ViewComplaintsPanel.java
│   ├── SearchComplaintsPanel.java
│   ├── ComplaintDetailPanel.java
│   ├── ReportsPanel.java
│   ├── ProfilePanel.java
│   ├── SettingsPanel.java
│   └── AboutPanel.java
├── exceptions/                      # Custom checked exceptions
│   ├── DatabaseException.java
│   ├── ValidationException.java
│   ├── AuthenticationException.java
│   └── ReportException.java
└── util/
    ├── Constants.java
    ├── DateUtil.java
    ├── SessionManager.java
    └── Validator.java
sql/
└── complaint_management.sql         # Full schema, seed data, views, indexes
```

---

## ⚙️ OOP Concepts Demonstrated

| Concept | Where Used |
|---|---|
| **Abstraction** | `Person` (abstract), `IComplaint`, `IComplaintDAO`, `IUserDAO`, `IReport` |
| **Encapsulation** | All fields are `private` with getters/setters in every model |
| **Inheritance** | `Person → User → Admin / Employee` |
| **Polymorphism** | `UserDAOImpl.mapRow()` returns `Admin` or `Employee` as `User`; `IReport` → `CSVExporter` or `PrintManager` |
| **Constructor Overloading** | Every model, DAO, service, component and UI class |
| **Method Overloading** | `Validator.validateField()`, `ComplaintService.search()`, `updateStatus()` |
| **Method Overriding** | `Admin.getRole()`, `Employee.isAdmin()`, `PrintManager.print()` |
| **Interface** | `IComplaint`, `IComplaintDAO`, `IUserDAO`, `IReportDAO`, `IReport`, `Sidebar.NavigationListener` |

---

## 🛠️ Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 17+ |
| MySQL | 8.0+ |
| MySQL Connector/J | 8.x (JAR on classpath) |

---

## 🚀 Setup & Run

### 1 — Database Setup

```sql
-- In MySQL Workbench or mysql CLI:
SOURCE sql/complaint_management.sql;
```

### 2 — Default Credentials (seeded)

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin@123` | Administrator |
| `emp1`  | `Emp@12345` | Employee |
| `emp2`  | `Emp@12345` | Employee |

### 3 — Configure DB connection (if needed)

Edit `src/util/Constants.java` → `Constants.DB`:

```java
public static final String USERNAME = "root";
public static final String PASSWORD = "root";
```

### 4 — Compile (from `src/` directory)

```bash
javac -cp .;path/to/mysql-connector-j-8.x.jar -d out $(find . -name "*.java")
```

### 5 — Run

```bash
java -cp out;path/to/mysql-connector-j-8.x.jar Main
```

### Using an IDE (recommended)

1. Open the `CMS` folder as a project in IntelliJ IDEA / Eclipse / NetBeans.
2. Mark `src/` as the **Sources Root**.
3. Add `mysql-connector-j-8.x.jar` to the project's **classpath / module dependencies**.
4. Set `Main` as the **Run Configuration** main class.
5. Run.

---

## 📊 Database Schema Overview

| Table | Purpose |
|---|---|
| `users` | Admin and employee accounts |
| `complaints` | All complaint records |
| `complaint_history` | Status-change audit trail per complaint |
| `activity_log` | System-wide event log (login, create, assign…) |

---

## 📄 License

MIT License — © 2024 CMS Development Team
