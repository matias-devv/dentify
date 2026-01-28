package com.floss.odontologia.controller;

import com.floss.odontologia.service.interfaces.ISecretaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/secretary")
public class SecretaryController {

    @Autowired
    private ISecretaryService iSecretaryService;

    @PutMapping("/edit")
    public String editSecretary(@RequestBody Secretary secretary) {
        return iSecretaryService.editSecretary(secretary);
    }
}
