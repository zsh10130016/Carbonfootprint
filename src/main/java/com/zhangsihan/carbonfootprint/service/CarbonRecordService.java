package com.zhangsihan.carbonfootprint.service;

import com.zhangsihan.carbonfootprint.common.BusinessException;
import com.zhangsihan.carbonfootprint.common.ErrorCode;
import com.zhangsihan.carbonfootprint.dto.CarbonRecordCreateRequest;
import com.zhangsihan.carbonfootprint.dto.CarbonRecordQueryRequest;
import com.zhangsihan.carbonfootprint.entity.CarbonRecord;
import com.zhangsihan.carbonfootprint.entity.EmissionFactor;
import com.zhangsihan.carbonfootprint.entity.PointsLedger;
import com.zhangsihan.carbonfootprint.enums.ActivityType;
import com.zhangsihan.carbonfootprint.mapper.CarbonRecordMapper;
import com.zhangsihan.carbonfootprint.mapper.EmissionFactorMapper;
import com.zhangsihan.carbonfootprint.mapper.PointsLedgerMapper;
import com.zhangsihan.carbonfootprint.security.SecurityUtils;
import com.zhangsihan.carbonfootprint.vo.CarbonRecordVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarbonRecordService {

    private final CarbonRecordMapper carbonRecordMapper;
    private final EmissionFactorMapper emissionFactorMapper;
    private final PointsLedgerMapper pointsLedgerMapper;

    @Transactional
    public CarbonRecordVO createRecord(CarbonRecordCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        CarbonRecord record = buildRecord(null, userId, request);
        carbonRecordMapper.insert(record);
        syncPointsLedger(record);
        return toVO(record);
    }

    @Transactional
    public CarbonRecordVO updateRecord(Long id, CarbonRecordCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        getOwnedRecord(id, userId);
        CarbonRecord updated = buildRecord(id, userId, request);
        carbonRecordMapper.updateByIdAndUserId(updated);
        pointsLedgerMapper.deleteBySourceRecordIdAndUserId(id, userId);
        syncPointsLedger(updated);
        return toVO(carbonRecordMapper.findByIdAndUserId(id, userId));
    }

    @Transactional
    public void deleteRecord(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        getOwnedRecord(id, userId);
        pointsLedgerMapper.deleteBySourceRecordIdAndUserId(id, userId);
        carbonRecordMapper.deleteByIdAndUserId(id, userId);
    }

    public CarbonRecordVO getRecord(Long id) {
        return toVO(getOwnedRecord(id, SecurityUtils.getCurrentUserId()));
    }

    public List<CarbonRecordVO> listRecords(CarbonRecordQueryRequest queryRequest) {
        return carbonRecordMapper.findByCondition(SecurityUtils.getCurrentUserId(), queryRequest)
                .stream()
                .map(this::toVO)
                .toList();
    }

    private CarbonRecord buildRecord(Long id, Long userId, CarbonRecordCreateRequest request) {
        String activityType = ActivityType.from(request.getActivityType()).name();
        String subType = request.getSubType().trim().toUpperCase();
        String unit = request.getUnit().trim();
        EmissionFactor factor = emissionFactorMapper.findByKey(activityType, subType, unit);
        if (factor == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "未找到对应的排放因子，请检查活动类型、子类型和单位");
        }
        BigDecimal emissionKg = request.getAmount()
                .multiply(factor.getFactorValue())
                .setScale(2, RoundingMode.HALF_UP);
        CarbonRecord record = new CarbonRecord();
        record.setId(id);
        record.setUserId(userId);
        record.setActivityType(activityType);
        record.setSubType(subType);
        record.setAmount(request.getAmount().setScale(2, RoundingMode.HALF_UP));
        record.setUnit(unit);
        record.setEmissionFactorValue(factor.getFactorValue());
        record.setEmissionKg(emissionKg);
        record.setPoints(calculatePoints(activityType, subType, emissionKg));
        record.setNote(request.getNote());
        record.setOccurredAt(request.getOccurredAt());
        return record;
    }

    private int calculatePoints(String activityType, String subType, BigDecimal emissionKg) {
        // Use a simple demo-friendly rule: low-carbon behaviors start with higher base points,
        // then subtract points as the actual emission grows.
        int base = switch (activityType) {
            case "TRANSPORT" -> switch (subType) {
                case "WALK" -> 20;
                case "BIKE" -> 18;
                case "SUBWAY" -> 16;
                case "TRAIN" -> 15;
                case "BUS" -> 14;
                case "TAXI" -> 6;
                default -> 10;
            };
            case "HOME_ENERGY" -> 18;
            case "FOOD" -> switch (subType) {
                case "VEGETABLE" -> 16;
                case "CHICKEN" -> 11;
                case "DAIRY" -> 9;
                case "PORK" -> 6;
                case "BEEF" -> 3;
                default -> 8;
            };
            default -> 10;
        };
        return Math.max(0, base - emissionKg.setScale(0, RoundingMode.HALF_UP).intValue());
    }

    private void syncPointsLedger(CarbonRecord record) {
        PointsLedger ledger = new PointsLedger();
        ledger.setUserId(record.getUserId());
        ledger.setSourceRecordId(record.getId());
        ledger.setPoints(record.getPoints());
        ledger.setReason("记录碳足迹获得积分");
        pointsLedgerMapper.insert(ledger);
    }

    private CarbonRecord getOwnedRecord(Long id, Long userId) {
        CarbonRecord record = carbonRecordMapper.findByIdAndUserId(id, userId);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "记录不存在");
        }
        return record;
    }

    private CarbonRecordVO toVO(CarbonRecord record) {
        ActivityType activityType = ActivityType.from(record.getActivityType());
        return CarbonRecordVO.builder()
                .id(record.getId())
                .activityType(record.getActivityType())
                .activityLabel(activityType.getLabel())
                .subType(record.getSubType())
                .amount(record.getAmount())
                .unit(record.getUnit())
                .emissionFactorValue(record.getEmissionFactorValue())
                .emissionKg(record.getEmissionKg())
                .points(record.getPoints())
                .note(record.getNote())
                .occurredAt(record.getOccurredAt())
                .build();
    }
}
