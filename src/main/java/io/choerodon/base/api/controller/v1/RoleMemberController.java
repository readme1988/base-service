package io.choerodon.base.api.controller.v1;

import java.util.List;
import javax.validation.Valid;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.base.api.vo.UserVO;
import io.choerodon.core.annotation.Permission;
import io.choerodon.base.api.dto.RoleAssignmentDeleteDTO;
import io.choerodon.base.api.dto.RoleAssignmentSearchDTO;
import io.choerodon.base.api.dto.SimplifiedClientDTO;
import io.choerodon.base.api.dto.SimplifiedUserDTO;
import io.choerodon.base.api.query.ClientRoleQuery;
import io.choerodon.base.api.validator.MemberRoleValidator;
import io.choerodon.base.api.validator.RoleAssignmentViewValidator;
import io.choerodon.base.app.service.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.base.infra.dto.*;
import io.choerodon.base.infra.enums.ExcelSuffix;
import io.choerodon.core.base.BaseController;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.validator.ValidList;
import org.springframework.data.web.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;

/**
 * @author superlee
 * @author wuguokai
 */
@RestController
@RequestMapping(value = "/v1")
public class RoleMemberController extends BaseController {

    public static final String MEMBER_ROLE = "member-role";
    private RoleMemberService roleMemberService;

    private UserService userService;

    private ClientService clientService;

    private RoleService roleService;
    private UploadHistoryService uploadHistoryService;

    private MemberRoleValidator memberRoleValidator;

    public RoleMemberController(RoleMemberService roleMemberService,
                                UserService userService,
                                RoleService roleService,
                                ClientService clientService,
                                UploadHistoryService uploadHistoryService,
                                MemberRoleValidator memberRoleValidator) {
        this.roleMemberService = roleMemberService;
        this.userService = userService;
        this.roleService = roleService;
        this.uploadHistoryService = uploadHistoryService;
        this.clientService = clientService;
        this.memberRoleValidator = memberRoleValidator;
    }

    /**
     * 在site层分配角色
     * <p>
     * is_edit 是否是编辑，如果false就表示新建角色，true表示是在是编辑角色
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层批量分配给用户/客户端角色")
    @PostMapping(value = "/site/role_members")
    public ResponseEntity<List<MemberRoleDTO>> createOrUpdateOnSiteLevel(@RequestParam(value = "is_edit", required = false) Boolean isEdit,
                                                                         @RequestParam(name = "member_type", required = false) String memberType,
                                                                         @RequestParam(name = "member_ids") List<Long> memberIds,
                                                                         @RequestBody ValidList<MemberRoleDTO> memberRoleDTOList) {
        memberRoleValidator.distributionRoleValidator(ResourceLevel.SITE.value(), memberRoleDTOList);
        return new ResponseEntity<>(roleMemberService.createOrUpdateRolesByMemberIdOnSiteLevel(
                isEdit, memberIds, memberRoleDTOList, memberType), HttpStatus.OK);
    }

    /**
     * 在organization层分配角色
     */
    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层批量分配给用户角色/客户端")
    @PostMapping(value = "/organizations/{organization_id}/role_members")
    public ResponseEntity<List<MemberRoleDTO>> createOrUpdateOnOrganizationLevel(@RequestParam(value = "is_edit", required = false) Boolean isEdit,
                                                                                 @PathVariable(name = "organization_id") Long sourceId,
                                                                                 @RequestParam(name = "member_type", required = false) String memberType,
                                                                                 @RequestParam(name = "member_ids") List<Long> memberIds,
                                                                                 @RequestBody ValidList<MemberRoleDTO> memberRoleDTOList) {
        memberRoleValidator.distributionRoleValidator(ResourceLevel.ORGANIZATION.value(), memberRoleDTOList);
        return new ResponseEntity<>(roleMemberService.createOrUpdateRolesByMemberIdOnOrganizationLevel(
                isEdit, sourceId, memberIds, memberRoleDTOList, memberType), HttpStatus.OK);
    }

