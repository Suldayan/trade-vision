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
        List<RawMarketModel> allModels = ingestionRepository.findAll();

        List<RawMarketModel> modelsWithNullTimestamps = allModels.stream()
                .filter(model -> model.getTimestamp() == null)
                .toList();

        if (!modelsWithNullTimestamps.isEmpty()) {
            log.warn("{} out of {} market models have null timestamps",
                    modelsWithNullTimestamps.size(), allModels.size());

            modelsWithNullTimestamps.forEach(model ->
                    log.warn("Null timestamp in model: id={}, exchangeId={}, baseSymbol={}, quoteSymbol={}",
                            model.getId(), model.getExchangeId(),
                            model.getBaseSymbol(), model.getQuoteSymbol())
            );
        }

        List<RawMarketModel> validModels = allModels.stream()
                .filter(model -> model.getTimestamp() != null)
                .toList();

        return validModels.stream()
                .map(this::convertData)
                .toList();
    }

    private ProcessableMarketDTO convertData(@Nonnull RawMarketModel model) {
        return mapper.INSTANCE.modelToDTO(model);
    }
}
