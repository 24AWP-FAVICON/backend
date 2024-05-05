package com.example.demo.service;

import com.example.demo.repository.UserRepository;
import com.example.demo.dto.oauth2.JoinGoogleUserDTO;
import com.example.demo.dto.oauth2.CustomOAuth2User;
import com.example.demo.dto.oauth2.GoogleOAuth2DTO;
import com.example.demo.dto.oauth2.OAuth2DTO;
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

        log.info("google user: {}", oAuth2User.getAttributes());

        OAuth2DTO oAuth2DTO = new GoogleOAuth2DTO(oAuth2User.getAttributes());

        String googleId = oAuth2DTO.getId();
        String nickName = oAuth2DTO.getName();

        JoinGoogleUserDTO joinGoogleUserDTO = new JoinGoogleUserDTO(googleId, nickName, "ROLE_USER");


        if(!userRepository.existsById(googleId)) {
            googleJoinService.joinGoogleProcess(joinGoogleUserDTO);
        } else {
            googleJoinService.updateGoogleUser(joinGoogleUserDTO);
        }

        return new CustomOAuth2User(joinGoogleUserDTO);
    }


}