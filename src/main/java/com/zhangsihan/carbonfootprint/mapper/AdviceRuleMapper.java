package com.zhangsihan.carbonfootprint.mapper;

import com.zhangsihan.carbonfootprint.entity.AdviceRule;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdviceRuleMapper {
    List<AdviceRule> findAll();
}
