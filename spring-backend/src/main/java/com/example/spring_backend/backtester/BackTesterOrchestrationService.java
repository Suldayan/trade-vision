package com.example.spring_backend.backtester;

import com.example.spring_backend.backtester.internal.BackTestResult;
import com.example.spring_backend.common.BackTestRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BackTesterOrchestrationService {
    CompletableFuture<List<BackTestResult>> runOrchestration(MultipartFile file, List<BackTestRequest> requests);
}
