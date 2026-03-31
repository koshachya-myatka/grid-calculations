1. Собери заново jar-ники GridAnnotations, GridCalculator, GridShaper, GridWorker

2. Запусти GridShaper

3. Запусти GridDistributor
    - для добавления задачи кидай curl с изменением json с условием

    - curl -X POST http://localhost:8081/tasks ^
    -H "Content-Type: application/json" ^
    -d "{ \"fieldWidth\": 4, \"fieldLength\": 3, \"circles\": [ { \"x\": 0, \"y\": 0, \"color\": 0 }, { \"x\": 0, \"y\": 3, \"color\": 0 }, { \"x\": 2, \"y\": 2, \"color\": 1 } ] }"

    - curl -X POST http://localhost:8081/tasks ^
    -H "Content-Type: application/json" ^
    -d "{ \"fieldWidth\": 9, \"fieldLength\": 5, \"circles\": [ { \"x\": 0, \"y\": 0, \"color\": 0 }, { \"x\": 0, \"y\": 2, \"color\": 1 }, { \"x\": 0, \"y\": 6, \"color\": 1 }, { \"x\": 0, \"y\": 8, \"color\": 0 }, { \"x\": 2, \"y\": 4, \"color\": 0 }, { \"x\": 3, \"y\": 2, \"color\": 1 }, { \"x\": 4, \"y\": 0, \"color\": 0 }, { \"x\": 4, \"y\": 2, \"color\": 1 }, { \"x\": 4, \"y\": 4, \"color\": 0 }, { \"x\": 4, \"y\": 8, \"color\": 0 } ] }"

    - curl -X POST http://localhost:8081/tasks ^
    -H "Content-Type: application/json" ^
    -d "{ \"fieldWidth\": 10, \"fieldLength\": 10, \"circles\": [ { \"x\": 0, \"y\": 2, \"color\": 1 }, { \"x\": 0, \"y\": 4, \"color\": 1 }, { \"x\": 1, \"y\": 4, \"color\": 1 }, { \"x\": 1, \"y\": 8, \"color\": 0 }, { \"x\": 2, \"y\": 2, \"color\": 0 }, { \"x\": 2, \"y\": 4, \"color\": 0 }, { \"x\": 2, \"y\": 6, \"color\": 1 }, { \"x\": 3, \"y\": 3, \"color\": 1 }, { \"x\": 3, \"y\": 6, \"color\": 1 }, { \"x\": 4, \"y\": 0, \"color\": 0 }, { \"x\": 4, \"y\": 5, \"color\": 1 }, { \"x\": 4, \"y\": 9, \"color\": 1 }, { \"x\": 5, \"y\": 2, \"color\": 1 }, { \"x\": 5, \"y\": 7, \"color\": 1 }, { \"x\": 6, \"y\": 2, \"color\": 0 }, { \"x\": 6, \"y\": 6, \"color\": 1 }, { \"x\": 7, \"y\": 0, \"color\": 1 }, { \"x\": 7, \"y\": 4, \"color\": 0 }, { \"x\": 7, \"y\": 9, \"color\": 1 }, { \"x\": 8, \"y\": 6, \"color\": 1 }, { \"x\": 8, \"y\": 7, \"color\": 1 }, { \"x\": 9, \"y\": 2, \"color\": 0 }, { \"x\": 9, \"y\": 9, \"color\": 0 } ] }"

4. Запусти GridWorker из консоли в нужном кол-ве, меняя порты  
    - java -jar "D:\Study\Grid Calculations\GridWorker\target\GridWorker-1.0.jar" --server.port=8090  
    - java -jar "C:\Study\grid-calculations\GridWorker\target\GridWorker-1.0.jar" --server.port=8090