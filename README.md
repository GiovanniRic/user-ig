# Introduction 
A spring boot application with Java 21 that provides some services for managing users with roles.




# Run
To run the application execute the command with Maven:
```shell
mvn spring-boot:run
``` 
The application will be accessible at http://localhost:8080/

---

## 🔹 Example

### 🟢 Get Advice
**URL:** `/advice`  
**Method:** `POST`  
**Content-Type:** `application/json`

**Request Body Example:**
```json
{
    "id": 1,
    "jsonrpc": "2.0",
    "method": "giveMeAdvice",
    "params": 
        {
            "topic": "car",
            "amount": 12
        }
    
}
``` 