    /**
     * 在project层分配角色
     */
    @Permission(type = ResourceType.PROJECT, roles = InitRoleCode.PROJECT_OWNER)
    @ApiOperation(value = "项目层批量分配给用户/客户端角色")
    @PostMapping(value = "/projects/{project_id}/role_members")
    public ResponseEntity<List<MemberRoleDTO>> createOrUpdateOnProjectLevel(@RequestParam(value = "is_edit", required = false) Boolean isEdit,
                                                                            @PathVariable(name = "project_id") Long sourceId,
                                                                            @RequestParam(name = "member_type", required = false) String memberType,
                                                                            @RequestParam(name = "member_ids") List<Long> memberIds,
                                                                            @RequestBody ValidList<MemberRoleDTO> memberRoleDTOList) {
        memberRoleValidator.distributionRoleValidator(ResourceLevel.PROJECT.value(), memberRoleDTOList);
        return new ResponseEntity<>(roleMemberService.createOrUpdateRolesByMemberIdOnProjectLevel(
                isEdit, sourceId, memberIds, memberRoleDTOList, memberType), HttpStatus.OK);
    }

    /**
     * 在site层根据成员id和角色id删除角色
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层批量移除用户/客户端的角色")
    @PostMapping(value = "/site/role_members/delete")
    public ResponseEntity deleteOnSiteLevel(@RequestBody @Valid RoleAssignmentDeleteDTO roleAssignmentDeleteDTO) {
        RoleAssignmentViewValidator.validate(roleAssignmentDeleteDTO.getView());
        roleAssignmentDeleteDTO.setSourceId(0L);
        roleMemberService.deleteOnSiteLevel(roleAssignmentDeleteDTO);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 在organization层根据成员id和角色id删除角色
     */
    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层批量移除用户/客户端的角色")
    @PostMapping(value = "/organizations/{organization_id}/role_members/delete")
    public ResponseEntity deleteOnOrganizationLevel(@PathVariable(name = "organization_id") Long sourceId,
                                                    @RequestBody @Valid RoleAssignmentDeleteDTO roleAssignmentDeleteDTO) {
        RoleAssignmentViewValidator.validate(roleAssignmentDeleteDTO.getView());
        roleAssignmentDeleteDTO.setSourceId(sourceId);
        roleMemberService.deleteOnOrganizationLevel(roleAssignmentDeleteDTO);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 在project层根据id删除角色
     */
    @Permission(type = ResourceType.PROJECT, roles = InitRoleCode.PROJECT_OWNER)
    @ApiOperation(value = "项目层批量移除用户/客户端的角色")
    @PostMapping(value = "/projects/{project_id}/role_members/delete")
    public ResponseEntity deleteOnProjectLevel(@PathVariable(name = "project_id") Long sourceId,
                                               @RequestBody @Valid RoleAssignmentDeleteDTO roleAssignmentDeleteDTO) {
        RoleAssignmentViewValidator.validate(roleAssignmentDeleteDTO.getView());
        roleAssignmentDeleteDTO.setSourceId(sourceId);
        roleMemberService.deleteOnProjectLevel(roleAssignmentDeleteDTO);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 根据角色Id分页查询该角色被分配的用户
     *
     * @param roleAssignmentSearchDTO
     * @return
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层分页查询角色下的用户")
    @PostMapping(value = "/site/role_members/users")
    public ResponseEntity<PageInfo<UserDTO>> pagingQueryUsersByRoleIdOnSiteLevel(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
            @RequestParam(name = "role_id") Long roleId,
            @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO,
            @RequestParam(defaultValue = "true") boolean doPage) {
        return new ResponseEntity<>(userService.pagingQueryUsersByRoleIdOnSiteLevel(
                Pageable, roleAssignmentSearchDTO, roleId, doPage), HttpStatus.OK);
    }

    /**
     * 根据角色Id分页查询该角色被分配的客户端
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层分页查询角色下的客户端")
    @PostMapping(value = "/site/role_members/clients")
    @CustomPageRequest
    public ResponseEntity<PageInfo<ClientDTO>> pagingQueryClientsByRoleIdOnSiteLevel(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
            @RequestParam(name = "role_id") Long roleId,
            @RequestBody(required = false) @Valid ClientRoleQuery clientRoleSearchDTO) {
        return new ResponseEntity<>(clientService.pagingQueryUsersByRoleId(Pageable, ResourceType.SITE, 0L, clientRoleSearchDTO, roleId), HttpStatus.OK);
    }

    @CustomPageRequest
    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层分页查询角色下的用户")
    @PostMapping(value = "/organizations/{organization_id}/role_members/users")
    public ResponseEntity<PageInfo<UserDTO>> pagingQueryUsersByRoleIdOnOrganizationLevel(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
            @RequestParam(name = "role_id") Long roleId,
            @PathVariable(name = "organization_id") Long sourceId,
            @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO,
            @RequestParam(defaultValue = "true") boolean doPage) {
        return new ResponseEntity<>(userService.pagingQueryUsersByRoleIdOnOrganizationLevel(
                Pageable, roleAssignmentSearchDTO, roleId, sourceId, doPage), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层分页查询角色下的客户端")
    @PostMapping(value = "/organizations/{organization_id}/role_members/clients")
    public ResponseEntity<PageInfo<ClientDTO>> pagingQueryClientsByRoleIdOnOrganizationLevel(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
            @RequestParam(name = "role_id") Long roleId,
            @PathVariable(name = "organization_id") Long sourceId,
            @RequestBody(required = false) @Valid ClientRoleQuery clientRoleSearchDTO) {
        return new ResponseEntity<>(clientService.pagingQueryUsersByRoleId(Pageable, ResourceType.ORGANIZATION, sourceId, clientRoleSearchDTO, roleId), HttpStatus.OK);
    }

    /**
     * @param roleId
     * @param sourceId
     * @param roleAssignmentSearchDTO
     * @param doPage                  是否分页，如果为false，则不分页
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = InitRoleCode.PROJECT_OWNER)
    @ApiOperation(value = "项目层分页查询角色下的用户")
    @CustomPageRequest
    @PostMapping(value = "/projects/{project_id}/role_members/users")
    public ResponseEntity<PageInfo<UserDTO>> pagingQueryUsersByRoleIdOnProjectLevel(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
            @RequestParam(name = "role_id") Long roleId,
            @PathVariable(name = "project_id") Long sourceId,
            @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO,
            @RequestParam(defaultValue = "true") boolean doPage) {
        return new ResponseEntity<>(userService.pagingQueryUsersByRoleIdOnProjectLevel(
                Pageable, roleAssignmentSearchDTO, roleId, sourceId, doPage), HttpStatus.OK);
    }


    /**
     * @param projectId
     * @param roleAssignmentSearchDTO
     * @return
     */
    @Permission(type = ResourceType.PROJECT, permissionWithin = true)
    @ApiOperation(value = "项目层查询所有包含gitlab角色标签的用户")
    @PostMapping(value = "/projects/{project_id}/gitlab_role/users")
    public ResponseEntity<List<UserVO>> listUsersWithGitlabLabel(
            @PathVariable(name = "project_id") Long projectId,
            @RequestParam(name = "label_name") String labelName,
            @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        return new ResponseEntity<>(userService.listUsersWithGitlabLabel(projectId, labelName,roleAssignmentSearchDTO), HttpStatus.OK);
    }


    @Permission(type = ResourceType.PROJECT, roles = InitRoleCode.PROJECT_OWNER)
    @ApiOperation(value = "项目层分页查询角色下的客户端")
    @CustomPageRequest
    @PostMapping(value = "/projects/{project_id}/role_members/clients")
    public ResponseEntity<PageInfo<ClientDTO>> pagingQueryClientsByRoleIdOnProjectLevel(@ApiIgnore
                                                                                        @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
                                                                                        @RequestParam(name = "role_id") Long roleId,
                                                                                        @PathVariable(name = "project_id") Long sourceId,
                                                                                        @RequestBody(required = false) @Valid ClientRoleQuery clientRoleSearchDTO) {
        return new ResponseEntity<>(clientService.pagingQueryUsersByRoleId(Pageable, ResourceType.PROJECT, sourceId, clientRoleSearchDTO, roleId), HttpStatus.OK);
    }

    /**
     * 查询site层角色,附带该角色下分配的用户数
     *
     * @return 查询结果
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层查询角色列表以及该角色下的用户数量")
    @PostMapping(value = "/site/role_members/users/count")
    public ResponseEntity<List<RoleDTO>> listRolesWithUserCountOnSiteLevel(
            @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        return new ResponseEntity<>(roleService.listRolesWithUserCountOnSiteLevel(
                roleAssignmentSearchDTO), HttpStatus.OK);
    }

    /**
     * 查询site层角色,附带该角色下分配的客户端数
     *
     * @return 查询结果
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层查询角色列表以及该角色下的客户端数量")
    @PostMapping(value = "/site/role_members/clients/count")
    public ResponseEntity<List<RoleDTO>> listRolesWithClientCountOnSiteLevel(
            @RequestBody(required = false) @Valid ClientRoleQuery clientRoleSearchDTO) {
        return new ResponseEntity<>(roleService.listRolesWithClientCountOnSiteLevel(clientRoleSearchDTO), HttpStatus.OK);
    }

    /**
     * 分页查询site层有角色的用户
     *
     * @return 查询结果
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层分页查询site层有角色的用户")
    @GetMapping(value = "/site/role_members/users")
    @CustomPageRequest
    public ResponseEntity<PageInfo<UserDTO>> pagingQueryUsersOnSiteLevel(@ApiIgnore
                                                                         @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
                                                                         @RequestParam(required = false, name = "id") Long userId,
                                                                         @RequestParam(required = false) String email,
                                                                         @RequestParam(required = false) String param) {
        return new ResponseEntity<>(userService.pagingQueryUsersOnSiteLevel(userId, email, Pageable, param), HttpStatus.OK);
    }

    /**
     * 查询organization层角色,附带该角色下分配的用户数
     *
     * @return 查询结果
     */
    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层查询角色列表以及该角色下的用户数量")
    @PostMapping(value = "/organizations/{organization_id}/role_members/users/count")
    public ResponseEntity<List<RoleDTO>> listRolesWithUserCountOnOrganizationLevel(
            @PathVariable(name = "organization_id") Long sourceId,
            @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        return new ResponseEntity<>(roleService.listRolesWithUserCountOnOrganizationLevel(
                roleAssignmentSearchDTO, sourceId), HttpStatus.OK);
    }

    /**
     * 查询organization层角色,附带该角色下分配的客户端数
     *
     * @return 查询结果
     */
    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层查询角色列表以及该角色下的客户端数量")
    @PostMapping(value = "/organizations/{organization_id}/role_members/clients/count")
    public ResponseEntity<List<RoleDTO>> listRolesWithClientCountOnOrganizationLevel(
            @PathVariable(name = "organization_id") Long sourceId,
            @RequestBody(required = false) @Valid ClientRoleQuery clientRoleSearchDTO) {
        return new ResponseEntity<>(roleService.listRolesWithClientCountOnOrganizationLevel(
                clientRoleSearchDTO, sourceId), HttpStatus.OK);
    }

    /**
     * 查询project层角色,附带该角色下分配的用户数
     *
     * @return 查询结果
     */
    @Permission(type = ResourceType.PROJECT, roles = InitRoleCode.PROJECT_OWNER)
    @ApiOperation(value = "项目层查询角色列表以及该角色下的用户数量")
    @PostMapping(value = "/projects/{project_id}/role_members/users/count")
    public ResponseEntity<List<RoleDTO>> listRolesWithUserCountOnProjectLevel(
            @PathVariable(name = "project_id") Long sourceId,
            @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        return new ResponseEntity<>(roleService.listRolesWithUserCountOnProjectLevel(
                roleAssignmentSearchDTO, sourceId), HttpStatus.OK);
    }

    /**
     * 查询project层角色,附带该角色下分配的客户端数
     *
     * @return 查询结果
     */
    @Permission(type = ResourceType.PROJECT, roles = InitRoleCode.PROJECT_OWNER)
    @ApiOperation(value = "项目层查询角色列表以及该角色下的客户端数量")
    @PostMapping(value = "/projects/{project_id}/role_members/clients/count")
    public ResponseEntity<List<RoleDTO>> listRolesWithClientCountOnProjectLevel(
            @PathVariable(name = "project_id") Long sourceId,
            @RequestBody(required = false) @Valid ClientRoleQuery clientRoleSearchDTO) {
        return new ResponseEntity<>(roleService.listRolesWithClientCountOnProjectLevel(
                clientRoleSearchDTO, sourceId), HttpStatus.OK);
    }

    /**
     * 在site层查询用户，用户包含拥有的site层的角色
     *
     * @param roleAssignmentSearchDTO 搜索条件
     */
    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR})
    @ApiOperation(value = "全局层查询用户列表以及该用户拥有的角色")
    @PostMapping(value = "/site/role_members/users/roles")
    public ResponseEntity<PageInfo<UserDTO>> pagingQueryUsersWithSiteLevelRoles(@ApiIgnore
                                                                                @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
                                                                                @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        return new ResponseEntity<>(userService.pagingQueryUsersWithRoles(
                Pageable, roleAssignmentSearchDTO, 0L, ResourceType.SITE), HttpStatus.OK);
    }

