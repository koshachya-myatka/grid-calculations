1. Собери заново jar-ники GridAnnotations, GridCalculator, GridShaper, GridWorker
2. Запусти GridShaper
3. Запусти GridDistributor
    - для добавления задачи кидай curl с изменением пути до файла
    - ```curl -X POST -H "Content-Type: application/json" -d "D://Study//Grid Calculations//test2.txt" http://localhost:8081/tasks```
    - ```curl -X POST -H "Content-Type: application/json" -d "C://Study//grid-calculations//test2.txt" http://localhost:8081/tasks```
4. Запусти GridWorker из консоли в нужном кол-ве, меняя порты  
    - ```java -jar "D:\Study\Grid Calculations\GridWorker\target\GridWorker-1.0.jar" --server.port=8090```  
    - ```java -jar "C:\Study\grid-calculations\GridWorker\target\GridWorker-1.0.jar" --server.port=8090```
