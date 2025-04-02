package com.example.trade_vision_backend.strategies.internal;

import com.example.trade_vision_backend.processing.ProcessingDataService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SimpleMovingAverageServiceUnitTest {

    @Mock
    private ProcessingDataService processingDataService;

    @InjectMocks
    private SMAServiceImpl service;
}
