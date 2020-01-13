package io.choerodon.base.api.controller.v1;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.api.validator.Insert;
import io.choerodon.base.api.validator.Update;
import io.choerodon.base.app.service.PromptService;
import io.choerodon.base.infra.dto.PromptDTO;
import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author wkj
 */
@RestController
@RequestMapping("/v1/prompt")
public class PromptController {

    private PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    /**
     * 添加多语言映射
     *
     * @param promptDTO
     * @return PromptDTO
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "创建多语言映射")
    @PostMapping
    public ResponseEntity<PromptDTO> create(@RequestBody @Validated({Insert.class}) PromptDTO promptDTO) {
        return new ResponseEntity<>(promptService.create(promptDTO), HttpStatus.OK);
    }

    /**
     * 更新多语言映射
     *
     * @param id
     * @param promptDTO
     * @return PromptDTO
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "修改多语言映射")
    @PutMapping(value = "/{id}")
    public ResponseEntity<PromptDTO> update(@PathVariable("id") Long id, @RequestBody @Validated({Update.class}) PromptDTO promptDTO) {
        return new ResponseEntity<>(promptService.update(id, promptDTO), HttpStatus.OK);
    }


    /**
     * 分页查询多语言映射表
     *
     * @param promptCode
     * @param lang
     * @param serviceCode
     * @param description
     * @param pageable
     * @param params
     * @return
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "分页查询多语言映射")
    @GetMapping
    @CustomPageRequest
    public ResponseEntity<PageInfo<PromptDTO>> pagingQueryByOptions(@RequestParam(required = false) String promptCode,
                                                                    @RequestParam(required = false) String lang,
                                                                    @RequestParam(required = false) String serviceCode,
                                                                    @RequestParam(required = false) String description,
                                                                    @ApiIgnore @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                                    @RequestParam(required = false) String params) {
        return new ResponseEntity<>(promptService.queryByOptions(promptCode, lang, serviceCode, description, pageable, params), HttpStatus.OK);
    }

    /**
     * 根据id删除对应的多语言映射
     *
     * @param id
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "删除对应的多语言映射关系")
    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable("id") Long id) {
        promptService.delete(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * 根据id查询多语言映射关系
     *
     * @param id
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "查询id对应的多语言映射关系")
    @GetMapping("/{id}")
    public ResponseEntity<PromptDTO> queryById(@PathVariable("id") Long id) {
        return new ResponseEntity(promptService.queryById(id), HttpStatus.OK);
    }


    /**
     * 根据编码查询多语言映射关系
     *
     * @param code
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "查询编码对应的多语言映射关系")
    @GetMapping("/code")
    public ResponseEntity<PromptDTO> queryByCode(@RequestParam("code") String code,
                                                 @RequestParam(value = "lang", required = false) String lang) {
        return new ResponseEntity(promptService.queryByCode(code, lang), HttpStatus.OK);
    }

}
