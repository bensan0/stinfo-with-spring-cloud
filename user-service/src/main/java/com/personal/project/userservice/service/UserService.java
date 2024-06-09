package com.personal.project.userservice.service;

import com.personal.project.userservice.exceptionhandler.exception.SqlModifyFailedException;
import com.personal.project.userservice.exceptionhandler.exception.UserNotFoundException;
import com.personal.project.userservice.exceptionhandler.exception.WrongArgsException;
import com.personal.project.userservice.model.dto.common.*;

public interface UserService {

    void add(UserInsertRequestDTO dto);

    UserResponseDTO update(UserUpdateRequestDTO dto);

    UserResponseDTO findOne(String username);

    UserDailyCommentDTO upsertDailyComment(UserDailyCommentRequestDTO dto) throws SqlModifyFailedException;

    void updatePassword(UserChangePasswordRequestDTO dto) throws UserNotFoundException, WrongArgsException;

    UserRegularCommentDTO upsertRegularComment(UserRegularCommentRequestDTO dto);


}
