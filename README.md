# Education-API-Application
# Authentication API Documentation

## Base URL
```
http://localhost:8080
```

## Endpoints

## Authentication
### 1. Register New User

Create a new user account and receive an access token.

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
  "code": 200,
  "message": "Success",
  "timeStamp": "2025-11-02 16:50:30",
  "data": {
    "user": {},
    "accessToken": ""
  }
}
```
### 2. Login

Authenticate with email and password to receive an access token.

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "yourPassword123"
}
```

**Success Response:** `200 OK`
```json
{
  "status": true,
  "code": 200,
  "message": "Success",
  "timeStamp": "2025-11-02 16:13:03",
  "data": {
    "accessToken": "10|6ee8b5de134fd21c5d29a8a676096e50c9dfad57ee13c505dd0ce01dfb0a06d9"
  }
}
```
---
### 3. Get User Profile

Retrieve the authenticated user's profile information.

**Endpoint:** `GET /api/auth/profile`

**Headers:**
```
Authorization: Bearer <your_access_token>
```

**Response:** `200 OK`
```json
{
  "status": true,
  "code": 200,
  "message": "Success",
  "timeStamp": "2025-11-02 16:53:21",
  "data": {}
}
```

**Error Response:** `403 Forbidden`
```json
"Missing or invalid Authorization header"
```

or

```json
"Invalid or expired token"
```

**cURL Example:**
```bash
curl -X GET http://localhost:8081/api/auth/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---
### 4. Logout
Invalidate the current access token.
**Endpoint:** `GET /api/auth/logout`
**Headers:**
```
Authorization: Bearer <your_access_token>
```
**Response:** `204 No Content`
### 5. Delete Account

Permanently delete the authenticated user's account.

**Endpoint:** `DELETE /api/auth/`

**Headers:**
```
Authorization: Bearer <your_access_token>
```
**Response:** `204 No Content`

## Menu + Submenu + Detail Content API
### 1. Get Menu
Create a new user account and receive an access token.

**Endpoint:** `POST /api/api/data-structures/grades`

**Request Body:**
```json
{
  "status": true,
  "code": 200,
  "message": "Success",
  "timeStamp": "2025-11-02 16:59:11",
  "data": [
    {
      "id": 1,
      "name": "ថ្នាក់ទី៧",
      "description": null,
      "type": "GRADE"
    }
  ]
}
```
### 2. Get Menu + Submenu
**Endpoint:** `POST /api/api/data-structures/menus/{id}`
**Request Body:**
```json
{
  "status": true,
  "code": 200,
  "message": "Success",
  "timeStamp": "2025-11-02 17:09:54",
  "data": {
    "id": 104,
    "name": "ផែនដីវិទ្យា",
    "description": null,
    "type": "SUBJECT",
    "parentId": 1,
    "parent": {
      "id": 1,
      "name": "ថ្នាក់ទី៧",
      "type": "GRADE",
      "parent": null
    },
    "children": [
      {
        "id": 108,
        "name": "រ៉ែ និងថ្ម",
        "type": null,
        "parentId": 104,
        "description": null,
        "children": [
          {
            "id": 109,
            "name": "មេរៀនទី១៖ លក្ខណៈនៃរ៉ែ",
            "type": null,
            "parentId": 108,
            "description": null,
            "children": []
          }
        ]
      }
    ]
  }
}
```
### 3. Get Detail Content by Menu ID
**Endpoint:** `POST /api/data-structures/menus/details/{id}`
**Request Body:**
```json
{
  "status": true,
  "code": 200,
  "message": "Success",
  "timeStamp": "2025-11-02 17:24:56",
  "data": {
    "id": 4,
    "name": "មេរៀនទី១៖ សេចក្តីផ្តើមអំពីចំនួនគត់",
    "description": null,
    "type": "LESSON",
    "parentId": 3,
    "parent": {
      "id": 3,
      "name": "ចំនួនគត់",
      "type": "CHAPTER",
      "parent": {
        "id": 2,
        "name": "គណិតវិទ្យា",
        "type": "SUBJECT",
        "parent": {
          "id": 1,
          "name": "ថ្នាក់ទី៧",
          "type": "GRADE",
          "parent": null
        }
      }
    },
    "children": [],
    "content": {
      "id": 1,
      "dataStructureId": null,
      "title": "Graph Basics",
      "description": "An overview of graph theory concepts such as nodes, edges, and adjacency lists.",
      "components": [
        {
          "id": 1,
          "contentId": 1,
          "position": 1,
          "componentData": {
            "id": 1,
            "mainComponentId": 1,
            "dataType": "LATEX",
            "data": "\\int_{0}^{\\infty} e^{-x^2} dx = \\frac{\\sqrt{\\pi}}{2}"
          }
        }
      ]
    }
  }
}
```
### 4. Delete Menu By Id
**Endpoint:** `DELETE /api/data-structures/menus/{id}`
**Response:** `204 No Content`

## Content Text
### 1. Create Content
**Endpoint:** `POST /api/contents/{dataStructureId}`
**Request Body:**
```json
{
  "title": "Graph Basics",
  "description": "An overview of graph theory concepts such as nodes, edges, and adjacency lists."
}
```


## Support

For issues or questions, please contact the development team or create an issue in the project repository.