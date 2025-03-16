package com.example.trade_vision_backend.ingestion.market.domain.mapper;

import com.example.trade_vision_backend.ingestion.market.RawMarketDTO;
import com.example.trade_vision_backend.ingestion.market.RawMarketModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface RawMarketMapper {
    RawMarketMapper INSTANCE = Mappers.getMapper(RawMarketMapper.class);

    RawMarketModel dtoToEntity(RawMarketDTO dto);
    List<RawMarketModel> dtoSetToEntitySet(Set<RawMarketDTO> marketDTOS);
}
