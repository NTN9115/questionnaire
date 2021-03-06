package space.nyuki.questionnaire.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import space.nyuki.questionnaire.factory.TransFactory;
import space.nyuki.questionnaire.pojo.TransData;
import space.nyuki.questionnaire.service.ResultCollectionService;
import space.nyuki.questionnaire.utils.FileDownloadUtil;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("resultCollection")
public class ResultCollectionController {
	@Autowired
	private ResultCollectionService resultCollectionService;

	@GetMapping("{id}")
	@RequiresPermissions({"questionnaire:r","result_show:r","questionnaire:w","result_show:w"})
	public TransData getData(@PathVariable("id") String id) {
		return TransFactory.getSuccessResponse(resultCollectionService.getData(id));
	}

	@GetMapping("export/{id}")
	@RequiresGuest
	public ResponseEntity<Resource> exportXls(@PathVariable("id") String id, HttpServletRequest request) {
		System.out.println(SecurityUtils.getSubject().getPrincipal());
		Resource resource = resultCollectionService.exportXls(id);
		return FileDownloadUtil.responseEntity(request, resource);
	}
}
