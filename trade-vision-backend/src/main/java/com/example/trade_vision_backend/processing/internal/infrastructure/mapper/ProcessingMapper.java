package com.example.trade_vision_backend.processing.internal.infrastructure.mapper;

import com.example.trade_vision_backend.processing.ProcessedMarketDTO;
import com.example.trade_vision_backend.processing.ProcessedMarketModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProcessingMapper {
    ProcessingMapper INSTANCE = Mappers.getMapper(ProcessingMapper.class);

    ProcessedMarketDTO entityToDto(ProcessedMarketModel processedMarketModel);

    List<ProcessedMarketDTO> entityListToDtoList(List<ProcessedMarketModel> processedMarketModels);
}
