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

/**
 * 사용자 OAuth2 인증을 처리하는 서비스 클래스.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final GoogleJoinService googleJoinService;

    /**
     * OAuth2 사용자 정보를 로드하고, 사용자의 정보를 기반으로 데이터베이스에 사용자 정보를 저장합니다.
     *
     * @param userRequest OAuth2 사용자 요청 정보
     * @return 인증된 OAuth2 사용자
     * @throws OAuth2AuthenticationException 인증 예외 발생 시
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("Authenticated Google User: {}", oAuth2User.getAttributes());

        OAuth2DTO oAuth2DTO = new GoogleOAuth2DTO(oAuth2User.getAttributes());

        String userId = oAuth2DTO.getId();
        String nickName = oAuth2DTO.getName();

        JoinGoogleUserDTO joinGoogleUserDTO = new JoinGoogleUserDTO(userId, nickName, "ROLE_USER");

        // 사용자가 데이터베이스에 존재하지 않으면 신규 사용자로 가입 처리
        if (!userRepository.existsById(userId)) {
            googleJoinService.joinGoogleProcess(joinGoogleUserDTO);
        } else {
            // 이미 존재하는 사용자 정보 업데이트
            googleJoinService.updateGoogleUser(joinGoogleUserDTO);
        }

        return new CustomOAuth2User(joinGoogleUserDTO);
    }
}
