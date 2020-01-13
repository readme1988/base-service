package io.choerodon.base.infra.mapper;

import io.choerodon.base.infra.dto.PermissionDTO;
import io.choerodon.base.infra.dto.RoleDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author wuguokai
 */
public interface PermissionMapper extends Mapper<PermissionDTO> {

    List<PermissionDTO> fuzzyQuery(@Param("permissionDTO") PermissionDTO permissionDTO,
                                   @Param("param") String param);

    List<PermissionDTO> selectByRoleId(@Param("roleId") Long roleId,
                                       @Param("params") String params);

    Set<String> checkPermission(@Param("member_id") Long memberId, @Param("source_type") String sourceType,
                                @Param("source_id") Long sourceId, @Param("codes") Set<String> codes);

    List<PermissionDTO> selectErrorLevelPermissionByRole(@Param("role") RoleDTO role);

    List<PermissionDTO> queryByLevelAndCode(@Param("level") String level, @Param("params") String params);
}
