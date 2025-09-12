package com.gisquest.platform.conf;

import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.*;

import cn.hutool.core.collection.CollectionUtil;
import com.gisquest.platform.common.enums.SymmetricEncryKeyEnum;
import com.gisquest.platform.modules.identity.entity.*;
import com.gisquest.platform.modules.identity.query.UserExtQuery;
import com.gisquest.platform.modules.rightsmgr.RightsmgrService;
import com.gisquest.platform.modules.rightsmgr.entity.RolePrivilege;
import com.gisquest.platform.modules.rightsmgr.query.RolePrivilegeQuery;
import com.gisquest.platform.modules.rolemgr.entity.Role;
import com.gisquest.platform.modules.rolemgr.query.RoleQuery;
import com.gisquest.platform.modules.sysconfig.SysconfigService;
import com.gisquest.platform.modules.sysconfig.entity.KeyValueProperty;
import com.gisquest.platform.modules.sysconfig.query.KeyValuePropertyQuery;
import com.gisquest.platform.utils.EncryptFieldUtils;
import com.gisquest.platform.utils.IntegrityCheckUtils;

import com.gisquest.platform.utils.ThreadPoolMonitor;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.gisquest.cloud.crypto.algorithm.AlgorithmFactory;
import com.gisquest.cloud.crypto.algorithm.handle.AlgorithmHandler;
import com.gisquest.cloud.crypto.algorithm.service.westone.WestoneTools;
import com.gisquest.platform.common.constant.Constant;
import com.gisquest.platform.common.util.StringUtils;
import com.gisquest.platform.common.utils.DesensitizeTools;
import com.gisquest.platform.engine.ProcessEngineConfiguration;
import com.gisquest.platform.engine.ProcessEngines;
import com.gisquest.platform.engine.impl.BaseEngineConfigurationImpl;
import com.gisquest.platform.engine.impl.db.DbSqlSession;
import com.gisquest.platform.engine.interceptor.Command;
import com.gisquest.platform.engine.interceptor.CommandContext;
import com.gisquest.platform.modules.distric.DistricUserService;
import com.gisquest.platform.modules.identity.IdentityService;
import com.gisquest.platform.modules.identity.UserRegionService;
import com.gisquest.platform.modules.identity.query.UserQuery;
import com.gisquest.platform.modules.logmgr.LogmgrService;
import com.gisquest.platform.modules.logmgr.entity.LogDesign;
import com.gisquest.platform.modules.logmgr.query.LogDesignQuery;
import com.gisquest.platform.modules.rolemgr.RolemgrService;
import com.gisquest.platform.modules.rolemgr.entity.UserRole;
import com.gisquest.platform.modules.rolemgr.query.UserRoleQuery;


/**
 * @author wxy
 * @desc:手机号及证件号历史数据初始化处理
 */
@Component
public class EncryptCommandRunner implements ApplicationListener<MigrationCompletedEvent> {
    private static final Logger log = LoggerFactory.getLogger(EncryptCommandRunner.class);

    @Autowired
    protected IdentityService identityService;

    @Autowired
    protected UserRegionService userRegionService;

    @Autowired
    protected DistricUserService districUserService;

    @Autowired
    protected SysconfigService sysconfigService;

    @Autowired
    protected LogmgrService logmgrService;

    @Autowired
    protected RolemgrService rolemgrService;

    @Autowired
    protected RightsmgrService rightsmgrService;

    // 转换未加密的数据分页大小
    @Value("${platform.encrypt.pageSize}")
    protected String pageSize;
    // 是否开启数据加密
    @Value("${platform.encrypt.encryptEnable:true}")
    protected boolean encryptEnable;


    //默认密码加密方式
    @Value("${default.password.encryptType:base}")
    protected String pwdEncryptType;
    //切换密码加密方式后的默认密码
    @Value("${default.password.initPassword:1234}")
    protected String initPassword;

    //手机号和身份证号加密类型
    @Value("${default.cryptoType}")
    protected String cryptoType;

    //是否只处理完整性数据
    @Value("${platform.encrypt.onlyIntegrity:false}")
    protected boolean onlyIntegrity;

    /**
     * 加密完整性是否开启多线程及多线程数量
     * 0或1 表示单线程 有些加密机并发会有问题；
     * n: n>1,表示开启多线程；
     * 默认为0,即默认单线程
     */
    @Value("${platform.encrypt.threadSize:0}")
    protected int encryptThreadSize;

    private String databaseType;

    /**
     * 用于加解密、签名的线程池
     */
    public ThreadPoolExecutor encryptExecutor;

    @Override
    public void onApplicationEvent(MigrationCompletedEvent event) {
        try {
            log.info("MigrationCompletedEvent事件触发，开始执行敏感信息处理");
            if (encryptThreadSize > 1) {
                afterPropertiesSet1MultiThread();
            } else {
                afterPropertiesSet1SingleThread();
            }
        } catch (Exception e) {
            log.error("敏感信息处理异常{}", e.getMessage(), e);
        }
    }

    public void afterPropertiesSet1SingleThread() throws Exception {

        log.info("数据加密开关：{}", encryptEnable);
        log.info("密码加密方式：{},初始密码：{}", pwdEncryptType, initPassword);
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngines.getDefaultProcessEngine().getProcessEngineConfiguration();
        databaseType = processEngineConfiguration.getDatabaseType();
        CompletableFuture.runAsync(() -> {
            if (!onlyIntegrity) {
                if (encryptEnable) {
                    log.info("开始对敏感数据处理----");
                    long start = System.currentTimeMillis();
                    toEncryptUser();
                    toEncryptDepartment();
                    toEncryptUserExt();
                    toEncryptUserRegion();

                    //此功能以用最新的完整性校验方式实现 暂注释
        		/*	if(cryptoType !=null && "westone".equals(cryptoType)) {
        				toEncryptUserCode();
        			}*/
                    toEncryptUserFieldEncrypt();

                    long end = System.currentTimeMillis();
                    log.info("敏感信息处理结束----,耗时{}", end - start);
                } else {
                    //如果已经加密过了，需要解密
                    log.info("开始执行数据解密操作----");
                    long start = System.currentTimeMillis();
                    toDecryptUser();
                    toDecryptDepartment();
                    toDecryptUserExt();
                    toDecryptUserRegion();
                    long end = System.currentTimeMillis();
                    log.info("执行数据解密操作结束----,耗时{}", end - start);
                }

                log.info("开始执行密码加密方式切换脚本----");
                long start = System.currentTimeMillis();
                if (StringUtils.isNotEmpty(pwdEncryptType)) { //ews中pwdEncryptType没配置 为空，不执行

                    KeyValuePropertyQuery keyValuePropertyQuery = sysconfigService.createKeyValuePropertyQuery();
                    keyValuePropertyQuery.key("confirmInitAllPwd"); //确认初始化所有密码
                    List<KeyValueProperty> keyValuePropertyResponses = keyValuePropertyQuery.list();
                    String propertyValue = StringUtils.EMPTY;
                    if (!CollectionUtils.isEmpty(keyValuePropertyResponses)) {
                        propertyValue = keyValuePropertyResponses.get(0).getValue();
                    }
                    log.info("参数变量中确认初始化全部用户密码参数confirmInitAllPwd={}", propertyValue);
                    if (StringUtils.equals("1", propertyValue)) {
                        toSwitchEncryptType();
                    }

                }
                long end = System.currentTimeMillis();
                log.info("执行密码加密方式切换脚本结束----,耗时{}", end - start);
            }
            //用户完整性处理必须在前面的用户数据相关逻辑处理完毕后
            log.info("开始执行用户完整性数据处理----");
            long start = System.currentTimeMillis();
            processIntegrityOfUser();
            long end = System.currentTimeMillis();
            log.info("用户完整性数据处理完毕----,耗时{}ms", end - start);

            //用户扩展信息完整性处理
            log.info("开始执行用户扩展信息完整性数据处理----");
            start = System.currentTimeMillis();
            processIntegrityOfUserExt();
            end = System.currentTimeMillis();
            log.info("用户扩展信息完整性数据处理完毕----,耗时{}ms", end - start);

            //角色信息完整性处理
            log.info("开始执行角色信息完整性数据处理----");
            start = System.currentTimeMillis();
            processIntegrityOfRole();
            end = System.currentTimeMillis();
            log.info("角色信息完整性数据处理完毕----,耗时{}ms", end - start);

            //处理用户角色关联数据完整性，原本这部分可以异步，但是发现并发调卫士通似乎会报错，先改为顺序执行
            log.info("开始执行用户角色完整性数据处理----");
            start = System.currentTimeMillis();
            processIntegrityOfUserRole();
            end = System.currentTimeMillis();
            log.info("用户角色完整性数据处理完毕----,耗时{}ms", end - start);

            //处理用户角色关联数据完整性，原本这部分可以异步，但是发现并发调卫士通似乎会报错，先改为顺序执行
            log.info("开始执行角色权限完整性数据处理----");
            start = System.currentTimeMillis();
            processIntegrityOfRolePrivilege();
            end = System.currentTimeMillis();
            log.info("角色权限完整性数据处理完毕----,耗时{}ms", end - start);

            //处理日志数据完整性，原本这部分可以异步，但是发现并发调卫士通似乎会报错，先改为顺序执行
            log.info("开始执行日志完整性数据处理----");
            start = System.currentTimeMillis();
            processIntegrityOfLog();
            end = System.currentTimeMillis();
            log.info("日志完整性数据处理完毕----,耗时{}ms", end - start);
        });

    }

    public void afterPropertiesSet1MultiThread() throws Exception {

        log.info("数据加密开关：{}", encryptEnable);
        log.info("密码加密方式：{},初始密码：{}", pwdEncryptType, initPassword);
        encryptExecutor = new ThreadPoolExecutor(encryptThreadSize, encryptThreadSize, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue(1000), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
        ThreadPoolExecutor executor = new ThreadPoolExecutor(6, 6, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue(1000), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
        ThreadPoolMonitor.startMonitor(encryptExecutor, 2);
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngines.getDefaultProcessEngine().getProcessEngineConfiguration();
        databaseType = processEngineConfiguration.getDatabaseType();
        CompletableFuture.runAsync(() -> {
            if (!onlyIntegrity) {
                if (encryptEnable) {
                    log.info("开始对敏感数据处理----");
                    long start = System.currentTimeMillis();
                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        asyncEncryptUser();
                        asyncEncryptUserFieldEncrypt();
                    });
//                    CompletableFuture<Void> departmentFutrue = CompletableFuture.runAsync(this::asyncEncryptDepartment, executor);
//                    CompletableFuture<Void> userExtFutrue = CompletableFuture.runAsync(this::asyncEncryptUserExt, executor);
//                    CompletableFuture<Void> userRegionFutrue = CompletableFuture.runAsync(this::asyncEncryptUserRegion, executor);
//                    CompletableFuture.allOf(userFuture, departmentFutrue, userExtFutrue, userRegionFutrue);
                    // todo 待删除
                    CompletableFuture.allOf(userFuture).join();
                    long end = System.currentTimeMillis();
                    log.info("敏感信息处理结束----,耗时{}", end - start);
                } else {
//                    //如果已经加密过了，需要解密
//                    log.info("开始执行数据解密操作----");
//                    long start = System.currentTimeMillis();
//                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(this::asyncDecryptUser, executor);
//                    CompletableFuture<Void> departmentFutrue = CompletableFuture.runAsync(this::asyncDecryptDepartment, executor);
//                    CompletableFuture<Void> userExtFutrue = CompletableFuture.runAsync(this::asyncDecryptUserExt, executor);
//                    CompletableFuture<Void> userRegionFutrue = CompletableFuture.runAsync(this::asyncDecryptUserRegion, executor);
//                    CompletableFuture.allOf(userFuture, departmentFutrue, userExtFutrue, userRegionFutrue);
//                    long end = System.currentTimeMillis();
//                    log.info("执行数据解密操作结束----,耗时{}", end - start);
                }

                log.info("开始执行密码加密方式切换脚本----");
                long start = System.currentTimeMillis();
//                if (StringUtils.isNotEmpty(pwdEncryptType)) { //ews中pwdEncryptType没配置 为空，不执行
//
//                    KeyValuePropertyQuery keyValuePropertyQuery = sysconfigService.createKeyValuePropertyQuery();
//                    keyValuePropertyQuery.key("confirmInitAllPwd"); //确认初始化所有密码
//                    List<KeyValueProperty> keyValuePropertyResponses = keyValuePropertyQuery.list();
//                    String propertyValue = StringUtils.EMPTY;
//                    if (!CollectionUtils.isEmpty(keyValuePropertyResponses)) {
//                        propertyValue = keyValuePropertyResponses.get(0).getValue();
//                    }
//                    log.info("参数变量中确认初始化全部用户密码参数confirmInitAllPwd={}", propertyValue);
//                    if (StringUtils.equals("1", propertyValue)) {
//                        asyncSwitchEncryptType();
//                    }
//                }
                long end = System.currentTimeMillis();
                log.info("执行密码加密方式切换脚本结束----,耗时{}", end - start);
            }
//            //用户完整性处理必须在前面的用户数据相关逻辑处理完毕后
//            CompletableFuture<Void> userFuture = CompletableFuture.runAsync(this::asyncProcessIntegrityOfUser, executor);
//            //用户扩展信息完整性处理
//            CompletableFuture<Void> userExtFutrue = CompletableFuture.runAsync(this::asyncProcessIntegrityOfUserExt, executor);
//            //角色信息完整性处理
//            CompletableFuture<Void> roleFutrue = CompletableFuture.runAsync(this::asyncProcessIntegrityOfRole, executor);
//            //处理用户角色关联数据完整性，原本这部分可以异步，但是发现并发调卫士通似乎会报错，先改为顺序执行
//            CompletableFuture<Void> userRoleFutrue = CompletableFuture.runAsync(this::asyncProcessIntegrityOfUserRole, executor);
//            //处理用户角色关联数据完整性，原本这部分可以异步，但是发现并发调卫士通似乎会报错，先改为顺序执行
//            CompletableFuture<Void> rolePFutrue = CompletableFuture.runAsync(this::asyncProcessIntegrityOfRolePrivilege, executor);
//            //处理日志数据完整性，原本这部分可以异步，但是发现并发调卫士通似乎会报错，先改为顺序执行
//            CompletableFuture<Void> logFutrue = CompletableFuture.runAsync(this::asyncProcessIntegrityOfLog, executor);
//            CompletableFuture.allOf(userFuture, userExtFutrue, roleFutrue, userRoleFutrue, rolePFutrue, logFutrue);
            executor.shutdown();
            encryptExecutor.shutdown();
        });

    }

