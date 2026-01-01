package com.example.mj_craft.service;

import com.example.mj_craft.domain.Role;
import com.example.mj_craft.domain.User;
import com.example.mj_craft.dto.UserDTO;
import com.example.mj_craft.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public UserDTO signup(UserDTO userDTO) {
        User user;

        // 1. 중복 체크
        try {
            if (userRepository.existsByUserId(userDTO.getUserId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicated userId");
            }

            // 2. 암호화
            String encoded = passwordEncoder.encode(userDTO.getPassword());

            // 3, repository 저장
            // 일단 무조건 user로 저장하고 있음.. 어떻게 role / user를 이용할지 잘 생각해봐야 함.
            user = new User(userDTO.getUserId(), userDTO.getUserName(), userDTO.getEmail(), encoded, Role.USER);

            userRepository.save(user);
        } catch (DataIntegrityViolationException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicated User Id");
        }
        // 4. return UserDTO : 변경 사항(role)이 있음으로 다시 return 해야 함.
        // return시에 password는 암호화 되지 않은걸로 return하자.

        UserDTO savedUserDTO = new UserDTO();
        savedUserDTO.setUserName(user.getUserName());
        savedUserDTO.setId(user.getId());
        savedUserDTO.setEmail(user.getEmail());
        savedUserDTO.setRole(user.getRole()); // service에서 저장한 role return

        return savedUserDTO;
    }
}
