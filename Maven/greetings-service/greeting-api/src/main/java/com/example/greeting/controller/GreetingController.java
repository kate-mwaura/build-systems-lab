package com.example.greeting.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

@RestController
public class GreetingController {

    private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);

    @GetMapping("/greet")
    public String greet(@RequestParam(name = "name", defaultValue = "World") String name) {
	logger.info("Processing greeting request for: {}", name);
	String safeName = HtmlUtils.htmlEscape(name);

        if (logger.isDebugEnabled()) {
            logger.debug("Request details: name parameter is set to {}", name);
        }

        return String.format("Hello, %s!", safeName);
    }
}