    private void toSwitchEncryptType() {
        try {
            AlgorithmHandler pwdAlgorithmHandler = AlgorithmFactory.createAlgorithmHandler(pwdEncryptType);
            long count = identityService.createUserQuery().count();
            if (count > 0) {
                long pageNum = getPageNum(count);
                int startNum = 0;
                int pageSize1 = Integer.parseInt(pageSize);

                int maxResults;
                List<User> userResponses;
                List<User> userResponses1;
                String userName;
                UserEntity user;
                for (int i = 1; i <= pageNum; i++) {
                    maxResults = pageSize1 * i;
                    userResponses = identityService.createUserQuery().listPage(startNum,
                            maxResults);
                    userResponses1 = new ArrayList<>();
                    startNum = maxResults;
                    if (!CollectionUtils.isEmpty(userResponses)) {
                        for (User userResponse : userResponses) {
                            userName = userResponse.getUsername();
                            if (StringUtils.isNotEmpty(userName) && !StringUtils.equals(pwdEncryptType, userResponse.getPwdEncryptType())) {
                                String entryptPassword = pwdAlgorithmHandler.encodePassword(userName, initPassword, null);

                                userResponse.setPassWord(entryptPassword);
//                                if (cryptoType != null && "westone".equals(cryptoType)) {
//                                    //原先用的DesensitizeTools.encryptNumber(initPassword)) 会受对称非对称配置项影响 配置成对称加密是采用的sm4加密 为什么不直接用卫士通的sm2加密方法？？
//                                    userResponse.setPassWordSM2(WestoneTools.westoneSM2Encrypt(entryptPassword));
//                                }
                                userResponse.setPwdEncryptType(pwdEncryptType);
                                userResponse.setWzxcheck("false");
                                //pg库不支持批量
                                if ("postgres".equals(databaseType)) {
                                    updateUserPhoneOrPassword(userResponse);
                                } else {
                                    userResponses1.add(userResponse);
                                }
                            }
                        }
                        if (!CollectionUtils.isEmpty(userResponses1)) {
                            updateUserPhoneOrPasswordBatch(userResponses1);
                        }

                    }
                }
            }
        } catch (Exception e) {
            log.error("初始化user表密码失败:{}", e.getMessage());
        }
    }

