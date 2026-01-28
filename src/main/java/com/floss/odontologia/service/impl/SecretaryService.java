package com.floss.odontologia.service.impl;

import com.floss.odontologia.repository.ISecretaryRepository;
import com.floss.odontologia.service.interfaces.ISecretaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecretaryService implements ISecretaryService {

    @Autowired
    private ISecretaryRepository iSecretaryRepository;

    @Override
    public String createSecretary(Secretary secretary) {
        iSecretaryRepository.save(secretary);
        return "The secretary was saved successfully";
    }

    @Override
    public String editSecretary(Secretary secretary) {
        iSecretaryRepository.save(secretary);
        return "The secretary was updated successfully";
    }
}
