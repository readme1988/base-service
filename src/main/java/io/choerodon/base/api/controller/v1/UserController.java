package io.choerodon.base.api.controller.v1;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.Valid;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.annotation.Permission;
import io.choerodon.base.api.dto.*;
import io.choerodon.base.api.validator.UserValidator;
import io.choerodon.base.app.service.PasswordPolicyService;
import io.choerodon.base.app.service.UserService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import io.choerodon.core.enums.ResourceType;
import io.choerodon.base.infra.dto.OrganizationDTO;
import io.choerodon.base.infra.dto.PasswordPolicyDTO;
import io.choerodon.base.infra.dto.ProjectDTO;
import io.choerodon.base.infra.dto.UserDTO;
import io.choerodon.base.infra.utils.ParamUtils;
import io.choerodon.core.base.BaseController;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.NotFoundException;
import io.choerodon.core.iam.InitRoleCode;

import org.springframework.data.web.SortDefault;

import io.choerodon.swagger.annotation.CustomPageRequest;

/**
 * @author superlee
 */
@RestController
@RequestMapping(value = "/v1/users")
public class UserController extends BaseController {

    private UserService userService;

    private PasswordPolicyService passwordPolicyService;

    public UserController(UserService userService, PasswordPolicyService passwordPolicyService) {
        this.userService = userService;
        this.passwordPolicyService = passwordPolicyService;
    }

