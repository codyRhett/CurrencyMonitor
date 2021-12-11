# CurrencyMonitor
Должен быть установлен инструмент MAVEN

Программа осуществляет чтения курсов валют для RUB, JPY, USD из https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml Курсы валют для RUB-евро, USD-евро, JPY-евро сохранются в БД

Компиляция и запуск приложения:

Запустить терминал из корневой папки:

 - mvn clean install

 - mvn dependency:copy-dependencies

 - cd target/

 - java -cp CurrencyMonitor-1.0-SNAPSHOT.jar:dependency/sqlite-jdbc-3.36.0.2.jar main
 
После запуска будет сформирована таблица БД в папке target/

Для получения нужной валюты через HTTP запрос, необходимо ввести команду:

curl -v -X GET localhost:8001/api/currency?JPY

Где вместо JPY поставить нужный атрибут - RUB, JPY, USD

