package com.dentify.domain.userProfile.service;

import com.dentify.domain.userProfile.dto.request.UpdateUserProfileRequest;
import com.dentify.domain.userProfile.dto.response.UserProfileResponse;
import com.dentify.domain.userProfile.dto.response.UserSummaryResponse;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.security.dto.request.RegisterUserRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface IUserService {

    public boolean saveUser(RegisterUserRequest request);

    public UserProfile findUserAppEntityById(Long id);

    public UserProfile validateIfUserExists(Long id_app_user);

    UserProfileResponse findMe(UserDetails userDetails);

    UserProfileResponse updateUserApp(UserDetails userDetails, @Valid UpdateUserProfileRequest request);

    List<UserSummaryResponse> getAllAppUserSummaryFromDentists();

    String deactiveAppUser(Long id);

    public UserProfile findByIdWithAuthUser(Long id);
}