    @Permission(type = ResourceType.SITE, permissionLogin = true)
    @ApiOperation(value = "查询当前用户信息")
    @GetMapping(value = "/self")
    public ResponseEntity<UserDTO> querySelf() {
        return new ResponseEntity<>(userService.querySelf(), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE, permissionLogin = true)
    @ApiOperation(value = "根据id查询用户信息")
    @GetMapping(value = "/{id}/info")
    public ResponseEntity<UserDTO> queryInfo(@PathVariable Long id) {
        return Optional.ofNullable(userService.queryInfo(id))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(NotFoundException::new);
    }


    @Permission(permissionWithin = true)
    @ApiOperation(value = "获取组织注册信息")
    @GetMapping(value = "/registrant")
    public ResponseEntity<RegistrantInfoDTO> queryInfoSkipLogin(
            @RequestParam(value = "org_code") String orgCode) {
        return Optional.ofNullable(userService.queryRegistrantInfoAndAdmin(orgCode))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(NotFoundException::new);
    }


    @Permission(type = ResourceType.SITE, permissionLogin = true)
    @ApiOperation(value = "修改用户信息")
    @PutMapping(value = "/{id}/info")
    public ResponseEntity<UserDTO> updateInfo(@PathVariable Long id,
                                              @RequestBody @Validated({UserValidator.UserGroup.class}) UserDTO userDTO) {
        userDTO.setId(id);
        if (userDTO.getObjectVersionNumber() == null) {
            throw new CommonException("error.user.objectVersionNumber.null");
        }
        userDTO.setAdmin(null);
        //不能修改状态
        userDTO.setEnabled(null);
        userDTO.setLdap(null);
        userDTO.setOrganizationId(null);
        userDTO.setLoginName(null);
        return new ResponseEntity<>(userService.updateInfo(userDTO, true), HttpStatus.OK);
    }

    /**
     * 上传头像到文件服务返回头像url
     */
    @Permission(type = ResourceType.SITE, permissionLogin = true)
    @ApiOperation(value = "用户头像上传")
    @PostMapping(value = "/{id}/upload_photo")
    public ResponseEntity<String> uploadPhoto(@PathVariable Long id,
                                              @RequestPart MultipartFile file) {
        return new ResponseEntity<>(userService.uploadPhoto(id, file), HttpStatus.OK);
    }

    /**
     * 上传头像，支持裁剪，旋转，并保存
     */
    @Permission(type = ResourceType.SITE, permissionLogin = true)
    @ApiOperation(value = "用户头像上传裁剪，旋转并保存")
    @PostMapping(value = "/{id}/save_photo")
    public ResponseEntity<String> savePhoto(@PathVariable Long id,
                                            @RequestPart MultipartFile file,
                                            @ApiParam(name = "rotate", value = "顺时针旋转的角度", example = "90")
                                            @RequestParam(required = false) Double rotate,
                                            @ApiParam(name = "startX", value = "裁剪的X轴", example = "100")
                                            @RequestParam(required = false, name = "startX") Integer axisX,
                                            @ApiParam(name = "startY", value = "裁剪的Y轴", example = "100")
                                            @RequestParam(required = false, name = "startY") Integer axisY,
                                            @ApiParam(name = "endX", value = "裁剪的宽度", example = "200")
                                            @RequestParam(required = false, name = "endX") Integer width,
                                            @ApiParam(name = "endY", value = "裁剪的高度", example = "200")
                                            @RequestParam(required = false, name = "endY") Integer height) {
        return new ResponseEntity<>(userService.savePhoto(id, file, rotate, axisX, axisY, width, height), HttpStatus.OK);
    }


    @Permission(type = ResourceType.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "查询用户所在组织列表")
    @GetMapping(value = "/{id}/organizations")
    public ResponseEntity<Set<OrganizationDTO>> queryOrganizations(@PathVariable Long id,
                                                                   @RequestParam(required = false, name = "included_disabled")
                                                                           boolean includedDisabled) {
        return new ResponseEntity<>(userService.queryOrganizations(id, includedDisabled), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "查询用户所在项目列表")
    @GetMapping(value = "/{id}/projects")
    public ResponseEntity<List<ProjectDTO>> queryProjects(@PathVariable Long id,
                                                          @RequestParam(required = false, name = "included_disabled")
                                                                  boolean includedDisabled) {
        return new ResponseEntity<>(userService.queryProjects(id, includedDisabled), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "根据用户名查询用户信息")
    @GetMapping
    public ResponseEntity<UserDTO> query(@RequestParam(name = "login_name") String loginName) {
        return new ResponseEntity<>(userService.queryByLoginName(loginName), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE, permissionLogin = true)
    @ApiOperation(value = "修改密码")
    @PutMapping(value = "/{id}/password")
    public ResponseEntity selfUpdatePassword(@PathVariable Long id,
                                             @RequestBody @Valid UserPasswordDTO userPasswordDTO) {
        userService.selfUpdatePassword(id, userPasswordDTO, true, true);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE, permissionPublic = true)
    @ApiOperation(value = "用户信息校验")
    @PostMapping(value = "/check")
    public ResponseEntity check(@RequestBody UserDTO user) {
        userService.check(user);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * 分页查询所有的admin用户
     *
     * @return 分页的admin用户
     */
    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "分页模糊查询管理员用户列表")
    @GetMapping("/admin")
    @CustomPageRequest
    public ResponseEntity<PageInfo<UserDTO>> pagingQueryAdminUsers(@ApiIgnore
                                                                   @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
                                                                   @RequestParam(required = false) String loginName,
                                                                   @RequestParam(required = false) String realName,
                                                                   @RequestParam(required = false) String params) {
        return new ResponseEntity<>(userService.pagingQueryAdminUsers(Pageable, loginName, realName, params), HttpStatus.OK);
    }


    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "批量给用户添加管理员身份")
    @PostMapping("/admin")
    public ResponseEntity addDefaultUsers(@ModelAttribute("id") long[] ids) {
        userService.addAdminUsers(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "清除用户的管理员身份")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity deleteDefaultUser(@PathVariable long id) {
        userService.deleteAdminUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Permission(permissionWithin = true)
    @ApiOperation(value = "查询所有的Root用户 / DevOps服务迁移数据需要")
    @GetMapping("/admin_all")
    public ResponseEntity<List<UserDTO>> queryAllAdminUsers() {
        return new ResponseEntity<>(userService.queryAllAdminUsers(), HttpStatus.OK);
    }

    @Permission(permissionWithin = true)
    @ApiOperation(value = "根据id批量查询用户信息列表")
    @PostMapping(value = "/ids")
    public ResponseEntity<List<UserDTO>> listUsersByIds(@RequestBody Long[] ids,
                                                        @RequestParam(value = "only_enabled", defaultValue = "true", required = false) Boolean onlyEnabled) {
        return new ResponseEntity<>(userService.listUsersByIds(ids, onlyEnabled), HttpStatus.OK);
    }

    @Permission(permissionWithin = true)
    @ApiOperation(value = "根据email批量查询用户信息列表")
    @PostMapping(value = "/emails")
    public ResponseEntity<List<UserDTO>> listUsersByEmails(@RequestBody String[] emails) {
        return new ResponseEntity<>(userService.listUsersByEmails(emails), HttpStatus.OK);
    }

    @Permission(permissionWithin = true)
    @ApiOperation(value = "根据loginName批量查询用户信息列表")
    @PostMapping(value = "/login_names")
    public ResponseEntity<List<UserDTO>> listUsersByLoginNames(@RequestBody String[] loginNames,
                                                               @RequestParam(value = "only_enabled", defaultValue = "true", required = false) Boolean onlyEnabled) {
        return new ResponseEntity<>(userService.listUsersByLoginNames(loginNames, onlyEnabled), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE, permissionLogin = true)
    @ApiOperation("根据id分页获取组织列表和角色")
    @GetMapping("/{id}/organization_roles")
    @CustomPageRequest
    public ResponseEntity<PageInfo<OrganizationDTO>> pagingQueryOrganizationAndRolesById(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
            @PathVariable(value = "id") Long id,
            @RequestParam(value = "params", required = false) String[] params) {
        return new ResponseEntity<>(userService.pagingQueryOrganizationsWithRoles(Pageable, id, ParamUtils.arrToStr(params)), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE, permissionLogin = true)
    @ApiOperation("根据id分页获取项目列表和角色")
    @GetMapping("/{id}/project_roles")
    @CustomPageRequest
    public ResponseEntity<PageInfo<ProjectDTO>> pagingQueryProjectAndRolesById(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
            @PathVariable("id") Long id,
            @RequestParam(value = "params", required = false) String[] params) {
        return new ResponseEntity<>(userService.pagingQueryProjectAndRolesById(Pageable, id, ParamUtils.arrToStr(params)), HttpStatus.OK);
    }

    @Permission(permissionWithin = true)
    @ApiOperation("新建用户，并根据角色code分配角色")
    @PostMapping("/init_role")
    public ResponseEntity<UserDTO> createUserAndAssignRoles(@RequestBody CreateUserWithRolesDTO userWithRoles) {
        return new ResponseEntity<>(userService.createUserAndAssignRoles(userWithRoles), HttpStatus.OK);
    }

    @Permission(permissionWithin = true)
    @ApiOperation("得到所有用户id")
    @GetMapping("/ids")
    public ResponseEntity<Long[]> getUserIds() {
        return new ResponseEntity<>(userService.listUserIds(), HttpStatus.OK);
    }

    /**
     * 根据用户邮箱查询对应组织下的密码策略
     *
     * @return 目标组织密码策略
     */
    @Permission(permissionPublic = true)
    @ApiOperation(value = "根据用户邮箱查询对应组织下的密码策略")
    @GetMapping("/password_policies")
    public ResponseEntity<PasswordPolicyDTO> queryByUserEmail(@RequestParam(value = "email", required = false) String email) {
        Long organizationId = userService.queryOrgIdByEmail(email);
        return new ResponseEntity<>(passwordPolicyService.queryByOrgId(organizationId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE, roles = {InitRoleCode.SITE_ADMINISTRATOR})
    @ApiOperation(value = "卡片：新增用户统计")
    @GetMapping("/new")
    public ResponseEntity<Map<String, Object>> queryNewAndAllUsers() {
        return new ResponseEntity<>(userService.queryAllAndNewUsers(), HttpStatus.OK);
    }


    @Permission(type = ResourceType.SITE, permissionLogin = true)
    @ApiOperation("根据id分页获取用户所有角色列表")
    @GetMapping("/{id}/roles")
    @CustomPageRequest
    public ResponseEntity<PageInfo<UserRoleDTO>> pagingQueryRole(@ApiIgnore
                                                                 @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
                                                                 @PathVariable("id") Long id,
                                                                 @RequestParam(required = false) String name,
                                                                 @RequestParam(required = false) String level,
                                                                 @RequestParam(required = false) String params) {
        return new ResponseEntity<>(userService.pagingQueryRole(Pageable, id, name, level, params), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE, permissionPublic = true, permissionWithin = true)
    @ApiOperation(value = "完善用户信息，修改用户名、密码")
    @PutMapping(value = "/{id}/userInfo")
    public ResponseEntity<UserInfoDTO> updateUserInfo(@PathVariable Long id,
                                                      @RequestBody @Valid UserInfoDTO userInfoDTO) {
        return new ResponseEntity<>(userService.updateUserInfo(id, userInfoDTO), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层分页查询用户列表（包括用户信息以及所分配的全局角色信息）")
    @GetMapping(value = "/search")
    @CustomPageRequest
    public ResponseEntity<PageInfo<UserDTO>> pagingQueryUsersWithRolesOnSiteLevel(@ApiIgnore
                                                                                  @SortDefault(value = "id", direction = Sort.Direction.DESC) Pageable Pageable,
                                                                                  @RequestParam(required = false) String organizationName,
                                                                                  @RequestParam(required = false) String loginName,
                                                                                  @RequestParam(required = false) String realName,
                                                                                  @RequestParam(required = false) String roleName,
                                                                                  @RequestParam(required = false) Boolean enabled,
                                                                                  @RequestParam(required = false) Boolean locked,
                                                                                  @RequestParam(required = false) String params) {
        return new ResponseEntity<>(userService.pagingQueryUsersWithRolesOnSiteLevel(Pageable, organizationName, loginName, realName, roleName,
                enabled, locked, params), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层修改用户")
    @PutMapping(value = "/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id,
                                              @RequestBody @Validated({UserValidator.UserGroup.class}) UserDTO userDTO) {
        // 不能更新admin字段
        userDTO.setAdmin(null);
        // 不能更新ldap字段
        userDTO.setLdap(null);
        // 不能更新登录名
        userDTO.setLoginName(null);
        // 不能更新所属组织
        userDTO.setOrganizationId(null);
        // 不能修改密码
        userDTO.setPassword(null);
        userDTO.setId(id);
        return new ResponseEntity<>(userService.updateUser(userDTO), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层启用用户")
    @PutMapping(value = "/{id}/enable")
    public ResponseEntity<UserDTO> enableUser(@PathVariable Long id) {
        return new ResponseEntity<>(userService.enableUser(id), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层禁用用户")
    @PutMapping(value = "/{id}/disable")
    public ResponseEntity<UserDTO> disableUser(@PathVariable Long id) {
        return new ResponseEntity<>(userService.disableUser(id), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE)
    @ApiOperation(value = "全局层解锁用户")
    @PutMapping(value = "/{id}/unlock")
    public ResponseEntity<UserDTO> unlockUser(@PathVariable Long id) {
        return new ResponseEntity<>(userService.unlockUser(id), HttpStatus.OK);
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "根据用户id查询对应的组织和项目")
    @GetMapping("/{id}/organization_project")
    public ResponseEntity<OrganizationProjectDTO> queryOrganizationProjectByUserId(@PathVariable("id") Long id) {
        return new ResponseEntity<>(userService.queryOrganizationProjectByUserId(id), HttpStatus.OK);
    }

    @GetMapping(value = "/enable_user/route_rule_unused")
    @ApiOperation(value = "查询可用于路由配置的用户信息")
    @Permission(type = ResourceType.SITE)
    public ResponseEntity<List<UserDTO>> listEnableUsersByRouteRuleCode(@RequestParam(name = "user_name") String userName) {
        return new ResponseEntity<>(userService.listEnableUsersByRouteRuleCode(userName), HttpStatus.OK);
    }

    @Permission(type = ResourceType.SITE, permissionLogin = true)
    @ApiOperation("获取用户下的指定项目(没有项目权限或者项目不存在返回空)")
    @GetMapping("/{id}/projects/{project_id}")
    public ResponseEntity<ProjectDTO> queryProjectById(
            @PathVariable("id") Long id,
            @PathVariable("project_id") Long projectId) {
        return ResponseEntity.ok(userService.queryProjectById(id, projectId));
    }

    @Permission(type = ResourceType.SITE, permissionLogin = true)
    @ApiOperation("校验用户是否是项目的所有者")
    @GetMapping("/{id}/projects/{project_id}/check_is_owner")
    public ResponseEntity<Boolean> checkIsProjectOwner(
            @PathVariable("id") Long id,
            @PathVariable("project_id") Long projectId) {
        return ResponseEntity.ok(userService.checkIsProjectOwner(id, projectId));
    }

    @Permission(type = ResourceType.SITE, permissionLogin = true)
    @ApiOperation("校验用户是否是gitlab项目的所有者")
    @GetMapping("/{id}/projects/{project_id}/check_is_gitlab_owner")
    public ResponseEntity<Boolean> checkIsGitlabProjectOwner(
            @PathVariable("id") Long id,
            @PathVariable("project_id") Long projectId) {
        return ResponseEntity.ok(userService.checkIsGitlabProjectOwner(id, projectId));
    }
}