package space.nyuki.questionnaire.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import space.nyuki.questionnaire.factory.TransFactory;
import space.nyuki.questionnaire.group.GroupView;
import space.nyuki.questionnaire.pojo.Questionnaire;
import space.nyuki.questionnaire.pojo.QuestionnaireCreate;
import space.nyuki.questionnaire.pojo.TransData;
import space.nyuki.questionnaire.service.QuestionnaireService;

import java.util.Objects;

/**
 * @author ning
 * @createTime 12/1/19 4:13 PM
 * @description
 */

@Api(tags = "问卷调查表创建管理")
@RestController
@RequestMapping("/questionnaire")
public class QuestionnaireController {
	@Autowired
	private QuestionnaireService questionnaireService;

	@ApiOperation("创建问卷调查表")
	@PostMapping
	@RequiresPermissions("inquiry_config:w")
	public TransData createQuestionnaire(
			@RequestHeader(name = "token") String token,
			@Validated(GroupView.Create.class)
			@JsonView(GroupView.Create.class) @RequestBody Questionnaire questionnaire,
			BindingResult bindingResult) {
		questionnaireService.createQuestionnaire(questionnaire, token);
		return TransFactory.getSuccessResponse();
	}

	@JsonView(GroupView.Update.class)
	@RequiresPermissions(value = {"inquiry_config:r","inquiry_config:w"},logical = Logical.OR)
	@GetMapping("{id}")
	public TransData getQuestionnaireById(@PathVariable(name = "id") String id,
	                                      @RequestParam(name = "finish", required = false, defaultValue = "0") int isEdit) {
		if (isEdit == 0) {
			return TransFactory.getSuccessResponse(questionnaireService.getQuestionnaireById(id));
		} else {
			return TransFactory.getSuccessResponse(questionnaireService.getFinishQuestionnaireById(id));
		}

	}

	@ApiOperation("删除问卷调查表")
	@RequiresPermissions("inquiry_config:w")
	@DeleteMapping("{id}")
	public TransData deleteQuestionnaire(@PathVariable String id) {
		questionnaireService.deleteQuestionnaire(id);
		return TransFactory.getSuccessResponse();
	}

	@ApiOperation("还原被删除问卷调查表")
	@GetMapping("reverse/{id}")
	public TransData reverseDeletedQuestionnaire(@PathVariable String id) {
		questionnaireService.reverseDeleteQuestionnaire(id);
		return TransFactory.getSuccessResponse();
	}

	@ApiOperation("修改问卷调查表")
	@PutMapping
	@RequiresPermissions("inquiry_config:w")
	public TransData alterQuestionnaire(
			@RequestHeader(name = "token") String token,
			@JsonView(GroupView.Update.class)
			@Validated(GroupView.Update.class)
			@RequestBody Questionnaire questionnaire,
			BindingResult bindingResult) {
		questionnaireService.alterQuestionnaire(questionnaire, token);
		return TransFactory.getSuccessResponse();
	}

	@ApiOperation("列出问卷调查表")
	@GetMapping
	@RequiresPermissions(value = {"inquiry_config:r","inquiry_config:w"},logical = Logical.OR)
	@JsonView(GroupView.View.class)
	public TransData getQuestionnaire(
			@RequestParam(name = "isEdit", required = false) Integer isEdit
	) {
		if (Objects.isNull(isEdit)) {
			return TransFactory.getSuccessResponse(questionnaireService.getQuestionnaire());
		} else {
			return TransFactory.getSuccessResponse(questionnaireService.getQuestionnaire(isEdit));

		}


	}

	@ApiOperation("更改编辑状态")
	@GetMapping("edit")
	@RequiresPermissions(value = {"inquiry_config:w","template_control:w"},logical = Logical.OR)
	public TransData changeEditStatus(
			@RequestParam(name = "id", required = true) String id,
			@RequestParam(name = "isEdit", required = true) Integer isEdit
	) {
		questionnaireService.editChange(id, isEdit);
		return TransFactory.getSuccessResponse();
	}

	@PostMapping("create")
	@RequiresPermissions("template_control:w")
	public TransData createNewInstance(
			@RequestHeader(name = "token") String token,
			@Validated
			@RequestBody
					QuestionnaireCreate questionnaireCreate,
			BindingResult result) {
		questionnaireService.create(questionnaireCreate, token);
		return TransFactory.getSuccessResponse();
	}


}