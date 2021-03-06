package cn.booktable.appadmin.controller.sys;

import cn.booktable.core.constant.SystemConst;
import cn.booktable.core.page.PageDo;
import cn.booktable.core.view.JsonView;
import cn.booktable.exception.BusinessException;
import cn.booktable.modules.entity.sys.SysActionLogDo;
import cn.booktable.modules.entity.sys.SysUserDo;
import cn.booktable.modules.service.sys.SysParamService;
import cn.booktable.modules.service.sys.SysActionLogService;
import cn.booktable.modules.service.sys.SysUserService;
import cn.booktable.appadmin.config.AdminSysConfig;
import cn.booktable.appadmin.controller.base.ActionLogConst;
import cn.booktable.appadmin.controller.base.BaseController;
import cn.booktable.appadmin.controller.base.PermissionCode;
import cn.booktable.util.AssertUtils;
import cn.booktable.util.StringUtils;
import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author ljc
 */
@Controller
@RequestMapping("/sys/user")
public class SysUserController extends BaseController {
    private static Logger logger= LoggerFactory.getLogger(SysUserController.class);

    @Autowired
    private SysUserService sysUserService;

    @Resource
    private SysParamService sysParamService;
    @Autowired
    private SysActionLogService sysActionLogService;
    @Autowired
    private AdminSysConfig adminSysConfig;


    @RequestMapping(value="/list",method=RequestMethod.GET)
    public ModelAndView queryUserList_methodGet()
    {
        ModelAndView view=new ModelAndView("sys/user/list");
        return view;
    }

    @RequestMapping(value="/list",method=RequestMethod.POST)
    public ModelAndView queryUserList_methodPost(HttpServletRequest request,String startDate,String endDate, Long pageIndex,Integer pageSize)
    {
        ModelAndView view=new ModelAndView("sys/user/list_table");
        try{
            checkPermission(PermissionCode.system_user_list);
            Map<String, Object> selectItem=new HashMap<>();
            selectItem.put("userName", request.getParameter("userName"));
            selectItem.put("realName", request.getParameter("realName"));
            selectItem.put("telphone", request.getParameter("telphone"));
            setDateBetweemToMap(selectItem,startDate,endDate);
            pageIndex=pageIndex==null?1L:pageIndex;
            pageSize=pageSize==null?20:pageSize;

            PageDo<SysUserDo> result= sysUserService.querySysUserListPage(pageIndex, pageSize, selectItem);
            view.addObject("pagedata",result);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("??????????????????????????????", e);
        }
        return view;
    }

    @RequestMapping(value="/add",method=RequestMethod.GET)
    public ModelAndView addUser_methodGet(Integer id)
    {
        ModelAndView view=new ModelAndView("sys/user/edit");

        try{
            SysUserDo sysUser = sysUserService.findSysUserById(id);
            view.addObject("sysUser", sysUser);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("????????????ID??????????????????", e);
        }

        return view;
    }

