package com.example.trade_vision_backend.ingestion.internal.infrastructure.mapper;

import com.example.trade_vision_backend.ingestion.ProcessableMarketDTO;
import com.example.trade_vision_backend.ingestion.market.RawMarketModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngestionMapper {
    IngestionMapper INSTANCE = Mappers.getMapper(IngestionMapper.class);

    ProcessableMarketDTO modelToDTO(RawMarketModel model);
    List<ProcessableMarketDTO> modelListToDtoList(List<RawMarketModel> models);
}
