package lab.zhang.data_science.metrics_mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lab.zhang.data_science.metrics_mall.config.MetricVersionLifeStatusConfig;
import lab.zhang.data_science.metrics_mall.enums.LifeStatusEnum;
import lab.zhang.data_science.metrics_mall.mapper.MetricVersionMapper;
import lab.zhang.data_science.metrics_mall.model.MetricVersion;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricVersionDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricVersionDTO;
import lab.zhang.data_science.metrics_mall.service.MetricVersionService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricVersionStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
public class MetricVersionServiceImpl implements MetricVersionService {

    @Autowired
    private MetricVersionLifeStatusConfig lifeStatusConfig;

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
            log.warn("[metric_version] create failed, dto to insert is null");
            return false;
        }
        // validate required fields
        if (dto.getMetricId() == null || dto.getVersion() == null) {
            log.warn("[metric_version] create failed, required fields are missing, dto: {}", dto);
            return false;
        }
        // validate existence
        MetricVersion existedModel = getByMetricIdAndVersion(dto.getMetricId(), dto.getVersion());
        if (existedModel != null) {
            throw new IllegalStateException("metric version existed, version: " + existedModel.getVersion());
        }
        // validate isMain
        if (dto.getIsMain() != null) {
            if (dto.getIsMain() != 0) {
                throw new IllegalStateException("inserting version cannot be main, version: " + dto.getVersion());
            }
        } else {
            dto.setIsMain(0);
        }
        // validate life status
        if (dto.getLifeStatus() != null) {
            if (dto.getLifeStatus() != LifeStatusEnum.OFFLINE) {
                throw new IllegalStateException("inserting version life status invalid, version: " + dto.getVersion());
            }
        } else {
            dto.setLifeStatus(LifeStatusEnum.OFFLINE);
        }

        MetricVersionDAO dao = metricVersionStructMapper.dtoToDao(dto);
        dao.setTimeOnCreate();
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
            log.warn("[metric_version] update failed, dto to insert is null");
            return false;
        }
        // validate required fields
        if (dto.getMetricId() == null || dto.getVersion() == null) {
            log.warn("[metric_version] update failed, required fields are missing, dto: {}", dto);
            return false;
        }
        // validate existence
        MetricVersion existedModel = getByMetricIdAndVersion(dto.getMetricId(), dto.getVersion());
        if (existedModel == null) {
            throw new IllegalStateException("metric version not existed, version: " + existedModel.getVersion());
        }
        // validate isMain
        if (dto.getIsMain() != null) {
            if (existedModel.getIsMain() != 0 && dto.getIsMain() != 0) {
                throw new IllegalStateException("updating version cannot be main, version: " + dto.getVersion());
            }
        }
        // validate life status
        Set<LifeStatusEnum> availableLifeStatusSet = lifeStatusConfig.getAvailableLifeStatusTransitions(existedModel.getLifeStatus());
        if (dto.getLifeStatus() != null) {
            if (!availableLifeStatusSet.contains(dto.getLifeStatus())) {
                throw new IllegalStateException("updating version life status invalid, version: " + dto.getVersion()
                        + ", available statuses: " + availableLifeStatusSet);
            }
        }

        MetricVersionDAO dao = metricVersionStructMapper.dtoToDao(dto);
        dao.setTimeOnUpdate();
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

