package com.personal.project.scraperservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.personal.project.scraperservice.model.entity.ScraperErrorMessageDO;

public interface ErrorMessageService extends IService<ScraperErrorMessageDO> {

    void insert();
}
