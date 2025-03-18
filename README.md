Взаимодействие с backend(сервис):
Примечания:
1) Пункт na) - запрос, пункт nb) - ответ на запрос na) 
2) some text /some_variable/; some_variable = 123  <=> some text 123
3) 3)Для того, чтобы установить сессию с сервисом, нужно получить jwt-токен, и в каждом http-запросе (кроме /auth/** ) писать заголовок ==Authorization: Bearer /jwt-token/==,  в том числе в handshake-запросе для установления websocket-соединения. 


Запросы и ответы:
### 1a) http://localhost:8080/auth/signup - регистрация.###
Тело:
{
    "email": "sasha@mail.ru",
    "password": "pass"
}
### 1б) ###
Успешный ответ:
{
    "email": "sasha@mail.ru",
    "refresh": "cc769389-6152-4b43-8173-112647f61b1b",
    "jwt": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYXNoYUBtYWlsLnJ1IiwiaWF0IjoxNzQyMzE2MDM3LCJleHAiOjE3NDIzMTk2Mzd9.EaGxNLTthfYGpijkxUhOMnMNzB-7tyWUWajWrRfIvVE"
}

Неуспешный ответ:
{
    "error": "Unauthorized",
    "message": "/error-message/"
}


### 2a) http://localhost:8080/auth/signin - авторизация ###
Тело: 
{
    "email": "sasha@mail.ru",
    "password": "pass"
}
### 2б) ###
Успешный ответ:
{
    "email": "sasha@mail.ru",
    "refresh": "cc769389-6152-4b43-8173-112647f61b1b",
    "jwt": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYXNoYUBtYWlsLnJ1IiwiaWF0IjoxNzQyMzE2MDM3LCJleHAiOjE3NDIzMTk2Mzd9.EaGxNLTthfYGpijkxUhOMnMNzB-7tyWUWajWrRfIvVE"
}

Неуспешный ответ:
{
    "error": "Unauthorized",
    "message": "/error-message/"
}

### 3a) http://localhost:8080/auth/refresh - обновление access-токена (или же jwt-токена) ###
Тело: 
{
    "email": "user3@mail.ru",
    "refreshToken": "d15baf29-9930-4ef9-910a-9dead8df"
}

### 3б) ###
Успешный ответ:
{
    "email": "sasha@mail.ru",
    "jwt": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYXNoYUBtYWlsLnJ1IiwiaWF0IjoxNzQyMzIwNDk0LCJleHAiOjE3NDIzMjQwOTR9._wP9nMuIjLTbeA5UPl5b-46SGzBrLZqo5XntwxVv85Q"
}

Неуспешный ответ:
{
    "error": "Unauthorized",
    "message": "/error-message/"
} 

### 4a) http://localhost:8080/auth/signout - выход из аккаунта ###
Тело: 
{
    "email": "sasha@mail.ru",
    "refreshToken": "d15baf29-9930-4ef9-910a-9dead8df"
}

### 4б) ###
Успешный ответ:
{
    "email": "sasha@mail.ru",
    "message": "Refresh token successfully removed"
}

Неуспешный ответ:
{
    "email": "sasha@mail.ru",
    "message": "Refresh token wasn't removed"
}



### 5) ws://localhost:8080/ws - установление вебсокет-сессии
Заголовки:
Authorization: "Bearer /jwt-token/"

### 6) Websocket-сообщение (user -> backend): пользователь просматривает чат ###
Тело:
{
        category: "activity_status", 
        chatID: "123",
        status: "inactive"            
}

Примечание: указывать email пользователя не нужно, так как он привязан к сессии

### 7) Websocket-сообщение (user -> backend): пользователь перестал просматривать чат ###
Тело:
{
        category: "activity_status", // Обязательное поле для BaseNotification
        chatID: "123",            // Поле из ChatActivityChangeIngoingNotification
        status: "inactive"            // Поле из ChatActivityChangeIngoingNotification
}

### 8) Websocket-сообщение (backend -> user): пользователь (с почтой email) теперь просматривает чат, который получатель сообщения тоже в данный момент просматривает###
Тело:
{
    chatID: /chatID/,  
    email: /email/, // пользователь, который поменял статус  
    status: active,  
    category = "activity_status"  
}

### 9) Websocket-сообщение (backend -> user): пользователь (с почтой email) перестал просматривать чат, который получатель сообщения тоже в данный момент просматривает ###
Тело:
{
    chatID: /chatID/,  
    email: /email/, // пользователь, который поменял статус  
    status: inactive,  
    category = "activity_status"  
}
