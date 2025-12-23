package lab.zhang.data_science.metrics_mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lab.zhang.data_science.metrics_mall.config.MetricVersionLifeStatusConfig;
import lab.zhang.data_science.metrics_mall.enums.LifeStatusEnum;
import lab.zhang.data_science.metrics_mall.mapper.MetricVersionMapper;
import lab.zhang.data_science.metrics_mall.model.MetricVersion;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.metric_version.MetricVersionDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricVersionDTO;
import lab.zhang.data_science.metrics_mall.service.MetricService;
import lab.zhang.data_science.metrics_mall.service.MetricVersionService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricVersionStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Metric version service implementation.
 *
 * @author Rongjin Zhang
 * @date 2025-12-19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VersionServiceImpl implements MetricVersionService {

    @Autowired
    private MetricVersionLifeStatusConfig lifeStatusConfig;

    @Autowired
    private MetricService metricService;

    @Autowired
    private MetricVersionMapper metricVersionMapper;

    @Autowired
    private MetricVersionStructMapper metricVersionStructMapper;


    @Override
    public MetricVersion get(Long id) {
        if (id == null) {
            return null;
        }
        MetricVersionDAO dao = metricVersionMapper.selectById(id);
        return metricVersionStructMapper.daoToModel(dao);
    }


    @Override
    public MetricVersion getByMetricIdAndVersion(Long metricId, Integer version) {
        if (metricId == null || version == null) {
            throw new IllegalArgumentException("metric code or version is null");
        }

        LambdaQueryWrapper<MetricVersionDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MetricVersionDAO::getMetricId, metricId);
        queryWrapper.eq(MetricVersionDAO::getVersion, version);
        MetricVersionDAO dao = metricVersionMapper.selectOne(queryWrapper);
        return metricVersionStructMapper.daoToModel(dao);
    }

    @Override
    public List<MetricVersion> list(MetricVersionDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("[metric_version] list failed, dto is null");
        }
        // validate metric meta existence
        MetricMetaDAO metricMetaDAO = metricService.getMetricMetaDaoByCode(dto.getMetricCode());
        if (metricMetaDAO == null) {
            throw new IllegalArgumentException("[metric_version] list failed, metric meta not found: metricCode=" + dto.getMetricCode());
        }

        // query
        LambdaQueryWrapper<MetricVersionDAO> queryWrapper = new LambdaQueryWrapper<>();
        if (dto.getMetricId() != null) {
            queryWrapper.eq(MetricVersionDAO::getMetricId, dto.getMetricId());
        }
        if (dto.getVersion() != null) {
            queryWrapper.eq(MetricVersionDAO::getVersion, dto.getVersion());
        }
        if (dto.getIsMain() != null) {
            queryWrapper.eq(MetricVersionDAO::getIsMain, dto.getIsMain());
        }
        if (dto.getLifeStatus() != null) {
            queryWrapper.eq(MetricVersionDAO::getLifeStatus, dto.getLifeStatus());
        }
        List<MetricVersionDAO> daoList = metricVersionMapper.selectList(queryWrapper);

        return metricVersionStructMapper.daoToModelBatch(daoList);
    }


    @Override
    public boolean insert(MetricVersionDTO dto) {
        // validate input
        if (dto == null) {
            throw new IllegalArgumentException("[metric_version] creating failed, dto to insert is null");
        }
        // validate metric meta existence
        MetricMetaDAO metricMetaDAO = metricService.getMetricMetaDaoByCode(dto.getMetricCode());
        if (metricMetaDAO == null) {
            throw new IllegalArgumentException("[metric_version] creating failed, metric meta not found: metricCode=" + dto.getMetricCode());
        }
        Long metricMetaId = metricMetaDAO.getId();
        if (metricMetaId == null) {
            throw new IllegalStateException("[metric_version] creating failed, metric meta id is null");
        }

        // validate version existence
        MetricVersion existingModel = getByMetricIdAndVersion(metricMetaId, dto.getVersion());
        if (existingModel != null) {
            throw new IllegalStateException("[metric_version] creating failed, metric version existing, version=" + existingModel.getVersion());
        }
        // validate isMain
        if (dto.getIsMain() != null) {
            if (dto.getIsMain() != 0) {
                throw new IllegalStateException("[metric_version] creating failed, creating version cannot be main, version=" + dto.getVersion());
            }
        } else {
            dto.setIsMain(0);
        }
        // validate life status
        if (dto.getLifeStatus() != null) {
            if (dto.getLifeStatus() != LifeStatusEnum.DEV) {
                throw new IllegalStateException(String.format("[metric_version] creating failed, invalid version life status. Acceptable status=%s(%d), but you give=%s(%d)",
                        LifeStatusEnum.DEV, LifeStatusEnum.DEV.getId(), dto.getLifeStatus(), dto.getLifeStatus().getId()));
            }
        } else {
            dto.setLifeStatus(LifeStatusEnum.DEV);
        }

        // set metric id
        dto.setMetricId(metricMetaId);
        dto.setTimeOnCreate();
        MetricVersionDAO dao = metricVersionStructMapper.dtoToDao(dto);
        int rows = metricVersionMapper.insert(dao);
        if (rows > 0) {
            dto.setId(dao.getId());
            return true;
        }
        return false;
    }


    @Override
    public boolean update(MetricVersionDTO dto) {
        // validate input
        if (dto == null) {
            throw new IllegalArgumentException("[metric_version] updating failed, dto to update is null");
        }
        // validate metric meta existence
        MetricMetaDAO metricMetaDAO = metricService.getMetricMetaDaoByCode(dto.getMetricCode());
        if (metricMetaDAO == null) {
            throw new IllegalArgumentException("[metric_version] updating failed, metric meta not found: metricCode=" + dto.getMetricCode());
        }
        Long metricMetaId = metricMetaDAO.getId();
        if (metricMetaId == null) {
            throw new IllegalStateException("[metric_version] updating failed, metric meta id is null");
        }
        // validate version existence
        MetricVersion existingModel = getByMetricIdAndVersion(metricMetaId, dto.getVersion());
        if (existingModel == null) {
            throw new IllegalStateException("[metric_version] updating failed, metric version not found, version: " + dto.getVersion());
        }
        // validate isMain
        if (dto.getIsMain() != null) {
            // change to main version
            if (dto.getIsMain() != 0 && !Objects.equals(existingModel.getIsMain(), dto.getIsMain())) {
                // only ONLINE version can be set to main
                if (existingModel.getLifeStatus() != LifeStatusEnum.ONLINE) {
                    throw new IllegalStateException(String.format("[metric_version] updating failed, only ONLINE version can be set to main, current status=%s(%d)",
                            existingModel.getLifeStatus(), existingModel.getLifeStatus().getId()));
                }
            } else if (dto.getIsMain() == 0 && existingModel.getIsMain() != 0) {
                // todo: at least one main version should exist
            }
        }
        // validate life status
        Set<LifeStatusEnum> availableLifeStatusSet = lifeStatusConfig.getAvailableLifeStatusTransitions(existingModel.getLifeStatus());
        if (dto.getLifeStatus() != null && existingModel.getLifeStatus() != dto.getLifeStatus()) {
            if (!availableLifeStatusSet.contains(dto.getLifeStatus())) {
                throw new IllegalStateException(String.format("updating version life status invalid, current status=%s(%d), available statuses=%s, but you give=%s(%d)",
                        existingModel.getLifeStatus(), existingModel.getLifeStatus().getId(), availableLifeStatusSet, dto.getLifeStatus(), dto.getLifeStatus().getId()));
            }
        }

        dto.setId(existingModel.getId());
        dto.setTimeOnUpdate();
        MetricVersionDAO dao = metricVersionStructMapper.dtoToDao(dto);
        return metricVersionMapper.updateById(dao) > 0;
    }


    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        return metricVersionMapper.deleteById(id) > 0;
    }
}

