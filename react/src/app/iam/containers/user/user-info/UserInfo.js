import React, { Component, useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Button, Form, Icon, Input, Select, Modal as OldModal } from 'choerodon-ui';
import { Modal, Tooltip } from 'choerodon-ui/pro';
import { FormattedMessage } from 'react-intl';
import { Content, Header, Page, Permission, axios, Breadcrumb, Choerodon } from '@choerodon/boot';
import './Userinfo.less';
import TextEditToggle from './textEditToggle';
import EditUserInfo from './EditUserInfo';
import { useStore } from './stores';
import EditPassword from './EditPassword';

const { Text, Edit } = TextEditToggle;

const createKey = Modal.key();
function UserInfo(props) {
  const context = useStore();
  const { AppState, UserInfoStore, intl, intlPrefix, prefixCls } = context;
  const [enablePwd, setEnablePwd] = useState({});
  const [avatar, setAvatar] = useState('');
  const modalRef = React.createRef();
  const loadUserInfo = () => {
    AppState.loadUserInfo().then(data => {
      AppState.setUserInfo(data);
      UserInfoStore.setUserInfo(AppState.getUserInfo);
      setAvatar(UserInfoStore.getAvatar);
    });
  };

  const loadEnablePwd = () => {
    axios.get('/base/v1/system/setting/enable_resetPassword')
      .then((response) => {
        setEnablePwd(response);
      });
  };

  function renderAvatar({ id, realName }) {
    const image = avatar && {
      backgroundImage: `url(${Choerodon.fileServer(avatar)})`,
    };
    return (
      <div className={`${prefixCls}-avatar-wrap`}>
        <div
          className={`${prefixCls}-avatar`}
          style={image || {}}
        >
          {!avatar && realName && realName.charAt(0)}
        </div>
      </div>
    );
  }

  function renderUserInfo(user) {
    const { loginName, realName, email, language, timeZone, phone, ldap, organizationName, organizationCode, internationalTelCode } = user;
    return (
      <React.Fragment>
        <div className={`${prefixCls}-top-container`}>
          <div className={`${prefixCls}-avatar-wrap-container`}>
            {renderAvatar(user)}
          </div>
          <div className={`${prefixCls}-login-info`}>
            <div>{realName}</div>
            <div>{intl.formatMessage({ id: `${intlPrefix}.source` })}:{ldap ? intl.formatMessage({ id: `${intlPrefix}.ldap` }) : intl.formatMessage({ id: `${intlPrefix}.notldap` })}</div>
            <div>
              <span>{intl.formatMessage({ id: `${intlPrefix}.loginname` })}：</span>
              <Text style={{ fontSize: '13px' }}>
                <span>{loginName}</span>
              </Text>
            </div>
          </div>
        </div>
        <div className={`${prefixCls}-info-container`}>

          <div className={`${prefixCls}-info-container-account`}>
            <div>{intl.formatMessage({ id: `${intlPrefix}.account.info` })}</div>
            <div>
              <div>
                <span className={`${prefixCls}-info-container-account-title`}>{intl.formatMessage({ id: `${intlPrefix}.email` })}</span>
                <span className={`${prefixCls}-info-container-account-content`}>{email}</span>
              </div>
              <div>
                <span className={`${prefixCls}-info-container-account-title`}>{intl.formatMessage({ id: `${intlPrefix}.phone` })}</span>
                <span className={`${prefixCls}-info-container-account-content`}>{phone === null ? '无' : phone}</span>
              </div>
              <div>
                <span className={`${prefixCls}-info-container-account-title`}>{intl.formatMessage({ id: `${intlPrefix}.language` })}</span>
                <span className={`${prefixCls}-info-container-account-content ${prefixCls}-info-container-account-content-short`}>简体中文</span>
              </div>
              <div>
                <span className={`${prefixCls}-info-container-account-title`}>{intl.formatMessage({ id: `${intlPrefix}.timezone` })}</span>
                <span className={`${prefixCls}-info-container-account-content ${prefixCls}-info-container-account-content-short`}>中国</span>
              </div>
            </div>
          </div>
        </div>
        <div className={`${prefixCls}-info-container`}>

          <div className={`${prefixCls}-info-container-account`}>
            <div>{intl.formatMessage({ id: `${intlPrefix}.orginfo` })}</div>
            <div>
              <div>
                <span className={`${prefixCls}-info-container-account-title`}>{intl.formatMessage({ id: `${intlPrefix}.org.name` })}</span>
                <span className={`${prefixCls}-info-container-account-content `}>{organizationName}</span>
              </div>
              <div>
                <span className={`${prefixCls}-info-container-account-title`}>{intl.formatMessage({ id: `${intlPrefix}.org.code` })}</span>
                <span className={`${prefixCls}-info-container-account-content `}>{organizationCode}</span>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }

  function handleUpdateInfo() {
    Modal.open({
      key: createKey,
      title: '修改信息',
      style: {
        width: 380,
      },
      drawer: true,
      children: (
        <EditUserInfo
          // {...props}
          intl={intl}
          AppState={AppState}
          resetAvatar={setAvatar}
          intlPrefix={intlPrefix}
          forwardref={modalRef}
          UserInfoStore={UserInfoStore}
          loadUserInfo={loadUserInfo}
        />
      ),
      okText: '保存',
      onOk: () => {
        modalRef.current.handleSubmit();
        return false;
      },
    });
  }
  function handleUpdateStore() {
    OldModal.confirm({
      className: 'c7n-iam-confirm-modal',
      title: '修改仓库密码',
      content: '确定要修改您的gitlab仓库密码吗？点击确定后，您将跳转至GitLab仓库克隆密码的修改页面。',
      okText: '修改',
      width: 560,
      onOk: () => {
        const { resetGitlabPasswordUrl } = enablePwd;
        if (enablePwd.enable_reset) {
          window.open(resetGitlabPasswordUrl);
        }
      },
    });
  }


  function handleUpdatePassword() {
    const user = UserInfoStore.getUserInfo;
    Modal.open({
      key: createKey,
      title: '修改登录密码',
      style: {
        width: 380,
      },
      drawer: true,
      children: (
        <EditPassword
          // {...props}
          intl={intl}
          intlPrefix={intlPrefix}
          forwardref={modalRef}
          UserInfoStore={UserInfoStore}
        />
      ),
      okText: '保存',
      onOk: () => {
        modalRef.current.handleSubmit();
        return false;
      },
      footer: (okBtn, cancelBtn) => (
        <div>
          {!user.ldap ? okBtn : React.cloneElement(okBtn, { disabled: true })}
          {cancelBtn}
        </div>
      )
      ,
    });
  }

  useEffect(() => {
    loadUserInfo();
    loadEnablePwd();
  }, []);

  const render = () => {
    const user = UserInfoStore.getUserInfo;
    return (
      <Page
        service={[
          'base-service.user.query',
          'base-service.user.check',
          'base-service.user.querySelf',
          'base-service.user.queryInfo',
          'base-service.user.updateInfo',
          'base-service.user.uploadPhoto',
          'base-service.user.queryProjects',
        ]}
      >
        <Header className={`${prefixCls}-header`}>
          <Button className={`${prefixCls}-header-btn`} onClick={handleUpdateInfo.bind(this)} icon="mode_edit">
            修改信息
          </Button>
          <Tooltip title={AppState.getUserInfo.ldap ? 'LDAP用户无法修改登录密码' : ''}>
            <Button
              className="user-info-header-btn"
              onClick={handleUpdatePassword.bind(this)}
              icon="mode_edit"
              disabled={AppState.getUserInfo.ldap}
            >
              修改登录密码
            </Button>
          </Tooltip>
          <Button
            className="user-info-header-btn"
            onClick={handleUpdateStore.bind(this)}
            icon="mode_edit"
            disabled={!enablePwd.enable_reset}
          >
            修改仓库密码
          </Button>
        </Header>
        <Breadcrumb />
        <Content className={`${prefixCls}-container`}>
          {renderUserInfo(user)}
        </Content>
      </Page>
    );
  };
  return render();
}
export default Form.create({})(observer(UserInfo));
