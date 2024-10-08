package com.personal.project.gateway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.personal.project.gateway.mapper.ConnectionInfoMapper;
import com.personal.project.gateway.model.entity.ConnectionInfoDO;
import com.personal.project.gateway.service.ConnectionInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConnectionInfoServiceImpl extends ServiceImpl<ConnectionInfoMapper, ConnectionInfoDO> implements ConnectionInfoService {
}
