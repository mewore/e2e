package moe.mewore.e2e.springexample.controllers;

import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@ResponseBody
public class SpringExampleController {

    private String value = "initial";

    @EventListener
    public void onApplicationEvent(final ServletWebServerInitializedEvent event) {
        System.out.println("Initialized at port " + event.getWebServer().getPort());
        System.out.println("The port is: " + event.getWebServer().getPort());
        System.out.println("The protocol is: http");
        System.out.println("Waiting for 3 seconds...");
        try {
            Thread.sleep(3000L);
        } catch (final InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return;
        }
        System.out.println("Done waiting! Fully initialized!");
    }

    @GetMapping(path = "/value")
    String getValue() {
        return value;
    }

    @PutMapping(path = "/value")
    void setValue(@RequestBody final String value) {
        this.value = value;
    }
}
