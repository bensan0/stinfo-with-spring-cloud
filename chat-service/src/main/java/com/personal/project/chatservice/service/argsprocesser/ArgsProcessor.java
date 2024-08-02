package com.personal.project.chatservice.service.argsprocesser;

import java.util.List;
import java.util.Optional;

public interface ArgsProcessor {

	Optional<RemoteRequestDTO> process(List<String> args);
}
