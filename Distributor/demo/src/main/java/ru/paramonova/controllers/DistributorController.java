package ru.paramonova.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.paramonova.models.Distributor;

@RestController
@RequiredArgsConstructor
public class DistributorController {

    private final Distributor distributor;

    @GetMapping("/info")
    public String getFieldInfo() {
        return String.format("Ширина: %.0f,\nВысота: %.0f,\nКоличество кругов: %d,\nКруги: %s",
                distributor.getFieldWidth(),
                distributor.getFieldLength(),
                distributor.getCircles().size(),
                distributor.getCircles());
    }
}
