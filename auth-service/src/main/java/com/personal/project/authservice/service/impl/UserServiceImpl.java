package com.personal.project.authservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.personal.project.authservice.constant.UserStatus;
import com.personal.project.authservice.exception.DuplicatedUsernameException;
import com.personal.project.authservice.exception.UserNotFoundException;
import com.personal.project.authservice.exception.WrongPasswordException;
import com.personal.project.authservice.mapper.UserMapper;
import com.personal.project.authservice.model.dto.UserCacheDTO;
import com.personal.project.authservice.model.dto.UserDTO;
import com.personal.project.authservice.model.dto.UserSignUpRequestDTO;
import com.personal.project.authservice.model.entity.UserDO;
import com.personal.project.authservice.service.CacheService;
import com.personal.project.authservice.service.UserService;
import com.personal.project.authservice.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

	private final CacheService cacheService;

	public UserServiceImpl(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	@Override
	public void signUp(UserSignUpRequestDTO dto) {
		LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(UserDO::getUsername, dto.getUsername());
		Long count = baseMapper.selectCount(wrapper);

		if (count == 0) {
			UserDO userDO = BeanUtil.copyProperties(dto, UserDO.class);
			userDO.setSalt(RandomUtil.randomString(16));
			userDO.setPassword(PasswordUtil.generateUserPassword(dto.getPassword(), userDO.getSalt()));
			save(userDO);
		} else {
			throw new DuplicatedUsernameException();
		}
	}

	@Override
	public String signIn(UserSignUpRequestDTO dto) {
		//check
		LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(UserDO::getUsername, dto.getUsername())
				.ne(UserDO::getStatus, UserStatus.Disable.getStatus());
		UserDO userDO = baseMapper.selectOne(wrapper);

		if (userDO == null) {
			throw new UserNotFoundException();
		} else if (!userDO.getPassword().equals(PasswordUtil.generateUserPassword(dto.getPassword(), userDO.getSalt()))) {
			throw new WrongPasswordException();
		} else if (userDO.getStatus() == UserStatus.Reviewing) {
			throw new UserNotFoundException("用戶審核中");
		}

		//token
		String token = IdUtil.getSnowflake(1, 1).nextIdStr();

		cacheService.setCache(token, JSONUtil.toJsonStr(BeanUtil.copyProperties(userDO, UserCacheDTO.class)), Duration.of(24, ChronoUnit.HOURS));

		return token;
	}

	@Override
	public UserDTO getByName(String username) {
		LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(UserDO::getUsername, username)
				.eq(UserDO::getStatus, UserStatus.Enable);
		UserDO userDO = baseMapper.selectOne(wrapper);

		return BeanUtil.copyProperties(userDO, UserDTO.class);
	}
}
