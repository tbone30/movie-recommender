# SQLite Setup Guide

This file contains the exact code changes needed to configure the Movie Recommender System to use SQLite instead of PostgreSQL.

## Why SQLite?

- ✅ **Zero database server setup** - File-based database
- ✅ **Automatic database creation** - No manual database creation needed
- ✅ **Perfect for development** - Simpler to work with
- ✅ **Production ready** - Handles moderate loads efficiently
- ✅ **Portable** - Database file travels with your application

## Required Code Changes

### 1. Backend Changes (Spring Boot)

#### Update `backend/pom.xml`
Replace the PostgreSQL dependency with SQLite:

```xml
<!-- REPLACE this PostgreSQL dependency -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- WITH this SQLite dependency -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.44.1.0</version>
</dependency>
```

#### Update `backend/src/main/resources/application.properties`
Replace PostgreSQL configuration with SQLite:

```properties
# REPLACE PostgreSQL configuration:
# spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/movierecommender}
# spring.datasource.username=${DB_USERNAME:postgres}
# spring.datasource.password=${DB_PASSWORD:password}
# spring.datasource.driver-class-name=org.postgresql.Driver
# spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# WITH SQLite configuration:
spring.datasource.url=${DATABASE_URL:jdbc:sqlite:./data/movierecommender.db}
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.properties.hibernate.dialect=org.hibernate.community.dialect.SQLiteDialect

# Remove username/password (SQLite doesn't use them)
# spring.datasource.username=
# spring.datasource.password=
```

### 2. Python ML Service Changes

#### Update `python-ml-service/requirements.txt`
Remove PostgreSQL dependency (sqlite3 is built into Python):

```txt
# REMOVE this line:
psycopg2-binary==2.9.7

# sqlite3 is built into Python - no additional dependency needed!
```

#### Update `python-ml-service/database.py`
Replace the PostgreSQL connection logic with SQLite. Here's the key part that needs to change:

```python
# REPLACE the PostgreSQL connection setup in __init__ method:
class DatabaseManager:
    def __init__(self):
        # OLD PostgreSQL code:
        # database_url = os.environ.get('DATABASE_URL', 'postgresql://localhost:5432/movierecommender')
        # if database_url.startswith('postgres://'):
        #     database_url = database_url.replace('postgres://', 'postgresql://', 1)
        # parsed_url = urlparse(database_url)
        # self.db_config = {
        #     'host': parsed_url.hostname,
        #     'port': parsed_url.port or 5432,
        #     'database': parsed_url.path[1:],
        #     'user': parsed_url.username,
        #     'password': parsed_url.password
        # }

        # NEW SQLite code:
        database_url = os.environ.get('DATABASE_URL', 'sqlite:///./data/movierecommender.db')
        if database_url.startswith('sqlite:///'):
            self.db_path = database_url.replace('sqlite:///', '')
        elif database_url.startswith('sqlite://'):
            self.db_path = database_url.replace('sqlite://', '')
        else:
            self.db_path = './data/movierecommender.db'
        
        # Ensure data directory exists
        os.makedirs(os.path.dirname(self.db_path) if os.path.dirname(self.db_path) else '.', exist_ok=True)
```

#### Replace connection methods
You'll need to replace all `psycopg2` connection methods with `sqlite3`. The main method that needs updating:

```python
import sqlite3  # Add this import, remove psycopg2

# REPLACE connection method:
def get_connection(self):
    """Get database connection"""
    try:
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row  # Makes rows work like dictionaries
        return conn
    except sqlite3.Error as e:
        logger.error(f"Database connection error: {e}")
        raise
```

### 3. Environment Configuration

#### Backend `.env` or environment variables:
```properties
DATABASE_URL=jdbc:sqlite:./data/movierecommender.db
```

#### Python ML Service `.env`:
```env
DATABASE_URL=sqlite:///./data/movierecommender.db
FLASK_ENV=development
FLASK_DEBUG=True
PORT=5000
```

## What Happens After Making These Changes?

1. **Automatic Database Creation**: When you start the backend, SQLite will automatically create the database file at `backend/data/movierecommender.db`

2. **Schema Creation**: Hibernate will automatically create all the necessary tables (users, movies, ratings, recommendations, etc.)

3. **Shared Database**: Both the Spring Boot backend and Python ML service will use the same SQLite database file

4. **No Manual Setup**: Unlike PostgreSQL, you don't need to create databases, users, or set permissions

## Verification

After making the changes and starting the application:

```powershell
# Check if database file was created
if (Test-Path "backend\data\movierecommender.db") {
    Write-Host "✅ SQLite database created successfully!"
    
    # Check database size
    $dbFile = Get-Item "backend\data\movierecommender.db"
    Write-Host "Database size: $([math]::Round($dbFile.Length / 1KB, 2)) KB"
    
    # List tables (if you have sqlite3 command line tool)
    sqlite3 backend\data\movierecommender.db ".tables"
} else {
    Write-Host "❌ Database not created yet - check if backend is running"
}
```

## Database Browser (Optional)

For easy database viewing and management, you can install:
- **DB Browser for SQLite** - https://sqlitebrowser.org/
- **SQLite command line tools** - https://sqlite.org/download.html

This will let you:
- View database contents
- Run queries manually  
- Export/import data
- Modify user roles (like making users admins)

## Key Benefits

- ✅ **Zero database administration** - No PostgreSQL server to manage
- ✅ **Portable** - Database file can be copied, backed up easily
- ✅ **Fast development** - Start coding immediately
- ✅ **Perfect for learning** - Focus on the application logic
- ✅ **Production ready** - SQLite can handle moderate production loads
