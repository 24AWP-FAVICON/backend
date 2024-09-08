package com.example.demo.service.users.user;

import com.example.demo.repository.users.user.UserRepository;
import com.example.demo.dto.users.user.JoinGoogleUserDTO;
import com.example.demo.dto.users.user.CustomOAuth2User;
import com.example.demo.dto.users.user.GoogleOAuth2DTO;
import com.example.demo.dto.users.user.OAuth2DTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final GoogleJoinService googleJoinService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("Authenticated Google User: {}", oAuth2User.getAttributes());

        OAuth2DTO oAuth2DTO = new GoogleOAuth2DTO(oAuth2User.getAttributes());

        String userId = oAuth2DTO.getId();
        String nickName = oAuth2DTO.getName();

        JoinGoogleUserDTO joinGoogleUserDTO = new JoinGoogleUserDTO(userId, nickName, "ROLE_USER");


        if(!userRepository.existsById(userId)) {
            googleJoinService.joinGoogleProcess(joinGoogleUserDTO);
        } else {
            googleJoinService.updateGoogleUser(joinGoogleUserDTO);
        }

        return new CustomOAuth2User(joinGoogleUserDTO);
    }


}