    /**
     * 在site层查询用户，用户包含拥有的site层的角色 (可供平台开发者调用)
     *
     * @param roleAssignmentSearchDTO 搜索条件
     */
    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR, InitRoleCode.SITE_DEVELOPER})
    @ApiOperation(value = "全局层查询用户列表以及该用户拥有的角色")
    @PostMapping(value = "/site/role_members/users/roles/for_all")
    public ResponseEntity<PageInfo<UserDTO>> pagingQueryUsersWithSiteLevelRolesWithDeveloper(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
            @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        return new ResponseEntity<>(userService.pagingQueryUsersWithRoles(
                Pageable, roleAssignmentSearchDTO, 0L, ResourceType.SITE), HttpStatus.OK);
    }

    /**
     * 在site层查询客户端，客户端包含拥有的site层的角色
     *
     * @param clientRoleSearchDTO 搜索条件
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层查询客户端列表以及该客户端拥有的角色")
    @PostMapping(value = "/site/role_members/clients/roles")
    public ResponseEntity<PageInfo<ClientDTO>> pagingQueryClientsWithSiteLevelRoles(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
            @RequestBody(required = false) @Valid ClientRoleQuery clientRoleSearchDTO) {
        return new ResponseEntity<>(roleMemberService.pagingQueryClientsWithRoles(Pageable, clientRoleSearchDTO, 0L, ResourceType.SITE), HttpStatus.OK);
    }

    /**
     * 在site层查询用户，用户包含拥有的organization层的角色
     *
     * @param roleAssignmentSearchDTO 搜索条件
     */
    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层查询用户列表以及该用户拥有的角色")
    @PostMapping(value = "/organizations/{organization_id}/role_members/users/roles")
    public ResponseEntity<PageInfo<UserDTO>> pagingQueryUsersWithOrganizationLevelRoles(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
            @PathVariable(name = "organization_id") Long sourceId,
            @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        return new ResponseEntity<>(userService.pagingQueryUsersWithRoles(
                Pageable, roleAssignmentSearchDTO, sourceId, ResourceType.ORGANIZATION), HttpStatus.OK);
    }

    /**
     * 在组织层层查询用户，用户包含拥有的organization层的角色
     *
     * @param clientRoleSearchDTO 搜索条件
     */
    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层查询客户端列表以及该客户端拥有的角色")
    @PostMapping(value = "/organizations/{organization_id}/role_members/clients/roles")
    public ResponseEntity<PageInfo<ClientDTO>> pagingQueryClientsWithOrganizationLevelRoles(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
            @PathVariable(name = "organization_id") Long sourceId,
            @RequestBody(required = false) @Valid ClientRoleQuery clientRoleSearchDTO) {
        return new ResponseEntity<>(roleMemberService.pagingQueryClientsWithRoles(Pageable, clientRoleSearchDTO, sourceId, ResourceType.ORGANIZATION), HttpStatus.OK);
    }

    /**
     * 在项目层查询用户，用户包含拥有的project层的角色
     *
     * @param sourceId                源id，即项目id
     * @param roleAssignmentSearchDTO 查询请求体，无查询条件需要传{}
     */
    @Permission(type = ResourceType.PROJECT, roles = InitRoleCode.PROJECT_OWNER)
    @ApiOperation(value = "项目层查询用户列表以及该用户拥有的角色")
    @PostMapping(value = "/projects/{project_id}/role_members/users/roles")
    public ResponseEntity<PageInfo<UserDTO>> pagingQueryUsersWithProjectLevelRoles(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
            @PathVariable(name = "project_id") Long sourceId,
            @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        return new ResponseEntity<>(userService.pagingQueryUsersWithRoles(
                Pageable, roleAssignmentSearchDTO, sourceId, ResourceType.PROJECT), HttpStatus.OK);
    }


    /**
     * 在项目层查询客户端，客户端包含拥有的项目层的角色
     *
     * @param clientRoleSearchDTO 搜索条件
     */
    @Permission(type = ResourceType.PROJECT, roles = InitRoleCode.PROJECT_OWNER)
    @ApiOperation(value = "项目层查询客户端列表以及该客户端拥有的角色")
    @PostMapping(value = "/projects/{project_id}/role_members/clients/roles")
    public ResponseEntity<PageInfo<ClientDTO>> pagingQueryClientsWithProjectLevelRoles(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
            @PathVariable(name = "project_id") Long sourceId,
            @RequestBody(required = false) @Valid ClientRoleQuery clientRoleSearchDTO) {
        return new ResponseEntity<>(roleMemberService.pagingQueryClientsWithRoles(Pageable, clientRoleSearchDTO, sourceId, ResourceType.PROJECT), HttpStatus.OK);
    }


    /**
     * 在 organization 层根据 用户Id 及 组织Id 查询用户及该用户在此组织下拥有的角色
     */
    @Permission(type = ResourceType.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "组织层根据用户Id及组织Id查询用户及该用户拥有的角色")
    @GetMapping(value = "/organizations/{organization_id}/role_members/users/{user_id}")
    public ResponseEntity<List<RoleDTO>> getUserWithOrgLevelRolesByUserId(@PathVariable(name = "organization_id") Long organizationId,
                                                                          @PathVariable(name = "user_id") Long userId) {
        return new ResponseEntity<>(roleService.listRolesBySourceIdAndTypeAndUserId(ResourceLevel.ORGANIZATION.value(), organizationId, userId), HttpStatus.OK);
    }

    /**
     * 在 project 层根据 用户Id 及 项目Id 查询用户及该用户在此项目下拥有的角色
     */
    @Permission(type = ResourceType.PROJECT, permissionLogin = true)
    @ApiOperation(value = "项目层根据用户Id及项目Id查询用户及该用户拥有的角色")
    @GetMapping(value = "/projects/{project_id}/role_members/users/{user_id}")
    public ResponseEntity<List<RoleDTO>> getUserWithProjLevelRolesByUserId(@PathVariable(name = "project_id") Long projectId,
                                                                           @PathVariable(name = "user_id") Long userId) {
        return new ResponseEntity<>(roleService.listRolesBySourceIdAndTypeAndUserId(ResourceLevel.PROJECT.value(), projectId, userId), HttpStatus.OK);
    }


    /**
     * 全局层下载模板
     *
     * @return
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层下载excel导入模板")
    @GetMapping(value = "/site/role_members/download_templates")
    public ResponseEntity<Resource> downloadTemplatesOnSite() {
        return roleMemberService.downloadTemplates(ExcelSuffix.XLSX.value());
    }

    /**
     * 组织层下载模板
     *
     * @param organizationId
     * @return
     */
    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层下载excel导入模板")
    @GetMapping(value = "/organizations/{organization_id}/role_members/download_templates")
    public ResponseEntity<Resource> downloadTemplatesOnOrganization(@PathVariable(name = "organization_id") Long organizationId) {
        return roleMemberService.downloadTemplates(ExcelSuffix.XLSX.value());
    }

    /**
     * 项目层下载模板
     *
     * @param projectId
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = InitRoleCode.PROJECT_OWNER)
    @ApiOperation(value = "项目层下载excel导入模板")
    @GetMapping(value = "/projects/{project_id}/role_members/download_templates")
    public ResponseEntity<Resource> downloadTemplatesOnProject(@PathVariable(name = "project_id") Long projectId) {
        return roleMemberService.downloadTemplates(ExcelSuffix.XLSX.value());
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation("site层从excel里面批量导入用户角色关系")
    @PostMapping("/site/role_members/batch_import")
    public ResponseEntity import2MemberRoleOnSite(@RequestPart MultipartFile file) {
        roleMemberService.import2MemberRole(0L, ResourceLevel.SITE.value(), file);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation("组织层从excel里面批量导入用户角色关系")
    @PostMapping("/organizations/{organization_id}/role_members/batch_import")
    public ResponseEntity import2MemberRoleOnOrganization(@PathVariable(name = "organization_id") Long organizationId,
                                                          @RequestPart MultipartFile file) {
        roleMemberService.import2MemberRole(organizationId, ResourceLevel.ORGANIZATION.value(), file);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Permission(type = ResourceType.PROJECT, roles = InitRoleCode.PROJECT_OWNER)
    @ApiOperation("项目层从excel里面批量导入用户角色关系")
    @PostMapping("/projects/{project_id}/role_members/batch_import")
    public ResponseEntity import2MemberRoleOnProject(@PathVariable(name = "project_id") Long projectId,
                                                     @RequestPart MultipartFile file) {
        roleMemberService.import2MemberRole(projectId, ResourceLevel.PROJECT.value(), file);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation("查site层的历史")
    @GetMapping("/site/member_role/users/{user_id}/upload/history")
    public ResponseEntity<UploadHistoryDTO> latestHistoryOnSite(@PathVariable(name = "user_id") Long userId) {
        return new ResponseEntity<>(uploadHistoryService.latestHistory(userId, MEMBER_ROLE, 0L, ResourceLevel.SITE.value()), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation("查组织层的历史")
    @GetMapping("/organizations/{organization_id}/member_role/users/{user_id}/upload/history")
    public ResponseEntity<UploadHistoryDTO> latestHistoryOnOrganization(@PathVariable(name = "organization_id") Long organizationId,
                                                                        @PathVariable(name = "user_id") Long userId) {
        return new ResponseEntity<>(uploadHistoryService.latestHistory(userId, MEMBER_ROLE, organizationId, ResourceLevel.ORGANIZATION.value()), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT, roles = InitRoleCode.PROJECT_OWNER)
    @ApiOperation("查项目层的历史")
    @GetMapping("/projects/{project_id}/member_role/users/{user_id}/upload/history")
    public ResponseEntity<UploadHistoryDTO> latestHistoryOnProject(@PathVariable(name = "project_id") Long projectId,
                                                                   @PathVariable(name = "user_id") Long userId) {
        return new ResponseEntity<>(uploadHistoryService.latestHistory(userId, MEMBER_ROLE, projectId, ResourceLevel.PROJECT.value()), HttpStatus.OK);
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "分页查询全平台层用户（未禁用）")
    @GetMapping(value = "/all/users")
    @CustomPageRequest
    public ResponseEntity<PageInfo<SimplifiedUserDTO>> queryAllUsers(@ApiIgnore
                                                                     @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
                                                                     @RequestParam(value = "organization_id") Long organizationId,
                                                                     @RequestParam(value = "param", required = false) String param) {
        return new ResponseEntity<>(userService.pagingQueryAllUser(Pageable, param, organizationId), HttpStatus.OK);
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "分页查询全平台层客户端")
    @GetMapping(value = "/all/clients")
    @CustomPageRequest
    public ResponseEntity<PageInfo<SimplifiedClientDTO>> queryAllClients(@ApiIgnore
                                                                         @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
                                                                         @RequestParam(value = "param", required = false) String param) {
        return new ResponseEntity<>(clientService.pagingQueryAllClients(Pageable, param), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层更新用户角色")
    @PutMapping(value = "/site/users/{user_id}/assign_roles")
    public ResponseEntity<UserDTO> updateUserRolesOnSiteLevel(@PathVariable(name = "user_id") Long userId,
                                                              @RequestBody @Validated List<RoleDTO> roleDTOList) {
        return new ResponseEntity<>(userService.updateUserRoles(userId,
                ResourceLevel.SITE.value(), 0L, roleDTOList), HttpStatus.OK);
    }


    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层更新用户角色")
    @PutMapping(value = "/organizations/{organization_id}/users/{user_id}/assign_roles")
    public ResponseEntity<UserDTO> updateUserRolesOnOrganizationLevel(@PathVariable(name = "organization_id") Long organizationId,
                                                                      @PathVariable(name = "user_id") Long userId,
                                                                      @RequestBody @Validated List<RoleDTO> roleDTOList) {
        return new ResponseEntity<>(userService.updateUserRoles(userId,
                ResourceLevel.ORGANIZATION.value(), organizationId, roleDTOList), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT)
    @ApiOperation(value = "项目层更新用户角色")
    @PutMapping(value = "/projects/{project_id}/users/{user_id}/assign_roles")
    public ResponseEntity<UserDTO> updateUserRolesOnProjectLevel(@PathVariable(name = "project_id") Long projectId,
                                                                 @PathVariable(name = "user_id") Long userId,
                                                                 @RequestBody @Validated List<RoleDTO> roleDTOList) {
        return new ResponseEntity<>(userService.updateUserRoles
                (userId, ResourceLevel.PROJECT.value(), projectId, roleDTOList), HttpStatus.OK);
    }


    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层批量分配用户角色")
    @PostMapping(value = "/site/users/assign_roles")
    public ResponseEntity<List<MemberRoleDTO>> assignUsersRolesOnSiteLevel(@RequestBody List<MemberRoleDTO> memberRoleDTOS) {
        return new ResponseEntity<>(userService.assignUsersRoles(ResourceLevel.SITE.value(), 0L, memberRoleDTOS), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层批量分配用户角色")
    @PostMapping(value = "/organizations/{organization_id}/users/assign_roles")
    public ResponseEntity<List<MemberRoleDTO>> assignUsersRolesOnOrganizationLevel(@PathVariable(name = "organization_id") Long organizationId,
                                                                                   @RequestBody List<MemberRoleDTO> memberRoleDTOS) {
        return new ResponseEntity<>(userService.assignUsersRoles(ResourceLevel.ORGANIZATION.value(), organizationId, memberRoleDTOS), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT)
    @ApiOperation(value = "项目层批量分配用户角色")
    @PostMapping(value = "/projects/{project_id}/users/assign_roles")
    public ResponseEntity<List<MemberRoleDTO>> assignUsersRolesOnProjectLevel(@PathVariable(name = "project_id") Long projectId,
                                                                              @RequestBody List<MemberRoleDTO> memberRoleDTOS) {
        return new ResponseEntity<>(userService.assignUsersRoles(ResourceLevel.PROJECT.value(), projectId, memberRoleDTOS), HttpStatus.OK);
    }


    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层查询角色列表")
    @GetMapping(value = "/site/roles")
    public ResponseEntity<List<RoleDTO>> listRolesOnSiteLevel(@RequestParam(name = "role_name") String roleName,
                                                              @RequestParam(name = "only_select_enable", required = false, defaultValue = "true")
                                                                      Boolean onlySelectEnable) {
        return new ResponseEntity<>(roleService.listRolesByName(ResourceLevel.SITE.value(), 0L, roleName, onlySelectEnable), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层查询角色列表")
    @GetMapping(value = "/organizations/{organization_id}/roles")
    public ResponseEntity<List<RoleDTO>> listRolesOnOrganizationLevel(@PathVariable(name = "organization_id") Long organizationId,
                                                                      @RequestParam(name = "role_name") String roleName,
                                                                      @RequestParam(name = "only_select_enable", required = false, defaultValue = "true")
                                                                              Boolean onlySelectEnable) {
        return new ResponseEntity<>(roleService.listRolesByName(ResourceLevel.ORGANIZATION.value(), organizationId, roleName, onlySelectEnable), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT)
    @ApiOperation(value = "项目层查询角色列表")
    @GetMapping(value = "/projects/{project_id}/roles")
    public ResponseEntity<List<RoleDTO>> listRolesOnProjectLevel(@PathVariable(name = "project_id") Long projectId,
                                                                 @RequestParam(name = "role_name") String roleName,
                                                                 @RequestParam(name = "only_select_enable", required = false, defaultValue = "true")
                                                                         Boolean onlySelectEnable) {
        return new ResponseEntity<>(roleService.listRolesByName(ResourceLevel.PROJECT.value(), projectId, roleName, onlySelectEnable), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层查询启用状态的用户列表")
    @GetMapping(value = "/site/enableUsers")
    public ResponseEntity<List<UserDTO>> listUsersOnSiteLevel(@RequestParam(name = "user_name") String userName) {
        return new ResponseEntity<>(userService.listEnableUsersByName
                (ResourceLevel.SITE.value(), 0L, userName), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "组织层查询启用状态的用户列表")
    @GetMapping(value = "/organizations/{organization_id}/enableUsers")
    public ResponseEntity<List<UserDTO>> listUsersOnOrganizationLevel(@PathVariable(name = "organization_id") Long organizationId,
                                                                      @RequestParam(name = "user_name") String userName) {
        return new ResponseEntity<>(userService.listEnableUsersByName
                (ResourceLevel.ORGANIZATION.value(), organizationId, userName), HttpStatus.OK);
    }

    @Permission(type = ResourceType.PROJECT)
    @ApiOperation(value = "项目层查询启用状态的用户列表")
    @GetMapping(value = "/projects/{project_id}/enableUsers")
    public ResponseEntity<List<UserDTO>> listUsersOnProjectLevel(@PathVariable(name = "project_id") Long projectId,
                                                                 @RequestParam(name = "user_name") String userName) {
        return new ResponseEntity<>(userService.listEnableUsersByName
                (ResourceLevel.PROJECT.value(), projectId, userName), HttpStatus.OK);
    }

}
