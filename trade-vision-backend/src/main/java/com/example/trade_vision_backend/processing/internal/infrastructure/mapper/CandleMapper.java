package com.example.trade_vision_backend.processing.internal.infrastructure.mapper;

import com.example.trade_vision_backend.processing.CandleDTO;
import com.example.trade_vision_backend.processing.CandleModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CandleMapper {
    CandleMapper INSTANCE = Mappers.getMapper(CandleMapper.class);

    CandleDTO entityToDTO(CandleModel model);
    List<CandleDTO> entityListToDTOList(List<CandleModel> models);
}
