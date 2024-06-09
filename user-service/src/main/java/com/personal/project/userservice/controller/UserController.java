package com.personal.project.userservice.controller;

import com.personal.project.commoncore.constants.ResponseCode;
import com.personal.project.commoncore.response.CommonResponse;
import com.personal.project.userservice.exceptionhandler.exception.SqlModifyFailedException;
import com.personal.project.userservice.exceptionhandler.exception.UserNotFoundException;
import com.personal.project.userservice.exceptionhandler.exception.WrongArgsException;
import com.personal.project.userservice.model.dto.common.*;
import com.personal.project.userservice.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/user")
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}/info")
    public CommonResponse getUser(
            @PathVariable("username")
            @NotBlank(message = "username cannot be null or blank")
            String username
    ) {
        UserResponseDTO dto = userService.findOne(username);

        return dto == null ? CommonResponse.error(ResponseCode.Not_Found, null) : CommonResponse.ok(dto);
    }

    @PostMapping("/add")
    public CommonResponse addUser(
            @Valid @RequestBody UserInsertRequestDTO dto
    ) {
        userService.add(dto);

        return CommonResponse.ok(null);
    }

    @PostMapping("/update")
    public CommonResponse updateUser(
            @RequestBody UserUpdateRequestDTO dto
    ) {
        UserResponseDTO updated = userService.update(dto);

        return updated == null ? CommonResponse.error(ResponseCode.Failed, null) : CommonResponse.ok(updated);
    }

    @PostMapping("/update_password")
    public CommonResponse updatePassword(
            @Valid @RequestBody UserChangePasswordRequestDTO dto
    ) throws UserNotFoundException, WrongArgsException {
        userService.updatePassword(dto);

        return CommonResponse.ok(null);
    }

    @PostMapping("/upsert_daily_comment")
    public CommonResponse upsertDailyComment(
            @Valid @RequestBody UserDailyCommentRequestDTO dto
    ) throws SqlModifyFailedException {
        UserDailyCommentDTO userDailyCommentDto = userService.upsertDailyComment(dto);

        return CommonResponse.ok(userDailyCommentDto);
    }

    @PostMapping("/upsert_regular_comment")
    public CommonResponse upsertRegularComment(
            @Valid @RequestBody UserRegularCommentRequestDTO dto
    ) {
        UserRegularCommentDTO userRegularCommentDto = userService.upsertRegularComment(dto);

        return CommonResponse.ok(userRegularCommentDto);
    }
}
