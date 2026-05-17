# CKT Education API Documentation

## Base URL
```
http://localhost:8081
```

---

## Authentication

### 1. Register New User

**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "yourPassword123",
  "firstName": "John",
  "lastName": "Doe",
  "username": "user@example.com"
}
```

**Response:** `201 Created`
```json
{
  "status": true,
  "code": 201,
  "message": "Success",
  "timeStamp": "2025-11-02 16:50:30",
  "data": {
    "user": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "username": "user@example.com",
      "email": "user@example.com",
      "gender": null,
      "dateOfBirth": null,
      "avatar": null,
      "phoneNumber": null,
      "status": "active"
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

### 2. Login

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "yourPassword123"
}
```

**Response:** `200 OK`
```json
{
  "status": true,
  "code": 200,
  "message": "Success",
  "timeStamp": "2025-11-02 16:13:03",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

### 3. Get User Profile

**Endpoint:** `GET /api/users/profile`

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`
```json
{
  "status": true,
  "code": 200,
  "message": "Success",
  "timeStamp": "2025-11-02 16:53:21",
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "gender": null,
    "username": "user@example.com",
    "email": "user@example.com",
    "phoneNumber": null,
    "avatar": null,
    "dateOfBirth": null,
    "status": "active"
  }
}
```

---

### 4. Logout

**Endpoint:** `POST /api/auth/logout`

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`
```json
{
  "status": true,
  "code": 200,
  "message": "Success",
  "timeStamp": "2025-11-02 17:00:00",
  "data": null
}
```

---

### 5. Delete Account

**Endpoint:** `DELETE /api/auth/`

**Headers:** `Authorization: Bearer <token>`

**Response:** `204 No Content`

---

## Curriculum Menu API

### 1. Get All Grades

**Endpoint:** `GET /api/public/menus/grades`

**Response:** `200 OK`
```json
{
  "status": true,
  "code": 200,
  "message": "Success",
  "timeStamp": "2025-11-02 16:59:11",
  "data": [
    { "id": 1, "name": "ថ្នាក់ទី៧", "description": null, "type": "GRADE" },
    { "id": 2, "name": "ថ្នាក់ទី៨", "description": null, "type": "GRADE" },
    { "id": 3, "name": "ថ្នាក់ទី៩", "description": null, "type": "GRADE" },
    { "id": 4, "name": "ថ្នាក់ទី១០", "description": null, "type": "GRADE" },
    { "id": 5, "name": "ថ្នាក់ទី១១", "description": null, "type": "GRADE" },
    { "id": 6, "name": "ថ្នាក់ទី១២", "description": null, "type": "GRADE" }
  ]
}
```

---

### 2. Get Menu Tree by ID (Grade or Subject)

Returns a node with its full children tree (2 levels deep).

**Endpoint:** `GET /api/public/menus/{id}`

**Response:** `200 OK`
```json
{
  "status": true,
  "code": 200,
  "message": "Success",
  "timeStamp": "2025-11-02 17:09:54",
  "data": {
    "id": 42,
    "name": "គណិតវិទ្យា",
    "description": null,
    "type": "SUBJECT",
    "parentId": 6,
    "parent": {
      "id": 6,
      "name": "ថ្នាក់ទី១២",
      "type": "GRADE",
      "parent": null
    },
    "children": [
      {
        "id": 201,
        "name": "លីមីត និងភាពជាប់",
        "type": "CHAPTER",
        "parentId": 42,
        "description": null,
        "children": [
          {
            "id": 301,
            "name": "មេរៀនទី១៖ លក្ខណៈនៃលីមីត",
            "type": "LESSON",
            "parentId": 201,
            "description": null,
            "children": []
          }
        ]
      }
    ]
  }
}
```

---

### 3. Get Lesson Detail with Content

Returns full lesson detail including content and all components.

**Endpoint:** `GET /api/public/menus/details/{id}`

**Response:** `200 OK`
```json
{
  "status": true,
  "code": 200,
  "message": "Success",
  "timeStamp": "2025-11-02 17:24:56",
  "data": {
    "id": 303,
    "name": "មេរៀនទី១៖ និយមន័យនៃដេរីវេ",
    "description": null,
    "type": "LESSON",
    "parentId": 202,
    "parent": {
      "id": 202,
      "name": "ដេរីវេ និងការអនុវត្ត",
      "type": "CHAPTER",
      "parent": {
        "id": 42,
        "name": "គណិតវិទ្យា",
        "type": "SUBJECT",
        "parent": {
          "id": 6,
          "name": "ថ្នាក់ទី១២",
          "type": "GRADE",
          "parent": null
        }
      }
    },
    "children": [],
    "content": {
      "id": 10,
      "dataStructureId": 303,
      "title": "មេរៀនទី១៖ និយមន័យនៃដេរីវេ",
      "description": "ដេរីវេ f'(x) = lim(h→0) [f(x+h) - f(x)] / h",
      "components": [
        {
          "id": 20,
          "contentId": 10,
          "position": 1,
          "componentData": {
            "id": 20,
            "mainComponentId": 20,
            "dataType": "TEXT",
            "data": "ដេរីវេ f'(x) = lim(h→0) [f(x+h) - f(x)] / h\n\nច្បាប់: (xⁿ)' = nxⁿ⁻¹"
          }
        }
      ]
    }
  }
}
```

---

### 4. Create Data Structure

**Endpoint:** `POST /api/public/menus`

**Headers:** `Authorization: Bearer <token>`

**Permission:** `CREATE_DATA_STRUCTURE`

**Request Body:**
```json
{
  "name": "លីមីត និងភាពជាប់",
  "description": null,
  "type": "CHAPTER",
  "parentId": 42
}
```

**Response:** `204 No Content`

---

### 5. Update Data Structure

**Endpoint:** `PUT /api/public/menus/{id}`

**Headers:** `Authorization: Bearer <token>`

**Permission:** `UPDATE_DATA_STRUCTURE`

**Response:** `204 No Content`

---

### 6. Delete Data Structure

**Endpoint:** `DELETE /api/public/menus/{id}`

**Headers:** `Authorization: Bearer <token>`

**Permission:** `DELETE_DATA_STRUCTURE`

**Response:** `204 No Content`

---

## Content API

### 1. Create Content for a Lesson

**Endpoint:** `POST /api/public/contents/{dataStructureId}`

**Request Body:**
```json
{
  "title": "និយមន័យនៃដេរីវេ",
  "description": "ការប្រើប្រាស់ដេរីវេក្នុងការរកអត្រានៃការផ្លាស់ប្តូររបស់អនុគមន៍"
}
```

**Response:** `204 No Content`

---

## Component Text API

### 1. Create Component Text

**Endpoint:** `POST /api/component-texts/{contentId}`

**Headers:** `Authorization: Bearer <token>`

**Permission:** `CREATE_COMPONENT_TEXT`

**Request Body:**
```json
{
  "dataType": "TEXT",
  "data": "ដេរីវេ f'(x) = lim(h→0) [f(x+h) - f(x)] / h"
}
```

Supported `dataType`: `TEXT`, `LATEX`

**Response:** `204 No Content`

---

## Component Collapse API

### 1. Create Component Collapse

**Endpoint:** `POST /api/component-collapses`

**Request Body:**
```json
{
  "contentId": 10,
  "data": "ការបង្ហាញការដោះស្រាយបន្ថែម"
}
```

**Response:** `204 No Content`

---

### 2. Get All Component Collapses

**Endpoint:** `GET /api/component-collapses`

**Response:** `200 OK`
```json
{
  "status": true,
  "code": 200,
  "message": "Success",
  "timeStamp": "2025-11-02 17:30:00",
  "data": [
    {
      "id": 1,
      "componentId": 5,
      "data": "ការបង្ហាញការដោះស្រាយបន្ថែម"
    }
  ]
}
```

---

## User Management API

### 1. Search Users

**Endpoint:** `GET /api/users/search`

| Param | Default | Description |
|-------|---------|-------------|
| name | — | Search by first or last name |
| page | 1 | Page number |
| perPage | 15 | Items per page |
| sortBy | firstName | Sort field |
| order | asc | `asc` / `desc` |

**Response:** `200 OK`
```json
{
  "data": [
    { "id": 1, "firstName": "John", "lastName": "Doe", "email": "user@example.com", "status": "active" }
  ],
  "meta": {
    "page": 1,
    "perPage": 15,
    "totalPages": 1,
    "totalElements": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

## Role & Permission API

### 1. Get All Roles

**Endpoint:** `GET /api/roles`

**Response:** `200 OK`
```json
[
  { "id": 1, "name": "MASTER", "group": "MASTER" },
  { "id": 2, "name": "ADMIN", "group": "ADMIN" },
  { "id": 3, "name": "TEACHER", "group": "GENERAL" },
  { "id": 4, "name": "STUDENT", "group": "GENERAL" },
  { "id": 5, "name": "PARENT", "group": "GENERAL" },
  { "id": 6, "name": "USER", "group": "GENERAL" }
]
```

---

### 2. Assign Permissions to Role

**Endpoint:** `POST /api/roles/assign/role/{roleId}/permissions`

**Request Body:**
```json
{ "permissionIds": [1, 2, 3] }
```

**Response:** `204 No Content`

---

### 3. Assign Role to User

**Endpoint:** `POST /api/roles/assign/user/{userId}/role/{roleId}`

**Response:** `204 No Content`

---

### 4. Remove Role from User

**Endpoint:** `POST /api/roles/remove/user/{userId}/role/{roleId}`

**Response:** `204 No Content`

---

### 5. Get All Permissions

**Endpoint:** `GET /api/permissions`

**Response:** `200 OK`
```json
[
  { "id": 1, "name": "MANAGE ALL SYSTEM", "group": "SYSTEM_SUPPORT" },
  { "id": 2, "name": "CREATE DATA STRUCTURE", "group": "DATA_STRUCTURE" },
  { "id": 3, "name": "DELETE DATA STRUCTURE", "group": "DATA_STRUCTURE" }
]
```

---

## Full Curriculum Structure (Grades 7–12)

### Grade 7 (ថ្នាក់ទី៧)

| Subject | Chapters |
|---------|----------|
| គណិតវិទ្យា | ចំនួនគត់, ប្រភាគ, ស្វ័យគុណ, ផលធៀប, កន្សោម, សមីការ, បន្ទាត់, ពហុកោណ, ស្ថិតិ |
| ភាសាខ្មែរ | អក្សរសិល្ប៍, វេយ្យាករណ៍, រឿងខ្លី, កំណាព្យ, ការអាន, តែងសេចក្តី |
| រូបវិទ្យា | ការស្គាល់, រូបធាតុ, ដង់ស៊ីតេ, ចលនា, កម្លាំង |
| គីមីវិទ្យា | សេចក្តីផ្តើម, ល្បាយ, សូលុយស្យុង, អាតូម, បម្រែបម្រួល |
| ជីវវិទ្យា | ភាវៈរស់, កោសិកា, ចំណាត់ថ្នាក់, រុក្ខជាតិ, សត្វ |
| ផែនដីវិទ្យា | ប្រព័ន្ធផែនដី, រ៉ែ, ផ្លាក, អាកាសធាតុ, ព្រះអាទិត្យ |

### Grade 8 (ថ្នាក់ទី៨)

| Subject | Chapters |
|---------|----------|
| គណិតវិទ្យា | និទស្សន្ត, ការแยកកត្តា, សមីការ 2 អញ្ញាត, ប្រព័ន្ធ, ធរណីមាត្រ, រង្វង់, Solid, ប្រូបាប |
| ភាសាខ្មែរ | វេយ្យាករណ៍, ប្រលោមលោក, ល្ខោន, សុន្ទរកថា, សុភាសិត |
| រូបវិទ្យា | មេកានិក, រលក, កំដៅ, អគ្គិសនី |
| គីមីវិទ្យា | Periodic Table, ចំណង, ប្រតិកម្ម, ឧស្ម័ន |
| ជីវវិទ្យា | ចំណី, ប្រព័ន្ធឈាម, ដកដង្ហើម, ការបន្តពូជ |
| ប្រវត្តិសាស្ត្រ | អរិយធម៌, អាស៊ី, ខ្មែរបុរាណ |
| ភាសាអង់គ្លេស | Grammar, Reading, Writing |

### Grade 9 (ថ្នាក់ទី៩)

| Subject | Chapters |
|---------|----------|
| គណិតវិទ្យា | សមីការដឺក្រេទីពីរ, អនុគមន៍, ត្រីកោណមាត្រ, ប្រូបាប |
| ភាសាខ្មែរ | ការអានស្រង់, វចនានុក្រម, ការសរសេរ |
| រូបវិទ្យា | ថាមពល, ម៉ាញ៉េទិច, អុបទិច |
| គីមីវិទ្យា | Acid-Base, Redox, Organic Intro |
| ជីវវិទ្យា | ប្រព័ន្ធប្រសាទ, ហ័រម៉ូន, ភាពស៊ាំ |
| ប្រវត្តិសាស្ត្រ | អាណានិគម, សង្គ្រាមលោក |
| ភាសាអង់គ្លេស | Grammar Intermediate, Reading, Writing |

### Grade 10 (ថ្នាក់ទី១០)

| Subject | Chapters |
|---------|----------|
| គណិតវិទ្យា | អនុគមន៍, Log/Exp, វ៉ិចទ័រ, ត្រីកោណមាត្រ, ស្ថិតិ |
| ភាសាខ្មែរ | Prose, Grammar Advanced, Research Writing |
| រូបវិទ្យា | Projectile, Electricity, Electromagnetism |
| គីមីវិទ្យា | Thermochemistry, Kinetics, Equilibrium |
| ជីវវិទ្យា | Cell Biology, Genetics, Ecology |
| ប្រវត្តិសាស្ត្រ | ប្រវត្តិខ្មែរ, អាណានិគម |
| ភូមិវិទ្យា | ភូមិវិទ្យាកម្ពុជា, ភូមិវិទ្យាអាស៊ី |
| ភាសាអង់គ្លេស | Grammar Advanced, Academic Reading, Essay Writing |

### Grade 11 (ថ្នាក់ទី១១)

| Subject | Chapters |
|---------|----------|
| គណិតវិទ្យា | Matrix, Limit, Derivative Intro, Sequences |
| ភាសាខ្មែរ | Contemporary Literature, Advanced Writing |
| រូបវិទ្យា | DC Circuits, Electrostatics, Waves |
| គីមីវិទ្យា | Electrochemistry, Organic Chemistry, Nuclear Intro |
| ជីវវិទ្យា | Genetics Advanced, Evolution, Biotechnology |
| ប្រវត្តិសាស្ត្រ | ស.ស ទី២០, ប្រវត្តិខ្មែរ ១៩៥៣-២០០០ |
| ភូមិវិទ្យា | Physical Geography, Human Geography |
| ភាសាអង់គ្លេស | Advanced Grammar, Literature, Academic Writing |

### Grade 12 (ថ្នាក់ទី១២)

| Subject | Chapters |
|---------|----------|
| គណិតវិទ្យា | លីមីត (4), ដេរីវេ (6), អាំងតេក្រាល (6), ចំនួនកុំផ្លិច (4), លំដាប់/ស៊េរី (4), ប្រូបាប (4) |
| រូបវិទ្យា | AC Circuits (5), Electromagnetic Waves (3), Optics (4), Atomic (4), Nuclear (4) |
| គីមីវិទ្យា | Equilibrium (4), Acid-Base/pH (4), Electrochemistry (4), Organic (5), Polymers (4) |
| ជីវវិទ្យា | Genetics/Chromosomes (4), Inheritance (4), Molecular Biology (4), Evolution (4), Ecology (4) |
| ភាសាខ្មែរ | អក្សរសិល្ប៍ (4), វេយ្យាករណ៍ (4), កំណាព្យ (4), ការតែង (4) |
| ប្រវត្តិសាស្ត្រ | ប្រវត្តិខ្មែរ (5), ប្រវត្តិទំនើប (5), អាស៊ី-ប៉ាស៊ីហ្វិច (3), ពិភពលោក (4) |
| ភូមិវិទ្យា | ភូមិវិទ្យាកម្ពុជា (5), អាស៊ីអាគ្នេយ៍ (3), ពិភពលោក (4) |
| សេដ្ឋកិច្ច | មូលដ្ឋាន (4), Macro (4), ពាណិជ្ជកម្ម (4), សេដ្ឋកិច្ចខ្មែរ (4) |
| ភាសាអង់គ្លេស | Grammar (5), Reading (4), Writing (4), Speaking (4) |

---

## Default Test Accounts

| Email | Password | Role |
|-------|----------|------|
| master@gmail.com | master123 | MASTER |
| admin@gmail.com | password123 | ADMIN |
| teacher@gmail.com | password123 | TEACHER |
| student@gmail.com | password123 | STUDENT |
| parent@gmail.com | password123 | PARENT |
| user@gmail.com | password123 | USER |

---

## Error Responses

```json
{
  "status": false,
  "code": 404,
  "message": "Resource not found",
  "timeStamp": "2025-11-02 17:00:00",
  "data": null
}
```

| Code | Meaning |
|------|---------|
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 500 | Internal Server Error |

---

## Support

For issues, create a ticket in the project repository.
