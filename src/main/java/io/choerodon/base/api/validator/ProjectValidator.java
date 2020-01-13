package io.choerodon.base.api.validator;

import io.choerodon.base.infra.mapper.ProjectCategoryMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import io.choerodon.base.infra.dto.ProjectCategoryDTO;
import io.choerodon.core.exception.CommonException;

@Component
public class ProjectValidator {
    private ProjectCategoryMapper projectCategoryMapper;

    public ProjectValidator(ProjectCategoryMapper projectCategoryMapper) {
        this.projectCategoryMapper = projectCategoryMapper;
    }

    public ProjectCategoryDTO validateProjectCategory(String category) {
        if (ObjectUtils.isEmpty(category)) {
            throw new CommonException("error.project.category.empty");
        }
        ProjectCategoryDTO projectCategoryDTO = new ProjectCategoryDTO();
        projectCategoryDTO.setCode(category);
        projectCategoryDTO = projectCategoryMapper.selectOne(projectCategoryDTO);
        if (ObjectUtils.isEmpty(projectCategoryDTO)) {
            throw new CommonException("error.project.category.not.existed", category);
        }
        return projectCategoryDTO;
    }
}
