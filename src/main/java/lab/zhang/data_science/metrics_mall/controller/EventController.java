package lab.zhang.data_science.metrics_mall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lab.zhang.data_science.metrics_mall.common.response.ApiResponse;
import lab.zhang.data_science.metrics_mall.pojo.dto.EventDTO;
import lab.zhang.data_science.metrics_mall.model.EventAcceptanceResult;
import lab.zhang.data_science.metrics_mall.pojo.vo.EventResponseVO;
import lab.zhang.data_science.metrics_mall.pojo.qo.EventBatchQO;
import lab.zhang.data_science.metrics_mall.service.EventService;
import lab.zhang.data_science.metrics_mall.struct_mapper.EventStructMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Event controller for handling event reporting.
 *
 * @author Rongjin Zhang
 * 
 */
@Tag(name = "Event", description = "Event reporting APIs")
@RestController
@RequiredArgsConstructor
public class EventController extends BaseV1Controller {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventStructMapper eventStructMapper;
    
    /**
     * Report events.
     *
     * @param apiKey API key from request header
     * @param eventBatchQO event query object
     * @return event response
     */
    @Operation(summary = "Report events", description = "Report metric events to the system")
    @PostMapping("/events")
    public ApiResponse<EventResponseVO> report(
            @RequestHeader("X-API-Key") String apiKey,
            @Valid @RequestBody EventBatchQO eventBatchQO) {
        
        // Convert QO to Model
        List<EventDTO> eventDTOList = eventStructMapper.qoToDtoBatch(eventBatchQO.getEvents());
        
        // Call service layer
        EventAcceptanceResult result = eventService.processEventBatch(eventDTOList);
        
        // Convert Model to DTO
        EventResponseVO responseDTO = eventStructMapper.modelToVo(
                result.getAccepted(), result.getRejected());
        
        return ApiResponse.success(responseDTO);
    }
}

