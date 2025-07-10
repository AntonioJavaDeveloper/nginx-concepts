package br.com.javadeveloper.dslist.controllers;

import br.com.javadeveloper.dslist.dto.ServerResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Random;

@RestController
@RequestMapping(value = "/delayed")
public class DelayedController {
    @Value("${server.name:games2}")
    private String serverName;

    @GetMapping
    public ServerResponse<String> delayed() throws InterruptedException {
        int delay = new Random().nextInt(25000); // randômico até 25 segundos
        Thread.sleep(delay);
        String result = "Servidor: " + this.serverName + " | Delay: " + delay + "ms";
        return new ServerResponse<>(serverName, result);
    }
}
