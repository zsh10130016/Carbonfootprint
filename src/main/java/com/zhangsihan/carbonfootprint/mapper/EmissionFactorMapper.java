package com.zhangsihan.carbonfootprint.mapper;

import com.zhangsihan.carbonfootprint.entity.EmissionFactor;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmissionFactorMapper {
    EmissionFactor findByKey(@Param("activityType") String activityType,
                             @Param("subType") String subType,
                             @Param("unit") String unit);

    List<EmissionFactor> findAll();
}
