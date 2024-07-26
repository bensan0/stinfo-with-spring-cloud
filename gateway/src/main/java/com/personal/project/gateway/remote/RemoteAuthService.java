package com.personal.project.gateway.remote;

import com.personal.project.commoncore.response.InnerResponse;
import com.personal.project.gateway.model.dto.TokenDTO;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

public interface RemoteAuthService {

	Mono<InnerResponse> authToken(@RequestBody TokenDTO dto);
}
