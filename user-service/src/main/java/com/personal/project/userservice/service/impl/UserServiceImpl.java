package com.personal.project.userservice.service.impl;

import com.personal.project.userservice.constant.RoleEnum;
import com.personal.project.userservice.exceptionhandler.exception.SqlModifyFailedException;
import com.personal.project.userservice.exceptionhandler.exception.UserNotFoundException;
import com.personal.project.userservice.exceptionhandler.exception.WrongArgsException;
import com.personal.project.userservice.model.dto.common.*;
import com.personal.project.userservice.model.entity.User;
import com.personal.project.userservice.model.entity.UserDailyCommentDO;
import com.personal.project.userservice.model.entity.UserRegularCommentDO;
import com.personal.project.userservice.repository.dao.UserDao;
import com.personal.project.userservice.service.RoleService;
import com.personal.project.userservice.service.UserDailyCommentService;
import com.personal.project.userservice.service.UserRegularCommentService;
import com.personal.project.userservice.service.UserService;
import com.personal.project.userservice.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    private final RoleService roleService;

    private final UserDailyCommentService userDailyCommentService;

    private final UserRegularCommentService userRegularCommentService;

    public UserServiceImpl(
            UserDao userDao,
            RoleService roleService,
            UserDailyCommentService userDailyCommentService,
            UserRegularCommentService userRegularCommentService
    ) {
        this.userDao = userDao;
        this.roleService = roleService;
        this.userDailyCommentService = userDailyCommentService;
        this.userRegularCommentService = userRegularCommentService;
    }

    @Override
    public UserResponseDTO findOne(String username) {
        User user = userDao.findOne(username).orElseThrow(UserNotFoundException::new);
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                null,
                user.getRoleDO()
        );
    }

    @Override
    public UserDailyCommentDTO upsertDailyComment(UserDailyCommentRequestDTO dto) throws SqlModifyFailedException {
        UserDailyCommentDTO res = new UserDailyCommentDTO(dto.getId(), dto.getUserId(), dto.getComment());

        AtomicBoolean success = new AtomicBoolean(false);

        userDailyCommentService.findOne(dto.getUserId(), dto.getCreated()).ifPresentOrElse(
                c -> {
                    c.setComment(dto.getComment());
                    UserDailyCommentDO result = userDailyCommentService.insert(c);
                    if (result != null) {
                        success.set(true);
                    } else {
                        success.set(false);
                    }
                },
                () -> {
                    boolean result = userDailyCommentService.insert(dto.getUserId(), dto.getComment(), dto.getCreated());
                    success.set(result);
                }
        );

        if (!success.getPlain()) {
            throw new SqlModifyFailedException("upsert user daily comment failed");
        }

        return res;
    }

    @Override
    public void updatePassword(UserChangePasswordRequestDTO dto) throws UserNotFoundException, WrongArgsException {
        User user = userDao.findOne(dto.getUserId()).orElseThrow(UserNotFoundException::new);
        //check former
        if (!user.getPassword().equals(PasswordUtil.generateUserPassword(dto.getFormerPassword(), user.getSalt()))) {
            throw new WrongArgsException("Former password not correct");
        }

        user.setPassword(PasswordUtil.generateUserPassword(dto.getNewPassword(), user.getSalt()));
        user.setUpdatedAt(LocalDateTime.now());
        userDao.upsert(user);
    }

    @Override
    public UserRegularCommentDTO upsertRegularComment(UserRegularCommentRequestDTO dto) {
        UserRegularCommentDO regularComment = userRegularCommentService.findOneById(dto.getId())
                .orElse(UserRegularCommentDO.of(dto.getComment(), dto.getUserId()));
        regularComment.setComment(dto.getComment());
        UserRegularCommentDO inserted = userRegularCommentService.upsert(regularComment);

        return new UserRegularCommentDTO(inserted.getId(), inserted.getComment());
    }

    @Override
    public void add(UserInsertRequestDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setRoleDO(dto.getRoleDO());
        Pair<String, String> pair = PasswordUtil.generateUserPassword(user.getPassword());
        user.setSalt(pair.getLeft());
        user.setPassword(pair.getRight());
        if (user.getRoleDO() == null) {
            user.setRoleDO(roleService.findOne(RoleEnum.Member.name()));
        }

        userDao.upsert(user);
    }

    @Override
    public UserResponseDTO update(UserUpdateRequestDTO dto) {
        User user = userDao.findOne(dto.getUserId()).orElse(null);
        if (user != null) {
            user.setRoleDO(dto.getRoleDO() == null ? user.getRoleDO() : dto.getRoleDO());
            User saved = userDao.upsert(user);

            return new UserResponseDTO(
                    saved.getId(),
                    saved.getUsername(),
                    saved.getPassword(),
                    saved.getRoleDO()
            );
        }

        return null;
    }
}
