package cn.booktable.appadmin.controller.kids;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.context.MessageSource;

import cn.booktable.appadmin.utils.ViewUtils;
import cn.booktable.core.view.JsonView;
import cn.booktable.util.AssertUtils;
import cn.booktable.exception.BusinessException;
import cn.booktable.appadmin.controller.base.BaseController;
import cn.booktable.core.page.PageDo;
import cn.booktable.modules.entity.kids.KidsCopyDataDo; 
import cn.booktable.modules.service.kids.KidsCopyDataService; 

/**
 * 
 * @author ljc
 */
@Controller
@RequestMapping("/kids/kidsCopyData")
public class KidsCopyDataController extends BaseController{

	private static Logger logger= LoggerFactory.getLogger(KidsCopyDataController.class);

	@Autowired
	private MessageSource messageSource;
	@Autowired
	private KidsCopyDataService kidsCopyDataService;
	
	@GetMapping("/list")
	public ModelAndView list(){
		ModelAndView view=new ModelAndView("kids/kidsCopyData/list");
		return view;
	}

	@PostMapping("/list")
	@RequiresPermissions("kids:kidsCopyData:list")
	public ModelAndView listTable(HttpServletRequest request,@RequestParam(required = false,defaultValue ="1")Long pageIndex,@RequestParam(required = false,defaultValue ="20")Integer pageSize,String startDate,String endDate){
	ModelAndView view=new ModelAndView("kids/kidsCopyData/list_table");
		 Map<String,Object> selectItem = getRequestToParamMap(request); 
		 view.addObject("pagedata",kidsCopyDataService.queryKidsCopyDataListPage(pageIndex,pageSize,selectItem));
		 return view;
	}

	@GetMapping(value="/add")
	public ModelAndView add(HttpServletRequest request){
		ModelAndView view=new ModelAndView("kids/kidsCopyData/add");
		return view;
	}

	@PostMapping(value="/add")
	@RequiresPermissions("kids:kidsCopyData:add")
	public JsonView<String> add_POST(HttpServletRequest request,KidsCopyDataDo kidsCopyDataDo){
		JsonView<String> view=new JsonView<String>();
		try{
			Integer dbRes= kidsCopyDataService.insertKidsCopyData(kidsCopyDataDo);
			AssertUtils.isPositiveNumber(dbRes,"????????????");
			ViewUtils.submitSuccess(view,":????????????"+dbRes+"?????????",messageSource);
		}catch (Exception ex) {
			ViewUtils.pushException(view,messageSource,ex);
			logger.error("????????????",ex);
		}
		return view;
	}

	@GetMapping(value="/view")
	@RequiresPermissions("kids:kidsCopyData:view")
	public ModelAndView view(HttpServletRequest request,Long id){
		ModelAndView view =new ModelAndView("/kids/kidsCopyData/view");
		KidsCopyDataDo kidsCopyDataDo= kidsCopyDataService.findKidsCopyDataById(id);
		view.addObject("kidsCopyDataDo", kidsCopyDataDo);
		return view;
	}

	@GetMapping(value="/edit/{id}")
	public ModelAndView edit(HttpServletRequest request,@PathVariable("id") Long id){
		ModelAndView view =new ModelAndView("kids/kidsCopyData/edit");
		KidsCopyDataDo kidsCopyDataDo= kidsCopyDataService.findKidsCopyDataById(id);
		view.addObject("kidsCopyDataDo", kidsCopyDataDo);
		return view;
	}

	@PostMapping(value="/edit/{id}")
	@RequiresPermissions("kids:kidsCopyData:edit")
	public JsonView<String> edit_POST(HttpServletRequest request,@PathVariable("id") Long id, KidsCopyDataDo kidsCopyDataDo){
		JsonView<String> view =new JsonView<String>();
		try{
			kidsCopyDataDo.setId(id);
			Integer dbRes= kidsCopyDataService.updateKidsCopyDataById(kidsCopyDataDo);
			AssertUtils.isPositiveNumber(dbRes,"????????????");
			ViewUtils.submitSuccess(view,messageSource);
		}catch (Exception ex) {
			ViewUtils.pushException(view,messageSource,ex);
			logger.error("????????????",ex);
		}
		return view;
	}

	@PostMapping(value="/delete")
	@RequiresPermissions("kids:kidsCopyData:delete")
	public JsonView<String> delete(HttpServletRequest request, Long id){
		JsonView<String> view=new JsonView<String>();
		try{
			AssertUtils.notNull(id, "????????????");
			Integer result=kidsCopyDataService.deleteKidsCopyDataById(id);
			AssertUtils.isPositiveNumber(result,"????????????");
			setPromptMessage(view, view.CODE_SUCCESS, "????????????");
		}catch (BusinessException e) {
			setPromptException(view, e);
		}catch (Exception ex) {
			logger.error("????????????",ex);
			setPromptException(view, ex);
		}
		return view;
	}
}