    @RequestMapping(value="/add",method=RequestMethod.POST)
    public JsonView<String> addUser(HttpServletRequest request, SysUserDo user,String password2)
    {
        if(null != user.getId()){//????????????
            return editUser(user,password2);
        }

        JsonView<String> view=new JsonView<String>();
        SysActionLogDo actionLogDo=getUserActionLog(request,ActionLogConst.model_system_sysuser,"??????????????????",SysActionLogDo.LEVEL_IMPORTANT);
        try{
            checkPermission(PermissionCode.system_user_add);
            AssertUtils.isNotBlank(user.getUserName(), "?????????????????????");
            AssertUtils.isNotBlank(user.getTelphone(), "?????????????????????");
            AssertUtils.isNotBlank(user.getPassword(), "????????????????????????");
            SysUserDo dbUser=sysUserService.findSysUserByUserName(user.getUserName());
            if(dbUser!=null)
            {
                throw new BusinessException("??????????????????");
            }
            if(!Pattern.compile(SystemConst.REGEX_TELEPHONE).matcher(user.getTelphone()).matches()){
                throw new BusinessException("????????????????????????");
            }
            if(!Pattern.compile(SystemConst.REGEX_PWD).matcher(user.getPassword()).matches()){
                throw new BusinessException("???????????????8???,????????????????????????????????????");
            }
            if(!user.getPassword().equals(password2))
            {
                throw new BusinessException("?????????????????????");
            }

            if(user.getLocked()==null)
            {
                user.setLocked(1);//?????????
            }
            String psw= DigestUtils.md5Hex(password2+ adminSysConfig.getPasswordKey());
            user.setPassword(psw);
            user.setPlatformId(currentPlatformId());

            SysUserDo currentUser=currentUser();
            AssertUtils.notNull(currentUser,"????????????");
            actionLogDo.setContent("???????????????"+currentUser.getUserName()+",???????????????"+user.getUserName());

            sysUserService.insertSysUser(user);
            view.setCode(view.CODE_SUCCESS);
            view.setMsg("????????????");
        }catch(BusinessException e)
        {
            setPromptException(view, e);
            actionLogDo.setContent("?????????????????????"+e.getMessage());
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("??????????????????", e);
            actionLogDo.setContent("?????????????????????"+e.getMessage());
        }finally {
            sysActionLogService.insertSysActionLog(actionLogDo);
        }
        return view;
    }

    /**
     * ????????????
     */
    public JsonView<String> editUser(SysUserDo user,String password2)
    {
        JsonView<String> view=new JsonView<String>();
        try{
            checkPermission(PermissionCode.system_user_edit);
            AssertUtils.isNotBlank(user.getTelphone(), "?????????????????????");
            if(!Pattern.compile(SystemConst.REGEX_TELEPHONE).matcher(user.getTelphone()).matches()){
                throw new BusinessException("????????????????????????");
            }
            if(user.getLocked()==null)
            {
                user.setLocked(1);//?????????
            }
//			SysUserDo newUser = new SysUserDo();
//			newUser.setId(user.getId());
//			newUser.setRealName(user.getRealName());
//			newUser.setTelphone(user.getTelphone());
//			newUser.setLocked(user.getLocked());
            SysUserDo dbUser=sysUserService.findSysUserById(user.getId());
            dbUser.setRealName(user.getRealName());
            dbUser.setTelphone(user.getTelphone());
            dbUser.setLocked(user.getLocked());
            dbUser.setMotto(user.getMotto());
            sysUserService.updateSysUserById(dbUser);
            view.setCode(view.CODE_SUCCESS);
            view.setMsg("????????????");
        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception ex) {
            setPromptException(view, ex);
            logger.error("??????????????????", ex);
        }
        return view;
    }

    @RequestMapping(value="/modifyUserRoles",method=RequestMethod.POST)
    public JsonView<String> modifyUserRoles(HttpServletRequest request, String roleListStr,Integer userId)
    {
        JsonView<String> view=new JsonView<String>();
        SysActionLogDo actionLogDo=getUserActionLog(request,ActionLogConst.model_system_sysuser,"??????????????????",SysActionLogDo.LEVEL_IMPORTANT);
        try{
            checkPermission(PermissionCode.system_user_role_setting);
            AssertUtils.notNull(userId, "??????????????????");
            String[] roles=null;
            if(StringUtils.isNotBlank(roleListStr))
            {
                roles=roleListStr.split(",");
            }
            SysUserDo user= sysUserService.findSysUserById(userId);
            AssertUtils.notNull(user,"????????????");
            actionLogDo.setContent("????????????:"+user.getUserName()+"????????????"+roleListStr);
            sysUserService.modifyUserRoles(roles, userId);
            view.setCode(view.CODE_SUCCESS);
            view.setMsg("????????????");
        }catch (BusinessException e) {
            setPromptException(view, e);
            actionLogDo.setContent("???????????????????????????"+e.getMessage());
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("????????????????????????",e);
            actionLogDo.setContent("???????????????????????????"+e.getMessage());
        }finally {
            sysActionLogService.insertSysActionLog(actionLogDo);
        }
        return view;
    }

