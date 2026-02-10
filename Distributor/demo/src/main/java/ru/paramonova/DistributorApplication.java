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
    // java -jar "C:\Study\Grid Calculations\Distributor\demo\target\Distributor-0.jar" "C:\Study\Grid Calculations\test2.txt"

    @Bean
    public Distributor distributor() {
        String fileName = System.getProperty("fileName");
        System.out.println("Используется файл: " + fileName);
        return new Distributor(fileName);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Укажите название файла для задачи");
            System.exit(1);
        }
        System.setProperty("fileName", args[0]);
        SpringApplication.run(DistributorApplication.class, args);
    }
}
