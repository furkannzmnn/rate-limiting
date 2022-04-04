package com.example.ratelimiting.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/win")
public class WinTokenController {

    @RequestMapping("/token/{slug}")
    public String getWinToken(@PathVariable String slug) {
        return "Win Token";
    }
}