    @RequestMapping(value="/roleUserList",method=RequestMethod.GET)
    public ModelAndView queryRoleUserList_methodGet(HttpServletRequest request,String roleId)
    {
        ModelAndView view=new ModelAndView("sys/user/roleUserList");
        try{
            view.addObject("roleId", roleId);
        }catch (Exception e) {
            logger.error("???????????????????????????",e);
        }
        return view;
    }

    @RequestMapping(value="/roleUserList",method=RequestMethod.POST)
    public ModelAndView queryRoleUserList_methodPost(HttpServletRequest request,String startDate,String endDate, Long pageIndex,Integer pageSize)
    {
        ModelAndView view=new ModelAndView("sys/user/roleUserList_table");
        try{
            checkPermission(PermissionCode.system_role_permission_edit);
            String roleId=request.getParameter("roleId");
            Map<String, Object> selectItem=new HashMap<String, Object>();
            selectItem.put("userName", request.getParameter("userName"));
            selectItem.put("realName", request.getParameter("realName"));
            setDateBetweemToMap(selectItem,startDate,endDate);
            pageIndex=pageIndex==null?1L:pageIndex;
            pageSize=pageSize==null?20:pageSize;
            PageDo<SysUserDo> result= sysUserService.queryUserByRoleIdListPage(roleId, pageIndex, pageSize, selectItem);
            view.addObject("pagedata",result);
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("??????????????????????????????", e);
        }
        return view;
    }

    @RequestMapping(value="/modifyUserLock",method=RequestMethod.POST)
    public JsonView<String> modifyUserLock(HttpServletRequest request, String roleListStr, Integer userId, Boolean isLock)
    {
        JsonView<String> view=new JsonView<String>();
        SysActionLogDo actionLogDo=getUserActionLog(request,ActionLogConst.model_system_sysuser,"????????????????????????",SysActionLogDo.LEVEL_IMPORTANT);
        try{
            checkPermission(PermissionCode.system_user_lock);
            AssertUtils.notNull(userId, "??????????????????");
            AssertUtils.notNull(isLock, "?????????????????????");
            SysUserDo user=sysUserService.findSysUserById(userId);
            AssertUtils.notNull(user,"??????????????????");
            actionLogDo.setContent("????????????:"+user.getUserName() + "??????????????????"+(isLock==true?"??????":"??????"));
            Integer dbResult=sysUserService.updateUserLockStatus(userId, isLock);
            AssertUtils.isTrue(dbResult!=null && dbResult.intValue()>0,"??????????????????????????????");
            view.setCode(view.CODE_SUCCESS);
            view.setMsg("????????????");
        }catch (BusinessException e) {
            setPromptException(view, e);
            actionLogDo.setContent("??????????????????????????????"+e.getMessage());
        }catch (Exception ex) {
            setPromptException(view, ex);
            logger.error("??????????????????????????????",ex);
            actionLogDo.setContent("??????????????????????????????"+ex.getMessage());
        }finally {
            sysActionLogService.insertSysActionLog(actionLogDo);
        }
        return view;
    }

