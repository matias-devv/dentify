package com.floss.odontologia.service.impl;

import com.floss.odontologia.model.ResponsibleAdult;
import com.floss.odontologia.repository.IReponsibleRepository;
import com.floss.odontologia.service.interfaces.IResponsibleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResponsibleService implements IResponsibleService {

    @Autowired
    private IReponsibleRepository iResponsibleRepository;

    @Override
    public String createResponsible(ResponsibleAdult responsible) {
        iResponsibleRepository.save(responsible);
        return "The responsible adult was saved successfully";
    }
}
