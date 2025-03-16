package com.example.trade_vision_backend.ingestion.internal.infrastructure.service;

import com.example.trade_vision_backend.ingestion.IngestionDataService;
import com.example.trade_vision_backend.ingestion.ProcessableMarketDTO;
import com.example.trade_vision_backend.ingestion.internal.infrastructure.mapper.IngestionMapper;
import com.example.trade_vision_backend.ingestion.internal.infrastructure.repository.IngestionRepository;
import com.example.trade_vision_backend.ingestion.market.RawMarketModel;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionDataServiceImpl implements IngestionDataService {
    private final IngestionRepository ingestionRepository;
    private final IngestionMapper mapper;

    @Override
    public List<ProcessableMarketDTO> getAllData() {
        return ingestionRepository.findAll()
                .stream()
                .map(this::convertData)
                .toList();
    }

    private ProcessableMarketDTO convertData(@Nonnull RawMarketModel models) {
        return mapper.INSTANCE.modelToDTO(models);
    }
}
