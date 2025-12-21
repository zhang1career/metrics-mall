package lab.zhang.data_science.metrics_mall.service;

import lab.zhang.data_science.metrics_mall.model.Dimension;
import lab.zhang.data_science.metrics_mall.pojo.dto.DimensionDTO;

import java.util.List;

/**
 * Dimension service interface.
 *
 * @author Rongjin Zhang
 */
public interface DimensionService {

    /**
     * Get dimension by id.
     *
     * @param id dimension id
     * @return dimension model
     */
    Dimension get(Long id);

    /**
     * Get dimension by code.
     *
     * @param code dimension code
     * @return dimension model
     */
    Dimension getByCode(String code);

    /**
     * List all dimensions.
     *
     * @return dimension model list
     */
    List<Dimension> list();

    /**
     * Count all dimensions.
     *
     * @return dimension count
     */
    long count();

    /**
     * Insert a new dimension.
     *
     * @param dto dimension data transfer object
     * @return true if success
     */
    boolean insert(DimensionDTO dto);

    /**
     * Update an existing dimension.
     *
     * @param dto dimension data transfer object
     * @return true if success
     */
    boolean update(DimensionDTO dto);

    /**
     * Delete a dimension by id.
     *
     * @param id dimension id
     * @return true if success
     */
    boolean delete(Long id);
}

