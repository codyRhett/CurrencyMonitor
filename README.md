# CurrencyMonitor
Должен быть установлен инструмент MAVEN

Программа осуществляет чтения курсов валют для RUB, JPY, USD из https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml 
Курсы валют для RUB-евро, USD-евро, JPY-евро сохранются в БД

Компиляция и запуск приложения:

Запустить скрипт runScript.sh из корневой папки

 
Для получения нужной валюты через HTTP запрос, необходимо ввести команду:

curl -v -X GET localhost:8001/api/currency?JPY

Где вместо JPY поставить нужный атрибут - RUB, JPY, USD

