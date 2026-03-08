# Create a container from the official MySQL Docker Image:
    docker run -d --name ocean-mysql -e MYSQL_ROOT_PASSWORD=1234 -e MYSQL_DATABASE=ocean_explorer_db -p 3306:3306 mysql:8

# list all docker containers:
    docker ps -a

# Start container:
    docker start ocean-mysql

# start ocean-server (projekt_Unterlagen_von_Philip/ocean_explorer_v1.3/):
    1. java -jar oceanstarter.jar
    
    2. click on the start button

# run ShipBaseServerApplication

# run ShipApplication

## 📬 Send Requests to Ocean Explorer Server using Postman

You can test the **Ocean Explorer server** using Postman by following these steps:

### 1. Launch request
Open Postman on your computer and create a new request.

![Launch Postman](img_1.png)

---

### 2. Scan request

![Scan Server](img_2.png)

---

### 3. radar request

![img_3.png](img_3.png)

---

### 4. navigate request

![img_4.png](img_4.png)

---

### 5. exit request

![img_5.png](img_5.png)

---

### 6. autoPilot request

![img_6.png](img_6.png)
---


### 💡 Tips
- Use the following base URL for requests: `http://localhost:8080` (replace with your server's URL if different).
- Set the request method (GET, POST, etc.) according to the API endpoint you want to test.