package com.personal.project.authservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.personal.project.authservice.model.dto.ChangePasswordRequestDTO;
import com.personal.project.authservice.model.dto.UserDTO;
import com.personal.project.authservice.model.dto.UserSignUpRequestDTO;
import com.personal.project.authservice.model.entity.UserDO;


public interface UserService extends IService<UserDO> {

	void signUp(UserSignUpRequestDTO dto);

	String signIn(UserSignUpRequestDTO dto);

	Boolean checkToken(String token);

	Boolean changePassword(ChangePasswordRequestDTO dto);

	UserDTO getByName(String username);
}
