package io.choerodon.base.infra.feign.fallback;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.base.infra.dto.asgard.QuartzTask;
import io.choerodon.base.infra.dto.asgard.ScheduleMethodDTO;
import io.choerodon.base.infra.dto.asgard.ScheduleTaskDTO;
import io.choerodon.base.infra.dto.asgard.ScheduleTaskDetail;
import io.choerodon.core.exception.CommonException;
import io.choerodon.base.infra.feign.AsgardFeignClient;

/**
 * @author dengyouquan
 **/
@Component
public class AsgardFeignClientFallback implements AsgardFeignClient {
    @Override
    public void disableOrg(long orgId) {
        throw new CommonException("error.asgard.quartzTask.disableOrg");
    }

    @Override
    public void disableProj(long projectId) {
        throw new CommonException("error.asgard.quartzTask.disableProject");
    }

    @Override
    public ResponseEntity<QuartzTask> createOrgTask(long organizationId, ScheduleTaskDTO scheduleTaskDTO) {
        throw new CommonException("error.asgard.quartzTask.createOrganization");
    }

    @Override
    public void deleteOrgTask(long orgId, long id) {
        throw new CommonException("error.asgard.quartzTask.deleteOrganization");
    }

    @Override
    public void enableOrgTask(long orgId, long id, long objectVersionNumber) {
        throw new CommonException("error.asgard.quartzTask.enableOrganization");
    }

    @Override
    public ResponseEntity<List<ScheduleMethodDTO>> getMethodByService(long orgId, String service) {
        throw new CommonException("error.asgard.method.query");
    }

    @Override
    public void disableOrgTask(long orgId, long id, long objectVersionNumber) {
        throw new CommonException("error.asgard.orgTask.disable");
    }

    @Override
    public ResponseEntity<ScheduleTaskDetail> getTaskDetail(long orgId, long id) {
        throw new CommonException("error.asgard.task.detail");
    }
}