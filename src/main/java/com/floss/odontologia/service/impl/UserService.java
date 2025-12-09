package com.floss.odontologia.service.impl;

import com.floss.odontologia.model.User;
import com.floss.odontologia.repository.IUserRepository;
import com.floss.odontologia.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepository iUserRepository;

    @Override
    public String createUser(User user) {
        iUserRepository.save(user);
        return "user created";
    }
}
