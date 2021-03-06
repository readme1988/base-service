package io.choerodon.base.api.eventhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.base.api.dto.payload.UserMemberEventPayload;
import io.choerodon.base.infra.utils.CollectionUtils;
import io.choerodon.base.infra.dto.MemberRoleDTO;
import io.choerodon.base.infra.mapper.LabelMapper;
import io.choerodon.base.infra.mapper.MemberRoleMapper;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static io.choerodon.base.infra.utils.SagaTopic.MemberRole.MEMBER_ROLE_UPDATE;


/**
 * devops 0.8.0 -> 0.9.0平滑升级类
 *
 * @author superlee
 */
@Component
public class DevopsListener {

    private MemberRoleMapper memberRoleMapper;
    private SagaClient sagaClient;
    private ObjectMapper objectMapper = new ObjectMapper();

    private LabelMapper labelMapper;

    public DevopsListener(MemberRoleMapper memberRoleMapper,
                          SagaClient sagaClient,
                          LabelMapper labelMapper) {
        this.memberRoleMapper = memberRoleMapper;
        this.sagaClient = sagaClient;
        this.labelMapper = labelMapper;
    }

    @SagaTask(code = MEMBER_ROLE_UPDATE, sagaCode = "devops-upgrade-0.9", seq = 1, description = "iam接收devops平滑升级事件")
    public void assignRolesOnProject(String messgae) {
        MemberRoleDTO memberRole = new MemberRoleDTO();
        memberRole.setSourceType(ResourceLevel.PROJECT.value());
        memberRole.setMemberType("user");
        List<MemberRoleDTO> memberRoles = memberRoleMapper.select(memberRole);
        Map<Map<Long, Long>, List<MemberRoleDTO>> map
                = memberRoles.stream().collect(Collectors.groupingBy(m->{
                        Map<Long, Long> map1 = new HashMap<>();
                        map1.put(m.getSourceId(), m.getMemberId());
                        return map1;
                    }));
        List<UserMemberEventPayload> userMemberEventPayloads = new ArrayList<>();
        for (Map.Entry<Map<Long, Long>, List<MemberRoleDTO>> entry : map.entrySet()) {
            UserMemberEventPayload payload = new UserMemberEventPayload();
            List<MemberRoleDTO> mrs = entry.getValue();
            Long sourceId = null;
            Long userId = null;
            List<Long> roleIds = new ArrayList<>();
            for(MemberRoleDTO mr : mrs) {
                sourceId = mr.getSourceId();
                userId = mr.getMemberId();
                roleIds.add(mr.getRoleId());
            }
            payload.setResourceId(sourceId);
            payload.setResourceType("project");
            payload.setUserId(userId);
            if (!roleIds.isEmpty()) {
                payload.setRoleLabels(labelMapper.selectLabelNamesInRoleIds(roleIds));
            }
            userMemberEventPayloads.add(payload);
        }
        List<List<UserMemberEventPayload>> list = CollectionUtils.subList(userMemberEventPayloads, 1000);
        list.forEach(l -> {
            try {
                String input = objectMapper.writeValueAsString(l);
                String refIds = l.stream().map(t -> t.getUserId() + "").collect(Collectors.joining(","));
                sagaClient.startSaga(MEMBER_ROLE_UPDATE, new StartInstanceDTO(input, "users", refIds));
            } catch (Exception e) {
                throw new CommonException("error.iRoleMemberServiceImpl.updateMemberRole.event");
            }
        });
    }
}
