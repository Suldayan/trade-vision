package com.example.trade_vision_backend.processing.internal.infrastructure.mapper;

import com.example.trade_vision_backend.ingestion.ProcessableMarketDTO;
import com.example.trade_vision_backend.ingestion.market.RawMarketModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ProcessingMapper {
    ProcessingMapper INSTANCE = Mappers.getMapper(ProcessingMapper.class);

    Set<ProcessableMarketDTO> listToSet(List<ProcessableMarketDTO> models);
}