    @RequestMapping(value="/status",method=RequestMethod.POST)
    public JsonView<String> deleteUser(HttpServletRequest request, String roleListStr,Integer userId,Integer status)
    {
        JsonView<String> view=new JsonView<String>();
        SysActionLogDo actionLogDo=getUserActionLog(request,ActionLogConst.model_system_sysuser,"????????????",SysActionLogDo.LEVEL_IMPORTANT);
        try{
            checkPermission(PermissionCode.system_user_delete);
            AssertUtils.notNull(userId, "??????????????????");
            AssertUtils.notNull(status, "??????????????????");

            SysUserDo user=sysUserService.findSysUserById(userId);
            AssertUtils.notNull(user,"????????????");
            actionLogDo.setContent("????????????:"+user.getUserName()+"("+user.getRealName()+")");

            if(status==1)
            {
                Integer dbResult = sysUserService.recoverUserStatusById(userId);
                AssertUtils.isTrue(dbResult != null && dbResult.intValue() > 0, "??????????????????");
                view.setCode(view.CODE_SUCCESS);
                view.setMsg("????????????");
            }else {
                Integer dbResult = sysUserService.deleteSysUserById(userId);
                AssertUtils.isTrue(dbResult != null && dbResult.intValue() > 0, "??????????????????");
                view.setCode(view.CODE_SUCCESS);
                view.setMsg("????????????");
            }
        }catch (BusinessException e) {
            setPromptException(view, e);
            actionLogDo.setContent("?????????????????????"+e.getMessage());
        }catch (Exception e) {
            setPromptException(view, e);
            logger.error("????????????????????????",e);
            actionLogDo.setContent("?????????????????????"+e.getMessage());
        }finally {
            sysActionLogService.insertSysActionLog(actionLogDo);
        }
        return view;
    }

    /**
     *????????????
     */
    @RequestMapping(value = "/resetPassword")
    public JsonView<String> resetPassword(HttpServletRequest request, Integer userId,@RequestParam(defaultValue="")String password){
        JsonView<String> view = new JsonView<String>();

        SysActionLogDo actionLogDo=getUserActionLog(request, ActionLogConst.model_system_sysuser,"????????????",SysActionLogDo.LEVEL_IMPORTANT);
        try {
            checkPermission(PermissionCode.system_user_password_reset);
            if(StringUtils.isBlank(password)){
                password = sysParamService.queryValueByCode("DEFAULT_PASSWORD");
                password = StringUtils.isNotBlank(password)?password:"Aa123456";
            }
            SysUserDo user=sysUserService.findSysUserById(userId);
            AssertUtils.notNull(user,"??????????????????");
            actionLogDo.setContent("???????????????"+user.getUserName()+"("+user.getRealName()+")?????????");
            String newPsw=DigestUtils.md5Hex(password.trim()+ adminSysConfig.getPasswordKey());
            if(sysUserService.resetPassword(userId, newPsw,null) == 1){//?????????????????????????????????????????????modifyPwdTime
                view.setCode(JsonView.CODE_SUCCESS);
                view.setMsg("??????????????????");
            }else{
                view.setCode(JsonView.CODE_SUCCESS);
                view.setMsg("??????????????????");
            }
        } catch (BusinessException e) {
            setPromptException(view, e);
            actionLogDo.setContent("?????????????????????"+e.getMessage());
        } catch (Exception e) {
            logger.error("????????????????????????", e);
            setPromptMessage(view, JsonView.CODE_FAILE, "????????????????????????");
            actionLogDo.setContent("?????????????????????"+e.getMessage());
        }finally {
            sysActionLogService.insertSysActionLog(actionLogDo);
        }
        return view;
    }


    /**
     * ????????????ID
     * @return
     */
    protected int currentPlatformId()
    {
        SysUserDo sysUser=currentUser();
        if(sysUser==null || sysUser.getPlatformId()==null)
        {
            return SystemConst.PLATFORM_DEFAULT;
        }else {
            return sysUser.getPlatformId().intValue();
        }
    }

    /**
     * ??????????????????
     * @param request
     * @param model
     * @param detail
     * @param level
     * @return
     */
    protected SysActionLogDo getUserActionLog(HttpServletRequest request,String model,String detail,String level)
    {
        SysActionLogDo actionLogDo=new SysActionLogDo();
        actionLogDo.setMode(model);
        actionLogDo.setContent(JSON.toJSONString(request.getParameterMap()));
        actionLogDo.setActionUrl(request.getRequestURI());
        actionLogDo.setUserId(currentUser().getId());
        actionLogDo.setDetail(detail);
        actionLogDo.setLevel(level);
        return actionLogDo;
    }
}
