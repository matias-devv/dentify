package com.floss.odontologia.service.interfaces;

import com.floss.odontologia.dto.request.UserDTO;
import com.floss.odontologia.model.User;

public interface IUserService {

        //create
        public String createUser(UserDTO userDTO);

        public String validateUser(User user);

}