    public void toEncryptUser() {

        try {
            // 查询脱敏字段is null 数量
            //long userTMIsNullCount = identityService.createUserQuery().isUserTelephoneTmEmpty(true).count();
            long total = identityService.createUserQuery().count();
            if (total <= 0) {
                return;
            }
            long pageNum = getPageNum(total);
            int pageSize1 = Integer.parseInt(pageSize);
            for (int i = 0; i < pageNum; i++) {
                int start = pageSize1 * i;
                List<User> userResponses = identityService.createUserQuery().listPage(start, pageSize1);
                List<User> userList = new ArrayList<>();
                log.info("开始处理user表第{}页旧数据", i);
                if (!CollectionUtils.isEmpty(userResponses)) {
                    AlgorithmHandler defaultHandler;
                    for (User userResponse : userResponses) {
                        User user = new UserEntity();
                        user.setId(userResponse.getId());
                        // 加密、脱敏
                        String telephone = userResponse.getTelephone();
                        if (StringUtils.isNotEmpty(telephone)) {
                            if (StringUtils.isEmpty(userResponse.getTelephoneTm())) {

                                String encryptTelephone = DesensitizeTools.encryptNumber(telephone);
                                if (StringUtils.isEmpty(encryptTelephone)) { //cssp加密失败不会抛出异常，返回空 会导致值全部为空
                                    log.info("以:{}方式加密userId为:{}手机号:{} 失败", cryptoType, userResponse.getId(), telephone);
                                    continue;
                                }

                                String tmTelephone = DesensitizeTools.desensitizePhoneNumber(telephone);
                                user.setTelephone(encryptTelephone);
                                user.setTelephoneTm(tmTelephone);
                                user.setDataEncryptType(cryptoType);
                                userList.add(user);

                            } else {
                                //脱敏字段不为空，说明已经加密，判断是否更换了加密方式 不一致先解密后加密
                                telephone = userResponse.getTelephone();
                                String dataEncryptType = userResponse.getDataEncryptType();
                                if (StringUtils.isEmpty(dataEncryptType)) {
                                    log.error("userId为:{} dataEncryptType值不可为空", userResponse.getId());
                                    continue;
                                }
                                //加密方式改变，解密处理
                                if (!StringUtils.equals(dataEncryptType, cryptoType)) {
                                    defaultHandler = AlgorithmFactory.createAlgorithmHandler(dataEncryptType);
                                    String hexKey = SymmetricEncryKeyEnum.getKeyByCryptoType(dataEncryptType);

                                    try { //解密有问题时继续执行,for中可以加try catch，只有当异常时打印链路日志才会影响执行效率
                                        String decryptTelephone = defaultHandler.symmetricDecryption(telephone, hexKey, null);
                                        if (StringUtils.isNotEmpty(decryptTelephone)) {
                                            //加密处理
                                            String encryptTelephone = DesensitizeTools.encryptNumber(decryptTelephone);
                                            if (StringUtils.isEmpty(encryptTelephone)) { //cssp加密失败不会抛出异常，返回空 会导致值全部为空
                                                log.info("以:{}方式加密userId为:{}手机号:{} 失败", cryptoType, userResponse.getId(), decryptTelephone);
                                                continue;
                                            }
                                            user.setTelephoneTm(userResponse.getTelephoneTm());
                                            user.setTelephone(encryptTelephone);
                                            user.setDataEncryptType(cryptoType);
                                            userList.add(user);
                                        } else {
                                            log.info("以:{}方式解密userId为:{}手机号:{} 失败", cryptoType, userResponse.getId(), telephone);
                                        }
                                    } catch (Exception e) {
                                        log.error("处理user手机号机密性失败:{},userid为:{} 手机号:{}", e.getMessage(), userResponse.getId(), telephone);
                                    }
                                }
                            }
                        } else { //手机号为空 同步修改掉dataEncryptType
                            if (StringUtils.equals(cryptoType, userResponse.getDataEncryptType())) {
                                continue;
                            }
                            user.setTelephone(userResponse.getTelephone());
                            user.setTelephoneTm(userResponse.getTelephoneTm());
                            user.setDataEncryptType(cryptoType);
                            userList.add(user);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(userList)) {
                        try {
                            identityService.updateBatchUsers(userList);
                        } catch (Exception e) {
                            log.error("第{}页用户机密性批量更新失败", i, e);
                        }
                    }
                }
                log.info("处理user表第{}页旧数据完成", i);
            }

        } catch (Exception e) {
            log.error("处理user表失败：{}", e.getMessage());
        }
    }

    private void toEncryptUserFieldEncrypt() {
        if (!EncryptFieldUtils.isEnabledEncryptField()) {
            log.info("当前未开启存储机密性开关参数【{}】，不处理", Constant.ENABLED_ENCRYPT_FIELD);
            return;
        }
        //处理userCode和staffName
        try {
            UserQuery userQuery = identityService.createUserQuery().encryptIsNullOrNotTrue();
            long count = userQuery.count();
            log.info("待处理用户机密性总数 => {}", count);
            if (count == 0) {
                log.info("没有需要处理机密性性的用户数据");
                return;
            }
            long pageNum = getPageNum(count);
            int start = 0;
            for (int i = 1; i <= pageNum; i++) {
                log.info("开始处理用户表第{}页旧数据完整性", i);
                List<User> unEncryptUserList = userQuery.listPage(start, Integer.parseInt(pageSize));
                List<User> updateUserList = new ArrayList<>();
                if (CollectionUtil.isNotEmpty(unEncryptUserList)) {
                    for (User userResponse : unEncryptUserList) {
                        try {
                            User encryptUser = new UserEntity();
                            encryptUser.setId(userResponse.getId());
                            // 加密、脱敏
                            String userCode = userResponse.getUserCode();
                            if (StringUtils.isNotEmpty(userCode)) {
                                String encryptUserCode = EncryptFieldUtils.encryptNumberV5(userCode);
                                encryptUser.setUserCode(encryptUserCode);
                            }
                            String staffName = userResponse.getStaffName();
                            if (StringUtils.isNotEmpty(staffName)) {
                                String encryptStaffName = EncryptFieldUtils.encryptNumberV5(staffName);
                                encryptUser.setStaffName(encryptStaffName);
                            }
                            encryptUser.setEncrypt(1);
                            if (StringUtils.isBlank(userCode) && StringUtils.isBlank(staffName)) {
                                continue;
                            }
                            updateUserList.add(encryptUser);
                        } catch (Exception e) {
                            log.error("本条用户存储机密性数据处理失败，userId => {}", userResponse.getId(), e);
                        }
                    }
                    try {
                        if (CollectionUtils.isNotEmpty(updateUserList)) {
                            identityService.updateBatchUsers(updateUserList);
                        }
                    } catch (Exception e) {
                        log.error("第{}页用户存储机密性数据批量更新失败", i, e);
                        //当前批次失败后，跳过这一批，多批失败，则跳过多批，等能成功的都成功后，下次重启服务来处理之前失败的批次
                        start += Integer.parseInt(pageSize);
                    }
                }
                log.info("用户表第{}页旧数据存储机密性处理完成", i);
            }
        } catch (Exception e) {
            log.error("用户存储机密性数据处理失败", e.getMessage(), e);
        }
    }

    //卫斯通处理bt_user表中userCode加密
    public void toEncryptUserCode() {
        try {
            //查询UsercodeSM2字段is null 数量
            long userCodeSm2NullCount = identityService.createUserQuery().isUserCodeSm2Empty(true).count();
            if (userCodeSm2NullCount > 0) {
                long pageNum = getPageNum(userCodeSm2NullCount);
                int pageSize1 = Integer.parseInt(pageSize);
                List<User> userResponses1 = new ArrayList<>();
                for (int i = 1; i <= pageNum; i++) {
                    int maxResults = pageSize1 * i;
                    List<User> userResponses = identityService.createUserQuery().isUserCodeSm2Empty(true).listPage(0, maxResults);
                    if (!CollectionUtils.isEmpty(userResponses)) {
                        log.info("开始处理user表第{}页旧数据", i);
                        List<User> userList = new ArrayList<User>();
                        for (User userResponse : userResponses) {
                            String userCodeSm2 = userResponse.getUsercodeSM2();
                            String userCode = userResponse.getUserCode();
                            if (StringUtils.isNotEmpty(userCode) && StringUtils.isEmpty(userCodeSm2)) {
                                String s = DesensitizeTools.encryptNumber(userCode);
                                UserEntity user = new UserEntity();
                                user.setId(userResponse.getId());
                                user.setUserCode(s);
                                user.setUsercodeSM2(WestoneTools.westoneSM2Encrypt(s));
                                if ("postgres".equals(databaseType)) {
                                    updateUserCode(user);
                                } else {
                                    userResponses1.add(user);
                                }
                            }
                        }
                        if (!CollectionUtils.isEmpty(userResponses1)) {
                            //去掉之前一条一条更新改成批量更新
                            identityService.updateBatchUsers(userResponses1);
                        }
                        log.info("处理user表第{}页旧数据完成", i);
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理user表失败：{}", e.getMessage());
        }
    }

    public void toEncryptDepartment() {
        try {
            // 查询脱敏字段is null 数量
            //long userTMIsNullCount = identityService.createDepartmentQuery().isDepartmentTelephoneTmEmpty(true).count();
            long total = identityService.createDepartmentQuery().count();
            if (total <= 0) {
                return;
            }

            int pageNum = getPageNum(total);
            int pageSize1 = Integer.parseInt(pageSize);
            // 构建分页查询
            for (int i = 0; i < pageNum; i++) {
                int start = pageSize1 * i;
                List<Department> departmentResponseList = identityService.createDepartmentQuery().listPage(start, pageSize1);
                List<Department> departmentList = new ArrayList<>();
                if (!CollectionUtils.isEmpty(departmentResponseList)) {
                    log.info("开始处理Department表第{}页旧数据", i);
                    for (Department departmentResponse : departmentResponseList) {
                        // 加密、脱敏
                        String telephone = departmentResponse.getTelephone();
                        Department departmentEntity = new DepartmentEntity();
                        departmentEntity.setId(departmentResponse.getId());
                        if (StringUtils.isNotEmpty(telephone)) {
                            if (StringUtils.isEmpty(departmentResponse.getTelephoneTm())) {
                                String encryptTelephone = DesensitizeTools.encryptNumber(telephone);
                                if (StringUtils.isEmpty(encryptTelephone)) { //cssp加密失败不会抛出异常，返回空 会导致值全部为空
                                    log.info("手机号:[{}]加密失败, 加密方式:[{}], 部门ID:[{}]", telephone, cryptoType, departmentResponse.getId());
                                    continue;
                                }
                                String tmTelephone = DesensitizeTools.desensitizePhoneNumber(departmentResponse.getTelephone());
                                departmentEntity.setTelephone(encryptTelephone);
                                departmentEntity.setTelephoneTm(tmTelephone);
                                departmentEntity.setDataEncryptType(cryptoType);
                                departmentList.add(departmentEntity);

                            } else { //脱敏字段不为空，说明已经加密，判断是否更换了加密方式
                                log.info("部门[{}]脱敏字段不为空", departmentResponse.getId());
                                String dataEncryptType = departmentResponse.getDataEncryptType();
                                if (StringUtils.isEmpty(dataEncryptType)) {
                                    log.error("部门[{}]的dataEncryptType值为空，请检查！", departmentResponse.getId());
                                    continue;
                                }

                                //加密方式改变，切换加密
                                if (!StringUtils.equals(dataEncryptType, cryptoType)) {
                                    log.info("部门[{}]数据加密方式已改变，旧方式：[{}], 新方式：[{}]", departmentResponse.getId(), dataEncryptType, cryptoType);
                                    //按旧的加密方式解密手机号
                                    AlgorithmHandler defaultHandler = AlgorithmFactory.createAlgorithmHandler(dataEncryptType);
                                    String hexKey = SymmetricEncryKeyEnum.getKeyByCryptoType(dataEncryptType);

                                    String decryptTelephone = defaultHandler.symmetricDecryption(telephone, hexKey, null);
                                    if (StringUtils.isEmpty(decryptTelephone)) {
                                        //cssp加密失败不会抛出异常，返回空 会导致值全部为空
                                        log.info("部门[{}]手机号[{}]解密失败", departmentResponse.getId(), telephone);
                                        continue;
                                    }

                                    //按新的加密方式进行加密
                                    String encryptTelephone = DesensitizeTools.encryptNumber(telephone);
                                    if (StringUtils.isEmpty(encryptTelephone)) {
                                        //cssp加密失败不会抛出异常，返回空 会导致值全部为空
                                        log.info("部门[{}]手机号[{}]加密失败", departmentResponse.getId(), encryptTelephone);
                                        continue;
                                    }
                                    departmentEntity.setTelephoneTm(departmentResponse.getTelephoneTm());
                                    departmentEntity.setTelephone(encryptTelephone);
                                    departmentEntity.setDataEncryptType(cryptoType);
                                    departmentList.add(departmentEntity);
                                }
                            }
                        } else {
                            //手机号为空 同步修改掉dataEncryptType
                            if (StringUtils.equals(cryptoType, departmentResponse.getDataEncryptType())) {
                                continue;
                            }
                            log.info("部门[{}({})]的手机号为空", departmentResponse.getName(), departmentResponse.getId());
                            departmentEntity.setTelephone(departmentResponse.getTelephone());
                            departmentEntity.setTelephoneTm(departmentResponse.getTelephoneTm());
                            departmentEntity.setDataEncryptType(cryptoType);
                            departmentList.add(departmentEntity);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(departmentList)) {
                        try {
                            identityService.updateBatchDepartments(departmentList);
                        } catch (Exception e) {
                            log.error("第{}页部门信息机密性数据批量更新失败", i, e);
                        }
                    }
                    log.info("处理Department表第{}页旧数据完成", i);
                }
            }

        } catch (Exception e) {
            log.error("处理Department表失败：{}", e.getMessage());
        }
    }

    public void toEncryptUserExt() {

        try {
            // 查询脱敏字段is null 数量
            //long userExtTmIsNullCount = identityService.createUserExtQuery().count();
            long total = identityService.createUserExtQuery().count();
            if (total <= 0) {
                return;
            }
            int pageNum = getPageNum(total);
            int pageSize1 = Integer.parseInt(pageSize);
            for (int i = 0; i < pageNum; i++) {
                int start = pageSize1 * i;
                List<UserExt> userExtResponseList = identityService.createUserExtQuery().listPage(start, pageSize1);
                List<UserExt> userExtList = new ArrayList<>();
                if (!CollectionUtils.isEmpty(userExtResponseList)) {
                    log.info("开始处理userExt第{}页旧数据", i);
                    AlgorithmHandler defaultHandler;

                    for (UserExt userExtResponse : userExtResponseList) {
                        // 加密、脱敏
                        String certificatecode = userExtResponse.getCertificatecode();
                        String certificatecodeTm = null;
                        UserExt userExtEntity = new UserExtEntity();
                        userExtEntity.setId(userExtResponse.getId());

                        if (StringUtils.isNotEmpty(certificatecode)) {
                            if (StringUtils.isEmpty(userExtResponse.getCertificatecodeTm())) {
                                String encryptCertificatecode = DesensitizeTools.encryptNumber(certificatecode);
                                if (StringUtils.isEmpty(encryptCertificatecode)) { //cssp加密失败不会抛出异常，返回空 会导致值全部为空
                                    log.info("以:{}方式加密userExtId为:{}证件号:{} 失败", cryptoType, userExtResponse.getId(), certificatecode);
                                    continue;
                                }
                                certificatecodeTm = DesensitizeTools.desensitizeIdCardNumber(userExtResponse.getCertificatecode());
                                userExtEntity.setCertificatecode(encryptCertificatecode);
                                userExtEntity.setCertificatecodeTm(certificatecodeTm);
                                userExtEntity.setDataEncryptType(cryptoType);
                                userExtList.add(userExtEntity);
                            } else { //判断是否切换了加密方式

                                String dataEncryptType = userExtResponse.getDataEncryptType(); //dataEncryptType为空会报null异常，不报具体信息
                                if (StringUtils.isEmpty(dataEncryptType)) {
                                    log.error("userExtId为:{}证件号:{} dataEncryptType值不可为空，请检查数据", userExtResponse.getId(), certificatecode);
                                    continue;
                                }
                                defaultHandler = AlgorithmFactory.createAlgorithmHandler(dataEncryptType);
                                String hexKey = SymmetricEncryKeyEnum.getKeyByCryptoType(dataEncryptType);

                                if (StringUtils.equals(dataEncryptType, cryptoType)) { //加密方式改变，解密处理
                                    continue;
                                }

                                try { //各种原因导致的个别数据解密失败，不影响后续数据解密
                                    String decryptCertificatecode = defaultHandler.symmetricDecryption(userExtResponse.getCertificatecode(), hexKey, null);
                                    if (StringUtils.isNotBlank(decryptCertificatecode)) { //cssp加密失败不会抛出异常，返回空 会导致值全部为空
                                        String encryptCertificatecode = DesensitizeTools.encryptNumber(decryptCertificatecode);
                                        if (StringUtils.isEmpty(encryptCertificatecode)) {
                                            log.info("以:{}方式加密userExtId为:{}证件号:{} 失败", cryptoType, userExtResponse.getId(), certificatecode);
                                            continue;
                                        }
                                        userExtEntity.setCertificatecodeTm(userExtResponse.getCertificatecodeTm());
                                        userExtEntity.setCertificatecode(encryptCertificatecode);
                                        userExtEntity.setDataEncryptType(cryptoType);
                                        userExtList.add(userExtEntity);
                                    } else {
                                        log.info("以:{}方式解密userExtId为:{}证件号:{} 失败", dataEncryptType, userExtEntity.getId(), userExtResponse.getCertificatecode());
                                    }
                                } catch (Exception e) {
                                    log.error("处理userExt身份证号失败:{},userExtId为:{}证件号:{}", e.getMessage(), userExtResponse.getId(), certificatecode);
                                }
                            }
                        } else {
                            if (StringUtils.equals(cryptoType, userExtResponse.getDataEncryptType())) { //加密方式改变，解密处理
                                continue;
                            }
                            userExtEntity.setCertificatecodeTm(userExtResponse.getCertificatecodeTm());
                            userExtEntity.setCertificatecode(userExtResponse.getCertificatecode());
                            userExtEntity.setDataEncryptType(cryptoType);
                            userExtList.add(userExtEntity);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(userExtList)) {
                        try {
                            identityService.updateBatchUserExts(userExtList);
                        } catch (Exception e) {
                            log.error("第{}页用户扩展信息机密性数据批量更新失败", i, e);
                        }
                    }
                    log.info("处理userExt表第{}页旧数据完成", i);
                }
            }
        } catch (Exception e) {
            log.error("处理userExt表失败：{}", e.getMessage());
        }
    }

    public void toEncryptUserRegion() {
        try {
            // 查询脱敏字段is null 数量
            long userReginTmIsNullCount = userRegionService.createUserRegionQuery().isRegionTeleTmEmpty(true).count();
            // 脱敏字段未修改 全部为空才执行下面逻辑
            if (userReginTmIsNullCount > 0) {
                int pageNum = getPageNum(userReginTmIsNullCount);
                int pageSize1 = Integer.parseInt(pageSize);
                for (int i = 1; i <= pageNum; i++) {
                    int maxResults = pageSize1 * i;
                    List<UserRegion> userRegionResponse = userRegionService.createUserRegionQuery()
                            .isRegionTeleTmEmpty(true).listPage(0, maxResults);
                    List<UserRegion> userRegionResponse1 = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(userRegionResponse)) {
                        log.info("开始处理userRegion第{}页旧数据", i);
                        for (UserRegion userRegion : userRegionResponse) {
                            // 加密、脱敏
                            String regionTelephone = userRegion.getRegionTelephone();
                            String regionTelephoneTm = null;
                            if (StringUtils.isNotEmpty(regionTelephone)
                                    && StringUtils.isEmpty(userRegion.getRegionTelephoneTm())) {
                                // wxy 20230703 刘月华要求的----历史数据加密时手机号不做任何匹配，直接加密处理
                                regionTelephone = DesensitizeTools.encryptNumber(regionTelephone);
                                if (StringUtils.isNotEmpty(regionTelephone)) {
                                    regionTelephoneTm = DesensitizeTools
                                            .desensitizePhoneNumber(userRegion.getRegionTelephone());
                                    userRegion.setRegionTelephone(regionTelephone);
                                    userRegion.setRegionTelephoneTm(regionTelephoneTm);
                                    //pg库不支持批量
                                    if ("postgres".equals(databaseType)) {
                                        updateUserRegionTelephone(userRegion);
                                    } else {
                                        userRegionResponse1.add(userRegion);
                                    }
                                }
                            }
                        }
                        if (!CollectionUtils.isEmpty(userRegionResponse1)) {
                            updateUserRegionTelephoneBatch(userRegionResponse1);
                        }
                        log.info("处理userRegion表第{}页旧数据完成", i);
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理userRegion表失败：{}", e.getMessage());
        }
    }

    private void toDecryptUser() {
        try {
            long count = identityService.createUserQuery().count();
            if (count > 0) {
                long pageNum = getPageNum(count);
                int startNum = 0;
                int pageSize1 = Integer.parseInt(pageSize);

                int maxResults;
                List<User> userResponses;
                List<User> userResponses1;
                String telephone;
                UserEntity user;
                for (int i = 1; i <= pageNum; i++) {
                    maxResults = pageSize1 * i;
                    userResponses = identityService.createUserQuery().listPage(startNum,
                            maxResults);
                    userResponses1 = new ArrayList<>();
                    startNum = maxResults;
                    if (!CollectionUtils.isEmpty(userResponses)) {
                        log.info("开始解密user表第{}页数据", i);
                        for (User userResponse : userResponses) {
                            telephone = userResponse.getTelephone();
                            boolean telephoneDecrypt = false;
                            if (StringUtils.isNotEmpty(telephone) && StringUtils.isNotEmpty(userResponse.getTelephoneTm())) {
                                try { //解密有问题时继续执行,for中可以加try catch，只有当异常时打印链路日志才会影响执行效率
                                    telephone = DesensitizeTools.decryptNumber(telephone);
                                    userResponse.setTelephone(telephone);
                                    userResponse.setTelephoneTm("");
                                    telephoneDecrypt = true;
                                } catch (Exception e) {
                                    log.error("用户id:{} 手机号:{} 解密手机号失败：{}", userResponse.getId(), userResponse.getTelephone(), e.getMessage());
                                }
                            }
                            boolean fieldDecrypt = false;
                            if (userResponse.getEncrypt() != null && 1 == userResponse.getEncrypt()) {
                                String userCode = userResponse.getUserCode();
                                String staffName = userResponse.getStaffName();
                                String unEncryptUserCode = null;
                                String unEncryptStaffName = null;
                                try {
                                    if (StringUtils.isNotEmpty(userCode)) {
                                        unEncryptUserCode = EncryptFieldUtils.decryptNumberV5(userCode);
                                    }
                                } catch (Exception e) {
                                    log.error("用户id:{} 用户编码:{} 解密用户名失败：{}", userResponse.getId(), userResponse.getUserCode(), e.getMessage());
                                }
                                try {
                                    if (StringUtils.isNotEmpty(staffName)) {
                                        unEncryptStaffName = EncryptFieldUtils.decryptNumberV5(staffName);
                                    }
                                } catch (Exception e) {
                                    log.error("用户id:{} 用户昵称:{} 解密昵称失败：{}", userResponse.getId(), userResponse.getStaffName(), e.getMessage());
                                }

                                if ((StringUtils.isBlank(userCode) || StringUtils.isNotBlank(unEncryptUserCode)) && (StringUtils.isBlank(staffName) || StringUtils.isNotBlank(unEncryptStaffName))) {
                                    userResponse.setUserCode(unEncryptUserCode);
                                    userResponse.setStaffName(unEncryptStaffName);
                                    userResponse.setEncrypt(0);
                                    fieldDecrypt = true;
                                }

                            }

                            userResponse.setPassWord(null);//设为null，只更新手机号

                            if (telephoneDecrypt || fieldDecrypt) {
                                userResponses1.add(userResponse);
                            }
                        }

                        if (CollectionUtils.isNotEmpty(userResponses1)) {
                            identityService.updateBatchUsers(userResponses1);
                        }
                        log.info("解密处理user表第{}页数据完成", i);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解密处理user表失败:{}", e);
        }
    }

    private void toDecryptDepartment() {
        try {
            long count = identityService.createDepartmentQuery().count();
            if (count > 0) {
                int pageNum = getPageNum(count);
                int startNum = 0;
                int pageSize1 = Integer.parseInt(pageSize);


                int maxResults;
                List<Department> departmentResponse;
                List<Department> departmentResponse1;
                String telephone;
                for (int i = 1; i <= pageNum; i++) {
                    maxResults = pageSize1 * i;
                    departmentResponse = identityService.createDepartmentQuery().listPage(startNum, maxResults);
                    departmentResponse1 = new ArrayList<>();
                    startNum = maxResults;
                    if (!CollectionUtils.isEmpty(departmentResponse)) {
                        log.info("开始解密Department表第{}页数据", i);
                        for (Department depResponse : departmentResponse) {
                            telephone = depResponse.getTelephone();
                            if (StringUtils.isNotEmpty(telephone) && StringUtils.isNotEmpty(depResponse.getTelephoneTm())) {
                                try { //解密有问题时继续执行,for中可以加try catch，只有当异常时打印链路日志才会影响执行效率
                                    telephone = DesensitizeTools.decryptNumber(telephone);

                                    if (StringUtils.isNotEmpty(telephone)) {

                                        depResponse.setTelephone(telephone);
                                        depResponse.setTelephoneTm("");
                                        //pg库不支持批量
                                        if ("postgres".equals(databaseType)) {
                                            updateDepartmentPhone(depResponse);
                                        } else {
                                            departmentResponse1.add(depResponse);
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error("部门id:{} 手机号:{} 解密手机号失败：{}", depResponse.getId(), depResponse.getTelephone(), e.getMessage());
                                }
                            }
                        }
                        if (!CollectionUtils.isEmpty(departmentResponse1)) {
                            updateDepartmentPhoneBatch(departmentResponse1);
                        }
                        log.info("解密Department表第{}页数据完成", i);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解密处理Department表失败：{}", e.getMessage());
        }
    }

    private void toDecryptUserExt() {

        try {
            long count = identityService.createUserExtQuery().count();
            if (count > 0) {
                int pageNum = getPageNum(count);
                int startNum = 0;
                int pageSize1 = Integer.parseInt(pageSize);

                int maxResults;
                List<UserExt> userExtResponse;
                List<UserExt> userExtResponseList;
                String certificatecode;
                UserExtEntity userExtEntity;
                for (int i = 1; i <= pageNum; i++) {
                    maxResults = pageSize1 * i;
                    userExtResponse = identityService.createUserExtQuery()
                            .listPage(startNum, maxResults);
                    userExtResponseList = new ArrayList<>();
                    startNum = maxResults;
                    if (!CollectionUtils.isEmpty(userExtResponse)) {
                        log.info("开始解密处理userExt第{}页数据", i);
                        for (UserExt userExtResponse1 : userExtResponse) {
                            certificatecode = userExtResponse1.getCertificatecode();

                            if (StringUtils.isNotEmpty(certificatecode) && StringUtils.isNotEmpty(userExtResponse1.getCertificatecodeTm())) {
                                try { //解密有问题时继续执行,for中可以加try catch，只有当异常时打印链路日志才会影响执行效率

                                    certificatecode = DesensitizeTools.decryptNumber(certificatecode);
                                    if (StringUtils.isNotEmpty(certificatecode)) {

                                        userExtResponse1.setCertificatecode(certificatecode);
                                        userExtResponse1.setCertificatecodeTm("");
                                        //pg库不支持批量
                                        if ("postgres".equals(databaseType)) {
                                            updateUserExtCertificatecode(userExtResponse1);
                                        } else {
                                            userExtResponseList.add(userExtResponse1);
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error("用户扩展id:{} 证件号:{} 解密证件号失败：{}", userExtResponse1.getId(), certificatecode, e.getMessage());
                                }
                            }
                        }
                        if (!CollectionUtils.isEmpty(userExtResponseList)) {
                            updateUserExtCertificatecodeBatch(userExtResponseList);
                        }
                        log.info("解密userExt表第{}页数据完成", i);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解密处理userExt表失败：{}", e.getMessage());
        }

    }

    private void toDecryptUserRegion() {
        try {
            long count = selectUserRegionByQueryCriteriaAll();

            if (count > 0) {
                int pageNum = getPageNum(count);
                int startNum = getPageNum(count);
                int pageSize1 = Integer.parseInt(pageSize);

                int maxResults;
                List<UserRegion> userRegionResponse;
                List<UserRegion> userRegionResponse1;
                String regionTelephone;
                for (int i = 1; i <= pageNum; i++) {
                    maxResults = pageSize1 * i;
                    userRegionResponse = userRegionService.createUserRegionQuery().listPage(startNum, maxResults);
                    userRegionResponse1 = new ArrayList<>();
                    startNum = maxResults;
                    if (!CollectionUtils.isEmpty(userRegionResponse)) {
                        log.info("开始解密处理userRegion第{}页数据", i);
                        for (UserRegion userRegion : userRegionResponse) {
                            regionTelephone = userRegion.getRegionTelephone();
                            if (StringUtils.isNotEmpty(regionTelephone) && StringUtils.isNotEmpty(userRegion.getRegionTelephoneTm())) {
                                try { //解密有问题时继续执行,for中可以加try catch，只有当异常时打印链路日志才会影响执行效率
                                    regionTelephone = DesensitizeTools.decryptNumber(regionTelephone);
                                    if (StringUtils.isNotEmpty(regionTelephone)) {

                                        userRegion.setRegionTelephone(regionTelephone);
                                        userRegion.setRegionTelephoneTm("");
                                        //pg库不支持批量
                                        if ("postgres".equals(databaseType)) {
                                            updateUserRegionTelephone(userRegion);
                                        } else {
                                            userRegionResponse1.add(userRegion);
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error("userRegionid:{} 手机号:{} 解密手机号失败：{}", userRegion.getId(), regionTelephone, e.getMessage());
                                }
                            }
                        }
                        if (!CollectionUtils.isEmpty(userRegionResponse1)) {
                            updateUserRegionTelephoneBatch(userRegionResponse1);
                        }
                        log.info("解密userRegion表第{}页数据完成", i);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解密处理userRegion表失败：{}", e.getMessage());
        }
    }

    /**
     * 处理用户信息完整性
     */
    private void processIntegrityOfUser() {
        if (!IntegrityCheckUtils.isEnabledMac()) {
            log.info("当前未开启完整性校验参数【{}】，不处理", Constant.ENABLED_MAC);
            return;
        }
        try {
            UserQuery userQuery = identityService.createUserQuery().wzxcheckIsNullOrNotTrue();
            long count = userQuery.count();
            log.info("待处理用户完整性总数 => {}", count);
            if (count == 0) {
                log.info("没有需要处理完整性的用户数据");
                return;
            }
            long pageNum = getPageNum(count);
            int start = 0;
            for (int i = 1; i <= pageNum; i++) {
                log.info("开始处理用户表第{}页旧数据完整性", i);
                List<User> userList = userQuery.listPage(start, Integer.parseInt(pageSize));
                if (CollectionUtil.isNotEmpty(userList)) {
                    for (User user : userList) {
                        /**
                         *  由于快速构建V5.0版本 完整性处理时，staffName和userCode是明文的 所以V4.1同步处理 ,  数据先解密 明文数据做完整性处理
                         */
                        String userCode = user.getUserCode();
                        String staffName = user.getStaffName();
                        if (user.getEncrypt() != null && user.getEncrypt() == 1) {
                            try {
                                if (StringUtils.isNotEmpty(userCode)) {
                                    String unEncryptUserCode = EncryptFieldUtils.decryptNumberV5(userCode);
                                    user.setUserCode(unEncryptUserCode);
                                }
                            } catch (Exception e) {
                                log.error("用户id:{} 用户编码:{} 解密用户名失败：{}, 跳过本次完整性处理", user.getId(), user.getUserCode(), e.getMessage());
                                continue;
                            }
                            try {
                                if (StringUtils.isNotEmpty(staffName)) {
                                    String unEncryptStaffName = EncryptFieldUtils.decryptNumberV5(staffName);
                                    user.setStaffName(unEncryptStaffName);
                                }
                            } catch (Exception e) {
                                log.error("用户id:{} 用户昵称:{} 解密昵称失败：{}, 跳过本次完整性处理", user.getId(), user.getStaffName(), e.getMessage());
                                continue;
                            }
                        }

                        try {
                            IntegrityCheckUtils.generateMac(user);
                        } catch (Exception e) {
                            log.error("本条用户完整性数据处理失败，userId => {}", user.getId(), e);
                        }
                        if (user.getEncrypt() != null && user.getEncrypt() == 1) {
                            // 做完整性处理后 恢复之前的密文
                            user.setStaffName(staffName);
                            user.setUserCode(userCode);
                        }
                    }
                    try {
                        identityService.updateBatchUsers(userList);
                    } catch (Exception e) {
                        log.error("第{}页用户完整性数据批量更新失败", i, e);
                        //当前批次失败后，跳过这一批，多批失败，则跳过多批，等能成功的都成功后，下次重启服务来处理之前失败的批次
                        start += Integer.parseInt(pageSize);
                    }
                }
                log.info("用户表第{}页旧数据完整性处理完成", i);
            }
        } catch (Exception e) {
            log.error("用户完整性数据处理失败", e.getMessage(), e);
        }
    }

    /**
     * 处理用户扩展信息完整性
     */
    private void processIntegrityOfUserExt() {
        if (!IntegrityCheckUtils.isEnabledMac()) {
            log.info("当前未开启完整性校验参数【{}】，不处理", Constant.ENABLED_MAC);
            return;
        }
        try {
            UserExtQuery userExtQuery = identityService.createUserExtQuery().wzxcheckIsNullOrNotTrue();
            long count = userExtQuery.count();
            log.info("待处理用户扩展信息完整性总数 => {}", count);
            if (count == 0) {
                log.info("没有需要处理完整性的用户扩展信息数据");
                return;
            }
            long pageNum = getPageNum(count);
            int start = 0;
            for (int i = 1; i <= pageNum; i++) {
                log.info("开始处理用户扩展信息表第{}页旧数据完整性", i);
                List<UserExt> userExtList = userExtQuery.listPage(start, Integer.parseInt(pageSize));
                if (CollectionUtil.isNotEmpty(userExtList)) {
                    for (UserExt userExt : userExtList) {
                        try {
                            IntegrityCheckUtils.generateMac(userExt);
                        } catch (Exception e) {
                            log.error("本条用户扩展信息完整性数据处理失败，userId => {}", userExt.getId(), e);
                        }
                    }
                    try {
                        identityService.updateBatchUserExts(userExtList);
                    } catch (Exception e) {
                        log.error("第{}页用户扩展信息完整性数据批量更新失败", i, e);
                        //当前批次失败后，跳过这一批，多批失败，则跳过多批，等能成功的都成功后，下次重启服务来处理之前失败的批次
                        start += Integer.parseInt(pageSize);
                    }
                }
                log.info("用户扩展信息表第{}页旧数据完整性处理完成", i);
            }
        } catch (Exception e) {
            log.error("用户扩展信息完整性数据处理失败", e.getMessage(), e);
        }
    }

    /**
     * 处理角色信息完整性
     */
    private void processIntegrityOfRole() {
        if (!IntegrityCheckUtils.isEnabledMac()) {
            log.info("当前未开启完整性校验参数【{}】，不处理", Constant.ENABLED_MAC);
            return;
        }
        try {
            RoleQuery roleQuery = rolemgrService.createRoleQuery().wzxcheckIsNullOrNotTrue();
            long count = roleQuery.count();
            log.info("待处理用户完整性总数 => {}", count);
            if (count == 0) {
                log.info("没有需要处理完整性的用户数据");
                return;
            }
            long pageNum = getPageNum(count);
            int start = 0;
            for (int i = 1; i <= pageNum; i++) {
                log.info("开始处理用户表第{}页旧数据完整性", i);
                List<Role> roleList = roleQuery.listPage(start, Integer.parseInt(pageSize));
                if (CollectionUtil.isNotEmpty(roleList)) {
                    for (Role role : roleList) {
                        try {
                            IntegrityCheckUtils.generateMac(role);
                        } catch (Exception e) {
                            log.error("本条角色完整性数据处理失败，roleId => {}", role.getId(), e);
                        }
                    }
                    try {
                        rolemgrService.updateRoleBatch(roleList);
                    } catch (Exception e) {
                        log.error("第{}页角色完整性数据批量更新失败", i, e);
                        //当前批次失败后，跳过这一批，多批失败，则跳过多批，等能成功的都成功后，下次重启服务来处理之前失败的批次
                        start += Integer.parseInt(pageSize);
                    }
                }
                log.info("角色表第{}页旧数据完整性处理完成", i);
            }
        } catch (Exception e) {
            log.error("角色完整性数据处理失败", e.getMessage(), e);
        }
    }

    /**
     * 处理用户角色信息完整性
     */
    private void processIntegrityOfUserRole() {
        if (!IntegrityCheckUtils.isEnabledMac()) {
            log.info("当前未开启完整性校验参数【{}】，不处理", Constant.ENABLED_MAC);
            return;
        }
        try {
            UserRoleQuery userRoleQuery = rolemgrService.createUserRoleQuery().wzxcheckIsNullOrNotTrue();
            long count = userRoleQuery.count();
            log.info("待处理用户角色关联完整性总数 => {}", count);
            if (count == 0) {
                log.info("没有需要处理完整性的用户角色数据");
                return;
            }
            long pageNum = getPageNum(count);
            int start = 0;
            for (int i = 1; i <= pageNum; i++) {
                log.info("开始处理用户角色关联表第{}页旧数据完整性", i);
                List<UserRole> userRoleList = userRoleQuery.listPage(start, Integer.parseInt(pageSize));
                if (CollectionUtil.isNotEmpty(userRoleList)) {
                    for (UserRole userRole : userRoleList) {
                        try {
                            IntegrityCheckUtils.generateMac(userRole);
                        } catch (Exception e) {
                            log.error("本条用户角色完整性数据处理失败，userRoleId => {}", userRole.getId(), e);
                        }
                    }
                    try {
                        rolemgrService.updateUserRoleBatch(userRoleList);
                    } catch (Exception e) {
                        log.error("第{}页用户角色关联完整性数据批量更新失败", i, e);
                        //当前批次失败后，跳过这一批，多批失败，则跳过多批，等能成功的都成功后，下次重启服务来处理之前失败的批次
                        start += Integer.parseInt(pageSize);
                    }
                }
                log.info("用户角色关联表第{}页旧数据完整性处理完成", i);
            }
        } catch (Exception e) {
            log.error("用户角色完整性数据处理失败", e.getMessage(), e);
        }
    }

    /**
     * 处理角色权限信息完整性
     */
    private void processIntegrityOfRolePrivilege() {
        if (!IntegrityCheckUtils.isEnabledMac()) {
            log.info("当前未开启完整性校验参数【{}】，不处理", Constant.ENABLED_MAC);
            return;
        }
        try {
            RolePrivilegeQuery rolePrivilegeQuery = rightsmgrService.createRolePrivilegeQuery().wzxcheckIsNullOrNotTrue();
            long count = rolePrivilegeQuery.count();
            log.info("待处理角色权限关联完整性总数 => {}", count);
            if (count == 0) {
                log.info("没有需要处理完整性的角色权限数据");
                return;
            }
            long pageNum = getPageNum(count);
            int start = 0;
            for (int i = 1; i <= pageNum; i++) {
                log.info("开始处理角色权限关联表第{}页旧数据完整性", i);
                List<RolePrivilege> rolePrivilegeList = rolePrivilegeQuery.listPage(start, Integer.parseInt(pageSize));
                if (CollectionUtil.isNotEmpty(rolePrivilegeList)) {
                    for (RolePrivilege rolePrivilege : rolePrivilegeList) {
                        try {
                            IntegrityCheckUtils.generateMac(rolePrivilege);
                        } catch (Exception e) {
                            log.error("本条角色权限完整性数据处理失败，rolePrivilegeId => {}", rolePrivilege.getId(), e);
                        }
                    }
                    try {
                        rightsmgrService.updateRolePrivilegeBatch(rolePrivilegeList);
                    } catch (Exception e) {
                        log.error("第{}页角色权限完整性数据批量更新失败", i, e);
                        //当前批次失败后，跳过这一批，多批失败，则跳过多批，等能成功的都成功后，下次重启服务来处理之前失败的批次
                        start += Integer.parseInt(pageSize);
                    }
                }
                log.info("角色权限关联表第{}页旧数据完整性处理完成", i);
            }
        } catch (Exception e) {
            log.error("角色权限完整性数据处理失败", e.getMessage(), e);
        }
    }

    /**
     * 处理日志信息完整性
     */
    private void processIntegrityOfLog() {
        if (!IntegrityCheckUtils.isEnabledMac()) {
            log.info("当前未开启完整性校验参数【{}】，不处理", Constant.ENABLED_MAC);
            return;
        }
        try {
            LogDesignQuery logDesignQuery = logmgrService.createLogDesignQuery().wzxcheckIsNullOrNotTrue().loginAndLogout();
            long count = logDesignQuery.count();
            log.info("待处理日志完整性总数 => {}", count);
            if (count == 0) {
                log.info("没有需要处理完整性的日志数据");
                return;
            }
            long pageNum = getPageNum(count);
            int start = 0;
            for (int i = 1; i <= pageNum; i++) {
                log.info("开始处理日志表第{}页数据完整性", i);
                List<LogDesign> logDesignList = logDesignQuery.listPage(start, Integer.parseInt(pageSize));
                if (CollectionUtil.isNotEmpty(logDesignList)) {
                    for (LogDesign logDesign : logDesignList) {
                        try {
                            IntegrityCheckUtils.generateMac(logDesign);
                        } catch (Exception e) {
                            log.error("本条日志完整性数据处理失败，logId => {}", logDesign.getId(), e);
                        }
                    }
                    try {
                        logmgrService.updateBatchLog(logDesignList);
                    } catch (Exception e) {
                        log.error("第{}页日志完整性数据批量更新失败", i, e);
                        //当前批次失败后，跳过这一批，多批失败，则跳过多批，等能成功的都成功后，下次重启服务来处理之前失败的批次
                        start += Integer.parseInt(pageSize);
                    }
                }
                log.info("日志表第{}页旧数据完整性处理完成", i);
            }
        } catch (Exception e) {
            log.error("用户完整性数据处理失败", e.getMessage(), e);
        }
    }

    private void asyncSwitchEncryptType() {
        try {
            AlgorithmHandler pwdAlgorithmHandler = AlgorithmFactory.createAlgorithmHandler(pwdEncryptType);
            long count = identityService.createUserQuery().count();
            if (count > 0) {
                long pageNum = getPageNum(count);
                int startNum = 0;
                int pageSize1 = Integer.parseInt(pageSize);

                int maxResults;
                List<User> userResponses;
                List<User> userResponses1;
                for (int i = 1; i <= pageNum; i++) {
                    maxResults = pageSize1 * i;
                    userResponses = identityService.createUserQuery().listPage(startNum,
                            maxResults);
                    userResponses1 = new ArrayList<>();
                    startNum = maxResults;
                    if (!CollectionUtils.isEmpty(userResponses)) {
                        toMultiThreadSwithUserPaswordList(userResponses, userResponses1, pwdAlgorithmHandler);
                        if (!CollectionUtils.isEmpty(userResponses1)) {
                            updateUserPhoneOrPasswordBatch(userResponses1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("初始化user表密码失败:{}", e.getMessage());
        }
    }


    public void toMultiThreadSwithUserPaswordList(List<User> userResponses, List<User> userResponses1, AlgorithmHandler pwdAlgorithmHandler) {
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (User userResponse : userResponses) {
            // 1.1 主线程中加入到List，多线程中加入会有并发问题
            userResponses1.add(userResponse);
            futureList.add(CompletableFuture.runAsync(() -> {
                //1.2 原来的id值先置空
                String id = userResponse.getId();
                userResponse.setId(null);
                String userName = userResponse.getUsername();
                if (StringUtils.isNotEmpty(userName) && !StringUtils.equals(pwdEncryptType, userResponse.getPwdEncryptType())) {
                    String entryptPassword = pwdAlgorithmHandler.encodePassword(userName, initPassword, null);

                    userResponse.setPassWord(entryptPassword);
                    userResponse.setPwdEncryptType(pwdEncryptType);
                    userResponse.setWzxcheck("false");
                    // 1.3 需要更新时，还原原来的id值
                    userResponse.setId(id);
                }
            }, encryptExecutor));
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        // 1.4 主线程中去除id为空的数据
        for (int i = userResponses1.size() - 1; i >= 0; i--) {
            if (StringUtils.isEmpty(userResponses1.get(i).getId())) {
                userResponses1.remove(i);
            }
        }
    }

    public void asyncEncryptUser() {

        try {
            // 查询脱敏字段is null 数量
            //long userTMIsNullCount = identityService.createUserQuery().isUserTelephoneTmEmpty(true).count();
            long total = identityService.createUserQuery().count();
            if (total <= 0) {
                return;
            }
            long pageNum = getPageNum(total);
            int pageSize1 = Integer.parseInt(pageSize);
            for (int i = 0; i < pageNum; i++) {
                int start = pageSize1 * i;
                List<User> userResponses = identityService.createUserQuery().listPage(start, pageSize1);
                List<User> userList = new ArrayList<>();
                log.info("开始处理user表第{}页旧数据", i);
                if (!CollectionUtils.isEmpty(userResponses)) {
                    toMultiThreadEncryptUserListTelephone(userResponses, userList);
                    if (CollectionUtils.isNotEmpty(userList)) {
                        try {
                            identityService.updateBatchUsers(userList);
                        } catch (Exception e) {
                            log.error("第{}页用户机密性批量更新失败", i, e);
                        }
                    }
                }
                log.info("处理user表第{}页旧数据完成", i);
            }

        } catch (Exception e) {
            log.error("处理user表失败：{}", e.getMessage());
        }
    }

    public void toMultiThreadEncryptUserListTelephone(List<User> userList, List<User> encryptUserList) {
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (User userResponse : userList) {
            if (StringUtils.isBlank(userResponse.getUserCode()) && StringUtils.isBlank(userResponse.getStaffName())) {
                continue;
            }
            User user = new UserEntity();
            // 1.1 主线程中加入到List，多线程中加入会有并发问题
            encryptUserList.add(user);
            futureList.add(CompletableFuture.runAsync(() -> {
                try {
                    // todo defaultHandler 是否有可调优的地方
                    AlgorithmHandler defaultHandler;

                    // 加密、脱敏
                    String telephone = userResponse.getTelephone();
                    if (StringUtils.isNotEmpty(telephone)) {
                        if (StringUtils.isEmpty(userResponse.getTelephoneTm())) {

                            String encryptTelephone = DesensitizeTools.encryptNumber(telephone);
                            if (StringUtils.isEmpty(encryptTelephone)) { //cssp加密失败不会抛出异常，返回空 会导致值全部为空
                                log.info("以:{}方式加密userId为:{}手机号:{} 失败", cryptoType, userResponse.getId(), telephone);
                                return;
                            }

                            String tmTelephone = DesensitizeTools.desensitizePhoneNumber(telephone);
                            user.setId(userResponse.getId());
                            user.setTelephone(encryptTelephone);
                            user.setTelephoneTm(tmTelephone);
                            user.setDataEncryptType(cryptoType);
                        } else {
                            //脱敏字段不为空，说明已经加密，判断是否更换了加密方式 不一致先解密后加密
                            telephone = userResponse.getTelephone();
                            String dataEncryptType = userResponse.getDataEncryptType();
                            if (StringUtils.isEmpty(dataEncryptType)) {
                                log.error("userId为:{} dataEncryptType值不可为空", userResponse.getId());
                                return;
                            }
                            //加密方式改变，解密处理
                            if (!StringUtils.equals(dataEncryptType, cryptoType)) {
                                // todo 分析是否有多线程问题
                                defaultHandler = AlgorithmFactory.createAlgorithmHandler(dataEncryptType);
                                String hexKey = SymmetricEncryKeyEnum.getKeyByCryptoType(dataEncryptType);

                                try { //解密有问题时继续执行,for中可以加try catch，只有当异常时打印链路日志才会影响执行效率
                                    String decryptTelephone = defaultHandler.symmetricDecryption(telephone, hexKey, null);
                                    if (StringUtils.isNotEmpty(decryptTelephone)) {
                                        //加密处理
                                        String encryptTelephone = DesensitizeTools.encryptNumber(decryptTelephone);
                                        if (StringUtils.isEmpty(encryptTelephone)) { //cssp加密失败不会抛出异常，返回空 会导致值全部为空
                                            log.info("以:{}方式加密userId为:{}手机号:{} 失败", cryptoType, userResponse.getId(), decryptTelephone);
                                            return;
                                        }
                                        user.setId(userResponse.getId());
                                        user.setTelephoneTm(userResponse.getTelephoneTm());
                                        user.setTelephone(encryptTelephone);
                                        user.setDataEncryptType(cryptoType);
                                    } else {
                                        log.info("以:{}方式解密userId为:{}手机号:{} 失败", cryptoType, userResponse.getId(), telephone);
                                    }
                                } catch (Exception e) {
                                    log.error("处理user手机号机密性失败:{},userid为:{} 手机号:{}", e.getMessage(), userResponse.getId(), telephone);
                                }
                            }
                        }
                    } else { //手机号为空 同步修改掉dataEncryptType
                        if (StringUtils.equals(cryptoType, userResponse.getDataEncryptType())) {
                            return;
                        }
                        user.setId(userResponse.getId());
                        user.setTelephone(userResponse.getTelephone());
                        user.setTelephoneTm(userResponse.getTelephoneTm());
                        user.setDataEncryptType(cryptoType);
                    }
                } catch (Exception e) {
                    log.error("本条用户存储机密性数据处理失败，userId => {}", userResponse.getId(), e);
                }
            }, encryptExecutor));

        }
        CompletableFuture.allOf( futureList.toArray(new CompletableFuture[0])).join();
        // 1.2 主线程中去除id为空的数据
        for (int i = encryptUserList.size() - 1; i >= 0; i--) {
            if (StringUtils.isEmpty(encryptUserList.get(i).getId())) {
                encryptUserList.remove(i);
            }
        }
    }

    private void asyncEncryptUserFieldEncrypt() {
        if (!EncryptFieldUtils.isEnabledEncryptField()) {
            log.info("当前未开启存储机密性开关参数【{}】，不处理", Constant.ENABLED_ENCRYPT_FIELD);
            return;
        }
        //处理userCode和staffName
        try {
            UserQuery userQuery = identityService.createUserQuery().encryptIsNullOrNotTrue();
            long count = userQuery.count();
            log.info("待处理用户机密性总数 => {}", count);
            if (count == 0) {
                log.info("没有需要处理机密性性的用户数据");
                return;
            }
            long pageNum = getPageNum(count);
            int start = 0;
            for (int i = 1; i <= pageNum; i++) {
                log.info("------>page begin:{}", i);

                log.info("开始处理用户表第{}页旧数据完整性", i);
                List<User> unEncryptUserList = userQuery.listPage(start, Integer.parseInt(pageSize));
                List<User> updateUserList = new ArrayList<>();
                toMultiThreadEncryptUserList(unEncryptUserList, updateUserList);
                try {
                    if (CollectionUtils.isNotEmpty(updateUserList)) {
                        identityService.updateBatchUsers(updateUserList);
                    }
                } catch (Exception e) {
                    log.error("----> page {} error", i, e);
                    log.error("第{}页用户存储机密性数据批量更新失败", i, e);
                    //当前批次失败后，跳过这一批，多批失败，则跳过多批，等能成功的都成功后，下次重启服务来处理之前失败的批次
                    start += Integer.parseInt(pageSize);
                }
                log.info("用户表第{}页旧数据存储机密性处理完成", i);
                log.info("------>page end:{}", i);
            }
        } catch (Exception e) {
            log.error("用户存储机密性数据处理失败", e.getMessage(), e);
        }
    }

    public void toMultiThreadEncryptUserList(List<User> userList, List<User> encryptUserList) {
        if (CollectionUtil.isEmpty(userList)) {
            return;
        }
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (User user : userList) {
            if (StringUtils.isBlank(user.getUserCode()) && StringUtils.isBlank(user.getStaffName())) {
                continue;
            }
            User encryptUser = new UserEntity();
            encryptUserList.add(encryptUser);
            futureList.add(CompletableFuture.runAsync(() -> {
                try {
                    encryptUser.setId(user.getId());
                    // 加密、脱敏
                    String userCode = user.getUserCode();
                    if (StringUtils.isNotEmpty(userCode)) {
                        String encryptUserCode = EncryptFieldUtils.encryptNumberV5(userCode);
                        encryptUser.setUserCode(encryptUserCode);
                    }
                    String staffName = user.getStaffName();
                    if (StringUtils.isNotEmpty(staffName)) {
                        String encryptStaffName = EncryptFieldUtils.encryptNumberV5(staffName);
                        encryptUser.setStaffName(encryptStaffName);
                    }
                    encryptUser.setEncrypt(1);
//                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error("本条用户存储机密性数据处理失败，userId => {}", user.getId(), e);
                }
            }, encryptExecutor));
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
    }

    public void asyncEncryptDepartment() {
        try {
            // 查询脱敏字段is null 数量
            //long userTMIsNullCount = identityService.createDepartmentQuery().isDepartmentTelephoneTmEmpty(true).count();
            long total = identityService.createDepartmentQuery().count();
            if (total <= 0) {
                return;
            }

            int pageNum = getPageNum(total);
            int pageSize1 = Integer.parseInt(pageSize);
            // 构建分页查询
            for (int i = 0; i < pageNum; i++) {
                int start = pageSize1 * i;
                List<Department> departmentResponseList = identityService.createDepartmentQuery().listPage(start, pageSize1);
                List<Department> departmentList = new ArrayList<>();
                log.info("开始处理Department表第{}页旧数据", i);
                if (!CollectionUtils.isEmpty(departmentResponseList)) {

                    toMultiThreadEncryptDepartmentList(departmentResponseList, departmentList);
                    if (CollectionUtils.isNotEmpty(departmentList)) {
                        try {
                            identityService.updateBatchDepartments(departmentList);
                        } catch (Exception e) {
                            log.error("第{}页部门信息机密性数据批量更新失败", i, e);
                        }
                    }
                }
                log.info("处理Department表第{}页旧数据完成", i);
            }

        } catch (Exception e) {
            log.error("处理Department表失败：{}", e.getMessage());
        }
    }

    public void toMultiThreadEncryptDepartmentList(List<Department> departmentResponseList, List<Department> departmentList) {
        if (CollectionUtil.isEmpty(departmentResponseList)) {
            return;
        }
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (Department departmentResponse : departmentResponseList) {
            // 加密、脱敏
            String telephone = departmentResponse.getTelephone();
            Department departmentEntity = new DepartmentEntity();
            // 1.1 主线程中加入到List，多线程中加入会有并发问题
            departmentList.add(departmentEntity);
            futureList.add(CompletableFuture.runAsync(() -> {
                try{
                    if (StringUtils.isNotEmpty(telephone)) {
                        if (StringUtils.isEmpty(departmentResponse.getTelephoneTm())) {
                            String encryptTelephone = DesensitizeTools.encryptNumber(telephone);
                            if (StringUtils.isEmpty(encryptTelephone)) { //cssp加密失败不会抛出异常，返回空 会导致值全部为空
                                log.info("手机号:[{}]加密失败, 加密方式:[{}], 部门ID:[{}]", telephone, cryptoType, departmentResponse.getId());
                                return;
                            }
                            String tmTelephone = DesensitizeTools.desensitizePhoneNumber(departmentResponse.getTelephone());
                            departmentEntity.setTelephone(encryptTelephone);
                            departmentEntity.setTelephoneTm(tmTelephone);
                            departmentEntity.setDataEncryptType(cryptoType);
                            departmentEntity.setId(departmentResponse.getId());

                        } else { //脱敏字段不为空，说明已经加密，判断是否更换了加密方式
                            log.info("部门[{}]脱敏字段不为空", departmentResponse.getId());
                            String dataEncryptType = departmentResponse.getDataEncryptType();
                            if (StringUtils.isEmpty(dataEncryptType)) {
                                log.error("部门[{}]的dataEncryptType值为空，请检查！", departmentResponse.getId());
                                return;
                            }

                            //加密方式改变，切换加密
                            if (!StringUtils.equals(dataEncryptType, cryptoType)) {
                                log.info("部门[{}]数据加密方式已改变，旧方式：[{}], 新方式：[{}]", departmentResponse.getId(), dataEncryptType, cryptoType);
                                //按旧的加密方式解密手机号
                                AlgorithmHandler defaultHandler = AlgorithmFactory.createAlgorithmHandler(dataEncryptType);
                                String hexKey = SymmetricEncryKeyEnum.getKeyByCryptoType(dataEncryptType);

                                String decryptTelephone = defaultHandler.symmetricDecryption(telephone, hexKey, null);
                                if (StringUtils.isEmpty(decryptTelephone)) {
                                    //cssp加密失败不会抛出异常，返回空 会导致值全部为空
                                    log.info("部门[{}]手机号[{}]解密失败", departmentResponse.getId(), telephone);
                                    return;
                                }

                                //按新的加密方式进行加密
                                String encryptTelephone = DesensitizeTools.encryptNumber(telephone);
                                if (StringUtils.isEmpty(encryptTelephone)) {
                                    //cssp加密失败不会抛出异常，返回空 会导致值全部为空
                                    log.info("部门[{}]手机号[{}]加密失败", departmentResponse.getId(), encryptTelephone);
                                    return;
                                }
                                departmentEntity.setTelephoneTm(departmentResponse.getTelephoneTm());
                                departmentEntity.setTelephone(encryptTelephone);
                                departmentEntity.setDataEncryptType(cryptoType);
                                departmentEntity.setId(departmentResponse.getId());
                            }
                        }
                    } else {
                        //手机号为空 同步修改掉dataEncryptType
                        if (StringUtils.equals(cryptoType, departmentResponse.getDataEncryptType())) {
                            return;
                        }
                        log.info("部门[{}({})]的手机号为空", departmentResponse.getName(), departmentResponse.getId());
                        departmentEntity.setTelephone(departmentResponse.getTelephone());
                        departmentEntity.setTelephoneTm(departmentResponse.getTelephoneTm());
                        departmentEntity.setDataEncryptType(cryptoType);
                        departmentEntity.setId(departmentResponse.getId());
                    }
                } catch (Exception e){
                    log.error("本条部门存储机密性数据处理失败，deptId => {}", departmentResponse.getId(), e);
                }
            }, encryptExecutor));
        }
        CompletableFuture.allOf((CompletableFuture[]) futureList.toArray(new CompletableFuture[0])).join();
        // 1.2 主线程中去除id为空的数据
        for (int i = departmentList.size() - 1; i >= 0; i--) {
            if (StringUtils.isEmpty(departmentList.get(i).getId())) {
                departmentList.remove(i);
            }
        }
    }

    public void asyncEncryptUserExt() {

        try {
            // 查询脱敏字段is null 数量
            //long userExtTmIsNullCount = identityService.createUserExtQuery().count();
            long total = identityService.createUserExtQuery().count();
            if (total <= 0) {
                return;
            }
            int pageNum = getPageNum(total);
            int pageSize1 = Integer.parseInt(pageSize);
            for (int i = 0; i < pageNum; i++) {
                int start = pageSize1 * i;
                List<UserExt> userExtResponseList = identityService.createUserExtQuery().listPage(start, pageSize1);
                List<UserExt> userExtList = new ArrayList<>();
                if (!CollectionUtils.isEmpty(userExtResponseList)) {
                    log.info("开始处理userExt第{}页旧数据", i);
                    toMultiThreadEncryptUserExtList(userExtResponseList, userExtList);
                    if (CollectionUtils.isNotEmpty(userExtList)) {
                        try {
                            identityService.updateBatchUserExts(userExtList);
                        } catch (Exception e) {
                            log.error("第{}页用户扩展信息机密性数据批量更新失败", i, e);
                        }
                    }
                    log.info("处理userExt表第{}页旧数据完成", i);
                }
            }
        } catch (Exception e) {
            log.error("处理userExt表失败：{}", e.getMessage());
        }
    }

    public void toMultiThreadEncryptUserExtList(List<UserExt> userExtResponseList, List<UserExt> userExtList) {
        if (CollectionUtil.isEmpty(userExtResponseList)) {
            return;
        }
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (UserExt userExtResponse : userExtResponseList) {
            // 加密、脱敏
            String certificatecode = userExtResponse.getCertificatecode();

            UserExt userExtEntity = new UserExtEntity();
            // 1.1 主线程中加入到List，多线程中加入会有并发问题
            userExtList.add(userExtEntity);

            futureList.add(CompletableFuture.runAsync(() -> {
                try{
                    if (StringUtils.isNotEmpty(certificatecode)) {
                        if (StringUtils.isEmpty(userExtResponse.getCertificatecodeTm())) {
                            String encryptCertificatecode = DesensitizeTools.encryptNumber(certificatecode);
                            if (StringUtils.isEmpty(encryptCertificatecode)) { //cssp加密失败不会抛出异常，返回空 会导致值全部为空
                                log.info("以:{}方式加密userExtId为:{}证件号:{} 失败", cryptoType, userExtResponse.getId(), certificatecode);
                                return;
                            }
                            String certificatecodeTm = null;
                            certificatecodeTm = DesensitizeTools.desensitizeIdCardNumber(userExtResponse.getCertificatecode());
                            userExtEntity.setCertificatecode(encryptCertificatecode);
                            userExtEntity.setCertificatecodeTm(certificatecodeTm);
                            userExtEntity.setDataEncryptType(cryptoType);
                            userExtEntity.setId(userExtResponse.getId());
                        } else { //判断是否切换了加密方式

                            String dataEncryptType = userExtResponse.getDataEncryptType(); //dataEncryptType为空会报null异常，不报具体信息
                            if (StringUtils.isEmpty(dataEncryptType)) {
                                log.error("userExtId为:{}证件号:{} dataEncryptType值不可为空，请检查数据", userExtResponse.getId(), certificatecode);
                                return;
                            }
                            AlgorithmHandler defaultHandler = AlgorithmFactory.createAlgorithmHandler(dataEncryptType);
                            String hexKey = SymmetricEncryKeyEnum.getKeyByCryptoType(dataEncryptType);

                            if (StringUtils.equals(dataEncryptType, cryptoType)) { //加密方式改变，解密处理
                                return;
                            }

                            try { //各种原因导致的个别数据解密失败，不影响后续数据解密
                                String decryptCertificatecode = defaultHandler.symmetricDecryption(userExtResponse.getCertificatecode(), hexKey, null);
                                if (StringUtils.isNotBlank(decryptCertificatecode)) { //cssp加密失败不会抛出异常，返回空 会导致值全部为空
                                    String encryptCertificatecode = DesensitizeTools.encryptNumber(decryptCertificatecode);
                                    if (StringUtils.isEmpty(encryptCertificatecode)) {
                                        log.info("以:{}方式加密userExtId为:{}证件号:{} 失败", cryptoType, userExtResponse.getId(), certificatecode);
                                        return;
                                    }
                                    userExtEntity.setCertificatecodeTm(userExtResponse.getCertificatecodeTm());
                                    userExtEntity.setCertificatecode(encryptCertificatecode);
                                    userExtEntity.setDataEncryptType(cryptoType);
                                    userExtEntity.setId(userExtResponse.getId());
                                } else {
                                    log.info("以:{}方式解密userExtId为:{}证件号:{} 失败", dataEncryptType, userExtEntity.getId(), userExtResponse.getCertificatecode());
                                }
                            } catch (Exception e) {
                                log.error("处理userExt身份证号失败:{},userExtId为:{}证件号:{}", e.getMessage(), userExtResponse.getId(), certificatecode);
                            }
                        }
                    } else {
                        if (StringUtils.equals(cryptoType, userExtResponse.getDataEncryptType())) { //加密方式改变，解密处理
                            return;
                        }
                        userExtEntity.setCertificatecodeTm(userExtResponse.getCertificatecodeTm());
                        userExtEntity.setCertificatecode(userExtResponse.getCertificatecode());
                        userExtEntity.setDataEncryptType(cryptoType);
                        userExtEntity.setId(userExtResponse.getId());
                    }
                } catch (Exception e){
                    log.error("本条用户扩展信息存储机密性数据处理失败，userExtId => {}", userExtResponse.getId(), e);
                }
            }, encryptExecutor));

        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        // 1.2 主线程中去除id为空的数据
        for (int i = userExtList.size() - 1; i >= 0; i--) {
            if (StringUtils.isEmpty(userExtList.get(i).getId())) {
                userExtList.remove(i);
            }
        }
    }

    public void asyncEncryptUserRegion() {
        try {
            // 查询脱敏字段is null 数量
            long userReginTmIsNullCount = userRegionService.createUserRegionQuery().isRegionTeleTmEmpty(true).count();
            // 脱敏字段未修改 全部为空才执行下面逻辑
            if (userReginTmIsNullCount > 0) {
                int pageNum = getPageNum(userReginTmIsNullCount);
                int pageSize1 = Integer.parseInt(pageSize);
                for (int i = 1; i <= pageNum; i++) {
                    int maxResults = pageSize1 * i;
                    List<UserRegion> userRegionResponse = userRegionService.createUserRegionQuery()
                            .isRegionTeleTmEmpty(true).listPage(0, maxResults);
                    List<UserRegion> userRegionResponse1 = new ArrayList<>();
                    log.info("开始处理userRegion第{}页旧数据", i);
                    toMultiThreadEncryptUserRegionList(userRegionResponse, userRegionResponse1);
                    if (!CollectionUtils.isEmpty(userRegionResponse)) {
                        if (!CollectionUtils.isEmpty(userRegionResponse1)) {
                            updateUserRegionTelephoneBatch(userRegionResponse1);
                        }
                    }
                    log.info("处理userRegion表第{}页旧数据完成", i);
                }
            }
        } catch (Exception e) {
            log.error("处理userRegion表失败：{}", e.getMessage());
        }
    }

    public void toMultiThreadEncryptUserRegionList(List<UserRegion> userRegionResponse, List<UserRegion> userRegionResponse1) {
        if (CollectionUtil.isEmpty(userRegionResponse)) {
            return;
        }
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (UserRegion userRegion : userRegionResponse) {
            // 1.1 主线程中加入到List，多线程中加入会有并发问题
            userRegionResponse1.add(userRegion);
            futureList.add(CompletableFuture.runAsync(() -> {
                try{
                    //1.2 原来的id值先置空
                    String id = userRegion.getId();
                    userRegion.setId(null);
                    // 加密、脱敏
                    String regionTelephone = userRegion.getRegionTelephone();
                    if (StringUtils.isNotEmpty(regionTelephone)
                            && StringUtils.isEmpty(userRegion.getRegionTelephoneTm())) {
                        // wxy 20230703 刘月华要求的----历史数据加密时手机号不做任何匹配，直接加密处理
                        regionTelephone = DesensitizeTools.encryptNumber(regionTelephone);
                        if (StringUtils.isNotEmpty(regionTelephone)) {
                            String regionTelephoneTm = DesensitizeTools
                                    .desensitizePhoneNumber(userRegion.getRegionTelephone());
                            userRegion.setRegionTelephone(regionTelephone);
                            userRegion.setRegionTelephoneTm(regionTelephoneTm);
                            // 1.3 需要更新时，还原原来的id值
                            userRegion.setId(id);
                        }
                    }
                }catch (Exception e){
                    log.error("本条userRegion存储机密性数据处理失败，Id => {}", userRegion.getId(), e);
                }
            }, encryptExecutor));

        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        // 1.4 主线程中去除id为空的数据
        for (int i = userRegionResponse1.size() - 1; i >= 0; i--) {
            if (StringUtils.isEmpty(userRegionResponse1.get(i).getId())) {
                userRegionResponse1.remove(i);
            }
        }
    }

    private void asyncDecryptUser() {
        try {
            long count = identityService.createUserQuery().count();
            if (count > 0) {
                long pageNum = getPageNum(count);
                int startNum = 0;
                int pageSize1 = Integer.parseInt(pageSize);

                int maxResults;
                List<User> userResponses;
                List<User> userResponses1;
                for (int i = 1; i <= pageNum; i++) {
                    maxResults = pageSize1 * i;
                    userResponses = identityService.createUserQuery().listPage(startNum,
                            maxResults);
                    userResponses1 = new ArrayList<>();
                    startNum = maxResults;
                    if (!CollectionUtils.isEmpty(userResponses)) {
                        log.info("开始解密user表第{}页数据", i);
                        toMultiThreadDecryptUserListTelephone(userResponses, userResponses1);
                        if (CollectionUtils.isNotEmpty(userResponses1)) {
                            identityService.updateBatchUsers(userResponses1);
                        }
                        log.info("解密处理user表第{}页数据完成", i);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解密处理user表失败:{}", e);
        }
    }

    public void toMultiThreadDecryptUserListTelephone(List<User> userResponses, List<User> userResponses1) {
        if (CollectionUtil.isEmpty(userResponses)) {
            return;
        }
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (User userResponse : userResponses) {
            // 1.1 主线程中加入到List，多线程中加入会有并发问题
            userResponses1.add(userResponse);
            futureList.add(CompletableFuture.runAsync(() -> {
                try{
                    String telephone = userResponse.getTelephone();
                    boolean telephoneDecrypt = false;
                    // todo 待检查业务逻辑
                    if (StringUtils.isNotEmpty(telephone) && StringUtils.isNotEmpty(userResponse.getTelephoneTm())) {
                        try { //解密有问题时继续执行,for中可以加try catch，只有当异常时打印链路日志才会影响执行效率
                            telephone = DesensitizeTools.decryptNumber(telephone);
                            userResponse.setTelephone(telephone);
                            userResponse.setTelephoneTm("");
                            telephoneDecrypt = true;
                        } catch (Exception e) {
                            log.error("用户id:{} 手机号:{} 解密手机号失败：{}", userResponse.getId(), userResponse.getTelephone(), e.getMessage());
                        }
                    }
                    boolean fieldDecrypt = false;
                    if (userResponse.getEncrypt() != null && 1 == userResponse.getEncrypt()) {
                        String userCode = userResponse.getUserCode();
                        String staffName = userResponse.getStaffName();
                        String unEncryptUserCode = null;
                        String unEncryptStaffName = null;
                        try {
                            if (StringUtils.isNotEmpty(userCode)) {
                                unEncryptUserCode = EncryptFieldUtils.decryptNumberV5(userCode);
                            }
                        } catch (Exception e) {
                            log.error("用户id:{} 用户编码:{} 解密用户名失败：{}", userResponse.getId(), userResponse.getUserCode(), e.getMessage());
                        }
                        try {
                            if (StringUtils.isNotEmpty(staffName)) {
                                unEncryptStaffName = EncryptFieldUtils.decryptNumberV5(staffName);
                            }
                        } catch (Exception e) {
                            log.error("用户id:{} 用户昵称:{} 解密昵称失败：{}", userResponse.getId(), userResponse.getStaffName(), e.getMessage());
                        }

                        if ((StringUtils.isBlank(userCode) || StringUtils.isNotBlank(unEncryptUserCode)) && (StringUtils.isBlank(staffName) || StringUtils.isNotBlank(unEncryptStaffName))) {
                            userResponse.setUserCode(unEncryptUserCode);
                            userResponse.setStaffName(unEncryptStaffName);
                            userResponse.setEncrypt(0);
                            fieldDecrypt = true;
                        }

                    }

                    userResponse.setPassWord(null);//设为null，只更新手机号
                    // 1.2 不更新的数据id标记为空
                    if (!telephoneDecrypt && !fieldDecrypt) {
                        userResponse.setId(null);
                    }
                } catch (Exception e){
                    log.error("本条用户数据解密失败，userId => {}", userResponse.getId(), e);
                }
            }, encryptExecutor));
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        // 1.3 主线程中去除id为空的数据
        for (int i = userResponses1.size() - 1; i >= 0; i--) {
            if (StringUtils.isEmpty(userResponses1.get(i).getId())) {
                userResponses1.remove(i);
            }
        }
    }

    private void asyncDecryptDepartment() {
        try {
            long count = identityService.createDepartmentQuery().count();
            if (count > 0) {
                int pageNum = getPageNum(count);
                int startNum = 0;
                int pageSize1 = Integer.parseInt(pageSize);


                int maxResults;
                List<Department> departmentResponse;
                List<Department> departmentResponse1;
                String telephone;
                for (int i = 1; i <= pageNum; i++) {
                    maxResults = pageSize1 * i;
                    departmentResponse = identityService.createDepartmentQuery().listPage(startNum, maxResults);
                    departmentResponse1 = new ArrayList<>();
                    startNum = maxResults;
                    if (!CollectionUtils.isEmpty(departmentResponse)) {
                        log.info("开始解密Department表第{}页数据", i);
                        toMultiThreadDecryptDepartmentList(departmentResponse, departmentResponse1);
                        if (!CollectionUtils.isEmpty(departmentResponse1)) {
                            updateDepartmentPhoneBatch(departmentResponse1);
                        }
                        log.info("解密Department表第{}页数据完成", i);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解密处理Department表失败：{}", e.getMessage());
        }
    }

    public void toMultiThreadDecryptDepartmentList(List<Department> departmentResponse, List<Department> departmentResponse1) {
        if (CollectionUtil.isEmpty(departmentResponse)) {
            return;
        }
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (Department depResponse : departmentResponse) {
            // 1.1 主线程中加入到List，多线程中加入会有并发问题
            departmentResponse1.add(depResponse);
            futureList.add(CompletableFuture.runAsync(() -> {
                //1.2 原来的id值先置空
                String id = depResponse.getId();
                try{
                    String telephone = depResponse.getTelephone();
                    depResponse.setId(null);
                    if (StringUtils.isNotEmpty(telephone) && StringUtils.isNotEmpty(depResponse.getTelephoneTm())) {
                        try { //解密有问题时继续执行,for中可以加try catch，只有当异常时打印链路日志才会影响执行效率
                            telephone = DesensitizeTools.decryptNumber(telephone);
                            if (StringUtils.isNotEmpty(telephone)) {
                                depResponse.setTelephone(telephone);
                                depResponse.setTelephoneTm("");
                                // 1.3 更新时，还原原来的id值
                                depResponse.setId(id);
                            }
                        } catch (Exception e) {
                            log.error("部门id:{} 手机号:{} 解密手机号失败：{}", id, depResponse.getTelephone(), e.getMessage());
                        }
                    }
                } catch (Exception e){
                    log.error("本条部门数据解密失败，deptId => {}", id, e);
                }
            }, encryptExecutor));
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        // 1.4 主线程中去除id为空的数据
        for (int i = departmentResponse1.size() - 1; i >= 0; i--) {
            if (StringUtils.isEmpty(departmentResponse1.get(i).getId())) {
                departmentResponse1.remove(i);
            }
        }
    }

    private void asyncDecryptUserExt() {
        try {
            long count = identityService.createUserExtQuery().count();
            if (count > 0) {
                int pageNum = getPageNum(count);
                int startNum = 0;
                int pageSize1 = Integer.parseInt(pageSize);

                int maxResults;
                List<UserExt> userExtResponse;
                List<UserExt> userExtResponseList;
                String certificatecode;
                UserExtEntity userExtEntity;
                for (int i = 1; i <= pageNum; i++) {
                    maxResults = pageSize1 * i;
                    userExtResponse = identityService.createUserExtQuery()
                            .listPage(startNum, maxResults);
                    userExtResponseList = new ArrayList<>();
                    startNum = maxResults;
                    if (!CollectionUtils.isEmpty(userExtResponse)) {
                        log.info("开始解密处理userExt第{}页数据", i);
                        toMultiThreadDecryptUserExtList(userExtResponse, userExtResponseList);
                        if (!CollectionUtils.isEmpty(userExtResponseList)) {
                            updateUserExtCertificatecodeBatch(userExtResponseList);
                        }
                        log.info("解密userExt表第{}页数据完成", i);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解密处理userExt表失败：{}", e.getMessage());
        }

    }

    public void toMultiThreadDecryptUserExtList(List<UserExt> userExtResponse, List<UserExt> userExtResponseList) {
        if (CollectionUtil.isEmpty(userExtResponse)) {
            return;
        }
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (UserExt userExtResponse1 : userExtResponse) {
            // 1.1 主线程中加入到List，多线程中加入会有并发问题
            userExtResponseList.add(userExtResponse1);
            futureList.add(CompletableFuture.runAsync(() -> {
                //1.2 原来的id值先置空
                String id = userExtResponse1.getId();
                try{
                    userExtResponse1.setId(null);
                    String certificatecode = userExtResponse1.getCertificatecode();
                    if (StringUtils.isNotEmpty(certificatecode) && StringUtils.isNotEmpty(userExtResponse1.getCertificatecodeTm())) {
                        try { //解密有问题时继续执行,for中可以加try catch，只有当异常时打印链路日志才会影响执行效率

                            certificatecode = DesensitizeTools.decryptNumber(certificatecode);
                            if (StringUtils.isNotEmpty(certificatecode)) {

                                userExtResponse1.setCertificatecode(certificatecode);
                                userExtResponse1.setCertificatecodeTm("");
                                // 1.3 需要更新时，还原原来的id值
                                userExtResponse1.setId(id);
                            }
                        } catch (Exception e) {
                            log.error("用户扩展id:{} 证件号:{} 解密证件号失败：{}", id, certificatecode, e.getMessage());
                        }
                    }
                } catch (Exception e){
                    log.error("本条用户扩展数据解密失败，deptId => {}", id, e);
                }
            }, encryptExecutor));
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        // 1.4 主线程中去除id为空的数据
        for (int i = userExtResponseList.size() - 1; i >= 0; i--) {
            if (StringUtils.isEmpty(userExtResponseList.get(i).getId())) {
                userExtResponseList.remove(i);
            }
        }
    }

    private void asyncDecryptUserRegion() {
        try {
            long count = selectUserRegionByQueryCriteriaAll();

            if (count > 0) {
                int pageNum = getPageNum(count);
                int startNum = getPageNum(count);
                int pageSize1 = Integer.parseInt(pageSize);

                int maxResults;
                List<UserRegion> userRegionResponse;
                List<UserRegion> userRegionResponse1;
                String regionTelephone;
                for (int i = 1; i <= pageNum; i++) {
                    maxResults = pageSize1 * i;
                    userRegionResponse = userRegionService.createUserRegionQuery().listPage(startNum, maxResults);
                    userRegionResponse1 = new ArrayList<>();
                    startNum = maxResults;
                    if (!CollectionUtils.isEmpty(userRegionResponse)) {
                        log.info("开始解密处理userRegion第{}页数据", i);
                        toMultiThreadDecryptUserRegionList(userRegionResponse, userRegionResponse1);
                        if (!CollectionUtils.isEmpty(userRegionResponse1)) {
                            updateUserRegionTelephoneBatch(userRegionResponse1);
                        }
                        log.info("解密userRegion表第{}页数据完成", i);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解密处理userRegion表失败：{}", e.getMessage());
        }
    }

    public void toMultiThreadDecryptUserRegionList(List<UserRegion> userRegionResponse, List<UserRegion> userRegionResponse1) {
        if (CollectionUtil.isEmpty(userRegionResponse)) {
            return;
        }
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (UserRegion userRegion : userRegionResponse) {
            // 1.1 主线程中加入到List，多线程中加入会有并发问题
            userRegionResponse1.add(userRegion);
            futureList.add(CompletableFuture.runAsync(() -> {
                //1.2 原来的id值先置空
                String id = userRegion.getId();
                try{
                    userRegion.setId(null);
                    String regionTelephone = userRegion.getRegionTelephone();
                    if (StringUtils.isNotEmpty(regionTelephone) && StringUtils.isNotEmpty(userRegion.getRegionTelephoneTm())) {
                        try { //解密有问题时继续执行,for中可以加try catch，只有当异常时打印链路日志才会影响执行效率
                            regionTelephone = DesensitizeTools.decryptNumber(regionTelephone);
                            if (StringUtils.isNotEmpty(regionTelephone)) {

                                userRegion.setRegionTelephone(regionTelephone);
                                userRegion.setRegionTelephoneTm("");
                                // 1.3 需要更新时，还原原来的id值
                                userRegion.setId(id);
                            }
                        } catch (Exception e) {
                            log.error("userRegionid:{} 手机号:{} 解密手机号失败：{}", id, regionTelephone, e.getMessage());
                        }
                    }
                } catch (Exception e){
                    log.error("本条userRegion数据解密失败，Id => {}", id, e);
                }
            }, encryptExecutor));
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        // 1.4 主线程中去除id为空的数据
        for (int i = userRegionResponse1.size() - 1; i >= 0; i--) {
            if (StringUtils.isEmpty(userRegionResponse1.get(i).getId())) {
                userRegionResponse1.remove(i);
            }
        }
    }

    /**
     * 处理用户信息完整性
     */
    private void asyncProcessIntegrityOfUser() {
        if (!IntegrityCheckUtils.isEnabledMac()) {
            log.info("当前未开启完整性校验参数【{}】，不处理", Constant.ENABLED_MAC);
            return;
        }
        log.info("开始执行用户完整性数据处理----");
        long startTime = System.currentTimeMillis();

        try {
            UserQuery userQuery = identityService.createUserQuery().wzxcheckIsNullOrNotTrue();
            long count = userQuery.count();
            log.info("待处理用户完整性总数 => {}", count);
            if (count == 0) {
                log.info("没有需要处理完整性的用户数据");
                return;
            }
            long pageNum = getPageNum(count);
            int start = 0;
            for (int i = 1; i <= pageNum; i++) {
                log.info("开始处理用户表第{}页旧数据完整性", i);
                List<User> userList = userQuery.listPage(start, Integer.parseInt(pageSize));
                if (CollectionUtil.isNotEmpty(userList)) {
                    multiThreadProcessIntegrityOfUserList(userList);
                    try {
                        identityService.updateBatchUsersWzx(userList);
                    } catch (Exception e) {
                        log.error("第{}页用户完整性数据批量更新失败", i, e);
                        //当前批次失败后，跳过这一批，多批失败，则跳过多批，等能成功的都成功后，下次重启服务来处理之前失败的批次
                        start += Integer.parseInt(pageSize);
                    }
                }
                log.info("用户表第{}页旧数据完整性处理完成", i);
            }
        } catch (Exception e) {
            log.error("用户完整性数据处理失败", e.getMessage(), e);
        }
        long endTime = System.currentTimeMillis();
        log.info("用户完整性数据处理完毕----,耗时{}ms", endTime - startTime);
    }

    private void multiThreadProcessIntegrityOfUserList(List<User> userList) {
        if (CollectionUtil.isEmpty(userList)) {
            return;
        }
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (User user : userList) {
            futureList.add(CompletableFuture.runAsync(() -> {
                /**
                 *  由于快速构建V5.0版本 完整性处理时，staffName和userCode是明文的 所以V4.1同步处理 ,  数据先解密 明文数据做完整性处理
                 */
                String userCode = user.getUserCode();
                String staffName = user.getStaffName();
                if (user.getEncrypt() != null && user.getEncrypt() == 1) {
                    try {
                        if (StringUtils.isNotEmpty(userCode)) {
                            String unEncryptUserCode = EncryptFieldUtils.decryptNumberV5(userCode);
                            user.setUserCode(unEncryptUserCode);
                        }
                    } catch (Exception e) {
                        log.error("用户id:{} 用户编码:{} 解密用户名失败：{}, 跳过本次完整性处理", user.getId(), user.getUserCode(), e.getMessage());
                        return;
                    }
                    try {
                        if (StringUtils.isNotEmpty(staffName)) {
                            String unEncryptStaffName = EncryptFieldUtils.decryptNumberV5(staffName);
                            user.setStaffName(unEncryptStaffName);
                        }
                    } catch (Exception e) {
                        log.error("用户id:{} 用户昵称:{} 解密昵称失败：{}, 跳过本次完整性处理", user.getId(), user.getStaffName(), e.getMessage());
                        return;
                    }
                }

                try {
                    IntegrityCheckUtils.generateMac(user);
                } catch (Exception e) {
                    log.error("本条用户完整性数据处理失败，userId => {}", user.getId(), e);
                }
                if (user.getEncrypt() != null && user.getEncrypt() == 1) {
                    // 做完整性处理后 恢复之前的密文
                    user.setStaffName(staffName);
                    user.setUserCode(userCode);
                }
            }, encryptExecutor));
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
    }

    /**
     * 处理用户扩展信息完整性
     */
    private void asyncProcessIntegrityOfUserExt() {
        if (!IntegrityCheckUtils.isEnabledMac()) {
            log.info("当前未开启完整性校验参数【{}】，不处理", Constant.ENABLED_MAC);
            return;
        }
        log.info("开始执行用户扩展信息完整性数据处理----");
        long startTime = System.currentTimeMillis();
        try {
            UserExtQuery userExtQuery = identityService.createUserExtQuery().wzxcheckIsNullOrNotTrue();
            long count = userExtQuery.count();
            log.info("待处理用户扩展信息完整性总数 => {}", count);
            if (count == 0) {
                log.info("没有需要处理完整性的用户扩展信息数据");
                return;
            }
            long pageNum = getPageNum(count);
            int start = 0;
            for (int i = 1; i <= pageNum; i++) {
                log.info("开始处理用户扩展信息表第{}页旧数据完整性", i);
                List<UserExt> userExtList = userExtQuery.listPage(start, Integer.parseInt(pageSize));
                if (CollectionUtil.isNotEmpty(userExtList)) {
                    multiThreadProcessIntegrityOfUserExtList(userExtList);
                    try {
                        identityService.updateBatchUserExts(userExtList);
                    } catch (Exception e) {
                        log.error("第{}页用户扩展信息完整性数据批量更新失败", i, e);
                        //当前批次失败后，跳过这一批，多批失败，则跳过多批，等能成功的都成功后，下次重启服务来处理之前失败的批次
                        start += Integer.parseInt(pageSize);
                    }
                }
                log.info("用户扩展信息表第{}页旧数据完整性处理完成", i);
            }
        } catch (Exception e) {
            log.error("用户扩展信息完整性数据处理失败", e.getMessage(), e);
        }
        long endTime = System.currentTimeMillis();
        log.info("用户扩展信息完整性数据处理完毕----,耗时{}ms", endTime - startTime);
    }

    private void multiThreadProcessIntegrityOfUserExtList(List<UserExt> userExtList) {
        if (CollectionUtil.isEmpty(userExtList)) {
            return;
        }
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (UserExt userExt : userExtList) {
            futureList.add(CompletableFuture.runAsync(() -> {
                try {
                    IntegrityCheckUtils.generateMac(userExt);
                } catch (Exception e) {
                    log.error("本条用户扩展信息完整性数据处理失败，userId => {}", userExt.getId(), e);
                }
            }, encryptExecutor));

        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
    }

    /**
     * 处理角色信息完整性
     */
    private void asyncProcessIntegrityOfRole() {
        if (!IntegrityCheckUtils.isEnabledMac()) {
            log.info("当前未开启完整性校验参数【{}】，不处理", Constant.ENABLED_MAC);
            return;
        }
        log.info("开始执行角色完整性数据处理----");
        long startTime = System.currentTimeMillis();
        try {
            RoleQuery roleQuery = rolemgrService.createRoleQuery().wzxcheckIsNullOrNotTrue();
            long count = roleQuery.count();
            log.info("待处理角色完整性总数 => {}", count);
            if (count == 0) {
                log.info("没有需要处理完整性的角色数据");
                return;
            }
            long pageNum = getPageNum(count);
            int start = 0;
            for (int i = 1; i <= pageNum; i++) {
                log.info("开始处理角色表第{}页旧数据完整性", i);
                List<Role> roleList = roleQuery.listPage(start, Integer.parseInt(pageSize));
                if (CollectionUtil.isNotEmpty(roleList)) {
                    multiThreadProcessIntegrityOfRoleList(roleList);
                    try {
                        rolemgrService.updateRoleBatch(roleList);
                    } catch (Exception e) {
                        log.error("第{}页角色完整性数据批量更新失败", i, e);
                        //当前批次失败后，跳过这一批，多批失败，则跳过多批，等能成功的都成功后，下次重启服务来处理之前失败的批次
                        start += Integer.parseInt(pageSize);
                    }
                }
                log.info("角色表第{}页旧数据完整性处理完成", i);
            }
        } catch (Exception e) {
            log.error("角色完整性数据处理失败", e.getMessage(), e);
        }
        long endTime = System.currentTimeMillis();
        log.info("角色完整性数据处理完毕----,耗时{}ms", endTime - startTime);
    }

    private void multiThreadProcessIntegrityOfRoleList(List<Role> roleList) {
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (Role role : roleList) {
            futureList.add(CompletableFuture.runAsync(() -> {
                try {
                    IntegrityCheckUtils.generateMac(role);
                } catch (Exception e) {
                    log.error("本条角色完整性数据处理失败，roleId => {}", role.getId(), e);
                }
            }, encryptExecutor));
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
    }

    /**
     * 处理用户角色信息完整性
     */
    private void asyncProcessIntegrityOfUserRole() {
        if (!IntegrityCheckUtils.isEnabledMac()) {
            log.info("当前未开启完整性校验参数【{}】，不处理", Constant.ENABLED_MAC);
            return;
        }
        log.info("开始执行用户角色关联完整性数据处理----");
        long startTime = System.currentTimeMillis();
        try {
            UserRoleQuery userRoleQuery = rolemgrService.createUserRoleQuery().wzxcheckIsNullOrNotTrue();
            long count = userRoleQuery.count();
            log.info("待处理用户角色关联完整性总数 => {}", count);
            if (count == 0) {
                log.info("没有需要处理完整性的用户角色数据");
                return;
            }
            long pageNum = getPageNum(count);
            int start = 0;
            for (int i = 1; i <= pageNum; i++) {
                log.info("开始处理用户角色关联表第{}页旧数据完整性", i);
                List<UserRole> userRoleList = userRoleQuery.listPage(start, Integer.parseInt(pageSize));
                if (CollectionUtil.isNotEmpty(userRoleList)) {
                    multiThreadProcessIntegrityOfUserRoleList(userRoleList);
                    try {
                        rolemgrService.updateUserRoleBatch(userRoleList);
                    } catch (Exception e) {
                        log.error("第{}页用户角色关联完整性数据批量更新失败", i, e);
                        //当前批次失败后，跳过这一批，多批失败，则跳过多批，等能成功的都成功后，下次重启服务来处理之前失败的批次
                        start += Integer.parseInt(pageSize);
                    }
                }
                log.info("用户角色关联表第{}页旧数据完整性处理完成", i);
            }
        } catch (Exception e) {
            log.error("用户角色完整性数据处理失败", e.getMessage(), e);
        }
        long endTime = System.currentTimeMillis();
        log.info("用户角色关联完整性数据处理完毕----,耗时{}ms", endTime - startTime);
    }

    private void multiThreadProcessIntegrityOfUserRoleList(List<UserRole> userRoleList) {
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (UserRole userRole : userRoleList) {
            futureList.add(CompletableFuture.runAsync(() -> {
                try {
                    IntegrityCheckUtils.generateMac(userRole);
                } catch (Exception e) {
                    log.error("本条用户角色完整性数据处理失败，userRoleId => {}", userRole.getId(), e);
                }
            }, encryptExecutor));
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
    }

    /**
     * 处理角色权限信息完整性
     */
    private void asyncProcessIntegrityOfRolePrivilege() {
        if (!IntegrityCheckUtils.isEnabledMac()) {
            log.info("当前未开启完整性校验参数【{}】，不处理", Constant.ENABLED_MAC);
            return;
        }
        log.info("开始执行角色权限完整性数据处理----");
        long startTime = System.currentTimeMillis();
        try {
            RolePrivilegeQuery rolePrivilegeQuery = rightsmgrService.createRolePrivilegeQuery().wzxcheckIsNullOrNotTrue();
            long count = rolePrivilegeQuery.count();
            log.info("待处理角色权限关联完整性总数 => {}", count);
            if (count == 0) {
                log.info("没有需要处理完整性的角色权限数据");
                return;
            }
            long pageNum = getPageNum(count);
            int start = 0;
            for (int i = 1; i <= pageNum; i++) {
                log.info("开始处理角色权限关联表第{}页旧数据完整性", i);
                List<RolePrivilege> rolePrivilegeList = rolePrivilegeQuery.listPage(start, Integer.parseInt(pageSize));
                if (CollectionUtil.isNotEmpty(rolePrivilegeList)) {
                    multiThreadProcessIntegrityOfRolePrivilegeList(rolePrivilegeList);
                    try {
                        rightsmgrService.updateRolePrivilegeBatch(rolePrivilegeList);
                    } catch (Exception e) {
                        log.error("第{}页角色权限完整性数据批量更新失败", i, e);
                        //当前批次失败后，跳过这一批，多批失败，则跳过多批，等能成功的都成功后，下次重启服务来处理之前失败的批次
                        start += Integer.parseInt(pageSize);
                    }
                }
                log.info("角色权限关联表第{}页旧数据完整性处理完成", i);
            }
        } catch (Exception e) {
            log.error("角色权限完整性数据处理失败", e.getMessage(), e);
        }
        long endTime = System.currentTimeMillis();
        log.info("角色权限完整性数据处理完毕----,耗时{}ms", endTime - startTime);
    }

    private void multiThreadProcessIntegrityOfRolePrivilegeList(List<RolePrivilege> rolePrivilegeList) {
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (RolePrivilege rolePrivilege : rolePrivilegeList) {
            futureList.add(CompletableFuture.runAsync(() -> {
                try {
                    IntegrityCheckUtils.generateMac(rolePrivilege);
                } catch (Exception e) {
                    log.error("本条角色权限完整性数据处理失败，rolePrivilegeId => {}", rolePrivilege.getId(), e);
                }
            }, encryptExecutor));
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
    }

    /**
     * 处理日志信息完整性
     */
    private void asyncProcessIntegrityOfLog() {
        if (!IntegrityCheckUtils.isEnabledMac()) {
            log.info("当前未开启完整性校验参数【{}】，不处理", Constant.ENABLED_MAC);
            return;
        }
        log.info("开始执行日志完整性数据处理----");
        long startTime = System.currentTimeMillis();
        try {
            LogDesignQuery logDesignQuery = logmgrService.createLogDesignQuery().wzxcheckIsNullOrNotTrue().loginAndLogout();
            long count = logDesignQuery.count();
            log.info("待处理日志完整性总数 => {}", count);
            if (count == 0) {
                log.info("没有需要处理完整性的日志数据");
                return;
            }
            long pageNum = getPageNum(count);
            int start = 0;
            for (int i = 1; i <= pageNum; i++) {
                log.info("开始处理日志表第{}页数据完整性", i);
                List<LogDesign> logDesignList = logDesignQuery.listPage(start, Integer.parseInt(pageSize));
                if (CollectionUtil.isNotEmpty(logDesignList)) {
                    multiThreadProcessIntegrityOfLogList(logDesignList);
                    try {
                        logmgrService.updateBatchLog(logDesignList);
                    } catch (Exception e) {
                        log.error("第{}页日志完整性数据批量更新失败", i, e);
                        //当前批次失败后，跳过这一批，多批失败，则跳过多批，等能成功的都成功后，下次重启服务来处理之前失败的批次
                        start += Integer.parseInt(pageSize);
                    }
                }
                log.info("日志表第{}页旧数据完整性处理完成", i);
            }
        } catch (Exception e) {
            log.error("日志完整性数据处理失败", e.getMessage(), e);
        }
        long endTime = System.currentTimeMillis();
        log.info("日志完整性数据处理完毕----,耗时{}ms", endTime - startTime);
    }

    private void multiThreadProcessIntegrityOfLogList(List<LogDesign> logDesignList) {
        List<CompletableFuture<Void>> futureList = new ArrayList();
        for (LogDesign logDesign : logDesignList) {
            futureList.add(CompletableFuture.runAsync(() -> {
                try {
                    IntegrityCheckUtils.generateMac(logDesign);
                } catch (Exception e) {
                    log.error("本条日志完整性数据处理失败，logId => {}", logDesign.getId(), e);
                }
            }, encryptExecutor));
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
    }

    private int getPageNum(long total) {
        int result = (int) Math.ceil((double) total / Integer.parseInt(pageSize));
        return result == 0 ? 1 : result;
    }

    private DbSqlSession getDbSession() {
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngines.getDefaultProcessEngine().getProcessEngineConfiguration();
        CommandContext context = new CommandContext(new Command() {
            @Override
            public Object execute(CommandContext commandContext) {
                return null;
            }
        }, (BaseEngineConfigurationImpl) processEngineConfiguration);
        DbSqlSession dbSession = context.getDbSqlSession();
        return dbSession;
    }

    private void updateUserPhoneOrPasswordBatch(List<User> userEntityList) {
        DbSqlSession dbSession = getDbSession();
        try {
            dbSession.getSqlSession().update("updateUserPhoneOrPasswordBatch", userEntityList);
        } catch (Exception e) {
            log.error("updateUserPhoneOrPasswordBatch -> ", e);
        } finally {
            dbSession.close();
        }
    }

    private void updateUserPhoneOrPassword(User user) {
        DbSqlSession dbSession = getDbSession();
        try {
            dbSession.getSqlSession().update("updateUserPhoneOrPassword", user);
        } catch (Exception e) {
            log.error("updateUserPhoneOrPassword -> ", e);
        } finally {
            dbSession.close();
        }
    }

    private void updateDepartmentPhone(Department department) {
        DbSqlSession dbSession = getDbSession();
        try {
            dbSession.getSqlSession().update("updateDepartmentPhone", department);
        } catch (Exception e) {
            log.error("updateDepartmentPhone -> ", e);
        } finally {
            dbSession.close();
        }
    }

    private void updateDepartmentPhoneBatch(List<Department> departmentList) {
        DbSqlSession dbSession = getDbSession();
        try {
            dbSession.getSqlSession().update("updateDepartmentPhoneBatch", departmentList);
        } catch (Exception e) {
            log.error("updateDepartmentPhoneBatch -> ", e);
        } finally {
            dbSession.close();
        }
    }

    private void updateUserExtCertificatecode(UserExt userExt) {
        DbSqlSession dbSession = getDbSession();
        try {
            dbSession.getSqlSession().update("updateUserExtCertificatecode", userExt);
        } catch (Exception e) {
            log.error("updateUserExtCertificatecode -> ", e);
        } finally {
            dbSession.close();
        }
    }

    private void updateUserExtCertificatecodeBatch(List<UserExt> userExts) {
        DbSqlSession dbSession = getDbSession();
        try {
            dbSession.getSqlSession().update("updateUserExtCertificatecodeBatch", userExts);
        } catch (Exception e) {
            log.error("updateUserExtCertificatecodeBatch -> ", e);
        } finally {
            dbSession.close();
        }
    }

    private void updateUserRegionTelephone(UserRegion userRegion) {
        DbSqlSession dbSession = getDbSession();
        try {
            dbSession.getSqlSession().update("updateUserRegionTelephone", userRegion);
        } catch (Exception e) {
            log.error("updateUserRegionTelephone -> ", e);
        } finally {
            dbSession.close();
        }
    }

    private void updateUserRegionTelephoneBatch(List<UserRegion> userRegionList) {
        DbSqlSession dbSession = getDbSession();
        try {
            dbSession.getSqlSession().update("updateUserRegionTelephoneBatch", userRegionList);
        } catch (Exception e) {
            log.error("updateUserRegionTelephoneBatch -> ", e);
        } finally {
            dbSession.close();
        }
    }


    public static void main(String[] args) {
        /*2b53c966d94ca422a6c1b92369a55c74e2bd63e53551a8bbf58b817b*/

       /* AlgorithmHandler smHandler = AlgorithmFactory.createAlgorithmHandler("base");

        //String password = smHandler.encodePassword("admin", "admin", null);
        boolean b = smHandler.validatePassword("admin", "1234", "4d5956ae7372070384f2802d9333cea81c6674bcda1f83f9e46fd240", null);

        System.out.println("");*/
        // WestoneTools.getWestoneKeyId();

    }


    private void updateUserCode(User user) {
        DbSqlSession dbSession = getDbSession();
        try {
            dbSession.getSqlSession().update("updateUserCode", user);
        } catch (Exception e) {
            log.error("updateUserCode -> ", e);
        } finally {
            dbSession.close();
        }
    }

    private long selectUserRegionByQueryCriteriaAll() {
        DbSqlSession dbSession = getDbSession();
        long count = 0;
        try {
            List<Object> lists = dbSession.getSqlSession().selectList("selectUserRegionByQueryCriteriaAll");
            if (!CollectionUtils.isEmpty(lists)) {
                count = lists.size();
            }
        } catch (Exception e) {
            log.error("selectUserRegionByQueryCriteriaAll -> ", e);
        } finally {
            dbSession.close();
        }
        return count;
    }
}
