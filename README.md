# spring-boot-translator
REST Service for translate with yandex API

В config.property лежит api key яндекса и url по которому можно получить ip адрес для заполнения соответствующего поля в БД. К проекту нужно подключить sqlite-jdbc-3.21.0.1.jar для работы с БД, который лежит в lib/.
Запустить SpringApplication, зайти на http://localhost:8080. Для получения перевода нужно ввести три обязательных параметра: 
текст для перевода, язык с какого, язык на который требуется перевести. Пример: http://localhost:8080/translate?text=hot&from=en&to=ru
