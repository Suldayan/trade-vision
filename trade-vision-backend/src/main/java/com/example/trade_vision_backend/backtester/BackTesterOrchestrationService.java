package com.example.trade_vision_backend.backtester;

import com.example.trade_vision_backend.backtester.internal.BackTestResult;
import com.example.trade_vision_backend.common.BackTestRequest;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BackTesterOrchestrationService {
    CompletableFuture<List<BackTestResult>> runOrchestration(MultipartFile file, List<BackTestRequest> requests);
}
