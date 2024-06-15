package com.personal.project.scraperservice.remote;

import org.springframework.cloud.openfeign.FallbackFactory;

public class RemoteReportFallbackFactory  implements FallbackFactory<RemoteReportService> {
    @Override
    public RemoteReportService create(Throwable cause) {
        return null;
    }
}
