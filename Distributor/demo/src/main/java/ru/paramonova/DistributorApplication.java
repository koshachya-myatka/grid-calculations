package ru.paramonova;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.paramonova.models.Distributor;


@SpringBootApplication
public class DistributorApplication {
    // комп
    // java -jar "D:\Study\Grid Calculations\Distributor\demo\target\Distributor-0.jar" "D:\Study\Grid Calculations\test2.txt"
    // ноут
    // java -jar "C:\Study\grid-calculations\Distributor\demo\target\Distributor-0.jar" "C:\Study\grid-calculations\test2.txt"

    public static void main(String[] args) {
        SpringApplication.run(DistributorApplication.class, args);
    }
}
