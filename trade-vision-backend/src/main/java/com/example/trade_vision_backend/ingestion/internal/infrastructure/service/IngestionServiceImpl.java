package com.example.trade_vision_backend.ingestion.internal.infrastructure.service;

import com.example.trade_vision_backend.ingestion.IngestionManagement;
import com.example.trade_vision_backend.ingestion.internal.infrastructure.exception.IngestionException;
import com.example.trade_vision_backend.ingestion.market.MarketService;
import com.example.trade_vision_backend.ingestion.market.domain.dto.MarketWrapperDTO;
import com.example.trade_vision_backend.ingestion.market.RawMarketDTO;
import com.example.trade_vision_backend.ingestion.market.RawMarketModel;
import com.example.trade_vision_backend.ingestion.internal.infrastructure.repository.IngestionRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionServiceImpl implements IngestionService {
    private final MarketService marketService;
    private final IngestionRepository ingestionRepository;
    private final IngestionManagement ingestionManagement;

    @Override
    public void sendEvent(@Nonnull List<RawMarketDTO> marketDTOS) {
        ingestionManagement.processFieldsForEvent(marketDTOS);
    }

    @Override
    @Transactional
    public void executeIngestion() {
        try {
            MarketWrapperDTO marketWrapper = marketService.getMarketsData();
            List<RawMarketDTO> rawMarketDTOS = marketService.convertWrapperDataToRecord(marketWrapper);
            List<RawMarketModel> rawMarketModels = marketService.rawMarketDTOToModel(rawMarketDTOS);
            saveMarketData(rawMarketModels);
            sendEvent(rawMarketDTOS);
            log.info("Successfully completed market ingestion flow");
        } catch (Exception ex) {
            throw new RestClientException("Market ingestion failed due to unexpected error", ex);
        }
    }

    // TODO configure a retry
    @Transactional
    @Override
    public void saveMarketData(@Nonnull List<RawMarketModel> latestFetchedData) throws IngestionException {
        try {
            List<RawMarketModel> repositoryModels = ingestionRepository.findAll();
            if (repositoryModels.isEmpty()) {
                log.info("No data available to update. Saved all models successfully");
                ingestionRepository.saveAll(latestFetchedData);
                return;
            }

            Map<List<String>, RawMarketModel> latestDataMap = createDataMap(latestFetchedData);

            List<RawMarketModel> modelsToDelete = repositoryModels.stream()
                    .filter(model -> !latestDataMap.containsKey(
                            List.of(
                            model.getBaseId(),
                            model.getQuoteId(),
                            model.getExchangeId())))
                    .collect(Collectors.toList());

            if (!modelsToDelete.isEmpty()) {
                ingestionRepository.deleteAll(modelsToDelete);
                log.info("Deleted {} obsolete models", modelsToDelete.size());
            }

            List<RawMarketModel> updatedData = createNewUpdatedModelSet(latestFetchedData, repositoryModels);
            ingestionRepository.saveAll(updatedData);
            log.info("Successfully updated and saved all data entries");
        } catch (DataAccessException ex) {
            throw new IngestionException("Database error occurred while saving market data", ex);
        } catch (Exception ex) {
            throw new IngestionException("An unexpected error occurred while saving raw market data", ex);
        }
    }

    @Nonnull
    private static <T extends RawMarketModel> Map<List<String>, T> createDataMap(@Nonnull Collection<T> data) {
        return data.stream()
                .collect(Collectors.toMap(
                        model -> List.of(
                                model.getBaseId(),
                                model.getQuoteId(),
                                model.getExchangeId()),
                        model -> model,
                        (existing, replacement) -> replacement
                ));
    }

    @Nonnull
    private List<RawMarketModel> createNewUpdatedModelSet(
            @Nonnull List<RawMarketModel> latestFetchedData,
            @Nonnull List<RawMarketModel> repositoryModels
    ) {
        Map<List<String>, RawMarketModel> mapForLatestData = createDataMap(latestFetchedData);
        Map<List<String>, RawMarketModel> mapForRepositoryData = createDataMap(repositoryModels);
        List<RawMarketModel> updatedData = new ArrayList<>();

        for (Map.Entry<List<String>, RawMarketModel> entry : mapForLatestData.entrySet()) {
            List<String> ids = entry.getKey();
            RawMarketModel latestModel = entry.getValue();

            if (mapForRepositoryData.containsKey(ids)) {
                RawMarketModel existingModel = mapForRepositoryData.get(ids);
                updateModelFields(existingModel, latestModel);
                updatedData.add(existingModel);
            } else {
                updatedData.add(latestModel);
            }
        }

        return updatedData;
    }

    private void updateModelFields (
            @Nonnull RawMarketModel existingModel,
            @Nonnull RawMarketModel newModel
    ) {
        existingModel.setRank(newModel.getRank());
        existingModel.setPriceQuote(newModel.getPriceQuote());
        existingModel.setPriceUsd(newModel.getPriceUsd());
        existingModel.setVolumeUsd24Hr(newModel.getVolumeUsd24Hr());
        existingModel.setPercentExchangeVolume(newModel.getPercentExchangeVolume());
        existingModel.setTradesCount24Hr(newModel.getTradesCount24Hr());
        existingModel.setUpdated(newModel.getUpdated());
        existingModel.setTimestamp(newModel.getTimestamp());
    }
}