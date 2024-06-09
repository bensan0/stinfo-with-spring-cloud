package com.personal.project.reportservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.personal.project.reportservice.mapper.ReportErrorMessageMapper;
import com.personal.project.reportservice.model.entity.ReportErrorMessageDO;
import com.personal.project.reportservice.service.ReportErrorMessageService;
import org.springframework.stereotype.Service;

@Service
public class ReportErrorMessageServiceImpl extends ServiceImpl<ReportErrorMessageMapper, ReportErrorMessageDO> implements ReportErrorMessageService {
}
