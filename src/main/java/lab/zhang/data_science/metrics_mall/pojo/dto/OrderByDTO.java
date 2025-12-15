package lab.zhang.data_science.metrics_mall.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order by model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderByDTO {

    /**
     * Field name.
     */
    private String field;

    /**
     * Sort direction: asc, desc.
     */
    private String sort;
}
