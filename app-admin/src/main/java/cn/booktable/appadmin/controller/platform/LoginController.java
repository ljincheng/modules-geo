package cn.booktable.appadmin.controller.platform;

import cn.booktable.core.constant.SystemConst;
import cn.booktable.cryptojs.CryptoJSUtil;
import cn.booktable.exception.BusinessException;
import cn.booktable.modules.entity.sys.AtlantisHtmlMenu;
import cn.booktable.modules.entity.sys.SysUserDo;
import cn.booktable.modules.entity.sys.SystemDo;
import cn.booktable.modules.service.sys.AtlantisHtmlMenuHandler;
import cn.booktable.modules.service.sys.MenuListHandler;
import cn.booktable.modules.service.sys.SysPermissionService;
import cn.booktable.modules.service.sys.SysUserService;
import cn.booktable.appadmin.config.AdminSysConfig;
import cn.booktable.appadmin.controller.base.BaseController;
import cn.booktable.util.AssertUtils;
import cn.booktable.util.StringUtils;
import cn.booktable.util.VerifyCodeUtils;
import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author ljc
 */
@Controller
//@EnableAutoConfiguration
public class LoginController extends BaseController {
    private static Logger logger= LoggerFactory.getLogger(LoginController.class);
    private static final String VIEWNAME_MAIN="platform/main";

    @Autowired
    private AdminSysConfig adminSysConfig;
    @Autowired
    private SysPermissionService sysPermissionService;
    @Autowired
    private SysUserService sysUserService;

    private void osInfo(ModelAndView modelAndView)
    {
        modelAndView.addObject("osName", "????????????");
    }

    @RequestMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //?????????????????????
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        String verifyCode = VerifyCodeUtils.generateTextCode(VerifyCodeUtils.TYPE_NUM_ONLY, 4, null);
        //??????????????????HttpSession??????
//		request.getSession().setAttribute("verifyCode", verifyCode);
        SecurityUtils.getSubject().getSession().setAttribute("verifyCode", verifyCode);
        logger.info("???????????????????????????[" + verifyCode + "],????????????HttpSession???");
        //?????????????????????????????????JPEG??????
        response.setContentType("image/jpeg");
        BufferedImage bufferedImage = VerifyCodeUtils.generateImageCode(verifyCode, 90, 30, 3, true, Color.WHITE, Color.BLACK, null);
        //???????????????
        ImageIO.write(bufferedImage, "JPEG", response.getOutputStream());
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView login(String sayit) {
        ModelAndView retVal = new ModelAndView();
        retVal.setViewName("platform/login");
        osInfo(retVal);
        return retVal;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ModelAndView login_post(HttpServletRequest request, String username, String password) {
        ModelAndView model = new ModelAndView();
        model.setViewName("platform/login");
        try{
            osInfo(model);
            //?????????????????????????????????????????????
            String submitCode = WebUtils.getCleanParam(request, "verifyCode");
            String verifyCode = (String)SecurityUtils.getSubject().getSession().getAttribute("verifyCode");
            logger.debug("??????[" + username + "]??????????????????????????????[" + submitCode + "],HttpSession??????????????????[" + verifyCode + "]");
            if (StringUtils.isEmpty(submitCode) || StringUtils.isEmpty(verifyCode) || !verifyCode.equals(submitCode.toLowerCase())){
                request.setAttribute("message_login", "??????????????????");
                return model;
            }
            try{

                if(StringUtils.isEmpty(verifyCode))
                {
                    request.setAttribute("message_login", "??????????????????");
                    return model;
                }
                String key="___".concat(verifyCode).concat("___");
                username= CryptoJSUtil.decrypt(username,key);
                password= CryptoJSUtil.decrypt(password,key);
            }catch (Exception ex)
            {
                request.setAttribute("message_login", "?????????????????????");
                return model;
            }

            String psw= DigestUtils.md5Hex(password+ adminSysConfig.getPasswordKey());
            UsernamePasswordToken token = new UsernamePasswordToken(username, psw);
            Subject currentUser = SecurityUtils.getSubject();
            if (currentUser.isAuthenticated()) {
                model = new ModelAndView("redirect:"+VIEWNAME_MAIN);
                return model;
            }
            currentUser.login(token);
            if (currentUser.isAuthenticated()) {
//                SavedRequest savedRequest= WebUtils.getSavedRequest(request);
//                if(null!=savedRequest){
//                    System.out.println("??????????????????????????????"+savedRequest.getRequestUrl());
//                    String viewName= "redirect:" + savedRequest.getRequestUrl().substring(1);
//                    return new ModelAndView(viewName);
//                }else {
                    model = new ModelAndView("redirect:"+VIEWNAME_MAIN);
                    return model;
//                }
            }

        }catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return model;
    }

    @RequestMapping("/platform/logout")
    public String logout(HttpServletRequest request){
        SecurityUtils.getSubject().logout();
        return InternalResourceViewResolver.REDIRECT_URL_PREFIX + "login";
    }


}
