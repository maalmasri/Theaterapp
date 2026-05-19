package com.maamouniverse.theaterapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {

    @GetMapping("/")
    public String startseite() {
        return "<h1>Willkommen!</h1><p>Diese Seite wurde direkt von Java generiert.</p>";
    }
}