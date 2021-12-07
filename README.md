# CurrencyMonitor

Программа осуществляет чтения курсов валют из https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml Курсы валют для RUB-евро, USD-евро, JPY-евро сохранются в БД

Компиляция и запуск приложения:

Запустить терминал из корневой папки:

 - mvn clean install

 - mvn dependency:copy-dependencies

 - cd target/

 - java -cp CurrencyMonitor-1.0-SNAPSHOT.jar:dependency main

P.S. Часть программы с REST не доделана.