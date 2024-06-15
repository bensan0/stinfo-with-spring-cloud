package com.personal.project.scraperservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.personal.project.scraperservice.mapper.ScraperErrorMessageMapper;
import com.personal.project.scraperservice.model.entity.ScraperErrorMessageDO;
import com.personal.project.scraperservice.service.ErrorMessageService;
import org.springframework.stereotype.Service;

@Service
public class ErrorMessageServiceImpl extends ServiceImpl<ScraperErrorMessageMapper, ScraperErrorMessageDO> implements ErrorMessageService {
}
