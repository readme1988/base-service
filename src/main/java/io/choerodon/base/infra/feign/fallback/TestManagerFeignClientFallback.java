package io.choerodon.base.infra.feign.fallback;

import io.choerodon.base.api.vo.AgileProjectInfoVO;
import io.choerodon.base.infra.feign.TestManagerFeignClient;
import io.choerodon.core.exception.CommonException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @author zmf
 * @since 12/13/19
 */
@Component
public class TestManagerFeignClientFallback implements TestManagerFeignClient {
    @Override
    public ResponseEntity<AgileProjectInfoVO> updateProjectInfo(Long projectId, AgileProjectInfoVO projectInfoVO) {
        throw new CommonException("error.test.manager.updateProjectInfo");
    }
}
