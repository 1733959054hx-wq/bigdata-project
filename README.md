# Big Data Course Project - Cloud Storage System

大数据原理与实践课程项目 - 云存储系统

## Project Description

基于Hadoop/Spark大数据技术栈的云存储系统，实现文件的上传、下载、分享、协作编辑等功能。系统采用前后端分离架构，后端使用Spring Boot，前端使用Vue.js。

## Project Structure

```
bigdata-project/
├── bigdata-backend/                    # 后端项目
│   └── bigdata后端集成版本/
│       └── bigdata/
│           ├── src/main/java/com/gzu/bigdata/
│           │   ├── controller/         # 控制器层
│           │   │   ├── FileController.java
│           │   │   ├── UserController.java
│           │   │   ├── AdminController.java
│           │   │   ├── FileShareController.java
│           │   │   ├── FileTrashController.java
│           │   │   └── DirectoryController.java
│           │   ├── service/            # 业务逻辑层
│           │   │   ├── FileService.java
│           │   │   ├── UserService.java
│           │   │   ├── AdminService.java
│           │   │   ├── FileShareService.java
│           │   │   └── HBaseDocumentService.java
│           │   ├── entity/             # 实体类
│           │   ├── mapper/             # MyBatis-Plus Mapper
│           │   ├── config/             # 配置类
│           │   ├── dto/                # 数据传输对象
│           │   ├── common/             # 公共工具类
│           │   ├── utils/              # 工具类（JWT等）
│           │   └── websocket/          # WebSocket协作编辑
│           ├── src/main/resources/
│           │   ├── application.properties
│           │   └── static/             # 静态资源
│           └── pom.xml
│
├── cloud-storage-frontend/             # 前端项目
│   └── cloud-storage-frontend/
│       ├── src/
│       │   ├── views/                  # 页面组件
│       │   │   ├── Login.vue           # 登录页
│       │   │   ├── Register.vue        # 注册页
│       │   │   ├── FileList.vue        # 文件列表
│       │   │   ├── Admin.vue           # 管理后台
│       │   │   ├── CollaborationEditor.vue  # 协作编辑器
│       │   │   ├── RecycleBin.vue      # 回收站
│       │   │   └── share.vue           # 文件分享
│       │   ├── api/                    # API接口
│       │   ├── stores/                 # Pinia状态管理
│       │   ├── router/                 # 路由配置
│       │   ├── components/             # 公共组件
│       │   ├── styles/                 # 样式文件
│       │   └── utils/                  # 工具函数
│       ├── public/                     # 静态资源
│       ├── package.json
│       └── vite.config.ts
│
└── README.md
```

## Tech Stack

### Backend
- **Framework**: Spring Boot 2.x
- **ORM**: MyBatis-Plus
- **Database**: MySQL
- **Authentication**: JWT (JSON Web Token)
- **WebSocket**: 实时协作编辑
- **Build Tool**: Maven

### Frontend
- **Framework**: Vue 3
- **UI Library**: Element Plus
- **State Management**: Pinia
- **Build Tool**: Vite
- **HTTP Client**: Axios

## Features

### Core Features
- User registration and login with JWT authentication
- File upload, download, and preview
- Directory management (create, rename, delete)
- File sharing with permission control
- Recycle bin for deleted files
- File search functionality

### Advanced Features
- **Real-time Collaboration**: WebSocket-based collaborative document editing
- **Admin Dashboard**: User management, file statistics, system configuration
- **Theme System**: Support for light/dark themes
- **Responsive Design**: Mobile-friendly interface

## Quick Start

### Prerequisites
- JDK 17 or higher
- Node.js 16 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

### Backend Setup

```bash
cd bigdata-backend/bigdata后端集成版本/bigdata

# Configure database in src/main/resources/application.properties
# spring.datasource.url=jdbc:mysql://localhost:3306/bigdata
# spring.datasource.username=root
# spring.datasource.password=your_password

# Build and run
mvn clean install
mvn spring-boot:run
```

Backend will start on `http://localhost:8080`

### Frontend Setup

```bash
cd cloud-storage-frontend/cloud-storage-frontend

# Install dependencies
npm install

# Development server
npm run dev

# Build for production
npm run build
```

Frontend will start on `http://localhost:5173`

## API Endpoints

### User API
- `POST /api/user/register` - User registration
- `POST /api/user/login` - User login
- `GET /api/user/info` - Get user info

### File API
- `POST /api/file/upload` - Upload file
- `GET /api/file/list` - List files
- `GET /api/file/download/{id}` - Download file
- `DELETE /api/file/{id}` - Delete file
- `POST /api/file/share` - Share file

### Admin API
- `GET /api/admin/users` - List all users
- `DELETE /api/admin/user/{id}` - Delete user
- `GET /api/admin/stats` - System statistics

## Database Schema

- **user** - User information table
- **file_metadata** - File metadata table
- **directory** - Directory structure table
- **file_share** - File sharing records
- **announcement** - System announcements

## License

MIT License
